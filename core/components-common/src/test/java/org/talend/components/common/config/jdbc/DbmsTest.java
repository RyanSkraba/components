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
 * Unit-tests for {@link Dbms}
 */
public class DbmsTest {

    @Test
    public void testGetters() {
        String expectedId = "mysql_id";
        String expectedProduct = "MYSQL";
        String expectedLabel = "Mapping Mysql";
        boolean expectedDefault = true;
        Dbms dbms = new Dbms(expectedId, expectedProduct, expectedLabel, expectedDefault);

        Assert.assertEquals(expectedId, dbms.getId());
    }

}
