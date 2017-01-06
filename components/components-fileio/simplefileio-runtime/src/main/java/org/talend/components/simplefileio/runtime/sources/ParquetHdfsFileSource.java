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

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.VoidCoder;
import org.apache.parquet.avro.AvroParquetInputFormat;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;

/**
 * Parquet implementation of HDFSFileSource.
 *
 * This implementation fixes a bug with the default coder and ensures that the Avro object is cloned before returning.
 */
public class ParquetHdfsFileSource extends FileSourceBase<Void, IndexedRecord, ParquetHdfsFileSource> {

    private final LazyAvroCoder<IndexedRecord> lac;

    private ParquetHdfsFileSource(String filepattern, LazyAvroCoder<IndexedRecord> lac, SerializableSplit serializableSplit) {
        super(filepattern, (Class) AvroParquetInputFormat.class, Void.class, IndexedRecord.class, serializableSplit);
        this.lac = lac;
        setDefaultCoder(VoidCoder.of(), (LazyAvroCoder) lac);
    }

    public static ParquetHdfsFileSource of(String filepattern, LazyAvroCoder<IndexedRecord> lac) {
        return new ParquetHdfsFileSource(filepattern, lac, null);
    }

    @Override
    protected ParquetHdfsFileSource createSourceForSplit(SerializableSplit serializableSplit) {
        ParquetHdfsFileSource source = new ParquetHdfsFileSource(filepattern, lac, serializableSplit);
        source.setLimit(getLimit());
        return source;
    }

    @Override
    protected TalendHdfsFileReader<Void, IndexedRecord, ParquetHdfsFileSource> createReaderForSplit(
            SerializableSplit serializableSplit) throws IOException {
        return new TalendHdfsFileReader<>(this);
    }
}