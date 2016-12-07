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
package org.apache.beam.sdk.io.hdfs;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.repackaged.com.google.common.base.Function;
import org.apache.beam.sdk.repackaged.com.google.common.collect.ImmutableList;
import org.apache.beam.sdk.repackaged.com.google.common.collect.Lists;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

/**
 * CSV implementation of HDFSFileSource.
 *
 * This implementation allows the recordDelimiter to be injected into the TextInputFormat.
 */
public class CsvHdfsFileSource extends HDFSFileSource<LongWritable, Text> {

    private final String recordDelimiter;

    protected CsvHdfsFileSource(String filepattern, String recordDelimiter) {
        super(filepattern, TextInputFormat.class, LongWritable.class, Text.class);
        this.recordDelimiter = recordDelimiter;
    }

    public CsvHdfsFileSource(String filepattern, String recordDelimiter, SerializableSplit serializableSplit) {
        super(filepattern, TextInputFormat.class, LongWritable.class, Text.class, serializableSplit);
        this.recordDelimiter = recordDelimiter;
    }

    public static CsvHdfsFileSource from(String filepattern, String recordDelimiter) {
        return new CsvHdfsFileSource(filepattern, recordDelimiter);
    }

    @Override
    public CsvHdfsFileReader createReader(PipelineOptions options) throws IOException {
        this.validate();

        if (serializableSplit == null) {
            return new CsvHdfsFileReader(this, filepattern, recordDelimiter);
        } else {
            return new CsvHdfsFileReader(this, filepattern, recordDelimiter, serializableSplit.getSplit());
        }
    }

    @Override
    public List<? extends BoundedSource<KV<LongWritable, Text>>> splitIntoBundles(long desiredBundleSizeBytes,
            PipelineOptions options) throws Exception {
        if (serializableSplit == null) {
            return Lists.transform(computeSplits(desiredBundleSizeBytes), new Function<InputSplit, CsvHdfsFileSource>() {

                @Override
                public CsvHdfsFileSource apply(@Nullable InputSplit inputSplit) {
                    return new CsvHdfsFileSource(filepattern, recordDelimiter, new SerializableSplit(inputSplit));
                }
            });
        } else {
            return ImmutableList.of(this);
        }
    }

    protected static class CsvHdfsFileReader extends HDFSFileReader<LongWritable, Text> {

        public CsvHdfsFileReader(CsvHdfsFileSource source, String filepattern, String recordDelimiter) throws IOException {
            super(source, filepattern, TextInputFormat.class);
            job.getConfiguration().set("textinputformat.record.delimiter", recordDelimiter);
        }

        public CsvHdfsFileReader(CsvHdfsFileSource source, String filepattern, String recordDelimiter, InputSplit split)
                throws IOException {
            super(source, filepattern, TextInputFormat.class, split);
            job.getConfiguration().set("textinputformat.record.delimiter", recordDelimiter);
        }
    }
}