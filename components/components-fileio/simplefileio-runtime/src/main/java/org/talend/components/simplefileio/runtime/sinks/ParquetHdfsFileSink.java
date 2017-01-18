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
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.mapreduce.Job;
import org.apache.parquet.avro.AvroParquetOutputFormat;
import org.apache.parquet.avro.AvroWriteSupport;
import org.apache.parquet.hadoop.ParquetOutputFormat;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;

/**
 * Sink for Parquet files.
 */
public class ParquetHdfsFileSink extends UgiFileSinkBase<Void, IndexedRecord> {

    public ParquetHdfsFileSink(UgiDoAs doAs, String path) {
        super(doAs, path, (Class) AvroParquetOutputFormat.class);
    }

    @Override
    protected void configure(Job job, KV<Void, IndexedRecord> sample) {
        IndexedRecord record = (IndexedRecord) sample.getValue();
        AvroWriteSupport.setSchema(job.getConfiguration(), record.getSchema());
        ParquetOutputFormat.setCompression(job, CompressionCodecName.SNAPPY);
    }

}
