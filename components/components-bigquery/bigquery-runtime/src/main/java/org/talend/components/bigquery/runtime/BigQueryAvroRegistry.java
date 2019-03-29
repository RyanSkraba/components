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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.SchemaBuilder;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.java8.SerializableFunction;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldValue;
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

        SchemaBuilder.FieldAssembler<org.apache.avro.Schema> fieldAssembler =
                SchemaBuilder.record("BigQuerySchema").fields();
        for (Field bqField : bqFields) {
            String name = bqField.getName();
            org.apache.avro.Schema fieldSchema = inferSchemaField(bqField);
            fieldAssembler = fieldAssembler.name(name).type(fieldSchema).noDefault();
        }
        return fieldAssembler.endRecord();
    }

    private org.apache.avro.Schema inferSchemaField(Field field) {
        Field.Mode mode = field.getMode();

        // Get the "basic" type of the field.
        org.apache.avro.Schema fieldSchema = inferSchemaFieldWithoutMode(field);

        // BigQuery fields are NULLABLE by default.
        if (Field.Mode.NULLABLE == mode || mode == null) {
            fieldSchema = AvroUtils.wrapAsNullable(fieldSchema);
        } else if (Field.Mode.REPEATED == mode) {
            // Determine if the field is an array.
            // https://cloud.google.com/bigquery/docs/reference/standard-sql/data-types#array-type
            fieldSchema = SchemaBuilder.array().items(fieldSchema);
        }
        return fieldSchema;
    }

    /**
     * All BigQuery types except Record/Struct and Arrays, no matter legacy or not, as {@link LegacySQLTypeName} is a
     * wrapper for {@link StandardSQLTypeName}
     */
    private org.apache.avro.Schema inferSchemaFieldWithoutMode(Field field) {
        LegacySQLTypeName sqlType = field.getType();
        switch (sqlType.getStandardType()) {
            case STRUCT:
            String name = field.getName();
            // Struct type
            // https://cloud.google.com/bigquery/docs/reference/standard-sql/data-types#struct-type
            SchemaBuilder.FieldAssembler<org.apache.avro.Schema> itemFieldAssembler =
                    SchemaBuilder.record(name).fields();
            for (Field itemField : field.getSubFields()) {
                itemFieldAssembler.name(itemField.getName()).type(inferSchemaField(itemField)).noDefault();
            }
            org.apache.avro.Schema recordSchema = itemFieldAssembler.endRecord();
            return recordSchema;
        case BYTES:
            return AvroUtils._bytes();
        case INT64:
            return AvroUtils._long();
        case FLOAT64:
            return AvroUtils._double();
        case BOOL:
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

    public Map<String, Object> convertFileds(List<FieldValue> fields, org.apache.avro.Schema schema) {
        Map<String, Object> container = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            FieldValue fieldValue = fields.get(i);
            org.apache.avro.Schema.Field fieldMeta = schema.getFields().get(i);
            container.put(fieldMeta.name(), convertField(fieldValue, fieldMeta.schema()));
        }
        return container;
    }

    private Object convertField(FieldValue fieldValue, org.apache.avro.Schema fieldSchema) {
        boolean nullable = AvroUtils.isNullable(fieldSchema);
        if (nullable && fieldValue.isNull()) {
            return null;
        }
        fieldSchema = AvroUtils.unwrapIfNullable(fieldSchema);
        switch (fieldValue.getAttribute()) {
        case PRIMITIVE:
            if (BigQueryType.TIMESTAMP.toString().equals(fieldSchema.getProp(TALEND_COLUMN_DB_TYPE))) {
                Double doubleValue = ((Long) fieldValue.getTimestampValue()) / 1000000.0;
                return formatTimestamp(doubleValue.toString());
            } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._double())) {
                return fieldValue.getDoubleValue();
            } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._boolean())) {
                return fieldValue.getBooleanValue();
            } else {
                return fieldValue.getValue();
            }
        case REPEATED:
            List<Object> listValue = new ArrayList<>();
            List<FieldValue> repeatedChildValue = fieldValue.getRepeatedValue();
            for (FieldValue childValue : repeatedChildValue) {
                listValue.add(convertField(childValue, fieldSchema.getElementType()));
            }
            return listValue;
        case RECORD:
            return convertFileds(fieldValue.getRecordValue(), fieldSchema);
        }
        throw TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_ARGUMENT).create();
    }

    /**
     * Formats BigQuery seconds-since-epoch into String matching JSON export. Thread-safe and
     * immutable.
     */
    private static final DateTimeFormatter DATE_AND_SECONDS_FORMATTER =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();

    // Package private for BigQueryTableRowIterator to use.
    static String formatTimestamp(String timestamp) {
        // timestamp is in "seconds since epoch" format, with scientific notation.
        // e.g., "1.45206229112345E9" to mean "2016-01-06 06:38:11.123456 UTC".
        // Separate into seconds and microseconds.
        double timestampDoubleMicros = Double.parseDouble(timestamp) * 1000000;
        long timestampMicros = (long) timestampDoubleMicros;
        long seconds = timestampMicros / 1000000;
        int micros = (int) (timestampMicros % 1000000);
        String dayAndTime = DATE_AND_SECONDS_FORMATTER.print(seconds * 1000);

        // No sub-second component.
        if (micros == 0) {
            return String.format("%s UTC", dayAndTime);
        }

        // Sub-second component.
        int digits = 6;
        int subsecond = micros;
        while (subsecond % 10 == 0) {
            digits--;
            subsecond /= 10;
        }
        String formatString = String.format("%%0%dd", digits);
        String fractionalSeconds = String.format(formatString, subsecond);
        return String.format("%s.%s UTC", dayAndTime, fractionalSeconds);
    }
}
