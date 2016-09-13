package org.talend.components.filedelimited.runtime;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.java8.SerializableFunction;

public class FileDelimitedAvroRegistry extends AvroRegistry {

    private static FileDelimitedAvroRegistry fileInputInstance;

    public FileDelimitedAvroRegistry() {

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

    public static FileDelimitedAvroRegistry getFileInputInstance() {
        if (fileInputInstance == null) {
            fileInputInstance = new FileDelimitedAvroRegistry();
        }
        return fileInputInstance;
    }

    private Schema inferSchemaRecord(IndexedRecord in) {
        return in.getSchema();
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

    public AvroConverter<String, ?> getConverterFromString(org.apache.avro.Schema.Field f) {
        Schema fieldSchema = AvroUtils.unwrapIfNullable(f.schema());
        if (AvroUtils.isSameType(fieldSchema, AvroUtils._boolean())) {
            return new StringToBooleanConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._decimal())) {
            return new StringToDecimalConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._double())) {
            return new StringToDoubleConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._float())) {
            return new StringToFloatConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._int())) {
            return new StringToIntegerConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._date())) {
            return new StringToDateConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._long())) {
            return new StringToLongConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._bytes())) {
            return new StringToBytesConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._byte())) {
            return new StringToByteConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._character())) {
            return new StringToCharacterConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._string())) {
            return super.getConverter(String.class);
        }
        throw new UnsupportedOperationException("The type " + fieldSchema.getType() + " is not supported."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static abstract class AsStringConverter<T> implements AvroConverter<String, T> {

        private final Schema.Field field;

        AsStringConverter(Schema.Field field) {
            this.field = field;
        }

        @Override
        public Schema getSchema() {
            return field.schema();
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

    public static class StringToBooleanConverter extends AsStringConverter<Boolean> {

        StringToBooleanConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Boolean convertToAvro(String value) {
            return value == null ? null : Boolean.parseBoolean(value);
        }
    }

    public static class StringToDecimalConverter extends AsStringConverter<BigDecimal> {

        StringToDecimalConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public BigDecimal convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : new BigDecimal(value);
        }
    }

    public static class StringToDoubleConverter extends AsStringConverter<Double> {

        StringToDoubleConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Double convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : Double.parseDouble(value);
        }
    }

    public static class StringToLongConverter extends AsStringConverter<Long> {

        StringToLongConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Long convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : Long.parseLong(value);
        }
    }

    public static class StringToFloatConverter extends AsStringConverter<Float> {

        StringToFloatConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Float convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : Float.parseFloat(value);
        }
    }

    public static class StringToDateConverter extends AsStringConverter<Long> {

        private final SimpleDateFormat format;

        StringToDateConverter(Schema.Field field) {
            super(field);
            String pattern = field.getProp(SchemaConstants.TALEND_COLUMN_PATTERN);
            format = new SimpleDateFormat(pattern);
        }

        @Override
        public Long convertToAvro(String value) {
            try {
                return StringUtils.isEmpty(value) ? null : format.parse(value).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        @Override
        public String convertToDatum(Long value) {
            return value == null ? null : format.format(new Date(value));
        }

    }

    public static class StringToIntegerConverter extends AsStringConverter<Integer> {

        StringToIntegerConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Integer convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : Integer.parseInt(value);
        }
    }

    public static class StringToByteConverter extends AsStringConverter<Byte> {

        StringToByteConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Byte convertToAvro(String value) {
            if (StringUtils.isEmpty(value)) {
                return null;
            }
            // TODO is decode
            boolean isDecode = false;
            if (isDecode) {
                return Byte.decode(value).byteValue();
            } else {
                return Byte.parseByte(value);
            }
        }
    }

    public static class StringToBytesConverter extends AsStringConverter<byte[]> {

        StringToBytesConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public byte[] convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : value.getBytes();
        }
    }

    public static class StringToCharacterConverter extends AsStringConverter<Character> {

        StringToCharacterConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Character convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : value.charAt(0);
        }
    }
}
