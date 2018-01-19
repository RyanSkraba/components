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
package org.talend.components.common.config.jdbc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit-tests for {@link DbmsType}
 */
public class DbmsTypeTest {

    @Test
    public void testGetters() {
        String expectedName = "testType";
        boolean expectedDefault = true;
        int expectedLength = 11;
        int expectedPrecision = 12;
        boolean expectedIgnoreLength = true;
        boolean expectedIgnorePrecision = false;
        boolean expectedPreBeforeLength = true;

        DbmsType dbmsType = new DbmsType("testType", true, 11, 12, true, false, true);

        Assert.assertEquals(expectedName, dbmsType.getName());
        Assert.assertEquals(expectedDefault, dbmsType.isDefault());
        Assert.assertEquals(expectedLength, dbmsType.getDefaultLength());
        Assert.assertEquals(expectedPrecision, dbmsType.getDefaultPrecision());
        Assert.assertEquals(expectedIgnoreLength, dbmsType.isIgnoreLength());
        Assert.assertEquals(expectedIgnorePrecision, dbmsType.isIgnorePrecision());
        Assert.assertEquals(expectedPreBeforeLength, dbmsType.isPreBeforeLength());
    }
}
