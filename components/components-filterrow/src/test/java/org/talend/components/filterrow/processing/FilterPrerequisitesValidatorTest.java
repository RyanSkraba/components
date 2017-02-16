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

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;
import org.talend.components.filterrow.functions.FunctionType;
import org.talend.components.filterrow.operators.OperatorType;
import org.talend.components.filterrow.runtime.FilterPrerequisitesValidator;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

/**
 * created by dmytro.chmyga on Dec 23, 2016
 */
public class FilterPrerequisitesValidatorTest {

    @Test
    public void testCreatingMatchFunction() {
        FunctionType function = FunctionType.MATCH;
        OperatorType operator = OperatorType.EQUALS;
        String value = "a*";
        FilterPrerequisitesValidator validator = new FilterPrerequisitesValidator();
        ValidationResult result = validator.validate(function, operator, Schema.Type.STRING, value);
        Assert.assertTrue("Match filter should have been created.", result.status == Result.OK);
    }

    @Test
    public void testWrongParametersForMatchFunction() {
        FunctionType function = FunctionType.MATCH;
        OperatorType operator = OperatorType.GREATER_OR_EQUAL_TO;
        String value = "a*";
        FilterPrerequisitesValidator validator = new FilterPrerequisitesValidator();
        ValidationResult result = validator.validate(function, operator, Schema.Type.STRING, value);
        Assert.assertTrue("Match filter should have not been created.", result.status == Result.ERROR);
        System.out.println(result.message);
    }

    @Test
    public void testIntegerFunction() {
        FunctionType function = FunctionType.EMPTY;
        OperatorType operator = OperatorType.GREATER_OR_EQUAL_TO;
        Integer value = 50;
        FilterPrerequisitesValidator validator = new FilterPrerequisitesValidator();
        ValidationResult result = validator.validate(function, operator, Schema.Type.INT, value);
        Assert.assertTrue("Match filter should have been created.", result.status == Result.OK);
    }

    @Test
    public void testIntegerWrongFunction() {
        FunctionType function = FunctionType.LENGTH;
        OperatorType operator = OperatorType.GREATER_OR_EQUAL_TO;
        Integer value = 50;
        FilterPrerequisitesValidator validator = new FilterPrerequisitesValidator();
        ValidationResult result = validator.validate(function, operator, Schema.Type.INT, value);
        Assert.assertTrue("Match filter should have not been created.", result.status == Result.ERROR);
        System.out.println(result.message);
    }

}
