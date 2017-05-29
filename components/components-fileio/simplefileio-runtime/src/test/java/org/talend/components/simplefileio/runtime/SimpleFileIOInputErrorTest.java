package org.talend.components.simplefileio.runtime;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.talend.components.test.RecordSetUtil.writeRandomCsvFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.simplefileio.SimpleFileIOErrorCode;
import org.talend.components.simplefileio.input.SimpleFileIOInputProperties;
import org.talend.components.test.BeamDirectTestResource;
import org.talend.components.test.MiniDfsResource;
import org.talend.daikon.exception.TalendRuntimeException;

/**
 * Created by rskraba on 12/05/17.
 */
public class SimpleFileIOInputErrorTest {

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
    public void testUnauthorizedRead() throws IOException, URISyntaxException {
        String inputFile = writeRandomCsvFile(mini.getFs(), "/user/test/input.csv", 0, 0, 10, 10, 6, ";", "\n");
        String fileSpec = mini.getFs().getUri().resolve("/user/test/input.csv").toString();
        Path filePath = new Path(fileSpec);

        // Ensure that the parent is unreadable.
        mini.getFs().setPermission(filePath.getParent(), new FsPermission(FsAction.ALL, FsAction.NONE, FsAction.NONE));
        mini.getFs().setOwner(filePath.getParent(), "gooduser", "gooduser");

        // Configure the component.
        SimpleFileIOInputProperties inputProps = SimpleFileIOInputRuntimeTest.createInputComponentProperties();
        inputProps.getDatasetProperties().path.setValue(fileSpec);
        inputProps.getDatasetProperties().getDatastoreProperties().userName.setValue("baduser");

        // Create the runtime.
        SimpleFileIOInputRuntime runtime = new SimpleFileIOInputRuntime();
        runtime.initialize(null, inputProps);

        // The exception that should be thrown.
        thrown.expect(TalendRuntimeException.class);
        thrown.expect(hasProperty("code", is(SimpleFileIOErrorCode.INPUT_NOT_AUTHORIZED)));
        thrown.expectMessage("baduser can not read from " + fileSpec
                + ". Please check user permissions or existence of base directory.");

        try {
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
        } catch (Pipeline.PipelineExecutionException e) {
            if (e.getCause() instanceof TalendRuntimeException)
                throw (TalendRuntimeException) e.getCause();
            throw e;
        }

    }

}
