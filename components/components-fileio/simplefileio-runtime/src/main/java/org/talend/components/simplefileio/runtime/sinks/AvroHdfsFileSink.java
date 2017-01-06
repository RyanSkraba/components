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
package org.talend.components.simplefileio.runtime.sinks;

import org.apache.avro.file.DataFileConstants;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyOutputFormat;
import org.apache.beam.sdk.io.hdfs.ConfigurableHDFSFileSink;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Sink for Avro files.
 */
public class AvroHdfsFileSink extends ConfigurableHDFSFileSink<AvroKey<IndexedRecord>, NullWritable> {

    public AvroHdfsFileSink(String path) {
        super(path, (Class) AvroKeyOutputFormat.class);
    }

    public AvroHdfsFileSink(String path, Configuration conf) {
        super(path, (Class) AvroKeyOutputFormat.class, conf);
    }

    @Override
    public WriteOperation<KV<AvroKey<IndexedRecord>, NullWritable>, ?> createWriteOperation(PipelineOptions options) {
        return new AvroWriteOperation(this, path);
    }

    public static class AvroWriteOperation extends HDFSWriteOperation<AvroKey<IndexedRecord>, NullWritable> {

        public AvroWriteOperation(AvroHdfsFileSink sink, String path) {
            super(sink, path, sink.formatClass);
        }

        @Override
        public AvroWriter createWriter(PipelineOptions options) throws Exception {
            return new AvroWriter(this, path);
        }

        public static class AvroWriter extends ConfigureWithSampleHDFSWriter<AvroKey<IndexedRecord>, NullWritable> {

            public AvroWriter(AvroWriteOperation writeOperation, String path) {
                super(writeOperation, path, writeOperation.formatClass);
            }

            protected void configure(Job job) {
                AvroKey<IndexedRecord> k = (AvroKey<IndexedRecord>) getSample().getKey();
                AvroJob.setOutputKeySchema(job, k.datum().getSchema());
                FileOutputFormat.setCompressOutput(job, true);
                job.getConfiguration().set(AvroJob.CONF_OUTPUT_CODEC, DataFileConstants.SNAPPY_CODEC);
            }
        }
    }
}
