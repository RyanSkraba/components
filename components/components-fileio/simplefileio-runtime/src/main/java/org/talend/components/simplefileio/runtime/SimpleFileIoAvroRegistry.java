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
package org.talend.components.simplefileio.runtime;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.ComparableIndexedRecordBase;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.java8.SerializableFunction;

public class SimpleFileIoAvroRegistry extends AvroRegistry {

    public static final String RECORD_NAME = "StringArrayRecord";

    public static final String FIELD_PREFIX = "field";

    private static final SimpleFileIoAvroRegistry sInstance = new SimpleFileIoAvroRegistry();

    /**
     * Hidden constructor: use the singleton.
     */
    private SimpleFileIoAvroRegistry() {

        // Ensure that we know how to get Schemas for String arrays.
        registerSchemaInferrer(String[].class, new SerializableFunction<String[], Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(String[] t) {
                return inferStringArray(t);
            }

        });

        // Ensure that we know how to get IndexedRecords for String arrays.
        registerIndexedRecordConverter(String[].class, StringArrayToIndexedRecordConverter.class);
    }

    public static SimpleFileIoAvroRegistry get() {
        return sInstance;
    }

    /**
     * Infers an Avro schema for the given DescribeSObjectResult. This can be an expensive operation so the schema
     * should be cached where possible. This is always an {@link Schema.Type#RECORD}.
     *
     * @param in the DescribeSObjectResult to analyse.
     * @return the schema for data given from the object.
     */
    private Schema inferStringArray(String[] in) {
        List<Schema.Field> fields = new ArrayList<>();

        SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.record(RECORD_NAME).fields();
        for (int i = 0; i < in.length; i++) {
            fa = fa.name(FIELD_PREFIX + i).type(Schema.create(Schema.Type.STRING)).noDefault();
        }
        return fa.endRecord();
    }

    public static class StringArrayToIndexedRecordConverter implements IndexedRecordConverter<String[], StringArrayIndexedRecord> {

        private Schema schema = null;

        @Override
        public Schema getSchema() {
            return schema;
        }

        @Override
        public void setSchema(Schema schema) {
            this.schema = schema;
        }

        @Override
        public Class<String[]> getDatumClass() {
            return String[].class;
        }

        @Override
        public String[] convertToDatum(StringArrayIndexedRecord value) {
            return value.value;
        }

        @Override
        public StringArrayIndexedRecord convertToAvro(String[] value) {
            if (schema == null)
                schema = SimpleFileIoAvroRegistry.get().inferSchema(value);
            return new StringArrayIndexedRecord(schema, value);
        }
    }

    public static class StringArrayIndexedRecord extends ComparableIndexedRecordBase {

        private final Schema schema;

        private final String[] value;

        public StringArrayIndexedRecord(Schema schema, String[] value) {
            this.schema = schema;
            this.value = value;
        }

        @Override
        public Schema getSchema() {
            return schema;
        }

        @Override
        public Object get(int i) {
            if (value.length > i) {
                return value[i];
            } else {
                return "";
            }
        }

        @Override
        public void put(int i, Object v) {
            value[i] = v == null ? null : String.valueOf(v);
        }
    }
}
