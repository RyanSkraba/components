package org.talend.components.processing.runtime.typeconverter;

import avro.shaded.com.google.common.collect.Lists;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.processing.definition.typeconverter.TypeConverterProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.converter.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 *
 */
public class TypeConverterUtils {

    public static Schema getUnwrappedSchema(Schema.Field field) {
        return AvroUtils.unwrapIfNullable(field.schema());
    }

    /**
     * Transform input schema to a new schema.
     * <p>
     * The schema of the array field `pathToConvert` will be modified to the schema of its fields.
     */
    public static Schema convertSchema(Schema inputSchema, Stack<String> converterPath, TypeConverterProperties.TypeConverterOutputTypes outputType, String outputFormat) {
        List<Schema.Field> fieldList = new ArrayList<>();
        String currentStep = converterPath.pop();
        for (Schema.Field field : inputSchema.getFields()) {
            Schema unwrappedSchema = getUnwrappedSchema(field);
            if (field.name().equals(currentStep)) {
                // We are on the path to be converted
                if (converterPath.size() == 0) {
                    // We are on the exact element to convert
                    fieldList.add(new Schema.Field(field.name(), TypeConverterUtils.getSchema(outputType, outputFormat), field.doc(), field.defaultVal()));
                } else {
                    // Going down in the hierarchy
                    fieldList.add(new Schema.Field(field.name(), TypeConverterUtils.convertSchema(unwrappedSchema, converterPath, outputType, outputFormat), field.doc(), field.defaultVal()));
                }
            } else {
                // We are not on the path to convert, just recopying schema
                fieldList.add(new Schema.Field(field.name(), field.schema(), field.doc(), field.defaultVal()));
            }
        }
        return Schema.createRecord(inputSchema.getName(), inputSchema.getDoc(), inputSchema.getNamespace(), inputSchema.isError(),
                fieldList);

    }

    /**
     * Copy fields value from inputRecord to outputRecordBuilder
     *
     * @param inputRecord
     * @param outputRecordBuilder
     */
    public static void copyFieldsValues(IndexedRecord inputRecord, GenericRecordBuilder outputRecordBuilder) {
        List<Schema.Field> fields = inputRecord.getSchema().getFields();
        for (Schema.Field field : fields) {
            outputRecordBuilder.set(field.name(), inputRecord.get(field.pos()));
        }
    }

    /**
     * Convert value of outputRecordBuilder according to pathSteps, outputType and outputFormat
     *
     * @param outputRecordBuilder
     * @param pathSteps
     * @param outputType
     * @param inputFormat
     */
    public static void convertValue(GenericRecordBuilder outputRecordBuilder, Stack<String> pathSteps, TypeConverterProperties.TypeConverterOutputTypes outputType, String inputFormat) {
        String fieldName = pathSteps.pop();
        Object value = outputRecordBuilder.get(fieldName);
        if (pathSteps.size() == 0) {
            Converter converter = outputType.getConverter();
            // Configure converters if needed
            if (TypeConverterProperties.TypeConverterOutputTypes.Date.equals(outputType) && inputFormat != null && !inputFormat.isEmpty()) {
                ((LocalDateConverter) converter).withDateTimeFormatter(DateTimeFormatter.ofPattern(inputFormat));
            } else if (TypeConverterProperties.TypeConverterOutputTypes.Time.equals(outputType) && inputFormat != null && !inputFormat.isEmpty()) {
                ((LocalTimeConverter) converter).withDateTimeFormatter(DateTimeFormatter.ofPattern(inputFormat));
            } else if (TypeConverterProperties.TypeConverterOutputTypes.DateTime.equals(outputType) && inputFormat != null && !inputFormat.isEmpty()) {
                ((LocalDateTimeConverter) converter).withDateTimeFormatter(DateTimeFormatter.ofPattern(inputFormat));
            } else if (TypeConverterProperties.TypeConverterOutputTypes.Decimal.equals(outputType) && inputFormat != null && !inputFormat.isEmpty()) {
                DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                symbols.setDecimalSeparator('.');
                symbols.setGroupingSeparator(',');
                DecimalFormat decimalFormat = new DecimalFormat(inputFormat, symbols);
                decimalFormat.setParseBigDecimal(true);
                ((BigDecimalConverter) converter).withDecimalFormat(decimalFormat);
            }

            Object convertedValue = converter.convert(value);
            // Convert to underlying types for Avro logical types
            convertedValue = TypeConverterUtils.convertToAvroPrimitiveType(outputType, convertedValue);

            outputRecordBuilder.set(fieldName, convertedValue);
        } else {
            TypeConverterUtils.convertValue((GenericRecordBuilder) value, pathSteps, outputType, inputFormat);
        }
    }

    private static Object convertToAvroPrimitiveType(TypeConverterProperties.TypeConverterOutputTypes outputType, Object convertedValue) {
        Object result = convertedValue;
        switch (outputType) {
            case Date:
                result = ((LocalDate) convertedValue).toEpochDay();
                break;
            case Time:
                result = ((LocalTime) convertedValue).toNanoOfDay() / 1000000L;
                break;
            case DateTime:
                result = ((LocalDateTime) convertedValue).toEpochSecond(ZoneOffset.ofTotalSeconds(0));
                break;
        }
        return result;
    }

    /**
     * Get the step from the hierachical string path
     *
     * @param path
     * @return
     */
    public static Stack<String> getPathSteps(String path) {
        if (path.startsWith(".")) {
            path = path.substring(1);
        }
        Stack<String> pathSteps = new Stack<String>();
        List<String> stepsList = Arrays.asList(path.split("\\."));
        pathSteps.addAll(Lists.reverse(stepsList));
        return pathSteps;
    }

    /**
     * Generate a schema from output type and format
     *
     * @param outputType
     * @param outputFormat
     * @return
     */
    public static Schema getSchema(TypeConverterProperties.TypeConverterOutputTypes outputType, String outputFormat) {
        Schema result = Schema.create(outputType.getTargetType());
        switch (outputType) {
            case Decimal:
                result = LogicalTypes.decimal(20, 4).addToSchema(result);
                break;
            case Date:
                result = LogicalTypes.date().addToSchema(result);
                break;
            case Time:
                result = LogicalTypes.timeMillis().addToSchema(result);
                break;
            case DateTime:
                result = LogicalTypes.timestampMillis().addToSchema(result);
                break;
        }
        return result;
    }

}
