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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.parquet.avro.AvroParquetOutputFormat;
import org.apache.parquet.avro.AvroWriteSupport;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetOutputFormat;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.hadoop.metadata.FileMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;
import org.talend.components.simplefileio.runtime.utils.FileSystemUtil;

/**
 * Sink for Parquet files.
 */
public class ParquetHdfsFileSink extends UgiFileSinkBase<Void, IndexedRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(ParquetHdfsFileSink.class);

    public ParquetHdfsFileSink(UgiDoAs doAs, String path, boolean overwrite, boolean mergeOutput) {
        super(doAs, path, overwrite, mergeOutput, (Class) AvroParquetOutputFormat.class);
    }

    @Override
    protected void configure(Job job, KV<Void, IndexedRecord> sample) {
        super.configure(job, sample);
        IndexedRecord record = (IndexedRecord) sample.getValue();
        AvroWriteSupport.setSchema(job.getConfiguration(), record.getSchema());
        ParquetOutputFormat.setCompression(job, CompressionCodecName.SNAPPY);
    }

    @Override
    protected void mergeOutput(FileSystem fs, String sourceFolder, String targetFile) throws IOException {
        FileStatus[] sourceStatuses = FileSystemUtil.listSubFiles(fs, sourceFolder);
        List<Path> sourceFiles = new ArrayList<>();
        for (FileStatus sourceStatus : sourceStatuses) {
            sourceFiles.add(sourceStatus.getPath());
        }
        FileMetaData mergedMeta = ParquetFileWriter.mergeMetadataFiles(sourceFiles, fs.getConf()).getFileMetaData();
        ParquetFileWriter writer = new ParquetFileWriter(fs.getConf(), mergedMeta.getSchema(), new Path(targetFile),
                ParquetFileWriter.Mode.CREATE);
        writer.start();
        for (Path input : sourceFiles) {
            writer.appendFile(fs.getConf(), input);
        }
        writer.end(mergedMeta.getKeyValueMetaData());
    }
}
