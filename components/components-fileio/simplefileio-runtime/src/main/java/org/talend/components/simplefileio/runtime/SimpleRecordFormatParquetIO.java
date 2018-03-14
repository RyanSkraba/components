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
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.VoidCoder;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.simplefileio.runtime.beamcopy.Write;
import org.talend.components.simplefileio.runtime.sinks.ParquetHdfsFileSink;
import org.talend.components.simplefileio.runtime.sinks.UnboundedWrite;
import org.talend.components.simplefileio.runtime.sources.ParquetHdfsFileSource;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;

public class SimpleRecordFormatParquetIO extends SimpleRecordFormatBase {

    static {
        // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
        SimpleFileIOAvroRegistry.get();
    }

    public SimpleRecordFormatParquetIO(UgiDoAs doAs, String path, boolean overwrite, int limit, boolean mergeOutput) {
        super(doAs, path, overwrite, limit, mergeOutput);
    }

    @Override
    public PCollection<IndexedRecord> read(PBegin in) {
        LazyAvroCoder<IndexedRecord> lac = LazyAvroCoder.of();

        ParquetHdfsFileSource source = ParquetHdfsFileSource.of(doAs, path, lac);
        source.setLimit(limit);
        source.getExtraHadoopConfiguration().addFrom(getExtraHadoopConfiguration());

        PCollection<KV<Void, IndexedRecord>> read = in.apply(Read.from(source)) //
                .setCoder(source.getDefaultOutputCoder());

        PCollection<IndexedRecord> pc1 = read.apply(Values.<IndexedRecord> create());

        return pc1;
    }

    @Override
    public PDone write(PCollection<IndexedRecord> in) {
        ParquetHdfsFileSink sink = new ParquetHdfsFileSink(doAs, path, overwrite, mergeOutput);
        sink.getExtraHadoopConfiguration().addFrom(getExtraHadoopConfiguration());

        PCollection<KV<Void, IndexedRecord>> pc1 = in.apply(ParDo.of(new FormatParquet()));
        pc1 = pc1.setCoder(KvCoder.of(VoidCoder.of(), LazyAvroCoder.of()));
        if (in.isBounded() == PCollection.IsBounded.BOUNDED) {
            return pc1.apply(Write.to(sink));
        } else {
            return pc1.apply(UnboundedWrite.of(sink));
        }
    }

    public static class FormatParquet extends DoFn<IndexedRecord, KV<Void, IndexedRecord>> {

        @ProcessElement
        public void processElement(ProcessContext c) {
            c.output(KV.of((Void) null, c.element()));
        }
    }

}
