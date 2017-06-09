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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;

import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileConstants;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyOutputFormat;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;
import org.talend.components.simplefileio.runtime.utils.FileSystemUtil;

/**
 * Sink for Avro files.
 */
public class AvroHdfsFileSink extends UgiFileSinkBase<AvroKey<IndexedRecord>, NullWritable> {

    private static final Logger LOG = LoggerFactory.getLogger(AvroHdfsFileSink.class);

    public AvroHdfsFileSink(UgiDoAs doAs, String path, boolean overwrite, boolean mergeOutput) {
        super(doAs, path, overwrite, mergeOutput, (Class) AvroKeyOutputFormat.class);
    }

    @Override
    protected void configure(Job job, KV<AvroKey<IndexedRecord>, NullWritable> sample) {
        super.configure(job, sample);
        AvroKey<IndexedRecord> k = sample.getKey();
        AvroJob.setOutputKeySchema(job, k.datum().getSchema());
        FileOutputFormat.setCompressOutput(job, true);
        job.getConfiguration().set(AvroJob.CONF_OUTPUT_CODEC, DataFileConstants.SNAPPY_CODEC);
    }

    @Override
    protected boolean mergeOutput(FileSystem fs, String sourceFolder, String targetFile) {
        try (DataFileWriter<GenericRecord> writer = new DataFileWriter<GenericRecord>(new GenericDatumWriter<GenericRecord>())) {
            FileStatus[] sourceStatuses = FileSystemUtil.listSubFiles(fs, sourceFolder);
            Schema schema = null;
            String inputCodec = null;
            OutputStream output = new BufferedOutputStream(fs.create(new Path(targetFile)));
            for (FileStatus sourceStatus : sourceStatuses) {
                try (DataFileStream<GenericRecord> reader = new DataFileStream<GenericRecord>(
                        new BufferedInputStream(fs.open(sourceStatus.getPath())), new GenericDatumReader<GenericRecord>())) {

                    if (schema == null) {
                        schema = reader.getSchema();
                        for (String key : reader.getMetaKeys()) {
                            if (!DataFileWriter.isReservedMeta(key)) {
                                writer.setMeta(key, reader.getMeta(key));
                            }
                        }
                        inputCodec = reader.getMetaString(DataFileConstants.CODEC);
                        if (inputCodec == null) {
                            inputCodec = DataFileConstants.NULL_CODEC;
                        }
                        writer.setCodec(CodecFactory.fromString(inputCodec));
                        writer.create(schema, output);
                    }
                    writer.appendAllFrom(reader, false);
                }
            }
        } catch (Exception e) {
            LOG.error("Error when merging files in {}.\n{}", sourceFolder, e.getMessage());
            return false;
        }
        return true;
    }
}
