package org.talend.components.pubsub.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.addSubscriptionForDataset;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createDataset;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createDatasetFromAvro;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createDatasetFromCSV;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createDatastore;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createOutput;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.runners.spark.SparkContextOptions;
import org.apache.beam.runners.spark.SparkRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.pubsub.PubSubDatasetProperties;
import org.talend.components.pubsub.PubSubDatastoreProperties;
import org.talend.components.pubsub.output.PubSubOutputProperties;

import com.google.cloud.pubsub.PubSub;
import com.google.cloud.pubsub.ReceivedMessage;
import com.google.cloud.pubsub.SubscriptionInfo;
import com.google.cloud.pubsub.TopicInfo;

public class PubSubOutputRuntimeTest implements Serializable {

    final static String uuid = UUID.randomUUID().toString();

    final static String topicName = "tcomp-pubsub-outputtest" + uuid;

    final static String subscriptionName = "tcomp-pubsub-outputtest-sub1" + uuid;

    static PubSub client = PubSubConnection.createClient(createDatastore());

    static {
        PubSubAvroRegistry.get();
    }

    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    Integer maxRecords = 10;

    PubSubDatastoreProperties datastoreProperties;

    PubSubDatasetProperties datasetProperties;

    @BeforeClass
    public static void initTopic() {
        client.create(TopicInfo.of(topicName));
        client.create(SubscriptionInfo.of(topicName, subscriptionName));
    }

    @AfterClass
    public static void cleanTopic() throws Exception {
        client.deleteTopic(topicName);
        client.deleteSubscription(subscriptionName);
        client.close();
    }

    @Before
    public void init() {
        datastoreProperties = createDatastore();
        datasetProperties = createDataset(datastoreProperties, topicName);
    }

    @Test
    public void outputCsv_Local() throws UnsupportedEncodingException {
        outputCsv(pipeline);
    }

    // TODO extract this to utils
    private Pipeline createSparkRunnerPipeline() {
        JavaSparkContext jsc = new JavaSparkContext("local[2]", this.getClass().getName());
        PipelineOptions o = PipelineOptionsFactory.create();
        SparkContextOptions options = o.as(SparkContextOptions.class);
        options.setProvidedSparkContext(jsc);
        options.setUsesProvidedSparkContext(true);
        options.setRunner(SparkRunner.class);

        return Pipeline.create(options);
    }

    @Test
    @Ignore
    public void outputCsv_Spark() throws UnsupportedEncodingException {
        outputCsv(createSparkRunnerPipeline());
    }

    private void outputCsv(Pipeline pipeline) throws UnsupportedEncodingException {
        String testID = "csvBasicTest" + new Random().nextInt();
        final String fieldDelimited = ";";

        List<Person> expectedPersons = Person.genRandomList(testID, maxRecords);
        List<String> expectedMessages = new ArrayList<>();
        List<String[]> sendMessages = new ArrayList<>();
        for (Person person : expectedPersons) {
            expectedMessages.add(person.toCSV(fieldDelimited));
            sendMessages.add(person.toCSV(fieldDelimited).split(fieldDelimited));
        }

        PubSubOutputRuntime outputRuntime = new PubSubOutputRuntime();
        outputRuntime.initialize(null, createOutput(createDatasetFromCSV(createDatastore(), topicName, fieldDelimited)));

        PCollection<IndexedRecord> records = (PCollection<IndexedRecord>) pipeline.apply(Create.of(sendMessages))
                .apply((PTransform) ConvertToIndexedRecord.of());

        records.setCoder(LazyAvroCoder.of()).apply(outputRuntime);

        pipeline.run().waitUntilFinish();

        List<String> actual = new ArrayList<>();
        while (true) {
            Iterator<ReceivedMessage> messageIterator = client.pull(subscriptionName, maxRecords);
            while (messageIterator.hasNext()) {
                ReceivedMessage next = messageIterator.next();
                actual.add(next.getPayloadAsString());
                next.ack();
            }
            if (actual.size() >= maxRecords) {
                break;
            }
        }
        assertThat(actual, containsInAnyOrder(expectedMessages.toArray()));
    }

    @Test
    public void outputAvro_Local() throws IOException {
        outputAvro(pipeline);
    }

    @Test
    @Ignore("Can not run together with outputCsv_Spark, JavaSparkContext can't modify in same jvm"
            + " error, or PAssert check with wrong data issue")
    public void outputAvro_Spark() throws IOException {
        outputAvro(createSparkRunnerPipeline());
    }

    private void outputAvro(Pipeline pipeline) throws IOException {
        String testID = "avroBasicTest" + new Random().nextInt();

        List<Person> expectedPersons = Person.genRandomList(testID, maxRecords);
        List<String> expectedMessages = new ArrayList<>();
        List<IndexedRecord> sendMessages = new ArrayList<>();
        for (Person person : expectedPersons) {
            expectedMessages.add(person.toAvroRecord().toString());
            sendMessages.add(person.toAvroRecord());
        }

        PubSubOutputRuntime outputRuntime = new PubSubOutputRuntime();
        outputRuntime.initialize(null,
                createOutput(createDatasetFromAvro(createDatastore(), topicName, Person.schema.toString())));

        PCollection<IndexedRecord> output = (PCollection<IndexedRecord>) pipeline
                .apply(Create.of(sendMessages).withCoder(LazyAvroCoder.of()));
        output.apply(outputRuntime);

        pipeline.run().waitUntilFinish();

        List<String> actual = new ArrayList<>();
        while (true) {
            Iterator<ReceivedMessage> messageIterator = client.pull(subscriptionName, maxRecords);
            while (messageIterator.hasNext()) {
                ReceivedMessage next = messageIterator.next();
                actual.add(Person.desFromAvroBytes(next.getPayload().toByteArray()).toAvroRecord().toString());
                next.ack();
            }
            if (actual.size() >= maxRecords) {
                break;
            }
        }
        assertThat(actual, containsInAnyOrder(expectedMessages.toArray()));
    }

    @Test
    public void createTopicSub_Local(){
        createTopicSub(pipeline);
    }

    @Test
    @Ignore
    public void createTopicSub_Spark() {
        createTopicSub(createSparkRunnerPipeline());
    }

    private void createTopicSub(Pipeline pipeline) {
        String testID = "createTopicSubTest" + new Random().nextInt();

        final String newTopicName = "tcomp-pubsub-createTopicSub" + uuid;

        final String newSubName = "tcomp-pubsub-createTopicSub-sub" + uuid;

        final String fieldDelimited = ";";

        List<Person> expectedPersons = Person.genRandomList(testID, maxRecords);
        List<String> expectedMessages = new ArrayList<>();
        List<String[]> sendMessages = new ArrayList<>();
        for (Person person : expectedPersons) {
            expectedMessages.add(person.toCSV(fieldDelimited));
            sendMessages.add(person.toCSV(fieldDelimited).split(fieldDelimited));
        }

        PubSubOutputRuntime outputRuntime = new PubSubOutputRuntime();
        PubSubOutputProperties outputProperties = createOutput(
                addSubscriptionForDataset(createDatasetFromCSV(createDatastore(), newTopicName, fieldDelimited), newSubName));
        outputProperties.topicOperation.setValue(PubSubOutputProperties.TopicOperation.CREATE_IF_NOT_EXISTS);
        outputRuntime.initialize(null, outputProperties);

        PCollection<IndexedRecord> records = (PCollection<IndexedRecord>) pipeline.apply(Create.of(sendMessages))
                .apply((PTransform) ConvertToIndexedRecord.of());

        records.setCoder(LazyAvroCoder.of()).apply(outputRuntime);

        pipeline.run().waitUntilFinish();

        List<String> actual = new ArrayList<>();
        while (true) {
            Iterator<ReceivedMessage> messageIterator = client.pull(newSubName, maxRecords);
            while (messageIterator.hasNext()) {
                ReceivedMessage next = messageIterator.next();
                actual.add(next.getPayloadAsString());
                next.ack();
            }
            if (actual.size() >= maxRecords) {
                break;
            }
        }

        client.deleteSubscription(newSubName);
        client.deleteTopic(newTopicName);
        assertThat(actual, containsInAnyOrder(expectedMessages.toArray()));
    }
}
