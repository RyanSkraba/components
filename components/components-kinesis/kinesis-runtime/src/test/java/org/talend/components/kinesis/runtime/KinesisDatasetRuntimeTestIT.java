package org.talend.components.kinesis.runtime;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.talend.components.kinesis.runtime.KinesisTestConstants.getDatasetForAvro;
import static org.talend.components.kinesis.runtime.KinesisTestConstants.getDatasetForCsv;
import static org.talend.components.kinesis.runtime.KinesisTestConstants.getDatasetForListStreams;
import static org.talend.components.kinesis.runtime.KinesisTestConstants.getLocalDatastore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.kinesis.KinesisDatasetProperties;
import org.talend.components.kinesis.KinesisRegion;
import org.talend.daikon.java8.Consumer;

import com.amazonaws.services.kinesis.AmazonKinesis;

public class KinesisDatasetRuntimeTestIT {

    final static String streamCsv = "streams1";

    final static String streamAvro = "streams2";

    final static Set<String> streamsName = new HashSet(Arrays.asList(streamCsv, streamAvro, "streams3"));

    final static AmazonKinesis amazonKinesis = KinesisClient.create(getLocalDatastore());

    @BeforeClass
    public static void initStreams() throws InterruptedException, IOException {
        for (String streamName : streamsName) {
            amazonKinesis.createStream(streamName, 1);
            Thread.sleep(500);
        }

        List<Person> expectedPersons = Person.genRandomList("csvBasicTest" + new Random().nextInt(), maxRecords);
        KinesisInputRuntime.CsvConverter converter = new KinesisInputRuntime.CsvConverter(fieldDelimited.getDelimiter());
        for (Person expectedPerson : expectedPersons) {
            String strPerson = expectedPerson.toCSV(fieldDelimited.getDelimiter());
            amazonKinesis.putRecord(streamCsv, ByteBuffer.wrap(strPerson.getBytes("UTF-8")), expectedPerson.group);
            String[] data = strPerson.split(fieldDelimited.getDelimiter());
            expectedCsv.add(new KinesisInputRuntime.StringArrayIndexedRecord(converter.inferStringArray(data), data));
        }

        expectedPersons = Person.genRandomList("avroBasicTest" + new Random().nextInt(), maxRecords);
        for (Person expectedPerson : expectedPersons) {
            amazonKinesis.putRecord(streamAvro, ByteBuffer.wrap(expectedPerson.serToAvroBytes()), expectedPerson.group);
            expectedAvro.add(expectedPerson.toAvroRecord());
            schemaStr = expectedPerson.toAvroRecord().getSchema().toString();
        }
    }

    @AfterClass
    public static void cleanStreams() {
        for (String streamName : streamsName) {
            amazonKinesis.deleteStream(streamName);
        }
    }

    KinesisDatasetRuntime runtime;

    static List<IndexedRecord> expectedCsv = new ArrayList<>();

    static List<IndexedRecord> expectedAvro = new ArrayList<>();

    final static Integer maxRecords = 10;

    final static KinesisDatasetProperties.FieldDelimiterType fieldDelimited =
            KinesisDatasetProperties.FieldDelimiterType.SEMICOLON;

    static String schemaStr = null;

    @Before
    public void init() {
        runtime = new KinesisDatasetRuntime();
    }

    // Can't use localstack to list streams by region
    @Test
    public void listStreams() {
        runtime.initialize(null, getDatasetForListStreams(getLocalDatastore(), KinesisRegion.DEFAULT, null));
        Set<String> streams = runtime.listStreams();
        assertEquals(streams, streamsName);
    }

    @Test
    public void getSchemaCsv() {
        runtime.initialize(null, getDatasetForCsv(getLocalDatastore(), streamCsv, fieldDelimited));
        Schema schema = runtime.getSchema();
        assertEquals(expectedCsv.get(0).getSchema(), schema);
    }

    @Test
    public void getSchemaAvro() {
        runtime.initialize(null, getDatasetForAvro(getLocalDatastore(), streamAvro, schemaStr));
        Schema schema = runtime.getSchema();
        assertEquals(expectedAvro.get(0).getSchema(), schema);
    }

    @Test
    public void getSampleCsv() {
        runtime.initialize(null, getDatasetForCsv(getLocalDatastore(), streamCsv, fieldDelimited));
        final List<IndexedRecord> actual = new ArrayList<>();
        runtime.getSample(10, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                actual.add(indexedRecord);
            }
        });
        assertThat(actual, containsInAnyOrder(expectedCsv.toArray()));
    }

    @Test
    public void getSampleAvro() {
        runtime.initialize(null, getDatasetForAvro(getLocalDatastore(), streamAvro, schemaStr));
        final List<IndexedRecord> actual = new ArrayList<>();
        runtime.getSample(10, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord indexedRecord) {
                actual.add(indexedRecord);
            }
        });
        assertThat(actual, containsInAnyOrder(expectedAvro.toArray()));
    }

}
