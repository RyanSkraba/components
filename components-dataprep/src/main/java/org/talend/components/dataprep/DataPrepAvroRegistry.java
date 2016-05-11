package org.talend.components.dataprep;

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroConverter;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.util.AvroTypes;
import org.talend.daikon.avro.util.AvroUtils;
import org.talend.daikon.java8.SerializableFunction;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataPrepAvroRegistry extends AvroRegistry {

    public static final String FAMILY_NAME = "Data Preparation";
    private static DataPrepAvroRegistry dataPrepInstance;

    private DataPrepAvroRegistry() {

        // Ensure that we know how to get Schemas for these DataPrep objects.
        registerSchemaInferrer(DataPrepField[].class, new SerializableFunction<DataPrepField[], Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(DataPrepField[] t) {
                return inferSchemaDataPrepResult(t);
            }

        });

        registerSchemaInferrer(DataPrepField.class, new SerializableFunction<DataPrepField, Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(DataPrepField t) {
                return inferSchemaField(t);
            }

        });
    }

    public static DataPrepAvroRegistry getDataPrepInstance() {
        if (dataPrepInstance == null)
            dataPrepInstance = new DataPrepAvroRegistry();
        return dataPrepInstance;
    }

    /**
     * @return The family that uses the specific objects that this converter knows how to translate.
     */
    public String getFamilyName() {
        return FAMILY_NAME;
    }

    /**
     * Infers an Avro schema for the given DataPrep row. This can be an expensive operation so the schema
     * should be cached where possible. This is always an {@link Schema.Type#RECORD}.
     *
     * @param in the DescribeSObjectResult to analyse.
     * @return the schema for data given from the object.
     */
    private Schema inferSchemaDataPrepResult(DataPrepField[] in) {
        List<Schema.Field> fields = new ArrayList<>();
        for (DataPrepField field : in) {

            Schema.Field avroField = new Schema.Field(field.getColumnName(), inferSchema(field), null, field.getContent());

            switch (field.getType()) {
                case "date":
                    avroField.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd");
                    break;
                //TODO add right handling Date type
                default:
                    break;
            }
            fields.add(avroField);
        }
        return Schema.createRecord("Null", null, null, false, fields);
    }

    /**
     * Infers an Avro schema for the given DataPrep Field. This can be an expensive operation so the schema should be
     * cached where possible. The return type will be the Avro Schema that can contain the field data without loss of
     * precision.
     *
     * @param field the Field to analyse.
     * @return the schema for data that the field describes.
     */
    private Schema inferSchemaField(DataPrepField field) {
        Schema base;
        switch (field.getType()) {
            case "boolean":
                base = AvroTypes._boolean();
                break;
            case "double":
                base = AvroTypes._double();
                break;
            case "integer":
                base = AvroTypes._int();
                break;
            case "float":
                base = AvroTypes._float();
                break;
            default:
                base = AvroTypes._string();
                break;
        }

        //TODO add handling for numeric, any and date.

        return base;
    }

    /**
     * A helper method to convert the String representation of a datum in the DataPrep system to the Avro type that
     * matches the Schema generated for it.
     *
     * @param f is field in Avro Schema.
     * @return converter for a given type.
     */
    public AvroConverter<String, ?> getConverterFromString(org.apache.avro.Schema.Field f) {
        Schema fieldSchema = AvroUtils.unwrapIfNullable(f.schema());
        // FIXME use avro type to decide the converter is not correct if the user change the avro type, Date to String
        // for instance
        if (AvroTypes.isSameType(fieldSchema, AvroTypes._boolean())) {
            return new StringToBooleanConverter(f);
        } else if (AvroTypes.isSameType(fieldSchema, AvroTypes._decimal())) {
            return new StringToDecimalConverter(f);
        } else if (AvroTypes.isSameType(fieldSchema, AvroTypes._double())) {
            return new StringToDoubleConverter(f);
        } else if (AvroTypes.isSameType(fieldSchema, AvroTypes._int())) {
            return new StringToIntegerConverter(f);
        } else if (AvroTypes.isSameType(fieldSchema, AvroTypes._date())) {
            return new StringToDateConverter(f);
        } else if (AvroTypes.isSameType(fieldSchema, AvroTypes._string())) {
            return super.getConverter(String.class);
        }
        throw new UnsupportedOperationException("The type " + fieldSchema.getType() + " is not supported."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // TODO(rskraba): These are probably useful utility items.

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
            return value == null ? null : new BigDecimal(value);
        }
    }

    public static class StringToDoubleConverter extends AsStringConverter<Double> {

        StringToDoubleConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public Double convertToAvro(String value) {
            return value == null ? null : Double.parseDouble(value);
        }
    }

    public static class StringToDateConverter extends AsStringConverter<Long> {

        private final SimpleDateFormat format;

        StringToDateConverter(Schema.Field field) {
            super(field);
            String pattern = field.getProp(SchemaConstants.TALEND_COLUMN_PATTERN);
            // TODO: null handling
            format = new SimpleDateFormat(pattern);
        }

        @Override
        public Long convertToAvro(String value) {
            try {
                return value == null ? null : format.parse(value).getTime();
            } catch (ParseException e) {
                // TODO: error handling
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
            return value == null ? null : Integer.parseInt(value);
        }
    }

}