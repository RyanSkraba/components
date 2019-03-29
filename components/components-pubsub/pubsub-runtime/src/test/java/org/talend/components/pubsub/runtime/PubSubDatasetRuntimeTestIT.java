package org.talend.components.pubsub.runtime;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.addSubscriptionForDataset;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createDataset;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createDatasetFromAvro;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createDatasetFromCSV;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createDatastore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.daikon.java8.Consumer;

import com.google.api.services.pubsub.model.PubsubMessage;

public class PubSubDatasetRuntimeTestIT {
    @ClassRule
    public static final TestRule DISABLE_IF_NEEDED = new DisableIfMissingConfig("bigquery.project");

    final static String uuid = UUID.randomUUID().toString();

    final static List<String> topics = Arrays.asList("tcomp-pubsub-datasettest1" + uuid, "tcomp-pubsub-datasettest2" + uuid,
                    "tcomp-pubsub-datasettest3" + uuid, "tcomp-pubsub-datasettest4" + uuid);

    final static List<String> subscriptionsForTP1 = Arrays.asList("tcomp-pubsub-datasettest1-sub1" + uuid,
            "tcomp-pubsub-datasettest1-sub2" + uuid, "tcomp-pubsub-datasettest1-sub3" + uuid);

    final static String subForTP2 = "tcomp-pubsub-datasettest2-sub" + uuid;

    // Have to create subscription for schema only, else it will make the getSample failed,
    // should caused by the deadline time of all message are not align.
    final static String sub2ForTP2 = "tcomp-pubsub-datasettest2-subschema" + uuid;

    final static String subForTP3 = "tcomp-pubsub-datasettest3-sub" + uuid;

    final static String sub2ForTP3 = "tcomp-pubsub-datasettest3-subschema" + uuid;

    final static String subForTP4 = "tcomp-pubsub-datasettest4-sub" + uuid;

    final static String fieldDelimited = ";";

    static PubSubClient client = PubSubTestConstants.PROJECT == null || PubSubTestConstants.PROJECT.isEmpty() ?
            null : PubSubConnection.createClient(createDatastore());

    static List<Person> expectedPersons;

    PubSubDatasetRuntime runtime;

    @BeforeClass
    public static void initTopics() throws IOException {
        for (String topic : topics) {
            client.createTopic(topic);
        }
        for (String sub : subscriptionsForTP1) {
            client.createSubscription(topics.get(0), sub);
        }
        client.createSubscription(topics.get(1), subForTP2);
        client.createSubscription(topics.get(1), sub2ForTP2);
        client.createSubscription(topics.get(2), subForTP3);
        client.createSubscription(topics.get(2), sub2ForTP3);
        client.createSubscription(topics.get(3), subForTP4);

        Integer maxRecords = 10;
        String testID = "sampleTest" + new Random().nextInt();
        expectedPersons = Person.genRandomList(testID, maxRecords);

        // send csv format to topic 2
        List<PubsubMessage> messages = new ArrayList<>();
        for (Person person : expectedPersons) {
            messages.add(new PubsubMessage().encodeData(person.toCSV(fieldDelimited).getBytes()));
        }
        client.publish(topics.get(1), messages);

        // send avro format to topic 3
        messages = new ArrayList<>();
        for (Person person : expectedPersons) {
            messages.add(new PubsubMessage().encodeData(person.serToAvroBytes()));
        }
        client.publish(topics.get(2), messages);
    }

    @AfterClass
    public static void cleanTopics() throws Exception {
        for (String topic : topics) {
            client.deleteTopic(topic);
        }
        for (String sub : subscriptionsForTP1) {
            client.deleteSubscription(sub);
        }
        client.deleteSubscription(subForTP2);
        client.deleteSubscription(sub2ForTP2);
        client.deleteSubscription(subForTP3);
        client.deleteSubscription(sub2ForTP3);
        client.deleteSubscription(subForTP4);
    }

    @Before
    public void reset() {
        runtime = new PubSubDatasetRuntime();
    }

    @Test
    public void listTopics() throws Exception {
        runtime.initialize(null, createDataset(createDatastore(), null));
        Set<String> retrieveTopics = runtime.listTopics();
        for (String topic : topics) {
            // GCP is a public resource, can't make sure the available topics only for tcomp test
            assertTrue(retrieveTopics.contains(topic));
        }
    }

    @Test
    public void listSubscriptions() throws Exception {
        runtime.initialize(null, createDataset(createDatastore(), topics.get(0)));
        Set<String> retrieveSubs = runtime.listSubscriptions();
        assertThat(retrieveSubs, containsInAnyOrder(subscriptionsForTP1.toArray()));
    }

    @Test
    public void getSchemaCsv() {
        runtime.initialize(null,
                addSubscriptionForDataset(createDatasetFromCSV(createDatastore(), topics.get(1), fieldDelimited), sub2ForTP2));
        Schema schema = runtime.getSchema();
        assertEquals("{\"type\":\"record\",\"name\":\"StringArrayRecord\","
                + "\"fields\":[{\"name\":\"field0\",\"type\":\"string\"},{\"name\":\"field1\","
                + "\"type\":\"string\"},{\"name\":\"field2\",\"type\":\"string\"},"
                + "{\"name\":\"field3\",\"type\":\"string\"}]}", schema.toString());
    }

    @Test
    public void getSchemaAvro() {
        runtime.initialize(null, addSubscriptionForDataset(
                createDatasetFromAvro(createDatastore(), topics.get(2), Person.schema.toString()), sub2ForTP3));
        Schema schema = runtime.getSchema();
        assertEquals(Person.schema.toString(), schema.toString());
    }

    @Test
    public void getSampleCsv() {
        runtime.initialize(null,
                addSubscriptionForDataset(createDatasetFromCSV(createDatastore(), topics.get(1), fieldDelimited), subForTP2));
        final List<String> actual = new ArrayList<>();
        runtime.getSample(10, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                actual.add(indexedRecord.toString());
            }
        });
        List<String> expected = new ArrayList<>();
        for (Person person : expectedPersons) {
            expected.add(ConvertToIndexedRecord.convertToAvro(person.toCSV(fieldDelimited).split(fieldDelimited)).toString());
        }
        assertThat(actual, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void getSampleCsv2() {
        getSampleCsv();
    }

    @Test
    public void getSampleAvro() {
        runtime.initialize(null, addSubscriptionForDataset(
                createDatasetFromAvro(createDatastore(), topics.get(2), Person.schema.toString()), subForTP3));
        final List<String> actual = new ArrayList<>();
        runtime.getSample(10, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                actual.add(indexedRecord.toString());
            }
        });
        List<String> expected = new ArrayList<>();
        for (Person person : expectedPersons) {
            expected.add(person.toAvroRecord().toString());
        }
        assertThat(actual, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void getSampleAvro2() {
        getSampleAvro();
    }

    @Test
    public void getEmptySample() {
        runtime.initialize(null, addSubscriptionForDataset(
                createDatasetFromAvro(createDatastore(), topics.get(3), Person.schema.toString()), subForTP4));
        final List<String> actual = new ArrayList<>();
        runtime.getSample(10, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                actual.add(indexedRecord.toString());
            }
        });
        assertThat(actual.size(), is(0));
    }

}
