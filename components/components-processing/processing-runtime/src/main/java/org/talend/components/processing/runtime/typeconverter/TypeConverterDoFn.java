package org.talend.components.processing.runtime.typeconverter;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.DoFn;
import org.talend.components.processing.definition.typeconverter.TypeConverterProperties;

import java.util.Stack;

public class TypeConverterDoFn extends DoFn<IndexedRecord, IndexedRecord> {

    private TypeConverterProperties properties;

    @ProcessElement
    public void processElement(ProcessContext context) {

        IndexedRecord inputRecord = context.element();
        Schema inputSchema = inputRecord.getSchema();

        // Compute new schema
        Schema outputSchema = inputSchema;

        for (TypeConverterProperties.TypeConverterPropertiesInner currentPathConverter : properties.converters.subProperties) {
            if (currentPathConverter.field != null && currentPathConverter.field.getValue() != null && !currentPathConverter.field.getValue().isEmpty() && currentPathConverter.outputType != null && currentPathConverter.outputType.getValue() != null) {
                Stack<String> pathSteps = TypeConverterUtils.getPathSteps(currentPathConverter.field.getValue());
                outputSchema = TypeConverterUtils.convertSchema(outputSchema, pathSteps,
                        TypeConverterProperties.TypeConverterOutputTypes.valueOf(currentPathConverter.outputType.getValue()),
                        currentPathConverter.outputFormat.getValue());
            }
        }

        // Compute new fields
        final GenericRecordBuilder outputRecordBuilder = new GenericRecordBuilder(outputSchema);
        // Copy original values
        TypeConverterUtils.copyFieldsValues(inputRecord, outputRecordBuilder);
        // Convert values
        for (TypeConverterProperties.TypeConverterPropertiesInner currentValueConverter : properties.converters.subProperties) {
            // Loop on converters
            if (currentValueConverter.field != null && currentValueConverter.field.getValue() != null && !currentValueConverter.field.getValue().isEmpty() && currentValueConverter.outputType != null && currentValueConverter.outputType.getValue() != null) {
                Stack<String> pathSteps = TypeConverterUtils.getPathSteps(currentValueConverter.field.getValue());
                TypeConverterUtils.convertValue(outputRecordBuilder, pathSteps,
                        TypeConverterProperties.TypeConverterOutputTypes.valueOf(currentValueConverter.outputType.getValue()),
                        currentValueConverter.outputFormat.getValue());
            }
        }

        context.output(outputRecordBuilder.build());
    }

    public TypeConverterDoFn withProperties(TypeConverterProperties properties) {
        this.properties = properties;
        return this;
    }
}
