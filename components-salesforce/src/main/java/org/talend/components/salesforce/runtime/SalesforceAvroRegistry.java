package org.talend.components.salesforce.runtime;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.avro.LogicalTypes;
import org.apache.avro.LogicalTypes.Decimal;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.talend.daikon.avro.AvroConverter;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.util.AvroUtils;
import org.talend.daikon.java8.SerializableFunction;

import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import org.talend.daikon.talend6.Talend6SchemaConstants;

/**
 * 
 */
public class SalesforceAvroRegistry extends AvroRegistry {

    public static final String FAMILY_NAME = "Salesforce";

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
        FieldAssembler<Schema> builder = SchemaBuilder.builder().record(in.getName()).fields();
        for (Field field : in.getFields()) {
            Schema fieldSchema = SalesforceAvroRegistry.get().inferSchema(field);
            String fieldDefault = field.getDefaultValueFormula();
            if (null == fieldDefault) {
                builder = builder.name(field.getName()).type(fieldSchema).noDefault();
            } else {
                builder = builder.name(field.getName()).type(fieldSchema).withDefault(fieldDefault);
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
            base = Schema.create(Schema.Type.BOOLEAN);
            break;
        case _double:
            base = Schema.create(Schema.Type.DOUBLE);
            break;
        case _int:
            base = Schema.create(Schema.Type.INT);
            break;
        case currency:
            // The decimal Avro logical type is wrapped on a BYTES implementation.
            Decimal d = LogicalTypes.decimal(field.getPrecision(), field.getScale());
            base = d.addToSchema(Schema.create(Schema.Type.BYTES));
            break;
        case date:
            base = Schema.create(Schema.Type.LONG);
            base.addProp(Talend6SchemaConstants.TALEND6_PATTERN, "yyyy-MM-dd"); //$NON-NLS-1$
            break;
        case datetime:
            base = Schema.create(Schema.Type.LONG);
            base.addProp(Talend6SchemaConstants.TALEND6_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'.000Z'"); //$NON-NLS-1$
            break;
        default:
            base = Schema.create(Schema.Type.STRING);
            break;
        }

        // Add some Talend6 custom properties to the schema.
        if (base.getType() == Schema.Type.STRING) {
            base.addProp(Talend6SchemaConstants.TALEND6_SIZE, field.getLength());
            base.addProp(Talend6SchemaConstants.TALEND6_PRECISION, field.getPrecision());
        } else {
            base.addProp(Talend6SchemaConstants.TALEND6_SIZE, field.getPrecision());
            base.addProp(Talend6SchemaConstants.TALEND6_PRECISION, field.getScale());
        }
        if (field.getDefaultValueFormula() != null) {
            base.addProp(Talend6SchemaConstants.TALEND6_DEFAULT_VALUE, field.getDefaultValueFormula());
        }

        // Optionally union with Schema.Type.NULL
        return field.getNillable() ? SchemaBuilder.builder().nullable().type(base) : base;
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

        switch (fieldSchema.getType()) {
        case BOOLEAN:
            return new StringToBooleanConverter(fieldSchema);
        case BYTES:
            // The BYTES type returns a BigDecimal for the decimal logical type.
            return new StringToDecimalConverter(fieldSchema);
        case DOUBLE:
            return new StringToDoubleConverter(fieldSchema);
        case INT:
            return new StringToIntegerConverter(fieldSchema);
        case LONG:
            return new StringToDateConverter(fieldSchema);
        case STRING:
            // The AvroRegistry provides a pass-through class for Strings.
            return super.getConverter(String.class);
        case ARRAY:
        case ENUM:
        case FIXED:
        case FLOAT:
        case MAP:
        case RECORD:
        case NULL:
        case UNION:
        default:
            // These types are not generated for Salesforce objects.
        }
        throw new UnsupportedOperationException("The type " + fieldSchema.getType() + " is not supported."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // TODO(rskraba): These are probably useful utility items.

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

    public static class StringToBooleanConverter extends AsStringConverter<Boolean> {

        StringToBooleanConverter(Schema schema) {
            super(schema);
        }

        @Override
        public Boolean convertToAvro(String value) {
            return value == null ? null : Boolean.parseBoolean(value);
        }
    }

    public static class StringToDecimalConverter extends AsStringConverter<BigDecimal> {

        StringToDecimalConverter(Schema schema) {
            super(schema);
        }

        @Override
        public BigDecimal convertToAvro(String value) {
            return value == null ? null : new BigDecimal(value);
        }
    }

    public static class StringToDoubleConverter extends AsStringConverter<Double> {

        StringToDoubleConverter(Schema schema) {
            super(schema);
        }

        @Override
        public Double convertToAvro(String value) {
            return value == null ? null : Double.parseDouble(value);
        }
    }

    public static class StringToDateConverter extends AsStringConverter<Long> {

        private final SimpleDateFormat format;

        StringToDateConverter(Schema schema) {
            super(schema);
            String pattern = schema.getProp(Talend6SchemaConstants.TALEND6_PATTERN);
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

        StringToIntegerConverter(Schema schema) {
            super(schema);
        }

        @Override
        public Integer convertToAvro(String value) {
            return value == null ? null : Integer.parseInt(value);
        }
    }

}
