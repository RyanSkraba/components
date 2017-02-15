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
package org.talend.components.filterrow.processing;

import org.talend.components.filterrow.functions.FunctionType;
import org.talend.components.filterrow.operators.OperatorType;

public class FilterDescriptor {

    private final String columnName;

    private final FunctionType functionType;

    private final OperatorType operatorType;

    private final Object predefinedValue;

    public FilterDescriptor(String columnName, FunctionType functionType, OperatorType operatorType, Object predefinedValue) {
        super();
        this.columnName = columnName;
        this.functionType = functionType;
        this.operatorType = operatorType;
        this.predefinedValue = predefinedValue;
    }

    public String getColumnName() {
        return columnName;
    }

    public FunctionType getFunctionType() {
        return functionType;
    }

    public OperatorType getOperatorType() {
        return operatorType;
    }

    public Object getPredefinedValue() {
        return predefinedValue;
    }

}
