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

package org.talend.components.adapter.beam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.options.PipelineOptions;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 * Make the TCOMP BoundedSource work on Beam runtime, wrapper it with relate beam interface
 * TCOMP BoundedSource must be serializable
 * Value type is always IndexedRecord, {@link LazyAvroCoder} is the default coder for IndexedRecord
 */
public class TCompBoundedSourceAdapter extends BoundedSource<IndexedRecord> {

    private org.talend.components.api.component.runtime.BoundedSource tCompSource;

    public TCompBoundedSourceAdapter(org.talend.components.api.component.runtime.BoundedSource tCompSource) {
        this.tCompSource = tCompSource;
    }

    @Override
    public List<? extends BoundedSource<IndexedRecord>> splitIntoBundles(long desiredBundleSizeBytes, PipelineOptions options)
            throws Exception {
        List<? extends org.talend.components.api.component.runtime.BoundedSource> boundedSources = tCompSource
                .splitIntoBundles(desiredBundleSizeBytes, null);
        List<TCompBoundedSourceAdapter> sources = new ArrayList();
        for (org.talend.components.api.component.runtime.BoundedSource boundedSource : boundedSources) {
            sources.add(new TCompBoundedSourceAdapter(boundedSource));
        }
        return sources;
    }

    @Override
    public long getEstimatedSizeBytes(PipelineOptions options) throws Exception {
        return tCompSource.getEstimatedSizeBytes(null);
    }

    @Override
    public boolean producesSortedKeys(PipelineOptions options) throws Exception {
        return tCompSource.producesSortedKeys(null);
    }

    @Override
    public BoundedReader<IndexedRecord> createReader(PipelineOptions options) throws IOException {
        return new TCompReaderAdapter(tCompSource.createReader(null), null);
    }

    @Override
    public void validate() {
        tCompSource.validate(null);
    }

    @Override
    public Coder<IndexedRecord> getDefaultOutputCoder() {
        return LazyAvroCoder.of();
    }

    protected static class TCompReaderAdapter<T> extends BoundedSource.BoundedReader {

        IndexedRecordConverter<T, ?> indexedRecordConverter;

        private org.talend.components.api.component.runtime.BoundedReader<T> reader;

        private TCompBoundedSourceAdapter source;

        public TCompReaderAdapter(org.talend.components.api.component.runtime.BoundedReader reader, TCompBoundedSourceAdapter source) {
            this.reader = reader;
            this.source = source;
        }

        public boolean start() throws IOException {
            return reader.start();
        }

        public boolean advance() throws IOException {
            return reader.advance();
        }

        public IndexedRecord getCurrent() throws NoSuchElementException {
            T current = reader.getCurrent();
            if (current == null) {
                return null;
            }
            if (current instanceof IndexedRecord) {
                return (IndexedRecord) current;
            }
            if (indexedRecordConverter == null) {
                indexedRecordConverter = (IndexedRecordConverter<T, ?>) (new AvroRegistry())
                        .createIndexedRecordConverter(current.getClass());
            }
            return indexedRecordConverter.convertToAvro(current);
        }

        public void close() throws IOException {
            reader.close();
        }

        public BoundedSource getCurrentSource() {
            return source;
        }
    }
}
