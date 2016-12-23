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
package org.talend.components.filterrow.operators;

import org.junit.Assert;
import org.junit.Test;

/**
 * created by dmytro.chmyga on Dec 22, 2016
 */
public class OperatorsTest {

    @Test
    public void testEqualsOperator() {
        String testedValue = new String("Abc");
        String predefinedValue = new String("Abc");
        EqualsOperator<String> strEqualsOperator = new EqualsOperator<String>(predefinedValue);
        Assert.assertTrue("String values are equal, but operator returned false.",
                strEqualsOperator.compareToObject(testedValue));

        String wrongStrTestedValue = new String("Asd");
        Assert.assertFalse("String values are not equal, but operator returned true.",
                strEqualsOperator.compareToObject(wrongStrTestedValue));

        Integer intTestedValue = new Integer(1234);
        Integer intPredefinedValue = new Integer(1234);
        EqualsOperator<Integer> intEqualsOperator = new EqualsOperator<Integer>(intPredefinedValue);
        Assert.assertTrue("Integer values are equal, but operator returned false.",
                intEqualsOperator.compareToObject(intTestedValue));
        Assert.assertFalse("Integer values are not equal, but operator returned true.", intEqualsOperator.compareToObject(2345));

        byte[] bytesTestValue = new byte[] { 1, 2, 3 };
        byte[] predefinedBytesValue = bytesTestValue;
        EqualsOperator<byte[]> bytesEqualsOperator = new EqualsOperator<>(predefinedBytesValue);
        Assert.assertTrue("Integer values are equal, but operator returned false.",
                bytesEqualsOperator.compareToObject(bytesTestValue));
    }

    @Test
    public void testNotEqualsOperator() {
        String testedValue = new String("Abc");
        String predefinedValue = new String("Asd");
        NotEqualsOperator<String> strEqualsOperator = new NotEqualsOperator<String>(predefinedValue);
        Assert.assertTrue("String values are not equal, but operator returned false.",
                strEqualsOperator.compareToObject(testedValue));

        String wrongStrTestedValue = new String("Asd");
        Assert.assertFalse("String values are equal, but operator returned true.",
                strEqualsOperator.compareToObject(wrongStrTestedValue));

        Integer intTestedValue = new Integer(1234);
        Integer intPredefinedValue = new Integer(2345);
        NotEqualsOperator<Integer> intEqualsOperator = new NotEqualsOperator<Integer>(intPredefinedValue);
        Assert.assertTrue("Integer values are not equal, but operator returned true.",
                intEqualsOperator.compareToObject(intTestedValue));
        Assert.assertFalse("Integer values are equal, but operator returned true.", intEqualsOperator.compareToObject(2345));
    }

}
