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
package org.talend.components.simplefileio.runtime;

import static org.talend.components.test.RecordSetUtil.getSimpleTestData;
import static org.talend.components.test.RecordSetUtil.writeRandomAvroFile;
import static org.talend.components.test.RecordSetUtil.writeRandomCsvFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties.FieldDelimiterType;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties.RecordDelimiterType;
import org.talend.components.simplefileio.SimpleFileIOFormat;
import org.talend.components.simplefileio.input.SimpleFileIOInputDefinition;
import org.talend.components.simplefileio.input.SimpleFileIOInputProperties;
import org.talend.components.simplefileio.output.SimpleFileIOOutputProperties;
import org.talend.components.test.BeamDirectTestResource;
import org.talend.components.test.MiniDfsResource;
import org.talend.components.test.RecordSet;
import org.talend.daikon.runtime.RuntimeUtil;

/**
 * Unit tests for {@link SimpleFileIOInputRuntime}.
 */
public class SimpleFileIOInputRuntimeTest {

    @Rule
    public MiniDfsResource mini = new MiniDfsResource();

    @Rule
    public BeamDirectTestResource beam = BeamDirectTestResource.of();

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleFileIOInputRuntime.class);

    /**
     * Instance to test. Definitions are immutable.
     */
    private final ComponentDefinition def = new SimpleFileIOInputDefinition();

    static {
        RuntimeUtil.registerMavenUrlHandler();
    }

    /**
     * @return the properties for this component, fully initialized with the default values.
     */
    public static SimpleFileIOInputProperties createInputComponentProperties() {
        // Configure the component.
        SimpleFileIOInputProperties inputProps = new SimpleFileIOInputProperties(null);
        inputProps.init();
        inputProps.setDatasetProperties(SimpleFileIODatasetRuntimeTest.createDatasetProperties());
        return inputProps;
    }

    /**
     * Basic unit test using all default values (except for the path) on an in-memory DFS cluster.
     */
    @Test
    public void testBasicDefaults() throws IOException, URISyntaxException {
        String inputFile = writeRandomCsvFile(mini.getFs(), "/user/test/input.csv", 0, 0, 10, 10, 6, ";", "\n");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.csv").toString();

        // Configure the component.
        SimpleFileIOInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().path.setValue(fileSpec);
        inputProps.getDatasetProperties().headerLine.setValue(0);

        // Create the runtime.
        SimpleFileIOInputRuntime runtime = new SimpleFileIOInputRuntime();
        runtime.initialize(null, inputProps);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> readLines = p.apply(runtime);

        // Check the expected values.
        List<IndexedRecord> expected = new ArrayList<>();
        for (String record : inputFile.split("\n")) {
            expected.add(ConvertToIndexedRecord.convertToAvro(record.split(";")));
        }
        PAssert.that(readLines).containsInAnyOrder(expected);

        // And run the test.
        p.run().waitUntilFinish();
    }

    /**
     * Basic unit test using all default values (except for the path) on an in-memory DFS cluster.
     */
    @Test
    public void testBasicCSV_changeSeparator() throws IOException, URISyntaxException {
        String inputFile = writeRandomCsvFile(mini.getFs(), "/user/test/input.csv", 0, 0, 10, 10, 6, " ", "\r\n");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.csv").toString();

        // Configure the component.
        SimpleFileIOInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().path.setValue(fileSpec);
        inputProps.getDatasetProperties().recordDelimiter.setValue(RecordDelimiterType.CRLF);
        inputProps.getDatasetProperties().fieldDelimiter.setValue(FieldDelimiterType.SPACE);
        inputProps.getDatasetProperties().headerLine.setValue(0);

        // Create the runtime.
        SimpleFileIOInputRuntime runtime = new SimpleFileIOInputRuntime();
        runtime.initialize(null, inputProps);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> readLines = p.apply(runtime);

        // Check the expected values.
        List<IndexedRecord> expected = new ArrayList<>();
        for (String record : inputFile.split("\r\n")) {
            expected.add(ConvertToIndexedRecord.convertToAvro(record.split(" ")));
        }
        PAssert.that(readLines).containsInAnyOrder(expected);

        // And run the test.
        p.run().waitUntilFinish();
    }

    /**
     * Basic unit test using all default values (except for the path) on an in-memory DFS cluster.
     */
    @Test
    public void testBasicCSV_changeSeparator2() throws IOException, URISyntaxException {
        String inputFile = writeRandomCsvFile(mini.getFs(), "/user/test/input.csv", 0, 0, 10, 10, 6, "\t", "\r");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.csv").toString();

        // Configure the component.
        SimpleFileIOInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().path.setValue(fileSpec);
        inputProps.getDatasetProperties().recordDelimiter.setValue(RecordDelimiterType.CR);
        inputProps.getDatasetProperties().fieldDelimiter.setValue(FieldDelimiterType.TABULATION);
        inputProps.getDatasetProperties().headerLine.setValue(0);

        // Create the runtime.
        SimpleFileIOInputRuntime runtime = new SimpleFileIOInputRuntime();
        runtime.initialize(null, inputProps);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> readLines = p.apply(runtime);

        // Check the expected values.
        List<IndexedRecord> expected = new ArrayList<>();
        for (String record : inputFile.split("\r")) {
            expected.add(ConvertToIndexedRecord.convertToAvro(record.split("\t")));
        }
        PAssert.that(readLines).containsInAnyOrder(expected);

        // And run the test.
        p.run().waitUntilFinish();
    }

    /**
     * Basic unit test using all default values (except for the path) on an in-memory DFS cluster.
     */
    @Test
    public void testBasicAvro() throws IOException, URISyntaxException {
        RecordSet rs = getSimpleTestData(0);
        writeRandomAvroFile(mini.getFs(), "/user/test/input.avro", rs);
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.avro").toString();

        // Configure the component.
        SimpleFileIOInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().format.setValue(SimpleFileIOFormat.AVRO);
        inputProps.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIOInputRuntime runtime = new SimpleFileIOInputRuntime();
        runtime.initialize(null, inputProps);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> readLines = p.apply(runtime);

        // Check the expected values.
        PAssert.that(readLines).containsInAnyOrder(rs.getAllData());

        // And run the test.
        p.run().waitUntilFinish();
    }

    @Test
    public void testBasicCsvLimit() throws IOException, URISyntaxException {

        String inputFile = writeRandomCsvFile(mini.getFs(), "/user/test/input.csv", 0, 0, 10, 10, 6, ";", "\n");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.csv").toString();

        // Configure the component.
        SimpleFileIOInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().path.setValue(fileSpec);
        inputProps.limit.setValue(2);
        inputProps.getDatasetProperties().headerLine.setValue(0);

        // Create the runtime.
        SimpleFileIOInputRuntime runtime = new SimpleFileIOInputRuntime();
        runtime.initialize(null, inputProps);

        // Use the runtime in a direct pipeline to test.
        // TODO(rskraba): This fails for certain values of targetParallelism! To fix.
        final Pipeline p = beam.createPipeline(1);

        PCollection<IndexedRecord> readLines = p.apply(runtime);

        List<IndexedRecord> expected = new ArrayList<>();
        for (String record : inputFile.split("\n")) {
            expected.add(ConvertToIndexedRecord.convertToAvro(record.split(";")));
        }
        expected = expected.subList(0, 2);

        PAssert.that(readLines).containsInAnyOrder(expected);
        p.run().waitUntilFinish();
    }

    @Test
    public void testBasicCsvCustomDelimiters() throws IOException, URISyntaxException {

        String inputFile = writeRandomCsvFile(mini.getFs(), "/user/test/input.csv", 0, 0, 10, 10, 6, "|", "---");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.csv").toString();

        // Configure the component.
        SimpleFileIOInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().path.setValue(fileSpec);
        inputProps.getDatasetProperties().recordDelimiter.setValue(RecordDelimiterType.OTHER);
        inputProps.getDatasetProperties().specificRecordDelimiter.setValue("---");
        inputProps.getDatasetProperties().fieldDelimiter.setValue(FieldDelimiterType.OTHER);
        inputProps.getDatasetProperties().specificFieldDelimiter.setValue("|");
        inputProps.getDatasetProperties().headerLine.setValue(0);

        // Create the runtime.
        SimpleFileIOInputRuntime runtime = new SimpleFileIOInputRuntime();
        runtime.initialize(null, inputProps);

        // Use the runtime in a direct pipeline to test.
        // TODO(rskraba): This fails for certain values of targetParallelism! To fix.
        final Pipeline p = beam.createPipeline(3);

        PCollection<IndexedRecord> readLines = p.apply(runtime);

        List<IndexedRecord> expected = new ArrayList<>();
        for (String record : inputFile.split("---")) {
            expected.add(ConvertToIndexedRecord.convertToAvro(record.split("\\Q|\\E")));
        }

        PAssert.that(readLines).containsInAnyOrder(expected);
        p.run().waitUntilFinish();
    }

    @Test
    public void testBasicCsvFormatting() throws IOException, URISyntaxException {

        // Create an input file with all of the 3 column examples from the examples.
        List<IndexedRecord> expected = new ArrayList<>();
        List<String> file = new ArrayList<>();
        for (CsvExample csvEx : CsvExample.getCsvExamples()) {
            // Ignore lines that don't have the same schema (3 columns)
            if (csvEx.getValues().length == 3) {
                for (String inputLine : csvEx.getPossibleInputLines()) {
                    file.add(inputLine);
                    expected.add(ConvertToIndexedRecord.convertToAvro(csvEx.getValues()));
                }
            }
        }

        mini.writeFile(mini.getFs(), "/user/test/input.csv", file.toArray(new String[0]));
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.csv").toString();

        // Configure the component.
        SimpleFileIOInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().path.setValue(fileSpec);
        //the default text enclosure is null as it make the CSV content can't be split if not null.
        inputProps.getDatasetProperties().textEnclosureCharacter.setValue("\"");
        inputProps.getDatasetProperties().headerLine.setValue(0);

        // Create the runtime.
        SimpleFileIOInputRuntime runtime = new SimpleFileIOInputRuntime();
        runtime.initialize(null, inputProps);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline(3);

        PCollection<IndexedRecord> readLines = p.apply(runtime);

        PAssert.that(readLines).containsInAnyOrder(expected);
        p.run().waitUntilFinish();
    }

    /**
     * Basic unit test using all default values (except for the path) on an in-memory DFS cluster.
     */
    @Ignore("To implement.")
    @Test
    public void testBasicParquet() throws IOException, URISyntaxException {
        RecordSet rs = getSimpleTestData(0);
        writeRandomAvroFile(mini.getFs(), "/user/test/input.avro", rs);
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.avro").toString();

        // Configure the component.
        SimpleFileIOInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().format.setValue(SimpleFileIOFormat.PARQUET);
        inputProps.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIOInputRuntime runtime = new SimpleFileIOInputRuntime();
        runtime.initialize(null, inputProps);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> readLines = p.apply(runtime);

        // Check the expected values.
        PAssert.that(readLines).containsInAnyOrder(rs.getAllData());

        // And run the test.
        p.run().waitUntilFinish();
    }

    /**
     * In the next test, we are providing a file with 32 columns, but some of the lines does not contains 32 columns.
     */
    @Ignore("Encoding trouble in the test.")
    @Test
    public void testBasicDefaultsInvalidColumnNumber() throws IOException {
        InputStream in = getClass().getResourceAsStream("invalidColumnNumber.txt");
        try (OutputStream inOnMinFS = mini.getFs().create(new Path("/user/test/invalidColumnNumber.txt"))) {
            inOnMinFS.write(IOUtils.toByteArray(in));
        }
        String fileSpec = mini.getFs().getUri().resolve("/user/test/invalidColumnNumber.txt").toString();

        // Configure the component.
        SimpleFileIOInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIOInputRuntime runtime = new SimpleFileIOInputRuntime();
        runtime.initialize(null, inputProps);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> readLines = p.apply(runtime);

        // Check the expected values.
        in = getClass().getResourceAsStream("invalidColumnNumber.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        List<IndexedRecord> expected = new ArrayList<>();
        String sCurrentLine = "";
        while ((sCurrentLine = br.readLine()) != null) {
            expected.add(ConvertToIndexedRecord.convertToAvro(sCurrentLine.split(";")));
        }

        PAssert.that(readLines).containsInAnyOrder(expected);

        // And run the test.
        p.run().waitUntilFinish();
    }

    /**
     * Test to read an Parquet input and dump it on CSV. This is a special case to see the support of ByteBuffer
     * coding/decoding. This test is currently not working due to log on the Beam class ExecutorServiceParallelExecutor,
     * that will move the offset of any ByteBuffer.
     */
    @Test
    public void testInputParquetByteBufferSerialization() throws IOException, URISyntaxException {
        InputStream in = getClass().getResourceAsStream("two_lines.snappy.parquet");
        try (OutputStream inOnMinFS = mini.getFs().create(new Path("/user/test/two_lines.snappy.parquet"))) {
            inOnMinFS.write(IOUtils.toByteArray(in));
        }
        String fileSpec = mini.getFs().getUri().resolve("/user/test/two_lines.snappy.parquet").toString();
        String fileSpecOutput = mini.getLocalFs().getUri().resolve(new Path(mini.newFolder().toString(), "output.csv").toUri()).toString();

        // Configure the component.
        SimpleFileIOInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().format.setValue(SimpleFileIOFormat.PARQUET);
        inputProps.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIOInputRuntime runtime = new SimpleFileIOInputRuntime();
        runtime.initialize(null, inputProps);

        SimpleFileIOOutputProperties outputProps = new SimpleFileIOOutputProperties(null);
        outputProps.init();
        outputProps.setDatasetProperties(SimpleFileIODatasetRuntimeTest.createDatasetProperties());
        outputProps.getDatasetProperties().path.setValue(fileSpecOutput);
        outputProps.getDatasetProperties().format.setValue(SimpleFileIOFormat.CSV);

        SimpleFileIOOutputRuntime runtimeO = new SimpleFileIOOutputRuntime();
        runtimeO.initialize(null, outputProps);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline(1);
        p.apply(runtime).apply(runtimeO);
        p.run().waitUntilFinish();

        mini.assertReadFile(mini.getLocalFs(), fileSpecOutput, "1;rdubois", "2;clombard");
    }
}
