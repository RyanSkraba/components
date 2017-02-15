// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.filterrow.runtime;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.filterrow.ConditionsTable;
import org.talend.components.filterrow.TFilterRowProperties;
import org.talend.components.filterrow.functions.FunctionType;
import org.talend.components.filterrow.operators.OperatorType;
import org.talend.components.filterrow.processing.FilterDescriptor;
import org.talend.components.filterrow.processing.LogicalOperator;
import org.talend.daikon.NamedThing;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

/**
 * created by dmytro.chmyga on Dec 19, 2016
 */
public class TFilterRowSink implements Sink {

    private static final long serialVersionUID = 2558711916032472975L;

    private final List<FilterDescriptor> filterDescriptors = new LinkedList<>();

    private Schema schemaFlow;

    private Schema schemaReject;

    private Schema inputSchema;

    private LogicalOperator logicalOperator;

    @Override
    public Schema getEndpointSchema(RuntimeContainer arg0, String arg1) throws IOException {
        return null;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer arg0) throws IOException {
        return null;
    }

    @Override
    public ValidationResult validate(RuntimeContainer arg0) {
        for (FilterDescriptor descriptor : filterDescriptors) {
            Schema.Field f = inputSchema.getField(descriptor.getColumnName());
            ValidationResult filteringValidationResult = new FilterPrerequisitesValidator().validate(descriptor.getFunctionType(),
                    descriptor.getOperatorType(), AvroUtils.unwrapIfNullable(f.schema()).getType(),
                    descriptor.getPredefinedValue());
            if (filteringValidationResult.getStatus() != Result.OK) {
                return filteringValidationResult;
            }
        }
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult initialize(RuntimeContainer arg0, ComponentProperties arg1) {
        TFilterRowProperties props = (TFilterRowProperties) arg1;
        filterDescriptors.clear();
        logicalOperator = props.logicalOperator.getValue();
        schemaFlow = props.schemaFlow.schema.getValue();
        schemaReject = props.schemaReject.schema.getValue();
        inputSchema = props.schemaMain.schema.getValue();
        ConditionsTable conditionsTable = props.conditionsTable;
        List<String> definedColumnFilters = conditionsTable.columnName.getValue();
        for (int i = 0; i < definedColumnFilters.size(); i++) {
            String columnName = definedColumnFilters.get(i);
            OperatorType operatorType = OperatorType.valueOf(String.valueOf(conditionsTable.operator.getValue().get(i)));
            FunctionType functionType = FunctionType.valueOf(String.valueOf(conditionsTable.function.getValue().get(i)));
            Object value = conditionsTable.value.getValue().get(i);
            filterDescriptors.add(new FilterDescriptor(columnName, functionType, operatorType, value));
        }
        return ValidationResult.OK;
    }

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new TFilterRowWriteOperation(this, filterDescriptors, schemaFlow, schemaReject, logicalOperator);
    }

}
