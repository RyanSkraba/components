// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.kinesis.runtime;

import static org.junit.Assume.assumeTrue;
import static org.talend.components.kinesis.runtime.KinesisTestConstants.getDatasetForAvro;
import static org.talend.components.kinesis.runtime.KinesisTestConstants.getDatasetForCsv;
import static org.talend.components.kinesis.runtime.KinesisTestConstants.getInputFromBeginning;
import static org.talend.components.kinesis.runtime.KinesisTestConstants.getLocalDatastore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.utils.SparkRunnerTestUtils;
import org.talend.components.kinesis.KinesisDatasetProperties;

import com.amazonaws.services.kinesis.AmazonKinesis;

public class KinesisInputRuntimeTestIT {

    final static String csvStream1 = "csvStream1";

    final static String csvStream2 = "csvStream2";

    final static String avroStream1 = "avroStream1";

    final static String avroStream2 = "avroStream2";

    final static Set<String> streamsName =
            new HashSet<>(Arrays.asList(csvStream1, csvStream2, avroStream1, avroStream2));

    final static AmazonKinesis amazonKinesis = KinesisClient.create(getLocalDatastore());

    final Integer maxRecords = 10;

    @BeforeClass
    public static void initStreams() throws InterruptedException {
        for (String streamName : streamsName) {
            amazonKinesis.createStream(streamName, 1);
            Thread.sleep(500);
        }
    }

    @AfterClass
    public static void cleanStreams() {
        for (String streamName : streamsName) {
            amazonKinesis.deleteStream(streamName);
        }
    }

    KinesisInputRuntime runtime;

    @Before
    public void init() {
        runtime = new KinesisInputRuntime();
    }

    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    @Test
    public void inputCsv_Local() throws IOException {
        inputCsv(pipeline, csvStream1);
    }

    @Test
    public void inputCsv_Spark() throws IOException {
        assumeTrue(SystemUtils.JAVA_VERSION.startsWith("1.8."));
        inputCsv(new SparkRunnerTestUtils(this.getClass().getName()).createPipeline(), csvStream2);
    }

    public void inputCsv(Pipeline pipeline, String streamName) throws IOException {
        String testID = "csvBasicTest" + new Random().nextInt();
        final String fieldDelimited = ";";

        List<Person> expectedPersons = Person.genRandomList(testID, maxRecords);
        List<IndexedRecord> expected = new ArrayList<>();
        KinesisInputRuntime.CsvConverter converter = new KinesisInputRuntime.CsvConverter(fieldDelimited);
        for (Person expectedPerson : expectedPersons) {
            String strPerson = expectedPerson.toCSV(fieldDelimited);
            amazonKinesis.putRecord(streamName, ByteBuffer.wrap(strPerson.getBytes("UTF-8")), expectedPerson.group);
            String[] data = strPerson.split(fieldDelimited);
            expected.add(new KinesisInputRuntime.StringArrayIndexedRecord(converter.inferStringArray(data), data));
        }

        runtime.initialize(null, getInputFromBeginning(getDatasetForCsv(getLocalDatastore(), streamName,
                KinesisDatasetProperties.FieldDelimiterType.SEMICOLON), null, maxRecords));

        PCollection<IndexedRecord> results = pipeline.apply(runtime);

        PAssert.that(results).containsInAnyOrder(expected);

        pipeline.run().waitUntilFinish();
    }

    @Test
    public void inputAvro_Local() throws IOException {
        inputAvro(pipeline, avroStream1);
    }

    @Test
    public void inputAvro_Spark() throws IOException {
        assumeTrue(SystemUtils.JAVA_VERSION.startsWith("1.8."));
        inputCsv(new SparkRunnerTestUtils(this.getClass().getName()).createPipeline(), avroStream2);
    }

    public void inputAvro(Pipeline pipeline, String streamName) throws IOException {
        String testID = "avroBasicTest" + new Random().nextInt();

        List<Person> expectedPersons = Person.genRandomList(testID, maxRecords);
        List<IndexedRecord> expected = new ArrayList<>();
        String schemaStr = null;
        for (Person expectedPerson : expectedPersons) {
            amazonKinesis.putRecord(streamName, ByteBuffer.wrap(expectedPerson.serToAvroBytes()), expectedPerson.group);
            expected.add(expectedPerson.toAvroRecord());
            schemaStr = expectedPerson.toAvroRecord().getSchema().toString();
        }

        runtime.initialize(null,
                getInputFromBeginning(getDatasetForAvro(getLocalDatastore(), streamName, schemaStr), null, maxRecords));

        PCollection<IndexedRecord> results = pipeline.apply(runtime);

        PAssert.that(results).containsInAnyOrder(expected);

        pipeline.run().waitUntilFinish();
    }

}
