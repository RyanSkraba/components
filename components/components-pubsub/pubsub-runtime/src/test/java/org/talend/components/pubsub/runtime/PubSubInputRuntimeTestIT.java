package org.talend.components.pubsub.runtime;

import static org.talend.components.pubsub.runtime.PubSubTestConstants.addSubscriptionForDataset;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createDataset;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createDatasetFromAvro;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createDatasetFromCSV;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createDatastore;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createInput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.google.api.services.pubsub.model.PubsubMessage;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.runners.spark.SparkContextOptions;
import org.apache.beam.runners.spark.SparkRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.values.PCollection;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.BeamJobRuntimeContainer;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.pubsub.PubSubDatasetProperties;
import org.talend.components.pubsub.PubSubDatastoreProperties;

public class PubSubInputRuntimeTestIT {

    final static String uuid = UUID.randomUUID().toString();

    final static String topicName = "tcomp-pubsub-inputtest" + uuid;

    final static String subscriptionName = "tcomp-pubsub-inputtest-sub1" + uuid;

    static PubSubClient client = PubSubConnection.createClient(createDatastore());

    // TODO extract this to utils
    private Pipeline createSparkRunnerPipeline() {
        PipelineOptions o = PipelineOptionsFactory.create();
        SparkContextOptions options = o.as(SparkContextOptions.class);
        JavaSparkContext jsc = new JavaSparkContext("local[2]", "PubSubInput");
        options.setProvidedSparkContext(jsc);
        options.setUsesProvidedSparkContext(true);
        options.setRunner(SparkRunner.class);
        runtimeContainer = new BeamJobRuntimeContainer(options);
        return Pipeline.create(options);
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
    public void inputCsv_Local() throws IOException {
        inputCsv(pipeline);
    }

    private void inputCsv(Pipeline pipeline) throws IOException {
        String testID = "csvBasicTest" + new Random().nextInt();
        final String fieldDelimited = ";";

        List<Person> expectedPersons = Person.genRandomList(testID, maxRecords);
        List<PubsubMessage> messages = new ArrayList<>();
        for (Person person : expectedPersons) {
            messages.add(new PubsubMessage().encodeData(person.toCSV(fieldDelimited).getBytes()));
        }
        client.publish(topicName, messages);

        PubSubInputRuntime inputRuntime = new PubSubInputRuntime();
        inputRuntime.initialize(runtimeContainer, createInput(
                addSubscriptionForDataset(createDatasetFromCSV(createDatastore(), topicName, fieldDelimited), subscriptionName),
                null, maxRecords));

        PCollection<IndexedRecord> readMessages = pipeline.apply(inputRuntime);

        List<IndexedRecord> expected = new ArrayList<>();
        for (Person person : expectedPersons) {
            expected.add(ConvertToIndexedRecord.convertToAvro(person.toCSV(fieldDelimited).split(fieldDelimited)));
        }
        PAssert.that(readMessages).containsInAnyOrder(expected);

        pipeline.run().waitUntilFinish();
    }

    @Test
    @Ignore("Can not run together with inputAvro_Spark, JavaSparkContext can't modify in same jvm"
            + " error, or PAssert check with wrong data issue")
    public void inputCsv_Spark() throws IOException {
        inputCsv(createSparkRunnerPipeline());
    }

    @Test
    public void inputAvro_Local() throws IOException {
        inputAvro(pipeline);
    }

    @Test
    public void inputAvro_Spark() throws IOException {
        inputAvro(createSparkRunnerPipeline());
    }

    private void inputAvro(Pipeline pipeline) throws IOException {
        String testID = "avroBasicTest" + new Random().nextInt();

        List<Person> expectedPersons = Person.genRandomList(testID, maxRecords);
        List<PubsubMessage> messages = new ArrayList<>();
        for (Person person : expectedPersons) {
            messages.add(new PubsubMessage().encodeData(person.serToAvroBytes()));
        }
        client.publish(topicName, messages);

        PubSubInputRuntime inputRuntime = new PubSubInputRuntime();
        inputRuntime
                .initialize(runtimeContainer,
                        createInput(addSubscriptionForDataset(
                                createDatasetFromAvro(createDatastore(), topicName, Person.schema.toString()), subscriptionName),
                                null, maxRecords));

        PCollection<IndexedRecord> readMessages = pipeline.apply(inputRuntime);

        List<IndexedRecord> expected = new ArrayList<>();
        for (Person person : expectedPersons) {
            expected.add(person.toAvroRecord());
        }
        PAssert.that(readMessages).containsInAnyOrder(expected);

        pipeline.run().waitUntilFinish();
    }
}
