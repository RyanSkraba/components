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
package org.talend.components.snowflake;

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class SnowflakeRollbackAndCommitPropertiesTest {

    SnowflakeRollbackAndCommitProperties rollbackAndCommitProperties;

    @Before
    public void reset() {
        rollbackAndCommitProperties = new SnowflakeRollbackAndCommitProperties("commit");
        rollbackAndCommitProperties.setupProperties();
    }

    @Test
    public void testSetupLayout() {
        assertEquals(0, rollbackAndCommitProperties.getForms().size());
        rollbackAndCommitProperties.setupLayout();
        assertEquals(1, rollbackAndCommitProperties.getForms().size());
        assertEquals(2, rollbackAndCommitProperties.getForm(Form.MAIN).getWidgets().size());
        assertNotNull(rollbackAndCommitProperties
                .getForm(Form.MAIN)
                .getWidget(rollbackAndCommitProperties.referencedComponent.getName()));
        assertNotNull(rollbackAndCommitProperties
                .getForm(Form.MAIN)
                .getWidget(rollbackAndCommitProperties.closeConnection.getName()));
    }

    @Test
    public void testGetReferencedComponentId() {
        String expectedStringValue = "SomeStringValue";
        rollbackAndCommitProperties.referencedComponent.componentInstanceId.setValue(expectedStringValue);

        assertEquals(rollbackAndCommitProperties.getReferencedComponentId(), expectedStringValue);
    }

    @Test
    public void testCheckCloseConnection() {
        assertTrue(rollbackAndCommitProperties.closeConnection.getValue());
        rollbackAndCommitProperties.closeConnection.setValue(false);

        assertFalse(rollbackAndCommitProperties.closeConnection.getValue());
    }

}
