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
package org.talend.components.filterrow.functions;

import org.junit.Assert;
import org.junit.Test;

public class FunctionsTest {

    @Test
    public void testEmptyFunction() {
        Object testObject = new Integer(212);
        EmptyFunction<Integer> intEmptyFunction = new EmptyFunction<>();
        Object returnedValue = intEmptyFunction.getValue((Integer) testObject);
        Assert.assertEquals("Returned value class is not the same as expected", testObject.getClass(), returnedValue.getClass());
        Assert.assertEquals("Returned value is not the same as expected", testObject, returnedValue);

        testObject = new String("aaa");
        EmptyFunction<String> stringEmptyFunction = new EmptyFunction<>();
        returnedValue = stringEmptyFunction.getValue((String) testObject);
        Assert.assertEquals("Returned value class is not the same as expected", testObject.getClass(), returnedValue.getClass());
        Assert.assertEquals("Returned value is not the same as expected", testObject, returnedValue);
    }

    @Test
    public void testLengthFunction() {
        String testValue = new String("String");
        Integer expected = testValue.length();
        LengthFunction f = new LengthFunction();
        Integer returnedValue = f.getValue(testValue);
        Assert.assertEquals("Returned value is not the same as expected", expected, returnedValue);
    }

    @Test
    public void testLowerCaseFunction() {
        String testValue = new String("String");
        String expected = testValue.toLowerCase();
        LowerCaseFunction function = new LowerCaseFunction();
        String returnedValue = function.getValue(testValue);
        Assert.assertEquals("Returned value is not the same as expected", expected, returnedValue);
    }

    @Test
    public void testUpperCaseFunction() {
        String testValue = new String("String");
        String expected = testValue.toUpperCase();
        UpperCaseFunction function = new UpperCaseFunction();
        String returnedValue = function.getValue(testValue);
        Assert.assertEquals("Returned value is not the same as expected", expected, returnedValue);
    }

    @Test
    public void testUpperCaseFirstFunction() {
        String testValue = new String("string");
        Character expected = new Character('S');
        UpperCaseFirstFunction function = new UpperCaseFirstFunction();
        Character returnedValue = function.getValue(testValue);
        Assert.assertEquals("Returned value is not the same as expected", expected, returnedValue);
    }

    @Test
    public void testLowerCaseFirstFunction() {
        String testValue = new String("String");
        Character expected = new Character('s');
        LowerCaseFirstFunction function = new LowerCaseFirstFunction();
        Character returnedValue = function.getValue(testValue);
        Assert.assertEquals("Returned value is not the same as expected", expected, returnedValue);
    }

    @Test
    public void testMatchFunction() {
        String testValue = "Some  new String";
        String pattern = "Some.*String";
        boolean expected = testValue.matches(pattern);
        Assert.assertEquals(true, expected);// Just to check ourselves that we understand the pattern correctly.
        MatchesFunction function = new MatchesFunction(pattern);
        Boolean returnedValue = function.getValue(testValue);
        Assert.assertEquals("Returned value is not the same as expected", expected, returnedValue);

        // Let's check negative result here also.
        testValue = "This new String";
        pattern = "That.*String";
        expected = testValue.matches(pattern);
        Assert.assertEquals(false, expected);// Just to check ourselves that we understand the pattern correctly.
        function = new MatchesFunction(pattern);
        returnedValue = function.getValue(testValue);
        Assert.assertEquals("Returned value is not the same as expected", expected, returnedValue);

    }

}
