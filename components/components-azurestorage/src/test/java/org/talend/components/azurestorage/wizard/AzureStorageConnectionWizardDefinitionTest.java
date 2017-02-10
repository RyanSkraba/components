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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.wizard.WizardImageType;

public class AzureStorageConnectionWizardDefinitionTest {

    private AzureStorageConnectionWizardDefinition azureStorageConnectionWizardDefinition;

    @Before
    public void setUp() throws Exception {
        azureStorageConnectionWizardDefinition = new AzureStorageConnectionWizardDefinition();
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageConnectionWizardDefinition#isTopLevel()
     */
    @Test
    public void testIsTopLevel() {
        boolean result = azureStorageConnectionWizardDefinition.isTopLevel();
        assertTrue("result should be true", result);
    }

    /**
     *
     * @see org.talend.components.azurestorage.wizard.AzureStorageConnectionWizardDefinition#getPngImagePath(WizardImageType)
     */
    @Test
    public void testGetPngImagePath() {
        String pngimagepath = azureStorageConnectionWizardDefinition.getPngImagePath(WizardImageType.TREE_ICON_16X16);
        assertEquals("connectionWizardIcon.png", pngimagepath);
        pngimagepath = azureStorageConnectionWizardDefinition.getPngImagePath(WizardImageType.WIZARD_BANNER_75X66);
        assertEquals("azureStorageWizardBanner.png", pngimagepath);
    }
}
