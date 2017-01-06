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
package org.talend.components.filedelimited.runtime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.common.runtime.FormatterUtils;
import org.talend.components.common.runtime.ParserUtils;
import org.talend.components.filedelimited.FileDelimitedProperties;
import org.talend.components.filedelimited.tfileinputdelimited.TFileInputDelimitedProperties;
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

    public AvroConverter<String, ?> getConverter(Schema.Field f, FileDelimitedProperties properties) {
        Schema fieldSchema = AvroUtils.unwrapIfNullable(f.schema());
        if (AvroUtils.isSameType(fieldSchema, AvroUtils._boolean())) {
            return new BooleanConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._decimal())) {
            return new DecimalConverter(f, properties);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._double())) {
            return new DoubleConverter(f, properties);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._float())) {
            return new FloatConverter(f, properties);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._int())) {
            return new IntegerConverter(f, properties);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._date())) {
            return new DateConverter(f, properties);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._long())) {
            return new LongConverter(f, properties);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._bytes())) {
            return new BytesConverter(f, properties);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._byte())) {
            return new ByteConverter(f, properties);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._short())) {
            return new ShortConverter(f, properties);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._character())) {
            return new CharacterConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._string())) {
            return super.getConverter(String.class);
        }
        throw new UnsupportedOperationException("The type " + fieldSchema.getType() + " is not supported."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static abstract class StringConverter<T> implements AvroConverter<String, T> {

        protected final Schema.Field field;

        protected FileDelimitedProperties properties;

        StringConverter(Schema.Field field) {
            this.field = field;
        }

        StringConverter(Schema.Field field, FileDelimitedProperties properties) {
            this.field = field;
            this.properties = properties;
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

        protected Character thousandsSepChar;

        protected Character decimalSepChar;

        protected final FileDelimitedProperties properties;

        private List<Boolean> decodeList;

        NumberConverter(Schema.Field field, FileDelimitedProperties properties) {
            this.field = field;
            this.properties = properties;
            if (properties.advancedSeparator.getValue()) {
                this.thousandsSepChar = ParserUtils.parseToCharacter(properties.thousandsSeparator.getValue());
                this.decimalSepChar = ParserUtils.parseToCharacter(properties.decimalSeparator.getValue());
            }
            if ((properties instanceof TFileInputDelimitedProperties)
                    && ((TFileInputDelimitedProperties) properties).enableDecode.getValue()) {
                Object values = ((TFileInputDelimitedProperties) properties).decodeTable.decode.getValue();
                if (values != null && values instanceof List) {
                    decodeList = (List<Boolean>) values;
                }
            }
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
            if (value == null) {
                return null;
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

        // Return current field decode flag
        public boolean isDecode() {
            if (decodeList != null && field.pos() < decodeList.size()) {
                return decodeList.get(field.pos());
            }
            return false;
        }

    }

    public static class BooleanConverter extends StringConverter<Boolean> {

        BooleanConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Boolean convertToAvro(String value) {
            return ParserUtils.parseToBoolean(value);
        }
    }

    public static class DecimalConverter extends NumberConverter<String> {

        DecimalConverter(Schema.Field field, FileDelimitedProperties properties) {
            super(field, properties);
        }

        @Override
        public String convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null
                    : new BigDecimal(ParserUtils.transformNumberString(value, thousandsSepChar, decimalSepChar)).toPlainString();
        }
    }

    public static class DoubleConverter extends NumberConverter<Double> {

        DoubleConverter(Schema.Field field, FileDelimitedProperties properties) {
            super(field, properties);
        }

        @Override
        public Double convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null
                    : Double.parseDouble(ParserUtils.transformNumberString(value, thousandsSepChar, decimalSepChar));
        }
    }

    public static class LongConverter extends NumberConverter<Long> {

        LongConverter(Schema.Field field, FileDelimitedProperties properties) {
            super(field, properties);
        }

        @Override
        public Long convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null
                    : ParserUtils.parseToLong(ParserUtils.transformNumberString(value, thousandsSepChar, decimalSepChar),
                            isDecode());
        }
    }

    public static class FloatConverter extends NumberConverter<Float> {

        FloatConverter(Schema.Field field, FileDelimitedProperties properties) {
            super(field, properties);
        }

        @Override
        public Float convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null
                    : Float.parseFloat(ParserUtils.transformNumberString(value, thousandsSepChar, decimalSepChar));
        }
    }

    public static class DateConverter extends StringConverter<Object> {

        String pattern;

        boolean isLenient;

        DateConverter(Schema.Field field, FileDelimitedProperties properties) {
            super(field, properties);
            pattern = field.getProp(SchemaConstants.TALEND_COLUMN_PATTERN);
            if (properties instanceof TFileInputDelimitedProperties) {
                isLenient = ((TFileInputDelimitedProperties) properties).checkDate.getValue();
            }
        }

        @Override
        public Long convertToAvro(String value) {
            Date date = null;
            if (!StringUtils.isEmpty(value)) {
                date = ParserUtils.parseToDate(value, pattern, !isLenient);
            }
            if (date != null) {
                return date.getTime();
            } else {
                return null;
            }
        }

        @Override
        public String convertToDatum(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof Date) {
                return FormatterUtils.formatDate(((Date) value), pattern);
            } else {
                return FormatterUtils.formatDate(new Date((Long) value), pattern);
            }
        }

    }

    public static class IntegerConverter extends NumberConverter<Integer> {

        IntegerConverter(Schema.Field field, FileDelimitedProperties properties) {
            super(field, properties);
        }

        @Override
        public Integer convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null
                    : ParserUtils.parseToInteger(ParserUtils.transformNumberString(value, thousandsSepChar, decimalSepChar),
                            isDecode());
        }
    }

    public static class ShortConverter extends NumberConverter<Integer> {

        ShortConverter(Schema.Field field, FileDelimitedProperties properties) {
            super(field, properties);
        }

        @Override
        public Integer convertToAvro(String value) {
            // String to Integer (datum type), then Integer (datum type to Integer (Avro-compatible types)
            return StringUtils.isEmpty(value) ? null
                    : ParserUtils
                            .parseToShort(ParserUtils.transformNumberString(value, thousandsSepChar, decimalSepChar), isDecode())
                            .intValue();
        }
    }

    public static class ByteConverter extends NumberConverter<Integer> {

        ByteConverter(Schema.Field field, FileDelimitedProperties properties) {
            super(field, properties);
        }

        @Override
        public Integer convertToAvro(String value) {
            // String to Byte (datum type), then Byte (datum type to Integer (Avro-compatible types)
            // Because of migration issue, decode flag is always 'true' for byte type
            return StringUtils.isEmpty(value) ? null
                    : ParserUtils.parseToByte(ParserUtils.transformNumberString(value, thousandsSepChar, decimalSepChar), true)
                            .intValue();
        }
    }

    public static class BytesConverter extends StringConverter<byte[]> {

        BytesConverter(Schema.Field field, FileDelimitedProperties properties) {
            super(field, properties);
        }

        @Override
        public byte[] convertToAvro(String value) {
            return StringUtils.isEmpty(value) ? null : value.getBytes();
        }

        @Override
        public String convertToDatum(byte[] value) {
            // byte[] to String with specified encoding
            return value == null ? null
                    : Charset.forName(properties.encoding.getEncoding()).decode(ByteBuffer.wrap(value)).toString();
        }

    }

    public static class CharacterConverter extends StringConverter<String> {

        CharacterConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public String convertToAvro(String value) {
            // String to Character (datum type), then Character (datum type to String (Avro-compatible types)
            return StringUtils.isEmpty(value) ? null : ParserUtils.parseToCharacter(value).toString();
        }
    }
}
