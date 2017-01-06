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
package org.talend.components.simplefileio.runtime.sources;

import java.io.IOException;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapreduce.AvroKeyInputFormat;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.hdfs.WritableCoder;
import org.apache.beam.sdk.util.CoderUtils;
import org.apache.beam.sdk.values.KV;
import org.apache.hadoop.io.NullWritable;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.simplefileio.runtime.coders.LazyAvroKeyWrapper;

/**
 * Avro implementation of HDFSFileSource.
 *
 * This implementation fixes a bug with the default coder and ensures that the Avro object is cloned before returning.
 */
public class AvroHdfsFileSource extends FileSourceBase<AvroKey, NullWritable, AvroHdfsFileSource> {

    private final LazyAvroCoder<?> lac;

    private AvroHdfsFileSource(String filepattern, LazyAvroCoder<?> lac, SerializableSplit serializableSplit) {
        super(filepattern, (Class) AvroKeyInputFormat.class, AvroKey.class, NullWritable.class, serializableSplit);
        this.lac = lac;
        setDefaultCoder(LazyAvroKeyWrapper.of(lac), WritableCoder.of(NullWritable.class));
    }

    public static AvroHdfsFileSource of(String filepattern, LazyAvroCoder<?> lac) {
        return new AvroHdfsFileSource(filepattern, lac, null);
    }

    @Override
    protected AvroHdfsFileSource createSourceForSplit(SerializableSplit serializableSplit) {
        AvroHdfsFileSource source = new AvroHdfsFileSource(filepattern, lac, serializableSplit);
        source.setLimit(getLimit());
        return source;
    }

    @Override
    protected TalendAvroHdfsFileReader createReaderForSplit(SerializableSplit serializableSplit) throws IOException {
        return new TalendAvroHdfsFileReader(this, filepattern, serializableSplit);
    }

    private static class TalendAvroHdfsFileReader extends TalendHdfsFileReader<AvroKey, NullWritable, AvroHdfsFileSource> {

        public TalendAvroHdfsFileReader(AvroHdfsFileSource source, String filepattern, SerializableSplit serializableSplit)
                throws IOException {
            super(source);
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