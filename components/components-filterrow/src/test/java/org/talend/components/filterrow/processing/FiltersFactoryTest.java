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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.junit.Assert;
import org.junit.Test;
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

public class FiltersFactoryTest {

    @Test
    public void testCreatingFunctions() {
        String predefinedValue = "Abc";
        Function<String, String> emptyFunction = FiltersFactory.createFunction(FunctionType.EMPTY, predefinedValue);
        assertThat(emptyFunction, instanceOf(EmptyFunction.class));
        String returnedValue = emptyFunction.getValue(predefinedValue);
        Assert.assertEquals("EmptyFunction didn't return the same value", predefinedValue, returnedValue);

        Function<?, ?> lengthFunction = FiltersFactory.createFunction(FunctionType.LENGTH, predefinedValue);
        assertThat(lengthFunction, instanceOf(LengthFunction.class));

        Function<?, ?> upperCaseFunction = FiltersFactory.createFunction(FunctionType.UPPER_CASE, predefinedValue);
        assertThat(upperCaseFunction, instanceOf(UpperCaseFunction.class));

        Function<?, ?> lowerCaseFunction = FiltersFactory.createFunction(FunctionType.LOWER_CASE, predefinedValue);
        assertThat(lowerCaseFunction, instanceOf(LowerCaseFunction.class));

        Function<?, ?> lowerCaseFirstFunction = FiltersFactory.createFunction(FunctionType.LOWER_CASE_FIRST, predefinedValue);
        assertThat(lowerCaseFirstFunction, instanceOf(LowerCaseFirstFunction.class));

        Function<?, ?> upperCaseFirstFunction = FiltersFactory.createFunction(FunctionType.UPPER_CASE_FIRST, predefinedValue);
        assertThat(upperCaseFirstFunction, instanceOf(UpperCaseFirstFunction.class));

        Function<?, ?> matchFunction = FiltersFactory.createFunction(FunctionType.MATCH, predefinedValue);
        assertThat(matchFunction, instanceOf(MatchesFunction.class));
    }

    @Test
    public void testCreatingOperator() {
        Object predefinedValue = "Abc";
        Operator<?> equals = FiltersFactory.createOperator(OperatorType.EQUALS, predefinedValue);
        assertThat(equals, instanceOf(EqualsOperator.class));

        Operator<?> notEquals = FiltersFactory.createOperator(OperatorType.NOT_EQUAL_TO, predefinedValue);
        assertThat(notEquals, instanceOf(NotEqualsOperator.class));

        Operator<?> greaterThan = FiltersFactory.createOperator(OperatorType.GREATER_THAN, predefinedValue);
        assertThat(greaterThan, instanceOf(GreaterThanOperator.class));

        Operator<?> lessThan = FiltersFactory.createOperator(OperatorType.LOWER_THAN, predefinedValue);
        assertThat(lessThan, instanceOf(LessThanOperator.class));

        Operator<?> greaterThanOrEquals = FiltersFactory.createOperator(OperatorType.GREATER_OR_EQUAL_TO, predefinedValue);
        assertThat(greaterThanOrEquals, instanceOf(GreaterOrEqualToOperator.class));

        Operator<?> lowerThanOrEquals = FiltersFactory.createOperator(OperatorType.LOWER_OR_EQUAL_TO, predefinedValue);
        assertThat(lowerThanOrEquals, instanceOf(LowerOrEqualToOperator.class));
    }

    @Test
    public void testCreatingFilter() {
        String predefinedPattern = "a.*b";
        FunctionType functionType = FunctionType.MATCH;
        OperatorType operatorType = OperatorType.EQUALS;
        Filter<?, ?> filter = FiltersFactory.createFilter(functionType, operatorType, predefinedPattern);
        assertThat(filter.getFunction(), instanceOf(MatchesFunction.class));
        assertThat(filter.getOperator(), instanceOf(EqualsOperator.class));

        String predefinedValue = "ab";
        functionType = FunctionType.LOWER_CASE;
        operatorType = OperatorType.EQUALS;
        filter = FiltersFactory.createFilter(functionType, operatorType, predefinedValue);
        assertThat(filter.getFunction(), instanceOf(LowerCaseFunction.class));
        assertThat(filter.getOperator(), instanceOf(EqualsOperator.class));

        Integer predefinedLength = 15;
        functionType = FunctionType.LENGTH;
        operatorType = OperatorType.EQUALS;
        filter = FiltersFactory.createFilter(functionType, operatorType, predefinedLength);
        assertThat(filter.getFunction(), instanceOf(LengthFunction.class));
        assertThat(filter.getOperator(), instanceOf(EqualsOperator.class));
    }

}
