package org.talend.components.processing.runtime.typeconverter;

import java.util.Stack;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.processing.definition.typeconverter.TypeConverterProperties;
import org.talend.daikon.java8.SerializableFunction;
import org.talend.daikon.properties.ValidationResult;

public class TypeConverterFunction
        implements SerializableFunction<IndexedRecord, IndexedRecord>, RuntimableRuntime<TypeConverterProperties> {

    private TypeConverterProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, TypeConverterProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public IndexedRecord apply(IndexedRecord inputRecord) {
        Schema inputSchema = inputRecord.getSchema();

        // Compute new schema
        Schema outputSchema = inputSchema;

        for (TypeConverterProperties.TypeConverterPropertiesInner currentPathConverter : properties.converters.subProperties) {
            if (currentPathConverter.field.getValue() != null && !currentPathConverter.field.getValue().isEmpty()
                    && currentPathConverter.outputType.getValue() != null) {
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
            if (currentValueConverter.field.getValue() != null && !currentValueConverter.field.getValue().isEmpty()
                    && currentValueConverter.outputType.getValue() != null) {
                Stack<String> pathSteps = TypeConverterUtils.getPathSteps(currentValueConverter.field.getValue());
                TypeConverterUtils.convertValue(inputSchema, outputRecordBuilder, pathSteps,
                        TypeConverterProperties.TypeConverterOutputTypes.valueOf(currentValueConverter.outputType.getValue()),
                        currentValueConverter.outputFormat.getValue());
            }
        }

        return outputRecordBuilder.build();
    }
}
