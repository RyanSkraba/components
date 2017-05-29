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

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.simplefileio.SimpleFileIOErrorCode;
import org.talend.components.simplefileio.output.SimpleFileIOOutputProperties;
import org.talend.components.test.BeamDirectTestResource;
import org.talend.components.test.MiniDfsResource;
import org.talend.daikon.exception.TalendRuntimeException;

/**
 * Unit tests for {@link SimpleFileIOOutputRuntime}.
 */
public class SimpleFileIOOutputErrorTest {

    @Rule
    public MiniDfsResource mini = new MiniDfsResource();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public BeamDirectTestResource beam = BeamDirectTestResource.of();

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

        // Requesting a wrong execution engine causes an exception.
        thrown.expect(TalendRuntimeException.class);
        thrown.expect(hasProperty("code", is(SimpleFileIOErrorCode.OUTPUT_ALREADY_EXISTS)));
        thrown.expectMessage("The path " + fileSpec + " already exists. Please remove it manually.");

        // Now try using the component.
        try {
            // Configure the component.
            SimpleFileIOOutputProperties props = SimpleFileIOOutputRuntimeTest.createOutputComponentProperties();
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
        } catch (Pipeline.PipelineExecutionException e) {
            if (e.getCause() instanceof TalendRuntimeException)
                throw (TalendRuntimeException) e.getCause();
            throw e;
        }
    }

    /**
     * Basic unit test using all default values (except for the path) on an in-memory DFS cluster.
     */
    @Test
    public void testUnauthorizedAccess() throws IOException, URISyntaxException {
        Path parent = new Path(mini.newFolder().toString());
        String fileSpec = mini.getLocalFs().getUri().resolve(new Path(parent, "output.csv").toUri()).toString();

        // Ensure that the parent is unwritable.
        FileUtil.chmod(parent.toUri().toString(), "000", true);

        // Requesting a wrong execution engine causes an exception.
        thrown.expect(TalendRuntimeException.class);
        thrown.expect(hasProperty("code", is(SimpleFileIOErrorCode.OUTPUT_NOT_AUTHORIZED)));
        thrown.expectMessage("Can not write to " + fileSpec
                + ". Please check user permissions or existence of base directory.");

        // Now try using the component.
        try {
            // Configure the component.
            SimpleFileIOOutputProperties props = SimpleFileIOOutputRuntimeTest.createOutputComponentProperties();
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
        } catch (Pipeline.PipelineExecutionException e) {
            if (e.getCause() instanceof TalendRuntimeException)
                throw (TalendRuntimeException) e.getCause();
            throw e;
        }

        // Check the expected values.
        mini.assertReadFile(mini.getLocalFs(), fileSpec, "1;one", "2;two");
    }
}
