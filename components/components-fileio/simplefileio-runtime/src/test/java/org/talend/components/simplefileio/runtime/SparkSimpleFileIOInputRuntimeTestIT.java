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

import static org.talend.components.test.RecordSetUtil.writeRandomCsvFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.RunnableOnService;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.simplefileio.input.SimpleFileIOInputProperties;
import org.talend.components.test.SparkIntegrationTestResource;

/**
 * Unit tests for {@link SimpleFileIOInputRuntime} using the Spark runner.
 */
public class SparkSimpleFileIOInputRuntimeTestIT {

    /** Resource that provides a {@link Pipeline} configured for Spark. */
    @Rule
    public SparkIntegrationTestResource spark = SparkIntegrationTestResource.ofLocal();

    /**
     * Demonstration using the {@link SparkIntegrationTestResource}.
     */
    @Category(RunnableOnService.class)
    @Test
    public void testSparkIntegrationTestResource() throws IOException {
        // Use the resource to create the pipeline.
        final Pipeline p = spark.createPipeline();

        // The pipeline transformations to test.
        PCollection<String> input = p.apply("create", Create.of("a a", "b c", "a a c"));
        input = input.apply("tokenize", ParDo.of(new ExtractWord()));
        PCollection<KV<String, Long>> counts = input.apply("count", Count.<String> perElement());

        // Check the expected results in the pipeline itself.
        PAssert.that(counts).containsInAnyOrder(KV.of("a", 4L), KV.of("b", 1L), KV.of("c", 2L));

        // Go!
        p.run().waitUntilFinish();
    }

    /** Example test DoFn. */
    private static class ExtractWord extends DoFn<String, String> {

        @ProcessElement
        public void processElement(ProcessContext c) {
            for (String token : c.element().split("\\s")) {
                c.output(token);
            }
        }
    }

    /**
     * Basic unit test using all default values (except for the path) on an in-memory DFS cluster.
     */
    @Category(RunnableOnService.class)
    @Test
    public void testBasicDefaults() throws IOException {
        FileSystem fs = FileSystem.get(spark.createHadoopConfiguration());
        String inputFile = writeRandomCsvFile(fs, "/tmp/test/input.csv", 0, 0, 10, 10, 6, ";", "\n");
        String fileSpec = fs.getUri().resolve("/tmp/test/input.csv").toString();

        // Configure the component.
        SimpleFileIOInputProperties inputProps = SimpleFileIOInputRuntimeTest.createInputComponentProperties();
        inputProps.getDatasetProperties().path.setValue(fileSpec);

        // Create the runtime.
        SimpleFileIOInputRuntime runtime = new SimpleFileIOInputRuntime();
        runtime.initialize(null, inputProps);

        // Use the runtime in a direct pipeline to test.
        final Pipeline p = spark.createPipeline();
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
}
