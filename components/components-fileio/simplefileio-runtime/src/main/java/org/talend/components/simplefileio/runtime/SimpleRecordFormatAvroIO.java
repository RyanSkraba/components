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
package org.talend.components.simplefileio.runtime;

import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.mapred.AvroKey;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.io.Write;
import org.apache.beam.sdk.io.hdfs.WritableCoder;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Keys;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.hadoop.io.NullWritable;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.simplefileio.runtime.coders.LazyAvroKeyWrapper;
import org.talend.components.simplefileio.runtime.sinks.AvroHdfsFileSink;
import org.talend.components.simplefileio.runtime.sources.AvroHdfsFileSource;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;

public class SimpleRecordFormatAvroIO extends SimpleRecordFormatBase {

    static {
        // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
        SimpleFileIOAvroRegistry.get();
    }

    public SimpleRecordFormatAvroIO(UgiDoAs doAs, String path, int limit) {
        super(doAs, path, limit);
    }

    @Override
    public PCollection<IndexedRecord> read(PBegin in) {
        // Reuseable coder.
        LazyAvroCoder<Object> lac = LazyAvroCoder.of();

        AvroHdfsFileSource source = AvroHdfsFileSource.of(doAs, path, lac);
        source.getExtraHadoopConfiguration().addFrom(getExtraHadoopConfiguration());

        source.setLimit(limit);
        PCollection<KV<AvroKey, NullWritable>> read = in.apply(Read.from(source)) //
                .setCoder(source.getDefaultOutputCoder());

        PCollection<AvroKey> pc1 = read.apply(Keys.<AvroKey> create());

        PCollection<Object> pc2 = pc1.apply(ParDo.of(new ExtractRecordFromAvroKey()));
        pc2 = pc2.setCoder(lac);

        PCollection<IndexedRecord> pc3 = pc2.apply(ConvertToIndexedRecord.<Object, IndexedRecord> of());

        return pc3;
    }

    @Override
    public PDone write(PCollection<IndexedRecord> in) {
        LazyAvroKeyWrapper lakw = LazyAvroKeyWrapper.of();
        AvroHdfsFileSink sink = new AvroHdfsFileSink(doAs, path);
        sink.getExtraHadoopConfiguration().addFrom(getExtraHadoopConfiguration());

        PCollection<KV<AvroKey<IndexedRecord>, NullWritable>> pc1 = in.apply(ParDo.of(new FormatAvro()));
        pc1 = pc1.setCoder(KvCoder.of(lakw, WritableCoder.of(NullWritable.class)));
        return pc1.apply(Write.to(sink));
    }

    public static class ExtractRecordFromAvroKey extends DoFn<AvroKey, Object> {

        static {
            // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
            SimpleFileIOAvroRegistry.get();
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            AvroKey in = c.element();
            c.output(in.datum());
        }
    }

    public static class FormatAvro extends DoFn<IndexedRecord, KV<AvroKey<IndexedRecord>, NullWritable>> {

        @ProcessElement
        public void processElement(ProcessContext c) {
            c.output(KV.of(new AvroKey<>(c.element()), NullWritable.get()));
        }
    }
}
