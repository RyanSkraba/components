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
package org.talend.components.azurestorage.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class AzureStorageConnectionEditWizardDefinitionTest {

    private AzureStorageConnectionEditWizardDefinition azureStorageConnectionEditWizardDefinition;

    @Before
    public void setUp() throws Exception {
        azureStorageConnectionEditWizardDefinition = new AzureStorageConnectionEditWizardDefinition();

    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageConnectionEditWizardDefinition#getName()
     */
    @Test
    public void testGetName() {
        String name = azureStorageConnectionEditWizardDefinition.getName();
        assertEquals("name cannot be null", AzureStorageConnectionEditWizardDefinition.COMPONENT_WIZARD_NAME, name);
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageConnectionEditWizardDefinition#isTopLevel()
     */
    @Test
    public void isTopLevel() {
        boolean result = azureStorageConnectionEditWizardDefinition.isTopLevel();
        assertFalse("result cannot be true", result);
    }

}
