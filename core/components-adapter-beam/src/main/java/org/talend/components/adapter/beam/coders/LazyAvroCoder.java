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

import static org.talend.components.adapter.beam.transform.ConvertToIndexedRecord.convertToAvro;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AtomicCoder;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CannotProvideCoderException;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderException;
import org.apache.beam.sdk.coders.CoderProvider;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.adapter.beam.BeamAdapterErrorCode;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.java8.Supplier;

import com.google.common.annotations.VisibleForTesting;

/**
 * Encode and decode records using an Avro {@link Schema} discovered at runtime.
 *
 * Normally, Beam Coders can fully specify how an instance should be serialized when the pipeline is constructed. This
 * Coder uses Avro serialization, but does not know the Avro {@link Schema} used to serialize the data is not known
 * until the pipeline is actually run.
 *
 * This is accomplished by adding a "distributed state" to the pipeline in the form of the {@link AvroSchemaHolder}.
 *
 * @param <T> The expected type of object.
 */
public class LazyAvroCoder<T> extends AtomicCoder<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(LazyAvroCoder.class);

    private transient static Supplier<AvroSchemaHolder> schemaRegistryFactory;

    private final AvroSchemaHolder avroSchemaHolder;

    private transient IndexedRecordConverter<T, IndexedRecord> converter;

    private transient AvroCoder<IndexedRecord> internalAvroCoder;

    protected LazyAvroCoder() {
        this.avroSchemaHolder = getSchemaRegistry().get();
    }

    public static void resetSchemaRegistry() {
        if (schemaRegistryFactory instanceof StaticSchemaHolderSupplier) {
            ((StaticSchemaHolderSupplier) schemaRegistryFactory).reset();
        }
        schemaRegistryFactory = null;
    }

    public static void setSchemaRegistry(Supplier<AvroSchemaHolder> factory) {
        // Ensure that the schema registry has not already been set.
        if (LazyAvroCoder.schemaRegistryFactory != null)
            throw BeamAdapterErrorCode.createSchemaRegistryAlreadyExists(null, schemaRegistryFactory.toString());
        schemaRegistryFactory = factory;
    }

    public static Supplier<AvroSchemaHolder> getSchemaRegistry() {
        if (schemaRegistryFactory == null) {
            schemaRegistryFactory = new StaticSchemaHolderSupplier();
        }
        return schemaRegistryFactory;
    }

    /**
     * Returns a {@link CoderProvider} which uses the {@link LazyAvroCoderProvider} if possible for all types.
     *
     * <p>
     * This method is invoked reflectively from {@link DefaultCoder}.
     */
    @SuppressWarnings("unused")
    public static CoderProvider getCoderProvider() {
        return new LazyAvroCoderProvider();
    }

    public static LazyAvroCoder of() {
        return new LazyAvroCoder();
    }

    protected Schema getSchema() {
        Schema schema = avroSchemaHolder.get();
        if (schema == null) {
            // This should not occur, since the encode must be called before the schema is available. If it happens, it
            // is an unrecoverable error.
            throw new Pipeline.PipelineExecutionException(
                    new NoSuchElementException("No schema found for " + avroSchemaHolder.getAvroSchemaId()));
        }
        return schema;
    }

    @Override
    public void encode(Object value, OutputStream outputStream) throws IOException {
        if (converter == null) {
            converter = ConvertToIndexedRecord.getConverter((T) value);
        }
        IndexedRecord ir = converter.convertToAvro((T) value);
        if (internalAvroCoder == null) {
            Schema s = converter.getSchema();
            avroSchemaHolder.put(s);
            @SuppressWarnings("unchecked")
            AvroCoder<IndexedRecord> tCoder = (AvroCoder<IndexedRecord>) (AvroCoder<? extends IndexedRecord>) AvroCoder
                    .of(ir.getSchema());
            internalAvroCoder = tCoder;
        }
        LOG.debug("Internal AvroCoder's schema is {}", internalAvroCoder.getSchema());
        LOG.debug("Encode value is {}", value);
        internalAvroCoder.encode(convertToAvro(value), outputStream);
    }

    @Override
    public T decode(InputStream inputStream) throws CoderException, IOException {
        if (internalAvroCoder == null) {
            @SuppressWarnings("unchecked")
            AvroCoder<IndexedRecord> tCoder = (AvroCoder<IndexedRecord>) (AvroCoder<? extends IndexedRecord>) AvroCoder
                    .of(getSchema());
            internalAvroCoder = tCoder;
        }
        return (T) internalAvroCoder.decode(inputStream);
    }

    /**
     * A AvroSchemaHolder supplier that can be used if none is specified.
     *
     * This stores schemas in a list in memory, so either
     * <ol>
     * <li>any Beam Pipeline must run in a single JVM, or</li>
     * <li>the PCollections that use this coder must not have their data transferred across nodes in a reduce-type or
     * repartitioning operation.</li>
     * </ol>
     */
    @VisibleForTesting
    static class StaticSchemaHolderSupplier implements Supplier<AvroSchemaHolder> {

        private static final AtomicInteger count = new AtomicInteger();

        private static final ArrayList<Schema> schemaList = new ArrayList<>();

        @Override
        public AvroSchemaHolder get() {
            return new StaticSchemaHolder();
        }

        /**
         * This must only be called when there are no running Pipelines using a static LazyAvroCoder.
         */
        public static void reset() {
            count.set(0);
            schemaList.clear();
        }

        @VisibleForTesting
        static Integer getCount() {
            return count.get();
        }

        @VisibleForTesting
        static List<Schema> getSchemas() {
            return schemaList;
        }

        synchronized private static Schema getSchema(int i) {
            return schemaList.size() > i ? StaticSchemaHolderSupplier.schemaList.get(i) : null;
        }

        synchronized private static void putSchema(int i, Schema s) {
            // Ensure that the array has enough space to hold the schema.
            while (StaticSchemaHolderSupplier.schemaList.size() < i + 1)
                StaticSchemaHolderSupplier.schemaList.add(null);
            StaticSchemaHolderSupplier.schemaList.set(i, s);
        }

        private static class StaticSchemaHolder implements AvroSchemaHolder {

            private final int uid = StaticSchemaHolderSupplier.count.getAndIncrement();

            @Override
            public String getAvroSchemaId() {
                return "Lazy" + uid;
            }

            @Override
            public Schema get() {
                return StaticSchemaHolderSupplier.getSchema(uid);
            }

            @Override
            public void put(Schema s) {
                StaticSchemaHolderSupplier.putSchema(uid, s);
            }
        }
    }

    /**
     * A {@link CoderProvider} that constructs a {@link LazyAvroCoderProvider} for any class that implements IndexedRecord.
     */
    static class LazyAvroCoderProvider extends CoderProvider {

        @Override
        public <T> Coder<T> coderFor(TypeDescriptor<T> typeDescriptor, List<? extends Coder<?>> componentCoders)
                throws CannotProvideCoderException {

            Type t = typeDescriptor.getType();
            if (IndexedRecord.class.isAssignableFrom(typeDescriptor.getRawType())) {
                Coder<T> c = LazyAvroCoder.<T> of();
                return c;
            }
            throw new CannotProvideCoderException(String.format("Cannot provide %s because %s is not implement IndexedRecord",
                    LazyAvroCoder.class.getSimpleName(), typeDescriptor));
        }
    }

}
