package org.talend.components.salesforce.runtime;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.java8.SerializableFunction;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;

/**
 *
 */
public class SalesforceAvroRegistry extends AvroRegistry {

    public static final String FAMILY_NAME = "Salesforce"; //$NON-NLS-1$

    /** When inferring a schema from a query, store the String identifier of the query. */
    public static final String PROP_QUERY_RESULT = FAMILY_NAME.toLowerCase() + ".queryResult"; //$NON-NLS-1$

    /** Record name for a schema inferred from a query. */
    public static final String QUERY_RESULT_RECORD_NAME = "QueryResultRecord"; //$NON-NLS-1$

    private static final SalesforceAvroRegistry sInstance = new SalesforceAvroRegistry();

    /**
     * Hidden constructor: use the singleton.
     */
    private SalesforceAvroRegistry() {

        // Ensure that we know how to get Schemas for these Salesforce objects.
        registerSchemaInferrer(DescribeSObjectResult.class, new SerializableFunction<DescribeSObjectResult, Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(DescribeSObjectResult t) {
                return inferSchemaDescribeSObjectResult(t);
            }

        });

        registerSchemaInferrer(Field.class, new SerializableFunction<Field, Schema>() {

            /** Default serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Schema apply(Field t) {
                return inferSchemaField(t);
            }

        });
    }

    public static SalesforceAvroRegistry get() {
        return sInstance;
    }

    /**
     * @return The family that uses the specific objects that this converter knows how to translate.
     */
    public String getFamilyName() {
        return FAMILY_NAME;
    }

    /**
     * Infers an Avro schema for the given DescribeSObjectResult. This can be an expensive operation so the schema
     * should be cached where possible. This is always an {@link Schema.Type#RECORD}.
     *
     * @param in the DescribeSObjectResult to analyse.
     * @return the schema for data given from the object.
     */
    private Schema inferSchemaDescribeSObjectResult(DescribeSObjectResult in) {
        List<Schema.Field> fields = new ArrayList<>();
        for (Field field : in.getFields()) {

            Schema.Field avroField = new Schema.Field(field.getName(), inferSchema(field), null, field.getDefaultValueFormula());
            // Add some Talend6 custom properties to the schema.
            Schema avroFieldSchema = avroField.schema();
            if (avroFieldSchema.getType() == Schema.Type.UNION) {
                for (Schema schema : avroFieldSchema.getTypes()) {
                    if (avroFieldSchema.getType() != Schema.Type.NULL) {
                        avroFieldSchema = schema;
                        break;
                    }
                }
            }
            if (AvroUtils.isSameType(avroFieldSchema, AvroUtils._string())) {
                if (field.getLength() != 0) {
                    avroField.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, String.valueOf(field.getLength()));
                }
                if (field.getPrecision() != 0) {
                    avroField.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, String.valueOf(field.getPrecision()));
                }
            } else {
                if (field.getPrecision() != 0) {
                    avroField.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, String.valueOf(field.getPrecision()));
                }
                if (field.getScale() != 0) {
                    avroField.addProp(SchemaConstants.TALEND_COLUMN_PRECISION, String.valueOf(field.getScale()));
                }
            }

            if (field.getReferenceTo() != null && field.getReferenceTo().length > 0 && field.getRelationshipName() != null) {
                avroField.addProp(SalesforceSchemaConstants.REF_MODULE_NAME, field.getReferenceTo()[0]);
                avroField.addProp(SalesforceSchemaConstants.REF_FIELD_NAME, field.getRelationshipName());
            }

            // pattern will be removed when we have db type for salesforce
            switch (field.getType()) {
            case date:
                avroField.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd");
                break;
            case datetime:
                avroField.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'.000Z'");
                break;
            case time:
                avroField.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, "HH:mm:ss.SSS'Z'");
                break;
            default:
                break;
            }
            if (avroField.defaultVal() != null) {
                // FIXME really needed as Schema.Field has ability to store default value
                avroField.addProp(SchemaConstants.TALEND_COLUMN_DEFAULT, String.valueOf(avroField.defaultVal()));
            }
            fields.add(avroField);
        }
        return Schema.createRecord(in.getName(), null, null, false, fields);
    }

    /**
     * Infers an Avro schema for the given Salesforce Field. This can be an expensive operation so the schema should be
     * cached where possible. The return type will be the Avro Schema that can contain the field data without loss of
     * precision.
     *
     * @param field the Field to analyse.
     * @return the schema for data that the field describes.
     */
    private Schema inferSchemaField(Field field) {
        // Logic taken from:
        // https://github.com/Talend/components/blob/aef0513e0ba6f53262b89ef2ea8a981cd1430d47/components-salesforce/src/main/java/org/talend/components/salesforce/runtime/SalesforceSourceOrSink.java#L214

        // Field type information at:
        // https://developer.salesforce.com/docs/atlas.en-us.200.0.object_reference.meta/object_reference/primitive_data_types.htm

        // Note: default values are at the field level, not attached to the field.
        // However, these properties are saved in the schema with Talend6SchemaConstants if present.
        Schema base;
        switch (field.getType()) {
        case _boolean:
            base = AvroUtils._boolean();
            break;
        case _double:
            base = AvroUtils._double();
            break;
        case percent:
            base = AvroUtils._double();
            break;
        case _int:
            base = AvroUtils._int();
            break;
        case currency:
            base = AvroUtils._decimal();
            break;
        case date:
            base = AvroUtils._date();
            break;
        case datetime:
            base = AvroUtils._date();
            break;
        case base64:
            base = AvroUtils._bytes();
            break;
        default:
            base = AvroUtils._string();
            break;
        }
        base = field.getNillable() ? AvroUtils.wrapAsNullable(base) : base;

        return base;
    }

    /**
     * A helper method to convert the String representation of a datum in the Salesforce system to the Avro type that
     * matches the Schema generated for it.
     *
     * @param f
     * @return
     */
    public AvroConverter<String, ?> getConverterFromString(org.apache.avro.Schema.Field f) {
        Schema fieldSchema = AvroUtils.unwrapIfNullable(f.schema());
        // FIXME use avro type to decide the converter is not correct if the user change the avro type, Date to String
        // for instance
        if (AvroUtils.isSameType(fieldSchema, AvroUtils._boolean())) {
            return new StringToBooleanConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._decimal())) {
            return new StringToDecimalConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._double())) {
            return new StringToDoubleConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._int())) {
            return new StringToIntegerConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._date())) {
            return new StringToDateConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._bytes())) {
            return new StringToBytesConverter(f);
        } else if (AvroUtils.isSameType(fieldSchema, AvroUtils._string())) {
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
            return StringUtils.isEmpty(value) ? null : Boolean.parseBoolean(value);
        }
    }

    public static class StringToBytesConverter extends AsStringConverter<byte[]> {

        StringToBytesConverter(Schema.Field field) {
            super(field);
        }

        @Override
        public byte[] convertToAvro(String value) {
            return value == null ? null : value.getBytes();
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
                return StringUtils.isEmpty(value) ? null : format.parse(value).getTime();
            } catch (ParseException e) {
                throw new ComponentException(e);
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

}
