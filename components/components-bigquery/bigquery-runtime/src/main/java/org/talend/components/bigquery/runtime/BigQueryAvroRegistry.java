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

package org.talend.components.bigquery.runtime;

import static org.talend.daikon.avro.SchemaConstants.TALEND_COLUMN_DB_TYPE;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.SchemaBuilder;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.ConvertAvroList;
import org.talend.daikon.java8.SerializableFunction;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;

public class BigQueryAvroRegistry extends AvroRegistry {

    private static final BigQueryAvroRegistry sInstance = new BigQueryAvroRegistry();

    private static final String REPEATED_MODE = "REPEATED";

    private static final String NULLABLE_MODE = "NULLABLE";

    private static final String REQUIRED_MODE = "REQUIRED";

    private BigQueryAvroRegistry() {
        registerSchemaInferrer(Schema.class, new SerializableFunction<Schema, org.apache.avro.Schema>() {

            @Override
            public org.apache.avro.Schema apply(Schema schema) {
                return inferBigQuerySchema(schema);
            }
        });
    }

    public static BigQueryAvroRegistry get() {
        return sInstance;
    }

    public TableSchema guessBigQuerySchema(org.apache.avro.Schema schema) {
        List<org.apache.avro.Schema.Field> fields = schema.getFields();
        if (fields.size() == 0) {
            return null;
        }
        List<TableFieldSchema> bqFields = new ArrayList<>();
        for (org.apache.avro.Schema.Field field : fields) {
            bqFields.add(tryArrayFieldSchema(field));
        }
        return new TableSchema().setFields(bqFields);
    }

    private TableFieldSchema tryArrayFieldSchema(org.apache.avro.Schema.Field field) {
        String fieldName = field.name();
        TableFieldSchema tableFieldSchema = new TableFieldSchema().setName(fieldName);
        boolean nullable = AvroUtils.isNullable(field.schema());
        if (!nullable) {
            tableFieldSchema = tableFieldSchema.setMode(REQUIRED_MODE);
        }
        org.apache.avro.Schema fieldSchema = AvroUtils.unwrapIfNullable(field.schema());
        if (fieldSchema.getType() == org.apache.avro.Schema.Type.ARRAY) {
            return tryFieldSchema(tableFieldSchema.setMode(REPEATED_MODE), fieldSchema.getElementType());
        }
        return tryFieldSchema(tableFieldSchema, fieldSchema);
    }

    private TableFieldSchema tryFieldSchema(TableFieldSchema fieldSchema, org.apache.avro.Schema avroSchema) {
        fieldSchema = fieldSchema.setType(getBQFieldType(avroSchema));

        if (avroSchema.getType() == org.apache.avro.Schema.Type.RECORD) {
            List<TableFieldSchema> childFields = new ArrayList<>();
            List<org.apache.avro.Schema.Field> avroChildFields = avroSchema.getFields();
            for (org.apache.avro.Schema.Field avroChildField : avroChildFields) {
                childFields.add(tryArrayFieldSchema(avroChildField));
            }
            fieldSchema.setFields(childFields);
        }
        return fieldSchema;
    }

    /**
     * Return the type defined on schema editor(user specify) first, else infer by the avro type
     *
     * @param schema
     * @return
     */
    private String getBQFieldType(org.apache.avro.Schema schema) {
        // try find db type which defined by user, and return
        if (schema.getProp(TALEND_COLUMN_DB_TYPE) != null) {
            return schema.getProp(TALEND_COLUMN_DB_TYPE);
        } // else infer by avro type, but for datetime/time/date/timestamp, only return string

        if (schema.getType() == org.apache.avro.Schema.Type.RECORD) {
            return BigQueryType.RECORD.toString();
        }
        if (AvroUtils.isSameType(schema, AvroUtils._boolean())) {
            return BigQueryType.BOOLEAN.toString();
        }
        if (AvroUtils.isSameType(schema, AvroUtils._bytes())) {
            return BigQueryType.BYTES.toString();
        }
        if (AvroUtils.isSameType(schema, AvroUtils._byte()) //
                || AvroUtils.isSameType(schema, AvroUtils._short()) //
                || AvroUtils.isSameType(schema, AvroUtils._int()) //
                || AvroUtils.isSameType(schema, AvroUtils._long())) {
            return BigQueryType.INTEGER.toString();
        }
        if (AvroUtils.isSameType(schema, AvroUtils._float()) //
                || AvroUtils.isSameType(schema, AvroUtils._double())) {
            return BigQueryType.FLOAT.toString();
        }
        // TODO(bchen): Need to find a way to specify db type for these type, and their avro type are String always,
        // no proper java type can work with these types

        // if (AvroUtils.isSameType(schema, AvroUtils._date())) {
        // return "DATETIME";
        // }
        // if (schema.getLogicalType() == LogicalTypes.date()) {
        // return "DATE";
        // }
        // if (schema.getLogicalType() == LogicalTypes.timeMicros() //
        // || schema.getLogicalType() == LogicalTypes.timeMillis()) {
        // return "TIME";
        // }
        // if (schema.getLogicalType() == LogicalTypes.timestampMicros() //
        // || schema.getLogicalType() == LogicalTypes.timestampMillis()) {
        // return "TIMESTAMP";
        //

        // others, should be AvroUtils._character(), AvroUtils._decimal(), AvroUtils._string()
        return BigQueryType.STRING.toString();
    }

    private org.apache.avro.Schema inferBigQuerySchema(Schema schema) {
        List<Field> bqFields = schema.getFields();
        if (bqFields.size() == 0) {
            return SchemaBuilder.builder().record("EmptyRecord").fields().endRecord();
        }

        SchemaBuilder.FieldAssembler<org.apache.avro.Schema> fieldAssembler = SchemaBuilder.record("BigQuerySchema").fields();

        for (Field bqField : bqFields) {
            String name = bqField.getName();
            Field.Type type = bqField.getType();
            Field.Mode mode = bqField.getMode();
            // All other primitive types
            org.apache.avro.Schema fieldSchema = inferSchemaDataTypeTryList(bqField);
            if (Field.Mode.NULLABLE == mode) {
                fieldSchema = AvroUtils.wrapAsNullable(fieldSchema);
            }
            fieldAssembler = fieldAssembler.name(name).type(fieldSchema).noDefault();
        }
        return fieldAssembler.endRecord();
    }

    private org.apache.avro.Schema inferSchemaDataTypeTryList(Field field) {
        String name = field.getName();
        Field.Type sqlType = field.getType();
        Field.Mode mode = field.getMode();
        if (Field.Mode.REPEATED == mode) {
            // Array type
            // https://cloud.google.com/bigquery/docs/reference/standard-sql/data-types#array-type
            org.apache.avro.Schema itemSchema = inferSchemaDataType(field);
            org.apache.avro.Schema arraySchema = SchemaBuilder.array().items(itemSchema);
            return arraySchema;
        } // not array
        return inferSchemaDataType(field);
    }

    /**
     * All BigQuery types except Record/Struct and Arrays, no matter legacy or not, as {@link LegacySQLTypeName} is a
     * wrapper for {@link StandardSQLTypeName}
     */
    private org.apache.avro.Schema inferSchemaDataType(Field field) {
        Field.Type sqlType = field.getType();
        switch (sqlType.getValue()) {
        case RECORD:
            String name = field.getName();
            // Struct type
            // https://cloud.google.com/bigquery/docs/reference/standard-sql/data-types#struct-type
            SchemaBuilder.FieldAssembler<org.apache.avro.Schema> itemFieldAssembler = SchemaBuilder.record(name).fields();
            for (Field itemField : sqlType.getFields()) {
                itemFieldAssembler.name(itemField.getName()).type(inferSchemaDataTypeTryList(itemField)).noDefault();
            }
            org.apache.avro.Schema recordSchema = itemFieldAssembler.endRecord();
            return recordSchema;
        case BYTES:
            return AvroUtils._bytes();
        case INTEGER:
            return AvroUtils._long();
        case FLOAT:
            return AvroUtils._double();
        case BOOLEAN:
            return AvroUtils._boolean();
        case DATETIME:
            org.apache.avro.Schema schemaDT = AvroUtils._string();
            schemaDT.addProp(TALEND_COLUMN_DB_TYPE, BigQueryType.DATETIME.toString());
            return schemaDT;
        case DATE:
            org.apache.avro.Schema schemaD = AvroUtils._string();
            schemaD.addProp(TALEND_COLUMN_DB_TYPE, BigQueryType.DATE.toString());
            return schemaD;
        // return LogicalTypes.date().addToSchema(AvroUtils._int());
        case TIME:
            org.apache.avro.Schema schemaT = AvroUtils._string();
            schemaT.addProp(TALEND_COLUMN_DB_TYPE, BigQueryType.TIME.toString());
            return schemaT;
        // return LogicalTypes.timeMicros().addToSchema(AvroUtils._long());
        case TIMESTAMP:
            org.apache.avro.Schema schemaTS = AvroUtils._string();
            schemaTS.addProp(TALEND_COLUMN_DB_TYPE, BigQueryType.TIMESTAMP.toString());
            return schemaTS;
        // return LogicalTypes.timestampMicros().addToSchema(AvroUtils._long());
        case STRING:
            return AvroUtils._string();
        default:
            // This should never occur.
            throw new RuntimeException("The BigQuery data type " + sqlType + " is not handled.");
        }
    }

    /**
     *
     * @param fieldSchema
     * @param <T>
     * @return
     */
    public <T> AvroConverter<? super T, ?> getConverter(org.apache.avro.Schema fieldSchema) {
        fieldSchema = AvroUtils.unwrapIfNullable(fieldSchema);
        if (AvroUtils.isSameType(fieldSchema, AvroUtils._boolean())) {
            return (AvroConverter<? super T, ?>) getConverter(Boolean.class);
        }
        if (AvroUtils.isSameType(fieldSchema, AvroUtils._double())) {
            return (AvroConverter<? super T, ?>) new AvroConverter<Number, Double>() {

                @Override
                public org.apache.avro.Schema getSchema() {
                    return AvroUtils._double();
                }

                @Override
                public Class<Number> getDatumClass() {
                    return Number.class;
                }

                @Override
                public Number convertToDatum(Double aDouble) {
                    return aDouble;
                }

                @Override
                public Double convertToAvro(Number number) {
                    return number == null ? null : number.doubleValue();
                }
            };
        }
        if (AvroUtils.isSameType(fieldSchema, AvroUtils._long())) {
            return (AvroConverter<? super T, ?>) new AvroConverter<Object, Long>() {

                @Override
                public org.apache.avro.Schema getSchema() {
                    return AvroUtils._long();
                }

                @Override
                public Class<Object> getDatumClass() {
                    return Object.class;
                }

                @Override
                public Number convertToDatum(Long aLong) {
                    return aLong;
                }

                @Override
                public Long convertToAvro(Object number) {
                    if (number instanceof Number)
                        return ((Number) number).longValue();
                    return number == null ? null : Long.valueOf(String.valueOf(number));
                }
            };
        }
        if (AvroUtils.isSameType(fieldSchema, AvroUtils._bytes())) {
            return new Unconverted(ByteBuffer.class, AvroUtils._bytes());
        }
        if (fieldSchema.getType() == org.apache.avro.Schema.Type.RECORD) {
            BigQueryTableRowIndexedRecordConverter recordTypeIndexedRecordConverter = new BigQueryTableRowIndexedRecordConverter();
            recordTypeIndexedRecordConverter.setSchema(fieldSchema);
            return (AvroConverter<? super T, ?>) recordTypeIndexedRecordConverter;
        }
        if (fieldSchema.getType() == org.apache.avro.Schema.Type.ARRAY) {
            org.apache.avro.Schema elementSchema = AvroUtils.unwrapIfNullable(fieldSchema.getElementType());
            // List.class is enough here, ConvertAvroList do not use it actually
            return new ConvertAvroList(List.class, elementSchema, getConverter(elementSchema));
        }
        // When construct BigQuery TableRow object, the value's java type is String for the rest of LegacySQLTypeName
        // And after AvroCoder, the type of String changes to Utf8, so need to convert it to String too
        return new AvroConverter<Object, Object>() {

            @Override
            public org.apache.avro.Schema getSchema() {
                return org.apache.avro.Schema.create(org.apache.avro.Schema.Type.STRING);
            }

            @Override
            public Class getDatumClass() {
                return Object.class;
            }

            @Override
            public Object convertToDatum(Object o) {
                return o == null ? null : String.valueOf(o);
            }

            @Override
            public Object convertToAvro(Object o) {
                return o == null ? null : String.valueOf(o);
            }
        };
    }

    public enum BigQueryType {
        RECORD,
        BOOLEAN,
        BYTES,
        INTEGER,
        FLOAT,
        DATETIME,
        DATE,
        TIME,
        TIMESTAMP,
        STRING
    }

}
