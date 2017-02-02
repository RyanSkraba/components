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
package org.talend.components.processing.runtime.filterrow;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.processing.filterrow.ConditionsRowConstant;
import org.talend.components.processing.filterrow.FilterRowProperties;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;

public class FilterRowDoFn extends DoFn<Object, IndexedRecord> {

    private FilterRowProperties properties = null;

    private Boolean hasOutputSchema = false;

    private Boolean hasRejectSchema = false;

    private IndexedRecordConverter converter = null;

    private transient Schema rejectSchema = null;

    @Setup
    public void setup() throws Exception {
    }

    @ProcessElement
    public void processElement(ProcessContext context) {
        if (converter == null) {
            AvroRegistry registry = new AvroRegistry();
            converter = registry.createIndexedRecordConverter(context.element().getClass());
        }
        IndexedRecord inputRecord = (IndexedRecord) converter.convertToAvro(context.element());

        if (rejectSchema == null) {
            rejectSchema = AvroUtils.createRejectSchema(inputRecord.getSchema(), "rejectOutput");
        }

        boolean returnedBooleanValue = true;
        String columnName = properties.columnName.getValue();

        // If there is no defined input, we filter nothing
        if (!StringUtils.isEmpty(columnName)) {
            List<Object> inputValues = getInputFields(inputRecord, columnName);
            if (inputValues.size() == 0) {
                // no valid field: reject the input
                returnedBooleanValue = false;
            }

            // TODO handle null with multiples values
            for (Object inputValue : inputValues) {
                returnedBooleanValue = returnedBooleanValue && checkCondition(inputValue, properties);
            }
        }

        if (returnedBooleanValue) {
            if (hasOutputSchema) {
                context.output(inputRecord);
            }
        } else {
            if (hasRejectSchema) {
                GenericRecordBuilder rejectRecord = new GenericRecordBuilder(rejectSchema);
                rejectRecord.set(AvroUtils.REJECT_FIELD_INPUT, inputRecord);
                // TODO define what we want into the error message
                rejectRecord.set(AvroUtils.REJECT_FIELD_ERROR_MESSAGE, "error message");
                context.sideOutput(FilterRowRuntime.rejectOutput, rejectRecord.build());
            }
        }
    }

    private <T extends Comparable<T>> Boolean checkCondition(Object inputValue, FilterRowProperties condition) {
        String function = condition.function.getValue();
        String conditionOperator = condition.operator.getValue();
        String referenceValue = condition.value.getValue();

        // Apply the transformation function on the input value
        inputValue = FilterRowUtils.applyFunction(inputValue, function);

        if (referenceValue != null) {
            // TODO: do not cast the reference value at each comparison
            Class<T> inputValueClass = TypeConverterUtils.getComparableClass(inputValue);
            if (inputValueClass != null) {
                T convertedReferenceValue = TypeConverterUtils.parseTo(referenceValue, inputValueClass);
                return FilterRowUtils.compare(inputValueClass.cast(inputValue), conditionOperator, convertedReferenceValue);
            } else {
                return FilterRowUtils.compare(inputValue.toString(), conditionOperator, referenceValue.toString());
            }
        } else {
            if (ConditionsRowConstant.Operator.EQUAL.equals(conditionOperator)) {
                return inputValue == null;
            } else { // Not Equals
                return inputValue != null;
            }
        }
    }

    private List<Object> getInputFields(IndexedRecord inputRecord, String columnName) {
        // TODO current implementation will only extract one element, but
        // further implementation may
        ArrayList<Object> inputFields = new ArrayList<Object>();
        String[] path = columnName.split("\\.");
        Schema schema = inputRecord.getSchema();

        for (Integer i = 0; i < path.length; i++) {
            // The column was existing on the input record, we forward it to the
            // output record.
            if (schema.getField(path[i]) == null) {
                throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_ARGUMENT, new Throwable(String.format("The field %s is not present on the input record", columnName)));
            }
            Object inputValue = inputRecord.get(schema.getField(path[i]).pos());

            // The current column can be a Record (an hierarchical sub-object)
            // or directly a value.
            if (inputValue instanceof Record) {
                // If we are on a record, we need to recursively do the process
                inputRecord = (IndexedRecord) inputValue;

                // The sub-schema at this level is a union of "empty" and a
                // record, so we need to get the true
                // sub-schema
                for (Schema childSchema : schema.getField(path[i]).schema().getTypes()) {
                    if (childSchema.getType().equals(Type.RECORD)) {
                        schema = childSchema;
                        break;
                    }
                }
            } else {
                // if we are on a object, then this is or the expected value of
                // an error.
                if (i == path.length - 1) {
                    inputFields.add(inputValue);
                } else {
                    // No need to go further, return an empty list
                    break;
                }
            }
        }

        return inputFields;
    }

    public FilterRowDoFn withOutputSchema(boolean hasSchema) {
        hasOutputSchema = hasSchema;
        return this;
    }

    public FilterRowDoFn withRejectSchema(boolean hasSchema) {
        hasRejectSchema = hasSchema;
        return this;
    }

    public FilterRowDoFn withProperties(FilterRowProperties properties) {
        this.properties = properties;
        return this;
    }
}
