// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.objects;

import org.junit.Assert;
import org.junit.Test;

public class TableObjectTest {

    @Test
    public void testGetFullNameEnclosed() {
        String expected = "\"test_db\".\"test_schema\".\"test_table\"";

        TableObject tableObject = new TableObject("test_db", "test_schema", "test_table");
        String actual = tableObject.getFullName();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetFullName() {
        String expected = "test_db.test_schema.test_table";

        TableObject tableObject = new TableObject("test_db", "test_schema", "test_table");
        String actual = tableObject.getFullName(false);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetFullNameMultipleCalls() {
        String expected = "test_db.test_schema.test_table";

        TableObject tableObject = new TableObject("test_db", "test_schema", "test_table");

        Assert.assertEquals(expected, tableObject.getFullName(false));
        Assert.assertEquals(expected, tableObject.getFullName(false));
        Assert.assertEquals(expected, tableObject.getFullName(false));
    }
}
