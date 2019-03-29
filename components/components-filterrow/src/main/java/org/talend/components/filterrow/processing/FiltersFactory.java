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

import org.talend.components.filterrow.functions.EmptyFunction;
import org.talend.components.filterrow.functions.Function;
import org.talend.components.filterrow.functions.FunctionType;
import org.talend.components.filterrow.functions.LengthFunction;
import org.talend.components.filterrow.functions.LowerCaseFirstFunction;
import org.talend.components.filterrow.functions.LowerCaseFunction;
import org.talend.components.filterrow.functions.MatchesFunction;
import org.talend.components.filterrow.functions.UpperCaseFirstFunction;
import org.talend.components.filterrow.functions.UpperCaseFunction;
import org.talend.components.filterrow.operators.EqualsOperator;
import org.talend.components.filterrow.operators.GreaterOrEqualToOperator;
import org.talend.components.filterrow.operators.GreaterThanOperator;
import org.talend.components.filterrow.operators.LessThanOperator;
import org.talend.components.filterrow.operators.LowerOrEqualToOperator;
import org.talend.components.filterrow.operators.NotEqualsOperator;
import org.talend.components.filterrow.operators.Operator;
import org.talend.components.filterrow.operators.OperatorType;

/**
 * created by dmytro.chmyga on Dec 22, 2016
 */
public class FiltersFactory {

    @SuppressWarnings("unchecked")
    public static <T> Operator<T> createOperator(OperatorType operatorType, T predefinedValue) {
        switch (operatorType) {
        case EQUALS:
            return new EqualsOperator<>(predefinedValue);
        case NOT_EQUAL_TO:
            return new NotEqualsOperator<>(predefinedValue);
        default:
            if (predefinedValue instanceof Comparable) {
                final Operator<?> operator = createComparableOperator(operatorType, asComparable((Comparable) predefinedValue));
                return (Operator<T>) operator;
            } else {
                return new EqualsOperator<>(predefinedValue);
            }
        }
    }

    private static <T extends Comparable<T>> T asComparable(final T predefinedValue) {
        return predefinedValue;
    }

    private static <T extends Comparable<T>> Operator<T> createComparableOperator(OperatorType operatorType, T predefinedValue) {
        switch (operatorType) {
        case GREATER_THAN:
            return new GreaterThanOperator<>(predefinedValue);
        case LOWER_THAN:
            return new LessThanOperator<>(predefinedValue);
        case GREATER_OR_EQUAL_TO:
            return new GreaterOrEqualToOperator<>(predefinedValue);
        case LOWER_OR_EQUAL_TO:
            return new LowerOrEqualToOperator<>(predefinedValue);
        default:
            return new EqualsOperator<>(predefinedValue);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, R> Function<T, R> createFunction(FunctionType functionType, Object predefinedValue) {
        switch (functionType) {
        case EMPTY:
            return (Function<T, R>) new EmptyFunction<>();
        case LOWER_CASE:
            return (Function<T, R>) new LowerCaseFunction();
        case UPPER_CASE:
            return (Function<T, R>) new UpperCaseFunction();
        case LOWER_CASE_FIRST:
            return (Function<T, R>) new LowerCaseFirstFunction();
        case UPPER_CASE_FIRST:
            return (Function<T, R>) new UpperCaseFirstFunction();
        case LENGTH:
            return (Function<T, R>) new LengthFunction();
        case MATCH:
            return (Function<T, R>) new MatchesFunction(predefinedValue == null ? null : String.valueOf(predefinedValue));
        default:
            return (Function<T, R>) new EmptyFunction<>();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, R> Filter<T, R> createFilter(FunctionType functionType, OperatorType operatorType, Object predefinedValue) {
        Function<T, R> function = createFunction(functionType, predefinedValue);
        Operator<T> operator;
        if (functionType == FunctionType.MATCH) {
            operator = (Operator<T>) createOperator(operatorType, new Boolean(true));
        } else {
            operator = (Operator<T>) createOperator(operatorType, predefinedValue);
        }
        return new Filter<>(function, operator);
    }

    public static Filter<?, ?> createFilter(FilterDescriptor descriptor) {
        return createFilter(descriptor.getFunctionType(), descriptor.getOperatorType(), descriptor.getPredefinedValue());
    }

}
