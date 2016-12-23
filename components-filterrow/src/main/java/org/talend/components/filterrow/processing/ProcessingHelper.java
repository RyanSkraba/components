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

import java.util.Arrays;
import java.util.Collection;

import org.apache.avro.Schema;
import org.talend.components.filterrow.functions.FunctionType;
import org.talend.components.filterrow.operators.OperatorType;

/**
 * created by dmytro.chmyga on Dec 23, 2016
 */
public class ProcessingHelper {

    private final static Collection<OperatorType> matchesOperators = Arrays.asList(OperatorType.EQUALS,
            OperatorType.NOT_EQUAL_TO);

    private final static Collection<OperatorType> allOperatorTypes = Arrays.asList(OperatorType.values());

    private final static Collection<OperatorType> comparableOperatorTypes = Arrays.asList(OperatorType.GREATER_OR_EQUAL_TO,
            OperatorType.GREATER_THAN, OperatorType.LOWER_THAN, OperatorType.LOWER_OR_EQUAL_TO);

    private final static Collection<FunctionType> allFunctions = Arrays.asList(FunctionType.values());

    public static Collection<OperatorType> getPossibleOperatorTypesByFunctionType(FunctionType functionType) {
        if (functionType == FunctionType.MATCH) {
            return matchesOperators;
        }
        return allOperatorTypes;
    }

    public static Collection<FunctionType> getPossibleFunctionTypesByFieldType(Schema.Type fieldType) {
        if (fieldType == Schema.Type.STRING) {
            return allFunctions;
        }
        return Arrays.asList(FunctionType.EMPTY);
    }

    public static Collection<OperatorType> getComparableOperatorTypes() {
        return comparableOperatorTypes;
    }

}
