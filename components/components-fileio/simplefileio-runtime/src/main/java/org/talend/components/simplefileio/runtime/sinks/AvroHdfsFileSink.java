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
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;

/**
 * Sink for Avro files.
 */
public class AvroHdfsFileSink extends UgiFileSinkBase<AvroKey<IndexedRecord>, NullWritable> {

    public AvroHdfsFileSink(UgiDoAs doAs, String path) {
        super(doAs, path, (Class) AvroKeyOutputFormat.class);
    }

    @Override
    protected void configure(Job job, KV<AvroKey<IndexedRecord>, NullWritable> sample) {
        AvroKey<IndexedRecord> k = sample.getKey();
        AvroJob.setOutputKeySchema(job, k.datum().getSchema());
        FileOutputFormat.setCompressOutput(job, true);
        job.getConfiguration().set(AvroJob.CONF_OUTPUT_CODEC, DataFileConstants.SNAPPY_CODEC);
    }
}
