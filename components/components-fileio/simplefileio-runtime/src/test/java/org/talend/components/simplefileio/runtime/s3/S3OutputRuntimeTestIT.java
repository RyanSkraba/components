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

package org.talend.components.simplefileio.runtime.s3;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.fs.FileSystem;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties;
import org.talend.components.simplefileio.SimpleFileIOFormat;
import org.talend.components.simplefileio.s3.S3DatasetProperties;
import org.talend.components.simplefileio.s3.output.S3OutputProperties;
import org.talend.components.test.MiniDfsResource;
import org.talend.components.test.SparkIntegrationTestResource;

public class S3OutputRuntimeTestIT {

    /** Set up credentials for integration tests. */
    @Rule
    public S3TestResource s3 = S3TestResource.of();

    @Rule
    public SparkIntegrationTestResource spark = SparkIntegrationTestResource.ofLocal();

    @Test
    public void testCsv_merge() throws IOException {
        S3DatasetProperties datasetProps = s3.createS3DatasetProperties();
        datasetProps.format.setValue(SimpleFileIOFormat.CSV);
        datasetProps.recordDelimiter.setValue(SimpleFileIODatasetProperties.RecordDelimiterType.LF);
        datasetProps.fieldDelimiter.setValue(SimpleFileIODatasetProperties.FieldDelimiterType.SEMICOLON);
        S3OutputProperties outputProperties = new S3OutputProperties("out");
        outputProperties.init();
        outputProperties.setDatasetProperties(datasetProps);
        outputProperties.mergeOutput.setValue(true);

        // Create the runtime.
        S3OutputRuntime runtime = new S3OutputRuntime();
        runtime.initialize(null, outputProperties);

        // Use the runtime in a Spark pipeline to test.
        final Pipeline p = spark.createPipeline();
        PCollection<IndexedRecord> input = p.apply( //
                Create.of(ConvertToIndexedRecord.convertToAvro(new String[] { "1", "one" }), //
                        ConvertToIndexedRecord.convertToAvro(new String[] { "2", "two" }))); //
        input.apply(runtime);

        // And run the test.
        p.run().waitUntilFinish();

        FileSystem s3FileSystem = S3Connection.createFileSystem(datasetProps);
        MiniDfsResource.assertReadFile(s3FileSystem, s3.getS3APath(datasetProps), "1;one", "2;two");
        MiniDfsResource.assertFileNumber(s3FileSystem, s3.getS3APath(datasetProps), 1);

    }

    @Test
    public void testAvro_merge() throws IOException {
        S3DatasetProperties datasetProps = s3.createS3DatasetProperties();
        datasetProps.format.setValue(SimpleFileIOFormat.AVRO);
        S3OutputProperties outputProperties = new S3OutputProperties("out");
        outputProperties.init();
        outputProperties.setDatasetProperties(datasetProps);
        outputProperties.mergeOutput.setValue(true);

        // Create the runtime.
        S3OutputRuntime runtime = new S3OutputRuntime();
        runtime.initialize(null, outputProperties);

        // Use the runtime in a Spark pipeline to test.
        final Pipeline p = spark.createPipeline();
        PCollection<IndexedRecord> input = p.apply( //
                Create.of(ConvertToIndexedRecord.convertToAvro(new String[] { "1", "one" }), //
                        ConvertToIndexedRecord.convertToAvro(new String[] { "2", "two" }))); //
        input.apply(runtime);

        // And run the test.
        p.run().waitUntilFinish();

        FileSystem s3FileSystem = S3Connection.createFileSystem(datasetProps);
        MiniDfsResource.assertReadAvroFile(s3FileSystem, s3.getS3APath(datasetProps),
                new HashSet<IndexedRecord>(Arrays.asList(ConvertToIndexedRecord.convertToAvro(new String[] { "1", "one" }), //
                        ConvertToIndexedRecord.convertToAvro(new String[] { "2", "two" }))),
                false);
        MiniDfsResource.assertFileNumber(s3FileSystem, s3.getS3APath(datasetProps), 1);

    }

    @Test
    public void testParquet_merge() throws IOException {
        S3DatasetProperties datasetProps = s3.createS3DatasetProperties();
        datasetProps.format.setValue(SimpleFileIOFormat.PARQUET);
        S3OutputProperties outputProperties = new S3OutputProperties("out");
        outputProperties.init();
        outputProperties.setDatasetProperties(datasetProps);
        outputProperties.mergeOutput.setValue(true);

        // Create the runtime.
        S3OutputRuntime runtime = new S3OutputRuntime();
        runtime.initialize(null, outputProperties);

        // Use the runtime in a Spark pipeline to test.
        final Pipeline p = spark.createPipeline();
        PCollection<IndexedRecord> input = p.apply( //
                Create.of(ConvertToIndexedRecord.convertToAvro(new String[] { "1", "one" }), //
                        ConvertToIndexedRecord.convertToAvro(new String[] { "2", "two" }))); //
        input.apply(runtime);

        // And run the test.
        p.run().waitUntilFinish();

        FileSystem s3FileSystem = S3Connection.createFileSystem(datasetProps);
        MiniDfsResource.assertReadParquetFile(s3FileSystem, s3.getS3APath(datasetProps),
                new HashSet<IndexedRecord>(Arrays.asList(ConvertToIndexedRecord.convertToAvro(new String[] { "1", "one" }), //
                        ConvertToIndexedRecord.convertToAvro(new String[] { "2", "two" }))),
                false);
        MiniDfsResource.assertFileNumber(s3FileSystem, s3.getS3APath(datasetProps), 1);

    }

}
