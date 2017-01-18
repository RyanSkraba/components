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

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.RunnableOnService;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.simplefileio.SimpleFileIoFormat;
import org.talend.components.simplefileio.output.SimpleFileIoOutputProperties;
import org.talend.components.test.MiniDfsResource;
import org.talend.components.test.SparkIntegrationTestResource;

/**
 * Unit tests for {@link SimpleFileIoOutputRuntime} using the Spark runner.
 */
public class SparkSimpleFileIoOutputRuntimeTestIT {

    /** Resource that provides a {@link Pipeline} configured for Spark. */
    @Rule
    public SparkIntegrationTestResource spark = SparkIntegrationTestResource.ofLocal();

    /**
     * Basic unit test using all default values (except for the path) on an in-memory DFS cluster.
     */
    @Category(RunnableOnService.class)
    @Ignore("BEAM-1206")
    @Test
    public void testBasicDefaults() throws IOException {
        FileSystem fs = FileSystem.get(spark.createHadoopConfiguration());
        String fileSpec = fs.getUri().resolve("/tmp/test/input.csv").toString();

        // Configure the component.
        SimpleFileIoOutputProperties props = SimpleFileIoOutputRuntimeTest.createOutputComponentProperties();
        props.getDatasetProperties().path.setValue(fileSpec);
        props.getDatasetProperties().format.setValue(SimpleFileIoFormat.AVRO);

        // Create the runtime.
        SimpleFileIoOutputRuntime runtime = new SimpleFileIoOutputRuntime();
        runtime.initialize(null, props);

        // Use the runtime in a Spark pipeline to test.
        final Pipeline p = spark.createPipeline();
        PCollection<IndexedRecord> input = p.apply( //
                Create.of(ConvertToIndexedRecord.convertToAvro(new String[] { "1", "one" }), //
                        ConvertToIndexedRecord.convertToAvro(new String[] { "2", "two" }))); //
        input.apply(runtime);

        // And run the test.
        p.run().waitUntilFinish();

        // Check the expected values.
        MiniDfsResource.assertReadFile(fs, fileSpec, "1;one", "2;two");
    }
}
