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
import java.net.URISyntaxException;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.fs.Path;
import org.joda.time.Duration;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.simplefileio.SimpleFileIOFormat;
import org.talend.components.simplefileio.output.SimpleFileIOOutputDefinition;
import org.talend.components.simplefileio.output.SimpleFileIOOutputProperties;
import org.talend.components.test.BeamDirectTestResource;
import org.talend.components.test.MiniDfsResource;
import org.talend.daikon.runtime.RuntimeUtil;

/**
 * Unit tests for {@link SimpleFileIOOutputRuntime}.
 */
@Ignore("Must be run manually and checked -- need a streaming pipeline test strategy.")
public class SimpleFileIOOutputRuntimeUnboundedTest {

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
    public void testBasicDefaultsUnbounded() throws IOException, URISyntaxException {
        String fileSpec = mini
                .getLocalFs()
                .getUri()
                .resolve(new Path(mini.newFolder().toString(), "output.csv").toUri())
                .toString();

        // Configure the component.
        SimpleFileIOOutputProperties props = createOutputComponentProperties();
        props.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIOOutputRuntime runtime = new SimpleFileIOOutputRuntime();
        runtime.initialize(null, props);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> input = p //
                .apply(GenerateSequence.from(0).withRate(10, Duration.millis(1000))) //
                .apply(ParDo.of(new GenerateDoFn()));
        input.apply(runtime);

        // And run the test.
        PipelineResult pr = p.run();

        // Check the expected values.
        mini.assertReadFile(mini.getLocalFs(), fileSpec, "1;one", "2;two");
    }

    /**
     * Basic unit test writing to Avro.
     */
    @Test
    public void testBasicAvroUnbounded() throws IOException, URISyntaxException {
        String fileSpec = mini
                .getLocalFs()
                .getUri()
                .resolve(new Path(mini.newFolder().toString(), "output.avro").toUri())
                .toString();

        // Configure the component.
        SimpleFileIOOutputProperties props = createOutputComponentProperties();
        props.getDatasetProperties().path.setValue(fileSpec);
        props.getDatasetProperties().format.setValue(SimpleFileIOFormat.AVRO);

        // Create the runtime.
        SimpleFileIOOutputRuntime runtime = new SimpleFileIOOutputRuntime();
        runtime.initialize(null, props);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> input = p //
                .apply(GenerateSequence.from(0).withRate(10, Duration.millis(1000))) //
                .apply(ParDo.of(new GenerateDoFn()));
        input.apply(runtime);

        // And run the test.
        PipelineResult pr = p.run();

        // Check the expected values.
        // TODO(rskraba): Implement a comparison for the file on disk.
        // mini.assertReadFile(mini.getLocalFs(), fileSpec, "1;one", "2;two");
    }

    /**
     * Basic unit test writing to Avro.
     */
    @Test
    public void testBasicAvroUnboundedWithWindow() throws IOException, URISyntaxException {
        String fileSpec = mini
                .getLocalFs()
                .getUri()
                .resolve(new Path(mini.newFolder().toString(), "output.avro").toUri())
                .toString();

        // Configure the component.
        SimpleFileIOOutputProperties props = createOutputComponentProperties();
        props.getDatasetProperties().path.setValue(fileSpec);
        props.getDatasetProperties().format.setValue(SimpleFileIOFormat.AVRO);

        // Create the runtime.
        SimpleFileIOOutputRuntime runtime = new SimpleFileIOOutputRuntime();
        runtime.initialize(null, props);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> input = p //
                .apply(GenerateSequence.from(0).withRate(10, Duration.millis(1000))) //
                .apply(ParDo.of(new GenerateDoFn()))
                .apply(Window.<IndexedRecord> into(FixedWindows.of(Duration.millis(30000))));

        input.apply(runtime);

        // And run the test.
        PipelineResult pr = p.run();

        // Check the expected values.
        // TODO(rskraba): Implement a comparison for the file on disk.
        // mini.assertReadFile(mini.getLocalFs(), fileSpec, "1;one", "2;two");
    }

    /**
     * Basic unit test writing to Parquet
     */
    @Test
    public void testBasicParquetUnbounded() throws IOException, URISyntaxException {
        String fileSpec = mini
                .getLocalFs()
                .getUri()
                .resolve(new Path(mini.newFolder().toString(), "output.parquet").toUri())
                .toString();

        // Configure the component.
        SimpleFileIOOutputProperties props = createOutputComponentProperties();
        props.getDatasetProperties().path.setValue(fileSpec);
        props.getDatasetProperties().format.setValue(SimpleFileIOFormat.PARQUET);

        // Create the runtime.
        SimpleFileIOOutputRuntime runtime = new SimpleFileIOOutputRuntime();
        runtime.initialize(null, props);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = beam.createPipeline();
        PCollection<IndexedRecord> input = p //
                .apply(GenerateSequence.from(0).withRate(10, Duration.millis(1000))) //
                .apply(ParDo.of(new GenerateDoFn()));
        input.apply(runtime);

        // And run the test.
        PipelineResult pr = p.run();

        // Check the expected values.
        // TODO(rskraba): Implement a comparison for the file on disk.
        // mini.assertReadFile(mini.getLocalFs(), fileSpec, "1;one", "2;two");
    }

    private static class GenerateDoFn extends DoFn<Long, IndexedRecord> {

        @ProcessElement
        public void processElement(ProcessContext c) {
            c.output(ConvertToIndexedRecord.convertToAvro(new String[] { String.valueOf(c.element()) }));
        }
    }
}
