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

package org.talend.components.adapter.beam;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.Sink;
import org.apache.beam.sdk.options.PipelineOptions;
import org.talend.components.api.component.runtime.Result;

import java.util.Map;

/**
 * Make the TCOMP Sink work on Beam runtime, wrapper it with relate beam interface
 * TCOMP Sink must be serializable
 */
public class TCompSinkAdapter extends Sink<IndexedRecord> {

    private org.talend.components.api.component.runtime.Sink tCompSink;

    public TCompSinkAdapter(org.talend.components.api.component.runtime.Sink tCompSink) {
        this.tCompSink = tCompSink;
    }

    @Override
    public void validate(PipelineOptions options) {
        tCompSink.validate(null);
    }

    @Override
    public WriteOperation<IndexedRecord, ?> createWriteOperation(PipelineOptions options) {
        return new TCompWriteOperationAdapter(tCompSink.createWriteOperation(), this);
    }

    public static class TCompWriteOperationAdapter extends Sink.WriteOperation<IndexedRecord, Result> {

        private org.talend.components.api.component.runtime.WriteOperation<Result> tCompWriteOperation;

        private TCompSinkAdapter sink;

        public TCompWriteOperationAdapter(org.talend.components.api.component.runtime.WriteOperation tCompWriteOperation,
                TCompSinkAdapter sink) {
            this.tCompWriteOperation = tCompWriteOperation;
            this.sink = sink;
        }

        @Override
        public void initialize(PipelineOptions options) throws Exception {
            tCompWriteOperation.initialize(null);
        }

        @Override
        public void finalize(Iterable<Result> writerResults, PipelineOptions options) throws Exception {
            //TODO store the result somewhere?
            Map<String, Object> results = tCompWriteOperation.finalize(writerResults, null);
        }

        @Override
        public Sink.Writer<IndexedRecord, Result> createWriter(PipelineOptions options) throws Exception {
            return new TCompWriterAdapter(tCompWriteOperation.createWriter(null), this);
        }

        @Override
        public Sink<IndexedRecord> getSink() {
            return sink;
        }

        public Coder<Result> getWriterResultCoder() {
            return SerializableCoder.of(Result.class);
        }
    }

    public static class TCompWriterAdapter extends Sink.Writer<IndexedRecord, Result> {

        private org.talend.components.api.component.runtime.Writer<Result> tCompWriter;

        private TCompWriteOperationAdapter wo;

        public TCompWriterAdapter(org.talend.components.api.component.runtime.Writer<Result> tCompWriter,
                TCompWriteOperationAdapter wo) {
            this.tCompWriter = tCompWriter;
            this.wo = wo;
        }

        @Override
        public void open(String uId) throws Exception {
            tCompWriter.open(uId);
        }

        @Override
        public void write(IndexedRecord value) throws Exception {
            tCompWriter.write(value);
        }

        @Override
        public Result close() throws Exception {
            return tCompWriter.close();
        }

        @Override
        public WriteOperation<IndexedRecord, Result> getWriteOperation() {
            return wo;
        }
    }
}
