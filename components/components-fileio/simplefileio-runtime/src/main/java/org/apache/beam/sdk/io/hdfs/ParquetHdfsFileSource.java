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

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.values.KV;
import org.apache.parquet.avro.AvroParquetInputFormat;

/**
 * Avro implementation of HDFSFileSource.
 *
 * This implementation fixes a bug with the default coder and ensures that the Avro object is cloned before returning.
 */
public class ParquetHdfsFileSource extends HDFSFileSource<Void, IndexedRecord> {

    private final Coder<KV<Void, IndexedRecord>> coder;

    protected ParquetHdfsFileSource(String filepattern, Coder<KV<Void, IndexedRecord>> coder) {
        // TODO(rskraba): I need to cast to Class here?
        super(filepattern, (Class) AvroParquetInputFormat.class, Void.class, IndexedRecord.class);
        this.coder = coder;
    }

    protected ParquetHdfsFileSource(String filepattern, Coder<KV<Void, IndexedRecord>> coder, SerializableSplit serializableSplit) {
        // TODO(rskraba): I need to cast to Class here?
        super(filepattern, (Class) AvroParquetInputFormat.class, Void.class, IndexedRecord.class, serializableSplit);
        this.coder = coder;
    }

    public static ParquetHdfsFileSource from(String filepattern, Coder<KV<Void, IndexedRecord>> coder) {
        return new ParquetHdfsFileSource(filepattern, coder);
    }

    @Override
    // TODO(rskraba): The HDFS beam version fails on looking up the default!
    public Coder<KV<Void, IndexedRecord>> getDefaultOutputCoder() {
        if (coder != null)
            return coder;
        return super.getDefaultOutputCoder();
    }

}