package org.talend.components.pubsub.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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
import org.junit.*;
import org.talend.components.adapter.beam.BeamJobRuntimeContainer;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.pubsub.PubSubDatasetProperties;
import org.talend.components.pubsub.PubSubDatastoreProperties;
import org.talend.components.pubsub.output.PubSubOutputProperties;

import com.google.api.services.pubsub.model.ReceivedMessage;

public class PubSubOutputRuntimeTestIT implements Serializable {

    final static String uuid = UUID.randomUUID().toString();

    final static String topicName = "tcomp-pubsub-outputtest" + uuid;

    final static String subscriptionName = "tcomp-pubsub-outputtest-sub1" + uuid;

    static PubSubClient client = PubSubConnection.createClient(createDatastore());

    static {
        PubSubAvroRegistry.get();
    }

    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    Integer maxRecords = 10;

    PubSubDatastoreProperties datastoreProperties;

    PubSubDatasetProperties datasetProperties;

    BeamJobRuntimeContainer runtimeContainer;

    @BeforeClass
    public static void initTopic() throws IOException {
        client.createTopic(topicName);
        client.createSubscription(topicName, subscriptionName);
    }

    @AfterClass
    public static void cleanTopic() throws Exception {
        client.deleteTopic(topicName);
        client.deleteSubscription(subscriptionName);
    }

    @Before
    public void init() {
        datastoreProperties = createDatastore();
        datasetProperties = createDataset(datastoreProperties, topicName);
        runtimeContainer = new BeamJobRuntimeContainer(pipeline.getOptions());
    }

    @Test
    public void outputCsv_Local() throws IOException {
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
        runtimeContainer = new BeamJobRuntimeContainer(options);
        return Pipeline.create(options);
    }

    @Test
    @Ignore
    public void outputCsv_Spark() throws IOException {
        outputCsv(createSparkRunnerPipeline());
    }

    private void outputCsv(Pipeline pipeline) throws IOException {
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
        outputRuntime.initialize(runtimeContainer,
                createOutput(createDatasetFromCSV(createDatastore(), topicName, fieldDelimited)));

        PCollection<IndexedRecord> records = (PCollection<IndexedRecord>) pipeline.apply(Create.of(sendMessages))
                .apply((PTransform) ConvertToIndexedRecord.of());

        records.setCoder(LazyAvroCoder.of()).apply(outputRuntime);

        pipeline.run().waitUntilFinish();

        List<String> actual = new ArrayList<>();
        while (true) {
            List<ReceivedMessage> messages = client.pull(subscriptionName, maxRecords);
            List<String> ackIds = new ArrayList<>();
            for (ReceivedMessage message : messages) {
                actual.add(new String(message.getMessage().decodeData()));
                ackIds.add(message.getAckId());
            }
            client.ack(subscriptionName, ackIds);
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
        outputRuntime.initialize(runtimeContainer,
                createOutput(createDatasetFromAvro(createDatastore(), topicName, Person.schema.toString())));

        PCollection<IndexedRecord> output = (PCollection<IndexedRecord>) pipeline.apply(Create.of(sendMessages));
        output.apply(outputRuntime);

        pipeline.run().waitUntilFinish();

        List<String> actual = new ArrayList<>();
        while (true) {
            List<ReceivedMessage> messages = client.pull(subscriptionName, maxRecords);
            List<String> ackIds = new ArrayList<>();
            for (ReceivedMessage message : messages) {
                actual.add(Person.desFromAvroBytes(message.getMessage().decodeData()).toAvroRecord().toString());
                ackIds.add(message.getAckId());
            }
            client.ack(subscriptionName, ackIds);
            if (actual.size() >= maxRecords) {
                break;
            }
        }
        assertThat(actual, containsInAnyOrder(expectedMessages.toArray()));
    }

    @Test
    public void createTopicSub_Local() throws IOException {
        createTopicSub(pipeline);
    }

    @Test
    @Ignore
    public void createTopicSub_Spark() throws IOException {
        createTopicSub(createSparkRunnerPipeline());
    }

    private void createTopicSub(Pipeline pipeline) throws IOException {
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
        outputRuntime.initialize(runtimeContainer, outputProperties);

        PCollection<IndexedRecord> records = (PCollection<IndexedRecord>) pipeline.apply(Create.of(sendMessages))
                .apply((PTransform) ConvertToIndexedRecord.of());

        records.setCoder(LazyAvroCoder.of()).apply(outputRuntime);

        pipeline.run().waitUntilFinish();

        List<String> actual = new ArrayList<>();
        while (true) {
            List<ReceivedMessage> messages = client.pull(newSubName, maxRecords);
            List<String> ackIds = new ArrayList<>();
            for (ReceivedMessage message : messages) {
                actual.add(new String(message.getMessage().decodeData()));
                ackIds.add(message.getAckId());
            }
            client.ack(newSubName, ackIds);
            if (actual.size() >= maxRecords) {
                break;
            }
        }

        client.deleteSubscription(newSubName);
        client.deleteTopic(newTopicName);
        assertThat(actual, containsInAnyOrder(expectedMessages.toArray()));
    }
}
