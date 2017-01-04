// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.simplefileio.SimpleFileIoFormat;
import org.talend.components.simplefileio.input.SimpleFileIoInputDefinition;
import org.talend.components.simplefileio.input.SimpleFileIoInputProperties;
import org.talend.components.simplefileio.output.SimpleFileIoOutputProperties;
import org.talend.components.test.BeamDirectTestResource;
import org.talend.components.test.MiniDfsResource;
import org.talend.components.test.RecordSet;
import org.talend.daikon.runtime.RuntimeUtil;

/**
 * Unit tests for {@link SimpleFileIoInputRuntime}.
 */
public class SimpleFileIoInputRuntimeTest {

    @Rule
    public MiniDfsResource mini = new MiniDfsResource();

    @Rule
    public BeamDirectTestResource beam = BeamDirectTestResource.of();

    /**
     * Instance to test. Definitions are immutable.
     */
    private final ComponentDefinition def = new SimpleFileIoInputDefinition();

    static {
        RuntimeUtil.registerMavenUrlHandler();
    }

    /**
     * @return the properties for this component, fully initialized with the default values.
     */
    public static SimpleFileIoInputProperties createInputComponentProperties() {
        // Configure the component.
        SimpleFileIoInputProperties inputProps = new SimpleFileIoInputProperties(null);
        inputProps.init();
        inputProps.setDatasetProperties(SimpleFileIoDatasetRuntimeTest.createDatasetProperties());
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
        SimpleFileIoInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIoInputRuntime runtime = new SimpleFileIoInputRuntime();
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
        p.run();
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
        SimpleFileIoInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().format.setValue(SimpleFileIoFormat.AVRO);
        inputProps.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIoInputRuntime runtime = new SimpleFileIoInputRuntime();
        runtime.initialize(null, inputProps);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> readLines = p.apply(runtime);

        // Check the expected values.
        PAssert.that(readLines).containsInAnyOrder(rs.getAllData());

        // And run the test.
        p.run();
    }

    @Test
    public void testBasicCsvCustomDelimiters() throws IOException, URISyntaxException {

        String inputFile = writeRandomCsvFile(mini.getFs(), "/user/test/input.csv", 0, 0, 10, 10, 6, "|", "---");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.csv").toString();

        // Configure the component.
        SimpleFileIoInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().path.setValue(fileSpec);
        inputProps.getDatasetProperties().recordDelimiter.setValue("---");
        inputProps.getDatasetProperties().fieldDelimiter.setValue("|");

        // Create the runtime.
        SimpleFileIoInputRuntime runtime = new SimpleFileIoInputRuntime();
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
        p.run();
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
        SimpleFileIoInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().format.setValue(SimpleFileIoFormat.PARQUET);
        inputProps.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIoInputRuntime runtime = new SimpleFileIoInputRuntime();
        runtime.initialize(null, inputProps);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> readLines = p.apply(runtime);

        // Check the expected values.
        PAssert.that(readLines).containsInAnyOrder(rs.getAllData());

        // And run the test.
        p.run();
    }

    /**
     * In the next test, we are providing a file with 32 columns, but some of the lines does not contains 32 columns.
     */
    @Test
    public void testBasicDefaultsInvalidColumnNumber() throws IOException {
        InputStream in = getClass().getResourceAsStream("invalidColumnNumber.txt");
        try (OutputStream inOnMinFS = mini.getFs().create(new Path("/user/test/invalidColumnNumber.txt"))) {
            inOnMinFS.write(IOUtils.toByteArray(in));
        }
        String fileSpec = mini.getFs().getUri().resolve("/user/test/invalidColumnNumber.txt").toString();

        // Configure the component.
        SimpleFileIoInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIoInputRuntime runtime = new SimpleFileIoInputRuntime();
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
        p.run();
    }

    /**
     * Test to read an Parquet input and dump it on CSV. This is a special case to see the support of ByteBuffer
     * coding/decoding. This test is currently not working due to log on the Beam class ExecutorServiceParallelExecutor,
     * that will move the offset of any ByteBuffer.
     */
    @Ignore("This test cannot work on local mode due to a BEAM issue.")
    @Test
    public void testInputParquetByteBufferSerialization() throws IOException, URISyntaxException {
        InputStream in = getClass().getResourceAsStream("two_lines.snappy.parquet");
        try (OutputStream inOnMinFS = mini.getFs().create(new Path("/user/test/two_lines.snappy.parquet"))) {
            inOnMinFS.write(IOUtils.toByteArray(in));
        }
        String fileSpec = mini.getFs().getUri().resolve("/user/test/two_lines.snappy.parquet").toString();
        String fileSpecOutput = mini.getLocalFs().getUri().resolve(mini.newFolder() + "/output.csv").toString();

        // Configure the component.
        SimpleFileIoInputProperties inputProps = createInputComponentProperties();
        inputProps.getDatasetProperties().format.setValue(SimpleFileIoFormat.PARQUET);
        inputProps.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIoInputRuntime runtime = new SimpleFileIoInputRuntime();
        runtime.initialize(null, inputProps);

        SimpleFileIoOutputProperties outputProps = new SimpleFileIoOutputProperties(null);
        outputProps.init();
        outputProps.setDatasetProperties(SimpleFileIoDatasetRuntimeTest.createDatasetProperties());
        outputProps.getDatasetProperties().path.setValue(fileSpecOutput);
        outputProps.getDatasetProperties().format.setValue(SimpleFileIoFormat.CSV);

        SimpleFileIoOutputRuntime runtimeO = new SimpleFileIoOutputRuntime();
        runtimeO.initialize(null, outputProps);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline(1);
        p.apply(runtime).apply(runtimeO);
        p.run();

        mini.assertReadFile(mini.getLocalFs(), fileSpecOutput, "1;rdubois", "2;clombard");
    }
}