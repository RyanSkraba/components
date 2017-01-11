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
package org.talend.components.adapter.beam.coders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.*;
import org.apache.beam.sdk.coders.protobuf.ProtoCoder;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.daikon.avro.AvroRegistry;

public class LazyAvroCoder<T> extends AtomicCoder<Object> {

    private final String id;

    private transient AvroCoder internalAvroCoder;

    private final static HashMap<String, Schema> schemaRegistry = new HashMap<>();

    private final static AtomicInteger count = new AtomicInteger();

    protected LazyAvroCoder() {
        this("Lazy" + count.getAndIncrement());
    }

    protected LazyAvroCoder(String id) {
        this.id = id;
    }

    public static LazyAvroCoder of() {
        return new LazyAvroCoder();
    }

    public static LazyAvroCoder of(String id) {
        return new LazyAvroCoder(id);
    }

    public String getAvroSchemaId() {
        return id;
    }

    protected Schema getSchema() {
        Schema schema = schemaRegistry.get(id);
        if (schema == null) {
            // This should not occur, since the encode must be called before the schema is available. If it happens, it
            // is an unrecoverable error.
            throw new Pipeline.PipelineExecutionException(new NoSuchElementException("No schema found for " + id));
        }
        return schema;
    }

    @Override
    public void encode(Object value, OutputStream outputStream, Context context) throws CoderException, IOException {
        if (internalAvroCoder == null) {
            Schema s = (value instanceof IndexedRecord) ? ((IndexedRecord) value).getSchema() : ConvertToIndexedRecord
                    .convertToAvro(value).getSchema();
            schemaRegistry.put(id, s);
            internalAvroCoder = AvroCoder.of(s);
        }
        if (value instanceof IndexedRecord)
            internalAvroCoder.encode(value, outputStream, context);
        else
            internalAvroCoder.encode(ConvertToIndexedRecord.convertToAvro(value), outputStream, context);
    }

    @Override
    public T decode(InputStream inputStream, Context context) throws CoderException, IOException {
        if (internalAvroCoder == null) {
            internalAvroCoder = AvroCoder.of(getSchema());
        }
        return (T) internalAvroCoder.decode(inputStream, context);
    }

    public static void registerAsFallback(Pipeline p) {
        p.getCoderRegistry().setFallbackCoderProvider(
                CoderProviders.firstOf(p.getCoderRegistry().getFallbackCoderProvider(), LazyAvroCoder.coderProvider()));
    }

    /**
     * The implementation of the {@link CoderProvider} for this {@link ProtoCoder} returned by {@link #coderProvider()}.
     */
    private static CoderProvider PROVIDER = new CoderProvider() {

        final AvroRegistry registry = new AvroRegistry();

        @Override
        public <T> Coder<T> getCoder(TypeDescriptor<T> type) throws CannotProvideCoderException {

            Type t = type.getType();
            if (t instanceof Class && registry.createIndexedRecordConverter((Class<?>) t) != null) {
                return LazyAvroCoder.of();
            }
            throw new CannotProvideCoderException(String.format("Cannot provide %s because %s is not registered in the %s.",
                    LazyAvroCoder.class.getSimpleName(), type, AvroRegistry.class.getSimpleName()));
        }
    };

    /**
     * The implementation of the {@link CoderProvider} for this {@link ProtoCoder} returned by {@link #coderProvider()}.
     */
    private static CoderFactory FACTORY = new CoderFactory() {

        @Override
        public Coder<?> create(List<? extends Coder<?>> componentCoders) {
            return LazyAvroCoder.of();
        }

        @Override
        public List<Object> getInstanceComponents(Object value) {
            return Collections.emptyList();
        }
    };

    /**
     * A {@link CoderProvider} that returns a {@link LazyAvroCoder} if it is possible to encode/decode the given class
     * or type.
     */
    private static CoderProvider coderProvider() {
        return PROVIDER;
    }

    /**
     * A {@link CoderProvider} that returns a {@link LazyAvroCoder} if it is possible to encode/decode the given class
     * or type.
     */
    public static CoderFactory coderFactory() {
        return FACTORY;
    }

}
