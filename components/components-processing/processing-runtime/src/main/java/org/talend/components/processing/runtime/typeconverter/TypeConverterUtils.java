package org.talend.components.processing.runtime.typeconverter;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.processing.definition.typeconverter.TypeConverterProperties.TypeConverterOutputTypes;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.converter.Converter;
import org.talend.daikon.converter.WithFormatConverter;

import avro.shaded.com.google.common.collect.Lists;

/**
 *
 */
public class TypeConverterUtils {

    /**
     * Transform input schema to a new schema.
     * <p>
     * The schema of the array field `pathToConvert` will be modified to the schema of its fields.
     */
    public static Schema convertSchema(Schema inputSchema, Stack<String> converterPath, TypeConverterOutputTypes outputType,
            String outputFormat) {
        List<Schema.Field> fieldList = new ArrayList<>();
        String currentStep = converterPath.pop();
        for (Schema.Field field : inputSchema.getFields()) {
            Schema unwrappedSchema = AvroUtils.unwrapIfNullable(field.schema());
            if (field.name().equals(currentStep)) {
                // We are on the path to be converted
                if (converterPath.size() == 0) {
                    // We are on the exact element to convert
                    Schema fieldSchema = TypeConverterUtils.getSchema(outputType, outputFormat);
                    // Ensure the output is nullable if the input is nullable.
                    if (AvroUtils.isNullable(field.schema()))
                        fieldSchema = AvroUtils.wrapAsNullable(fieldSchema);
                    fieldList.add(new Schema.Field(field.name(), fieldSchema, field.doc(), field.defaultVal()));
                } else {
                    // Going down in the hierarchy
                    fieldList.add(new Schema.Field(field.name(),
                            TypeConverterUtils.convertSchema(unwrappedSchema, converterPath, outputType, outputFormat),
                            field.doc(), field.defaultVal()));
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
    public static void convertValue(Schema inputSchema, GenericRecordBuilder outputRecordBuilder, Stack<String> pathSteps,
            TypeConverterOutputTypes outputType, String inputFormat) {
        String fieldName = pathSteps.pop();
        Object value = outputRecordBuilder.get(fieldName);
        if (pathSteps.size() == 0) {
            Converter converter = outputType.getConverter();

            // Some special processing is required if the source is a date-oriented type.
            TypeConverterOutputTypes srcDateTimeType = DateAndTimesConverter
                    .getDateTimeTypeFromLogicalType(inputSchema.getField(fieldName).schema());

            // Add the format if necessary. Formats are only used converting to and from Strings.
            boolean srcIsString = value instanceof CharSequence;
            boolean dstIsString = outputType == TypeConverterOutputTypes.String;
            if (dstIsString || srcIsString) {
                if (converter instanceof WithFormatConverter<?, ?>) {
                    WithFormatConverter<?, ?> formatConverter = (WithFormatConverter<?, ?>) converter;
                    switch (outputType) {
                    case Boolean:
                        // No pattern applies to boolean
                        break;
                    case Integer:
                    case Float:
                    case Double:
                    case Long:
                        // Converting to a number always uses the inputFormat as a decimal format.
                        if (inputFormat != null && !inputFormat.isEmpty()) {
                            formatConverter.withNumberFormatter(new DecimalFormat(inputFormat));
                        }
                        break;
                    case String:
                        // Converting to a String uses the expected date format if the source is a Date-oriented LogicalType,
                        // otherwise a number format
                        if (inputFormat != null && !inputFormat.isEmpty() && srcDateTimeType != null) {
                            switch (srcDateTimeType) {
                            case Date:
                                formatConverter.withDateFormatter(createDateTimeFormatter(inputFormat));
                                break;
                            case Time:
                                formatConverter.withTimeMillisFormatter(createDateTimeFormatter(inputFormat));
                                break;
                            case DateTime:
                                formatConverter.withTimestampMillisFormatter(createDateTimeFormatter(inputFormat));
                                break;
                            }
                        } else if (srcDateTimeType != null) {
                            switch (srcDateTimeType) {
                            case Date:
                                formatConverter.withDateFormatter(DateTimeFormatter.ISO_LOCAL_DATE);
                                break;
                            case Time:
                                formatConverter.withTimeMillisFormatter(DateTimeFormatter.ISO_LOCAL_TIME);
                                break;
                            case DateTime:
                                formatConverter.withTimestampMillisFormatter(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                                break;
                            }
                        } else if (inputFormat != null && !inputFormat.isEmpty()) {
                            formatConverter.withNumberFormatter(new DecimalFormat(inputFormat));
                        }
                        break;
                    case Date:
                        if (inputFormat != null && !inputFormat.isEmpty()) {
                            formatConverter.withDateFormatter(createDateTimeFormatter(inputFormat));
                        } else {
                            formatConverter.withDateFormatter(DateTimeFormatter.ISO_LOCAL_DATE);
                        }
                        break;
                    case Time:
                        if (inputFormat != null && !inputFormat.isEmpty()) {
                            formatConverter.withTimeMillisFormatter(createDateTimeFormatter(inputFormat));
                        } else {
                            formatConverter.withTimeMillisFormatter(DateTimeFormatter.ISO_LOCAL_TIME);
                        }
                        break;
                    case DateTime:
                        if (inputFormat != null && !inputFormat.isEmpty()) {
                            formatConverter.withTimestampMillisFormatter(createDateTimeFormatter(inputFormat));
                        } else {
                            formatConverter.withTimestampMillisFormatter(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                        break;
                    }
                }
            } else if (srcDateTimeType != null && (outputType == TypeConverterOutputTypes.Date
                    || outputType == TypeConverterOutputTypes.Time || outputType == TypeConverterOutputTypes.DateTime)) {
                // A custom conversion when the source and destination types are both numbers representing dates.
                converter = new DateAndTimesConverter(srcDateTimeType, outputType);
            }
            Object convertedValue = converter.convert(value);

            outputRecordBuilder.set(fieldName, convertedValue);
        } else {
            TypeConverterUtils.convertValue(inputSchema.getField(fieldName).schema(), (GenericRecordBuilder) value, pathSteps,
                    outputType, inputFormat);
        }
    }

    /**
     * @param format the {@link DateTimeFormatter} pattern to apply.
     * @return a formatter using the given pattern, but assuming the beginning of the unix epoch for all chrono fields that are
     * missing.
     */
    private static DateTimeFormatter createDateTimeFormatter(String format) {
        return new DateTimeFormatterBuilder().appendPattern(format).parseDefaulting(ChronoField.YEAR_OF_ERA, 1970)
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1).parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
                .parseDefaulting(ChronoField.OFFSET_SECONDS, 0).toFormatter();
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
    public static Schema getSchema(TypeConverterOutputTypes outputType, String outputFormat) {
        Schema result = Schema.create(outputType.getTargetType());
        switch (outputType) {
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

    /**
     * This converter is only used for Date/Time/DateTime types on source and destination, and logically converts
     * between them.
     */
    private static class DateAndTimesConverter extends Converter<Number> {

        private static final long MS_IN_A_DAY = 24 * 60 * 60 * 1000;

        private final TypeConverterOutputTypes srcType;

        private final TypeConverterOutputTypes dstType;

        DateAndTimesConverter(TypeConverterOutputTypes srcType, TypeConverterOutputTypes dstType) {
            this.srcType = srcType;
            this.dstType = dstType;
        }

        /**
         * @param srcSchema THe schema to investigate.
         * @return The Date/Time output type that corresponds to the logical type of the schema, or null if none.
         */
        public static TypeConverterOutputTypes getDateTimeTypeFromLogicalType(Schema srcSchema) {
            LogicalType srcLogicalType = AvroUtils.unwrapIfNullable(srcSchema).getLogicalType();
            if (srcLogicalType instanceof LogicalTypes.Date) {
                return TypeConverterOutputTypes.Date;
            } else if (srcLogicalType instanceof LogicalTypes.TimeMillis) {
                return TypeConverterOutputTypes.Time;
            } else if (srcLogicalType instanceof LogicalTypes.TimestampMillis) {
                return TypeConverterOutputTypes.DateTime;
            } else {
                return null;
            }
        }

        @Override
        public Number convert(Object value) {
            // By default, don't perform any conversion.
            Number srcValue = (Number) value;
            Number dstValue = srcValue;
            switch (dstType) {
            case Date:
                if (srcType == TypeConverterOutputTypes.DateTime)
                    // Remove the time component if converting from DateTime to Date.
                    dstValue = (int) (srcValue.longValue() / MS_IN_A_DAY);
                else if (srcType == TypeConverterOutputTypes.Time)
                    // There is no date component if the source is Time.
                    dstValue = 0;
                break;
            case Time:
                if (srcType == TypeConverterOutputTypes.DateTime)
                    // Remove the date component if converting from DateTime to Time.
                    dstValue = (int) (srcValue.longValue() % MS_IN_A_DAY);
                else if (srcType == TypeConverterOutputTypes.Date)
                    // There is no date component if the source is Date.
                    dstValue = 0;
                break;
            case DateTime:
                if (srcType == TypeConverterOutputTypes.Date)
                    // Add a time component if the source is Date.
                    dstValue = srcValue.longValue() * MS_IN_A_DAY;
                else if (srcType == TypeConverterOutputTypes.Time)
                    // Just convert to long if the source is Time (the Date will be 1970-01-01).
                    dstValue = srcValue.longValue();
                break;
            }
            return dstValue;
        }

    }

}
