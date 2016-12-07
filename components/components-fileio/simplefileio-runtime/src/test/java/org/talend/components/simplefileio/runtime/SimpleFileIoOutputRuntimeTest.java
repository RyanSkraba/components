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

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.simplefileio.SimpleFileIoFormat;
import org.talend.components.simplefileio.output.SimpleFileIoOutputDefinition;
import org.talend.components.simplefileio.output.SimpleFileIoOutputProperties;
import org.talend.components.test.BeamDirectTestResource;
import org.talend.components.test.MiniDfsResource;
import org.talend.daikon.runtime.RuntimeUtil;

/**
 * Unit tests for {@link SimpleFileIoOutputRuntime}.
 */
public class SimpleFileIoOutputRuntimeTest {

    @Rule
    public MiniDfsResource mini = new MiniDfsResource();

    @Rule
    public BeamDirectTestResource beam = BeamDirectTestResource.of();

    /**
     * Instance to test. Definitions are immutable.
     */
    private final ComponentDefinition def = new SimpleFileIoOutputDefinition();

    static {
        RuntimeUtil.registerMavenUrlHandler();
    }

    /**
     * @return the properties for this component, fully initialized with the default values.
     */
    public static SimpleFileIoOutputProperties createOutputComponentProperties() {
        // Configure the component.
        SimpleFileIoOutputProperties outputProps = new SimpleFileIoOutputProperties(null);
        outputProps.init();
        outputProps.setDatasetProperties(SimpleFileIoDatasetRuntimeTest.createDatasetProperties());
        return outputProps;
    }

    /**
     * Basic unit test using all default values (except for the path) on an in-memory DFS cluster.
     */
    @Test
    public void testBasicDefaults() throws IOException, URISyntaxException {
        String fileSpec = mini.getLocalFs().getUri().resolve(mini.newFolder() + "/output.csv").toString();

        // Configure the component.
        SimpleFileIoOutputProperties props = createOutputComponentProperties();
        props.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIoOutputRuntime runtime = new SimpleFileIoOutputRuntime();
        runtime.initialize(null, props);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> input = p.apply( //
                Create.of(ConvertToIndexedRecord.convertToAvro(new String[] { "1", "one" }), //
                        ConvertToIndexedRecord.convertToAvro(new String[] { "2", "two" }))); //
        input.apply(runtime);

        // And run the test.
        p.run();

        // Check the expected values.
        mini.assertReadFile(mini.getLocalFs(), fileSpec, "1;one", "2;two");
    }

    /**
     * Basic unit test writing to Avro.
     */
    @Test
    public void testBasicAvro() throws IOException, URISyntaxException {
        String fileSpec = mini.getLocalFs().getUri().resolve(mini.newFolder() + "/output.avro").toString();

        // Configure the component.
        SimpleFileIoOutputProperties props = createOutputComponentProperties();
        props.getDatasetProperties().path.setValue(fileSpec);
        props.getDatasetProperties().format.setValue(SimpleFileIoFormat.AVRO);

        // Create the runtime.
        SimpleFileIoOutputRuntime runtime = new SimpleFileIoOutputRuntime();
        runtime.initialize(null, props);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> input = p.apply( //
                Create.of(ConvertToIndexedRecord.convertToAvro(new String[] { "1", "one" }), //
                        ConvertToIndexedRecord.convertToAvro(new String[] { "2", "two" }))); //
        input.apply(runtime);

        // And run the test.
        p.run();

        // Check the expected values.
        // TODO(rskraba): Implement a comparison for the file on disk.
        // mini.assertReadFile(mini.getLocalFs(), fileSpec, "1;one", "2;two");
    }
}