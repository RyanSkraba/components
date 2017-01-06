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
package org.apache.beam.sdk.io.hdfs;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapreduce.AvroKeyInputFormat;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.repackaged.com.google.common.base.Function;
import org.apache.beam.sdk.repackaged.com.google.common.collect.ImmutableList;
import org.apache.beam.sdk.repackaged.com.google.common.collect.Lists;
import org.apache.beam.sdk.util.CoderUtils;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

/**
 * Avro implementation of HDFSFileSource.
 *
 * This implementation fixes a bug with the default coder and ensures that the Avro object is cloned before returning.
 */
public class TalendAvroHdfsFileSource extends HDFSFileSource<AvroKey, NullWritable> {

    private final Coder<KV<AvroKey, NullWritable>> coder;

    protected TalendAvroHdfsFileSource(String filepattern, Coder<KV<AvroKey, NullWritable>> coder) {
        // TODO(rskraba): I need to cast to Class here?
        super(filepattern, (Class) AvroKeyInputFormat.class, AvroKey.class, NullWritable.class);
        this.coder = coder;
    }

    protected TalendAvroHdfsFileSource(String filepattern, Coder<KV<AvroKey, NullWritable>> coder, SerializableSplit serializableSplit) {
        // TODO(rskraba): I need to cast to Class here?
        super(filepattern, (Class) AvroKeyInputFormat.class, AvroKey.class, NullWritable.class, serializableSplit);
        this.coder = coder;
    }

    public static TalendAvroHdfsFileSource from(String filepattern, Coder<KV<AvroKey, NullWritable>> coder) {
        return new TalendAvroHdfsFileSource(filepattern, coder);
    }

    @Override
    // TODO(rskraba): The HDFS beam version fails on looking up the default!
    public Coder<KV<AvroKey, NullWritable>> getDefaultOutputCoder() {
        if (coder != null)
            return coder;
        return super.getDefaultOutputCoder();
    }

    @Override
    public MyAvroKeyHdfsFileReader createReader(PipelineOptions options) throws IOException {
        this.validate();

        if (serializableSplit == null) {
            return new MyAvroKeyHdfsFileReader(this, filepattern, (Class) formatClass);
        } else {
            return new MyAvroKeyHdfsFileReader(this, filepattern, (Class) formatClass, serializableSplit.getSplit());
        }
    }

    @Override
    public List<? extends BoundedSource<KV<AvroKey, NullWritable>>> splitIntoBundles(long desiredBundleSizeBytes,
            PipelineOptions options) throws Exception {
        if (serializableSplit == null) {
            return Lists.transform(computeSplits(desiredBundleSizeBytes),
                    new Function<InputSplit, BoundedSource<KV<AvroKey, NullWritable>>>() {

                        @Override
                        public BoundedSource<KV<AvroKey, NullWritable>> apply(@Nullable InputSplit inputSplit) {
                            return new TalendAvroHdfsFileSource(filepattern, coder, new SerializableSplit(inputSplit));
                        }
                    });
        } else {
            return ImmutableList.of(this);
        }
    }

    protected static class MyAvroKeyHdfsFileReader extends HDFSFileReader<AvroKey, NullWritable> {

        public MyAvroKeyHdfsFileReader(BoundedSource<KV<AvroKey, NullWritable>> source, String filepattern,
                Class<? extends FileInputFormat<?, ?>> formatClass) throws IOException {
            super(source, filepattern, formatClass);
        }

        public MyAvroKeyHdfsFileReader(BoundedSource<KV<AvroKey, NullWritable>> source, String filepattern,
                Class<? extends FileInputFormat<?, ?>> formatClass, InputSplit split) throws IOException {
            super(source, filepattern, formatClass, split);
        }

        @Override
        protected KV<AvroKey, NullWritable> nextPair() throws IOException, InterruptedException {
            // Not only is the AvroKey reused by the file format, but the underlying GenericRecord is as well.
            KV<AvroKey, NullWritable> kv = super.nextPair();
            GenericRecord gr = (GenericRecord) kv.getKey().datum();
            gr = CoderUtils.clone(AvroCoder.of(gr.getSchema()), gr);
            return KV.of(new AvroKey(gr), kv.getValue());
        }

    }
}
