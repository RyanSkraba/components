package org.talend.components.common.runtime;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.AvroConverter;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.util.AvroUtils;
import org.talend.daikon.java8.SerializableFunction;
import org.talend.daikon.talend6.Talend6SchemaConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class GenericAvroRegistry extends AvroRegistry {

    private GenericAvroRegistry() {

        // Ensure that we know how to get Schemas for these Salesforce objects.
        registerSchemaInferrer(IndexedRecord.class, new SerializableFunction<IndexedRecord, Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(IndexedRecord t) {
                return inferSchemaRecord(t);
            }

        });

        registerSchemaInferrer(Schema.Field.class, new SerializableFunction<Schema.Field, Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(Schema.Field t) {
                return inferSchemaField(t);
            }

        });
    }

    private static final GenericAvroRegistry sInstance = new GenericAvroRegistry();

    public static GenericAvroRegistry get() {
        return sInstance;
    }

    private Schema inferSchemaRecord(IndexedRecord in) {
        FieldAssembler<Schema> builder = SchemaBuilder.builder().record(in.getSchema().getName()).fields();
        for (Schema.Field field : in.getSchema().getFields()) {
            Schema fieldSchema = inferSchema(field);
            Object fieldDefault = field.defaultVal();
            if (null == fieldDefault) {
                builder = builder.name(field.name()).type(fieldSchema).noDefault();
            } else {
                builder = builder.name(field.name()).type(fieldSchema).withDefault(fieldDefault);
            }
        }
        return builder.endRecord();
    }

    /**
     * Infers an Avro schema for the given Salesforce Field. This can be an expensive operation so the schema should be
     * cached where possible. The return type will be the Avro Schema that can contain the field data without loss of
     * precision.
     *
     * @param field the Field to analyse.
     * @return the schema for data that the field describes.
     */
    private Schema inferSchemaField(Schema.Field field) {
        return field.schema();
    }

    public AvroConverter<String, ?> convertToString(Schema.Field f) {
        Schema fieldSchema = AvroUtils.unwrapIfNullable(f.schema());

        switch (fieldSchema.getType()) {
            case LONG:
                String pattern = f.getProp(Talend6SchemaConstants.TALEND6_COLUMN_PATTERN);
                if (pattern != null) {
                    fieldSchema.addProp(Talend6SchemaConstants.TALEND6_COLUMN_PATTERN, pattern);
                    return new DateToStringConvert(fieldSchema);
                } else {
                    return super.getConverter(String.class);
                }
            default:
                return super.getConverter(String.class);
        }
    }

    public static abstract class AsStringConverter<T> implements AvroConverter<String, T> {

        private final Schema schema;

        AsStringConverter(Schema schema) {
            this.schema = schema;
        }

        @Override
        public Schema getSchema() {
            return schema;
        }

        @Override
        public Class<String> getDatumClass() {
            return String.class;
        }

        @Override
        public String convertToDatum(T value) {
            return value == null ? null : String.valueOf(value);
        }
    }


    public static class DateToStringConvert extends AsStringConverter<Date> {

        private final SimpleDateFormat format;

        DateToStringConvert(Schema schema) {
            super(schema);
            String pattern = schema.getProp(Talend6SchemaConstants.TALEND6_COLUMN_PATTERN);
            // TODO: null handling
            format = new SimpleDateFormat(pattern);
        }

        @Override
        public Date convertToAvro(String value) {
            try {
                return value == null ? null : format.parse(value);
            } catch (ParseException e) {
                // TODO: error handling
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        @Override
        public String convertToDatum(Date value) {
            return value == null ? null : format.format(value);
        }

    }

}
