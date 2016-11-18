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

import org.apache.avro.Schema;

import org.apache.avro.generic.IndexedRecord;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderException;
import org.apache.beam.sdk.coders.StandardCoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

public class LazyAvroCoder<T extends IndexedRecord> extends StandardCoder<T> {

    private final String id;

    private final transient Schema schema;

    private transient AvroCoder myInternalAvroCoder;

    private final static HashMap<String, Schema> schemaRegistry = new HashMap<>();

    public static <T extends IndexedRecord> LazyAvroCoder of(String id) {
        return new LazyAvroCoder(id);
    }

    protected LazyAvroCoder(String id) {
        this.id = id;
        this.schema = null;
    }

    @Override
    public void encode(T value, OutputStream outputStream, Context context) throws CoderException, IOException {
        if (myInternalAvroCoder == null) {
            schemaRegistry.put(id, value.getSchema());
        }
        myInternalAvroCoder = AvroCoder.of(schemaRegistry.get(id));
        myInternalAvroCoder.encode(value, outputStream, context);
    }

    @Override
    public T decode(InputStream inputStream, Context context) throws CoderException, IOException {
        if (myInternalAvroCoder == null) {
            return null;
        }
        myInternalAvroCoder = AvroCoder.of(schemaRegistry.get(id));
        return (T) myInternalAvroCoder.decode(inputStream, context);
    }

    @Override
    public List<? extends Coder<?>> getCoderArguments() {
        return null;
    }

    @Override
    public void verifyDeterministic() throws NonDeterministicException {

    }

    public Schema getSchema() {
        return this.schema;
    }

}
