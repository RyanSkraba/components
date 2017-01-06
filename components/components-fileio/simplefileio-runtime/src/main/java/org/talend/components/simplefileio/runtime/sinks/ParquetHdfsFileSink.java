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

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.io.hdfs.ConfigurableHDFSFileSink;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.parquet.avro.AvroParquetOutputFormat;
import org.apache.parquet.avro.AvroWriteSupport;
import org.apache.parquet.hadoop.ParquetOutputFormat;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

/**
 * Sink for Parquet files.
 */
public class ParquetHdfsFileSink extends ConfigurableHDFSFileSink<Void, IndexedRecord> {

    public ParquetHdfsFileSink(String path) {
        super(path, (Class) AvroParquetOutputFormat.class);
    }

    public ParquetHdfsFileSink(String path, Configuration conf) {
        super(path, (Class) AvroParquetOutputFormat.class, conf);
    }

    @Override
    public WriteOperation<KV<Void, IndexedRecord>, ?> createWriteOperation(PipelineOptions options) {
        return new ParquetWriteOperation(this, path);
    }

    public static class ParquetWriteOperation extends HDFSWriteOperation<Void, IndexedRecord> {

        public ParquetWriteOperation(ParquetHdfsFileSink sink, String path) {
            super(sink, path, sink.formatClass);
        }

        @Override
        public ParquetWriter createWriter(PipelineOptions options) throws Exception {
            return new ParquetWriter(this, path);
        }

        public static class ParquetWriter extends ConfigureWithSampleHDFSWriter<Void, IndexedRecord> {

            public ParquetWriter(ParquetWriteOperation writeOperation, String path) {
                super(writeOperation, path, writeOperation.formatClass);
            }

            protected void configure(Job job) {
                IndexedRecord record = (IndexedRecord) getSample().getValue();
                AvroWriteSupport.setSchema(job.getConfiguration(), record.getSchema());
                ParquetOutputFormat.setCompression(job, CompressionCodecName.SNAPPY);
            }
        }
    }
}
