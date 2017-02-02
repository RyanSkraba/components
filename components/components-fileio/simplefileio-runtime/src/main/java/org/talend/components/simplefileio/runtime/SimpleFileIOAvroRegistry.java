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

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.csv.CSVRecord;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.ComparableIndexedRecordBase;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.java8.SerializableFunction;

public class SimpleFileIOAvroRegistry extends AvroRegistry {

    public static final String RECORD_NAME = "StringArrayRecord";

    public static final String FIELD_PREFIX = "field";

    private static final SimpleFileIOAvroRegistry sInstance = new SimpleFileIOAvroRegistry();

    /**
     * Hidden constructor: use the singleton.
     */
    private SimpleFileIOAvroRegistry() {

        // Ensure that we know how to get Schemas for String arrays and CSVRecords
        registerSchemaInferrer(String[].class, new SerializableFunction<String[], Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(String[] t) {
                return inferStringArray(t);
            }

        });
        registerSchemaInferrer(CSVRecord.class, new SerializableFunction<CSVRecord, Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(CSVRecord t) {
                return inferCsvRecord(t);
            }

        });

        // Ensure that we know how to get IndexedRecords for String arrays.
        registerIndexedRecordConverter(String[].class, StringArrayToIndexedRecordConverter.class);
        registerIndexedRecordConverter(CSVRecord.class, CsvRecordToIndexedRecordConverter.class);
    }

    public static SimpleFileIOAvroRegistry get() {
        return sInstance;
    }

    /**
     * Infers an Avro schema for the given String array. This can be an expensive operation so the schema should be
     * cached where possible. This is always an {@link Schema.Type#RECORD}.
     *
     * @param in the String array to analyse.
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

    /**
     * Infers an Avro schema for the given String array. This can be an expensive operation so the schema should be
     * cached where possible. This is always an {@link Schema.Type#RECORD}.
     *
     * @param in the DescribeSObjectResult to analyse.
     * @return the schema for data given from the object.
     */
    private Schema inferCsvRecord(CSVRecord in) {
        List<Schema.Field> fields = new ArrayList<>();

        SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.record(RECORD_NAME).fields();
        for (int i = 0; i < in.size(); i++) {
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
                schema = SimpleFileIOAvroRegistry.get().inferSchema(value);
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

    public static class CsvRecordToIndexedRecordConverter implements IndexedRecordConverter<CSVRecord, CsvIndexedRecord> {

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
        public Class<CSVRecord> getDatumClass() {
            return CSVRecord.class;
        }

        @Override
        public CSVRecord convertToDatum(CsvIndexedRecord value) {
            return value.value;
        }

        @Override
        public CsvIndexedRecord convertToAvro(CSVRecord value) {
            if (schema == null)
                schema = SimpleFileIOAvroRegistry.get().inferSchema(value);
            return new CsvIndexedRecord(schema, value);
        }
    }

    public static class CsvIndexedRecord extends ComparableIndexedRecordBase {

        private final Schema schema;

        private final CSVRecord value;

        public CsvIndexedRecord(Schema schema, CSVRecord value) {
            this.schema = schema;
            this.value = value;
        }

        @Override
        public Schema getSchema() {
            return schema;
        }

        @Override
        public Object get(int i) {
            return value.get(i);
        }

        @Override
        public void put(int i, Object v) {
            throw new IndexedRecordConverter.UnmodifiableAdapterException();
        }
    }
}
