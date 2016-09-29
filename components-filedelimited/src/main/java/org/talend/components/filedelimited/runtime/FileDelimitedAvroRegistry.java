package org.talend.components.filedelimited.runtime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.ComponentConstants;
import org.talend.components.common.runtime.FormatterUtils;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.java8.SerializableFunction;

public class FileDelimitedAvroRegistry extends AvroRegistry {

    private static FileDelimitedAvroRegistry fileDelimitedInstance;

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

    public static FileDelimitedAvroRegistry get() {
        if (fileDelimitedInstance == null) {
            fileDelimitedInstance = new FileDelimitedAvroRegistry();
        }
        return fileDelimitedInstance;
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

    public AvroConverter<String, ?> getConverter(org.apache.avro.Schema.Field f) {
        Schema fieldSchema = AvroUtils.unwrapIfNullable(f.schema());
        if (AvroUtils.isSameType(fieldSchema, AvroUtils._boolean())) {
            return new BooleanConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._decimal())) {
            return new DecimalConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._double())) {
            return new DoubleConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._float())) {
            return new FloatConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._int())) {
            return new IntegerConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._date())) {
            return new DateConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._long())) {
            return new LongConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._bytes())) {
            return new BytesConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._byte())) {
            return new ByteConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._character())) {
            return new CharacterConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._string())) {
            return super.getConverter(String.class);
        }
        throw new UnsupportedOperationException("The type " + fieldSchema.getType() + " is not supported."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static abstract class StringConverter<T> implements AvroConverter<String, T> {

        protected final Schema.Field field;

        StringConverter(Schema.Field field) {
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

    public static abstract class NumberConverter<T> implements AvroConverter<String, T> {

        private final Schema.Field field;

        NumberConverter(Schema.Field field) {
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
            // TODO check nullable?
            if (value == null) {
                return null;
            }
            Character thousandsSepChar = null;
            Character decimalSepChar = null;
            String thousandsSepString = field.getProp(ComponentConstants.THOUSANDS_SEPARATOR);
            String decimalSepString = field.getProp(ComponentConstants.DECIMAL_SEPARATOR);
            if (thousandsSepString != null) {
                thousandsSepChar = thousandsSepString.charAt(0);
            }
            if (decimalSepString != null) {
                decimalSepChar = decimalSepString.charAt(0);
            }
            if (thousandsSepChar != null || decimalSepChar != null) {
                return FormatterUtils.formatNumber(new BigDecimal(String.valueOf(value)).toPlainString(), thousandsSepChar,
                        decimalSepChar);
            } else {
                if (value instanceof BigDecimal) {
                    String precision = field.getProp(SchemaConstants.TALEND_COLUMN_PRECISION);
                    if (precision != null) {
                        return ((BigDecimal) value).setScale(Integer.valueOf(precision), RoundingMode.HALF_UP).toPlainString();
                    } else {
                        return ((BigDecimal) value).toPlainString();
                    }
                } else if (AvroUtils.isSameType(AvroUtils._decimal(), AvroUtils.unwrapIfNullable(field.schema()))) {
                    String precision = field.getProp(SchemaConstants.TALEND_COLUMN_PRECISION);
                    if (precision != null) {
                        return new BigDecimal(String.valueOf(value)).setScale(Integer.valueOf(precision), RoundingMode.HALF_UP)
                                .toPlainString();
                    }
                }
                return String.valueOf(value);
            }
        }

    }

    public static class BooleanConverter extends StringConverter<Boolean> {

        BooleanConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Boolean convertToAvro(String value) {
            if (value == null) {
                return null;
            }
            if (value.equals("1")) {
                return Boolean.parseBoolean("true");
            }
            return Boolean.parseBoolean(value);
        }
    }

    public static class DecimalConverter extends NumberConverter<BigDecimal> {

        DecimalConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public BigDecimal convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : new BigDecimal(value);
        }
    }

    public static class DoubleConverter extends NumberConverter<Double> {

        DoubleConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Double convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : Double.parseDouble(value);
        }
    }

    public static class LongConverter extends NumberConverter<Long> {

        LongConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Long convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : Long.parseLong(value);
        }
    }

    public static class FloatConverter extends NumberConverter<Float> {

        FloatConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Float convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : Float.parseFloat(value);
        }
    }

    public static class DateConverter extends StringConverter<Object> {

        private final SimpleDateFormat format;

        DateConverter(Schema.Field field) {
            super(field);
            String pattern = field.getProp(SchemaConstants.TALEND_COLUMN_PATTERN);
            format = new SimpleDateFormat(pattern);
        }

        @Override
        public Long convertToAvro(String value) {
            try {
                return StringUtils.isEmpty(value) ? null : format.parse(value).getTime();
            } catch (ParseException e) {
                // For die one error and reject, only need throw the exception
                throw new ComponentException(e);
            }
        }

        @Override
        public String convertToDatum(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Date) {
                return format.format((Date) value);
            } else {
                return format.format(new Date((Long) value));
            }
        }

    }

    public static class IntegerConverter extends NumberConverter<Integer> {

        IntegerConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Integer convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : Integer.parseInt(value);
        }
    }

    public static class ByteConverter extends NumberConverter<Byte> {

        ByteConverter(Schema.Field field) {
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

    public static class BytesConverter extends StringConverter<byte[]> {

        BytesConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public byte[] convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : value.getBytes();
        }

        @Override
        public String convertToDatum(byte[] value) {
            return value == null ? null
                    : Charset.forName(field.getProp(ComponentConstants.FILE_ENCODING)).decode(ByteBuffer.wrap(value)).toString();
        }

    }

    public static class CharacterConverter extends StringConverter<Character> {

        CharacterConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Character convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : value.charAt(0);
        }
    }
}
