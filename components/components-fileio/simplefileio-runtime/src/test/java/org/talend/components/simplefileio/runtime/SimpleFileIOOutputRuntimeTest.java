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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.fs.Path;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.simplefileio.SimpleFileIOErrorCode;
import org.talend.components.simplefileio.SimpleFileIOFormat;
import org.talend.components.simplefileio.output.SimpleFileIOOutputDefinition;
import org.talend.components.simplefileio.output.SimpleFileIOOutputProperties;
import org.talend.components.test.BeamDirectTestResource;
import org.talend.components.test.MiniDfsResource;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.runtime.RuntimeUtil;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for {@link SimpleFileIOOutputRuntime}.
 */
public class SimpleFileIOOutputRuntimeTest {

    @Rule
    public MiniDfsResource mini = new MiniDfsResource();

    @Rule
    public BeamDirectTestResource beam = BeamDirectTestResource.of();

    /**
     * Instance to test. Definitions are immutable.
     */
    private final ComponentDefinition def = new SimpleFileIOOutputDefinition();

    static {
        RuntimeUtil.registerMavenUrlHandler();
    }

    /**
     * @return the properties for this component, fully initialized with the default values.
     */
    public static SimpleFileIOOutputProperties createOutputComponentProperties() {
        // Configure the component.
        SimpleFileIOOutputProperties outputProps = new SimpleFileIOOutputProperties(null);
        outputProps.init();
        outputProps.setDatasetProperties(SimpleFileIODatasetRuntimeTest.createDatasetProperties());
        return outputProps;
    }

    /**
     * Basic unit test using all default values (except for the path) on an in-memory DFS cluster.
     */
    @Test
    public void testBasicDefaults() throws IOException, URISyntaxException {
        String fileSpec = mini.getLocalFs().getUri().resolve(
                new Path(mini.newFolder().toString(), "output.csv").toUri()).toString();

        // Configure the component.
        SimpleFileIOOutputProperties props = createOutputComponentProperties();
        props.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIOOutputRuntime runtime = new SimpleFileIOOutputRuntime();
        runtime.initialize(null, props);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> input = p.apply( //
                Create.of(ConvertToIndexedRecord.convertToAvro(new String[] { "1", "one" }), //
                        ConvertToIndexedRecord.convertToAvro(new String[] { "2", "two" }))); //
        input.apply(runtime);

        // And run the test.
        p.run().waitUntilFinish();

        // Check the expected values.
        mini.assertReadFile(mini.getLocalFs(), fileSpec, "1;one", "2;two");
    }

    /**
     * Basic unit test using all default values (except for the path) on an in-memory DFS cluster.
     */
    @Test
    public void testBasicCsvFormat() throws IOException, URISyntaxException {
        // Fetch the expected results and input dataset.
        List<IndexedRecord> inputs = new ArrayList<>();
        List<String> expected = new ArrayList<>();
        for (CsvExample csvEx : CsvExample.getCsvExamples()) {
            // Ignore lines that don't have the same schema (3 columns)
            if (csvEx.getValues().length == 3) {
                expected.add(csvEx.getExpectedOutputLine());
                inputs.add(ConvertToIndexedRecord.convertToAvro(csvEx.getValues()));
            }
        }

        String fileSpec = mini.getLocalFs().getUri().resolve(
                new Path(mini.newFolder().toString(), "output.csv").toUri()).toString();

        // Configure the component.
        SimpleFileIOOutputProperties props = createOutputComponentProperties();
        props.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIOOutputRuntime runtime = new SimpleFileIOOutputRuntime();
        runtime.initialize(null, props);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> input = p.apply(Create.of(inputs)); //
        input.apply(runtime);

        // And run the test.
        p.run().waitUntilFinish();

        // Check the expected values.
        mini.assertReadFile(mini.getLocalFs(), fileSpec, expected.toArray(new String[0]));
    }

    /**
     * Basic unit test writing to Avro.
     */
    @Test
    public void testBasicAvro() throws IOException, URISyntaxException {
        String fileSpec = mini.getLocalFs().getUri().resolve(
                new Path(mini.newFolder().toString(), "output.avro").toUri()).toString();

        // Configure the component.
        SimpleFileIOOutputProperties props = createOutputComponentProperties();
        props.getDatasetProperties().path.setValue(fileSpec);
        props.getDatasetProperties().format.setValue(SimpleFileIOFormat.AVRO);

        // Create the runtime.
        SimpleFileIOOutputRuntime runtime = new SimpleFileIOOutputRuntime();
        runtime.initialize(null, props);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> input = p.apply( //
                Create.of(ConvertToIndexedRecord.convertToAvro(new String[] { "1", "one" }), //
                        ConvertToIndexedRecord.convertToAvro(new String[] { "2", "two" }))); //
        input.apply(runtime);

        // And run the test.
        p.run().waitUntilFinish();

        // Check the expected values.
        // TODO(rskraba): Implement a comparison for the file on disk.
        // mini.assertReadFile(mini.getLocalFs(), fileSpec, "1;one", "2;two");
    }

    /**
     * Basic unit test writing to Avro.
     */
    @Test
    public void testBasicAvroBytes() throws IOException, URISyntaxException {
        String fileSpec = mini.getLocalFs().getUri().resolve(
                new Path(mini.newFolder().toString(), "output.avro").toUri()).toString();

        // Configure the component.
        SimpleFileIOOutputProperties props = createOutputComponentProperties();
        props.getDatasetProperties().path.setValue(fileSpec);
        props.getDatasetProperties().format.setValue(SimpleFileIOFormat.AVRO);

        // Create the runtime.
        SimpleFileIOOutputRuntime runtime = new SimpleFileIOOutputRuntime();
        runtime.initialize(null, props);

        Schema s = SchemaBuilder.record("test").fields() //
                .name("key").type(Schema.create(Schema.Type.BYTES)).noDefault() //
                .name("value").type(Schema.create(Schema.Type.STRING)).noDefault() //
                .endRecord();

        IndexedRecord ir1 = new GenericData.Record(s);
        IndexedRecord ir2 = new GenericData.Record(s);
        ir1.put(0, ByteBuffer.wrap(new byte[] { 0x00, 0x01, 0x02 }));
        ir1.put(1, "012");
        ir2.put(0, ByteBuffer.wrap(new byte[] { 0x01, 0x02, 0x03 }));
        ir2.put(1, "123");

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> input = p.apply( //
                Create.of(ir1, //
                        ir2)); //
        input.apply(runtime);

        // And run the test.
        p.run().waitUntilFinish();

        // Check the expected values.
        // TODO(rskraba): Implement a comparison for the file on disk.
        // mini.assertReadFile(mini.getLocalFs(), fileSpec, "1;one", "2;two");
    }

    /**
     * Basic unit test using all default values (except for the path) on an in-memory DFS cluster.
     */
    @Test
    public void testTryToOverwrite() throws IOException, URISyntaxException {
        Path parent = new Path(mini.newFolder().toString());
        Path dst = new Path(parent, "output");
        String fileSpec = mini.getLocalFs().getUri().resolve(dst.toUri()).toString();

        // Write something to the file before trying to run.
        try (OutputStream out = mini.getLocalFs().create(new Path(dst, "part-00000"))) {
            out.write(0);
        }

        // Now try using the component.
        try {
            // Configure the component.
            SimpleFileIOOutputProperties props = SimpleFileIOOutputRuntimeTest.createOutputComponentProperties();
            props.getDatasetProperties().path.setValue(fileSpec);
            props.overwrite.setValue(true);

            // Create the runtime.
            SimpleFileIOOutputRuntime runtime = new SimpleFileIOOutputRuntime();
            runtime.initialize(null, props);

            // Use the runtime in a direct pipeline to test.
            final Pipeline p = beam.createPipeline();
            PCollection<IndexedRecord> input = p.apply( //
                    Create.of(ConvertToIndexedRecord.convertToAvro(new String[] { "1", "one" }), //
                            ConvertToIndexedRecord.convertToAvro(new String[] { "2", "two" }))); //
            input.apply(runtime);

            // And run the test.
            runtime.runAtDriver(null);
            p.run().waitUntilFinish();
        } catch (Pipeline.PipelineExecutionException e) {
            if (e.getCause() instanceof TalendRuntimeException)
                throw (TalendRuntimeException) e.getCause();
            throw e;
        }

        // Check the expected values, which should be overwritten.
        mini.assertReadFile(mini.getLocalFs(), fileSpec, "1;one", "2;two");
    }


}
