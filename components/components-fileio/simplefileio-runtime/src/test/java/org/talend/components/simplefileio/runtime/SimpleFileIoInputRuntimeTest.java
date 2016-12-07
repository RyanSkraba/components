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

import static org.talend.components.test.RecordSetUtil.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.simplefileio.SimpleFileIoFormat;
import org.talend.components.simplefileio.input.SimpleFileIoInputDefinition;
import org.talend.components.simplefileio.input.SimpleFileIoInputProperties;
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
        final Pipeline p = beam.createPipeline();
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

}