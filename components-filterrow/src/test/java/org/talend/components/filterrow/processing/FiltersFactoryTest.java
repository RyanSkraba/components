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

/**
 * created by dmytro.chmyga on Dec 23, 2016
 */
public class FiltersFactoryTest {

    @Test
    public void testCreatingFunctions() {
        String predefinedValue = "Abc";
        Function<String, String> emptyFunction = FiltersFactory.createFunction(FunctionType.EMPTY, predefinedValue);
        Assert.assertTrue("Function should be instance of EmptyFunction", (emptyFunction instanceof EmptyFunction<?>));
        String returnedValue = emptyFunction.getValue(predefinedValue);
        Assert.assertEquals("EmptyFunction didn't return the same value", predefinedValue, returnedValue);

        Function<?, ?> lengthFunction = FiltersFactory.createFunction(FunctionType.LENGTH, predefinedValue);
        Assert.assertTrue("Function should be instance of LengthFunction", (lengthFunction instanceof LengthFunction));

        Function<?, ?> upperCaseFunction = FiltersFactory.createFunction(FunctionType.UPPER_CASE, predefinedValue);
        Assert.assertTrue("Function should be instance of UpperCaseFunction", (upperCaseFunction instanceof UpperCaseFunction));

        Function<?, ?> lowerCaseFunction = FiltersFactory.createFunction(FunctionType.LOWER_CASE, predefinedValue);
        Assert.assertTrue("Function should be instance of LowerCaseFunction", (lowerCaseFunction instanceof LowerCaseFunction));

        Function<?, ?> lowerCaseFirstFunction = FiltersFactory.createFunction(FunctionType.LOWER_CASE_FIRST, predefinedValue);
        Assert.assertTrue("Function should be instance of LowerCaseFirstFunction",
                (lowerCaseFirstFunction instanceof LowerCaseFirstFunction));

        Function<?, ?> upperCaseFirstFunction = FiltersFactory.createFunction(FunctionType.UPPER_CASE_FIRST, predefinedValue);
        Assert.assertTrue("Function should be instance of UpperCaseFirstFunction",
                (upperCaseFirstFunction instanceof UpperCaseFirstFunction));

        Function<?, ?> matchFunction = FiltersFactory.createFunction(FunctionType.MATCH, predefinedValue);
        Assert.assertTrue("Function should be instance of MatchFunction", (matchFunction instanceof MatchesFunction));
    }

    @Test
    public void testCreatingOperator() {
        Object predefinedValue = "Abc";
        Operator<?> equals = FiltersFactory.createOperator(OperatorType.EQUALS, predefinedValue);
        Assert.assertTrue("Operator should be instance of EqualsOperator", (equals instanceof EqualsOperator<?>));

        Operator<?> notEquals = FiltersFactory.createOperator(OperatorType.NOT_EQUAL_TO, predefinedValue);
        Assert.assertTrue("Operator should be instance of NotEqualsOperator", (notEquals instanceof NotEqualsOperator<?>));

        Operator<?> greaterThan = FiltersFactory.createOperator(OperatorType.GREATER_THAN, predefinedValue);
        Assert.assertTrue("Operator should be instance of GreaterThanOperator", (greaterThan instanceof GreaterThanOperator<?>));

        Operator<?> lessThan = FiltersFactory.createOperator(OperatorType.LOWER_THAN, predefinedValue);
        Assert.assertTrue("Operator should be instance of LessThanOperator", (lessThan instanceof LessThanOperator<?>));

        Operator<?> greaterThanOrEquals = FiltersFactory.createOperator(OperatorType.GREATER_OR_EQUAL_TO, predefinedValue);
        Assert.assertTrue("Operator should be instance of GreaterOrEqualToOperator",
                (greaterThanOrEquals instanceof GreaterOrEqualToOperator<?>));

        Operator<?> lowerThanOrEquals = FiltersFactory.createOperator(OperatorType.LOWER_OR_EQUAL_TO, predefinedValue);
        Assert.assertTrue("Operator should be instance of LowerOrEqualToOperator",
                (lowerThanOrEquals instanceof LowerOrEqualToOperator<?>));
    }

    @Test
    public void testCreatingFilter() {
        String predefinedPattern = "a.*b";
        FunctionType functionType = FunctionType.MATCH;
        OperatorType operatorType = OperatorType.EQUALS;
        Filter<?, ?> filter = FiltersFactory.createFilter(functionType, operatorType, predefinedPattern);
        Assert.assertTrue("Function should be instance of MatchFunction", (filter.getFunction() instanceof MatchesFunction));
        Assert.assertTrue("Operator should be instance of EqualsOperator", (filter.getOperator() instanceof EqualsOperator<?>));

        String predefinedValue = "ab";
        functionType = FunctionType.LOWER_CASE;
        operatorType = OperatorType.EQUALS;
        filter = FiltersFactory.createFilter(functionType, operatorType, predefinedValue);
        Assert.assertTrue("Function should be instance of LowerCaseFunction",
                (filter.getFunction() instanceof LowerCaseFunction));
        Assert.assertTrue("Operator should be instance of EqualsOperator", (filter.getOperator() instanceof EqualsOperator<?>));

        Integer predefinedLength = 15;
        functionType = FunctionType.LENGTH;
        operatorType = OperatorType.EQUALS;
        filter = FiltersFactory.createFilter(functionType, operatorType, predefinedLength);
        Assert.assertTrue("Function should be instance of LengthFunction", (filter.getFunction() instanceof LengthFunction));
        Assert.assertTrue("Operator should be instance of EqualsOperator", (filter.getOperator() instanceof EqualsOperator<?>));
    }

}
