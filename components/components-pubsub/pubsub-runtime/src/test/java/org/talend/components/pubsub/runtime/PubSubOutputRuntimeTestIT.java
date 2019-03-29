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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.talend.components.adapter.beam.BeamJobRuntimeContainer;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.adapter.beam.utils.SparkRunnerTestUtils;
import org.talend.components.pubsub.PubSubDatasetProperties;
import org.talend.components.pubsub.PubSubDatastoreProperties;
import org.talend.components.pubsub.output.PubSubOutputProperties;

import com.google.api.services.pubsub.model.ReceivedMessage;

public class PubSubOutputRuntimeTestIT implements Serializable {
    @ClassRule
    public static final TestRule DISABLE_IF_NEEDED = new DisableIfMissingConfig("bigquery.project");

    final static String uuid = UUID.randomUUID().toString();

    final static String topicName = "tcomp-pubsub-outputtest" + uuid;

    final static String subscriptionName = "tcomp-pubsub-outputtest-sub1" + uuid;

    // two unit tests will create topic with this name but with Create_If_Not_Exists function
    final static String newTopicName = "tcomp-pubsub-createTopicSub" + uuid;

    final static String newSubName = "tcomp-pubsub-createTopicSub-sub" + uuid;

    static PubSubClient client = PubSubTestConstants.PROJECT == null || PubSubTestConstants.PROJECT.isEmpty() ?
            null : PubSubConnection.createClient(createDatastore());

    static {
        PubSubAvroRegistry.get();
    }

    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    Integer maxRecords = 10;

    PubSubDatastoreProperties datastoreProperties;

    PubSubDatasetProperties datasetProperties;

    BeamJobRuntimeContainer runtimeContainer;

    SparkRunnerTestUtils sparkRunner;

    @BeforeClass
    public static void initTopic() throws IOException {
        client.createTopic(topicName);
        client.createSubscription(topicName, subscriptionName);
    }

    @AfterClass
    public static void cleanTopic() throws Exception {
        client.deleteSubscription(subscriptionName);
        client.deleteTopic(topicName);
        client.deleteSubscription(newSubName);
        client.deleteTopic(newTopicName);
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

    @Test
    public void outputCsv_Spark() throws IOException {
        sparkRunner = new SparkRunnerTestUtils(this.getClass().getName());
        runtimeContainer = sparkRunner.createRuntimeContainer();
        outputCsv(sparkRunner.createPipeline());
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

        PCollection<IndexedRecord> records = (PCollection<IndexedRecord>) pipeline.apply(Create.of(sendMessages)).apply(
                (PTransform) ConvertToIndexedRecord.of());

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
    public void outputAvro_Spark() throws IOException {
        sparkRunner = new SparkRunnerTestUtils(this.getClass().getName());
        runtimeContainer = sparkRunner.createRuntimeContainer();
        outputAvro(sparkRunner.createPipeline());
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
    public void createTopicSub_Spark() throws IOException {
        sparkRunner = new SparkRunnerTestUtils(this.getClass().getName());
        runtimeContainer = sparkRunner.createRuntimeContainer();
        createTopicSub(sparkRunner.createPipeline());
    }

    private void createTopicSub(Pipeline pipeline) throws IOException {
        String testID = "createTopicSubTest" + new Random().nextInt();

        final String fieldDelimited = ";";

        List<Person> expectedPersons = Person.genRandomList(testID, maxRecords);
        List<String> expectedMessages = new ArrayList<>();
        List<String[]> sendMessages = new ArrayList<>();
        for (Person person : expectedPersons) {
            expectedMessages.add(person.toCSV(fieldDelimited));
            sendMessages.add(person.toCSV(fieldDelimited).split(fieldDelimited));
        }

        PubSubOutputRuntime outputRuntime = new PubSubOutputRuntime();
        PubSubOutputProperties outputProperties = createOutput(addSubscriptionForDataset(
                createDatasetFromCSV(createDatastore(), newTopicName, fieldDelimited), newSubName));
        outputProperties.topicOperation.setValue(PubSubOutputProperties.TopicOperation.CREATE_IF_NOT_EXISTS);
        outputRuntime.initialize(runtimeContainer, outputProperties);

        PCollection<IndexedRecord> records = (PCollection<IndexedRecord>) pipeline.apply(Create.of(sendMessages)).apply(
                (PTransform) ConvertToIndexedRecord.of());

        records.setCoder(LazyAvroCoder.of()).apply(outputRuntime);

        pipeline.run().waitUntilFinish();

        List<String> actual = new ArrayList<>();
        while (true) {
            List<ReceivedMessage> messages = client.pull(newSubName, maxRecords);
            if (messages == null) {
                continue;
            }
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

        assertThat(actual, containsInAnyOrder(expectedMessages.toArray()));
    }
}
