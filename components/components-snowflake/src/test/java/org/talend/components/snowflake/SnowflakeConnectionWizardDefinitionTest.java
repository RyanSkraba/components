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
package org.talend.components.snowflake;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.daikon.definition.DefinitionImageType;

/**
 * Unit tests for {@link SnowflakeConnectionWizardDefinition} class
 */
public class SnowflakeConnectionWizardDefinitionTest {

    private SnowflakeConnectionWizardDefinition definition;

    @Before
    public void setup() {
        definition = new SnowflakeConnectionWizardDefinition();
    }

    @Test
    public void testCreateWizardWithSetConnectionProperties() {
        SnowflakeConnectionProperties connectionProperties = new SnowflakeConnectionProperties("connection");
        SnowflakeConnectionWizard snowflakeConnectionWizard = (SnowflakeConnectionWizard) definition.createWizard(connectionProperties,
                "location");
        Assert.assertEquals(connectionProperties, snowflakeConnectionWizard.tProps.getConnectionProperties());
        Assert.assertTrue(snowflakeConnectionWizard.supportsProperties(connectionProperties));
    }

    @Test
    public void testSupportProperties() {
        Assert.assertTrue(definition.supportsProperties(SnowflakeConnectionProperties.class));
    }

    /**
     * Test if new method returns the same result as deprecated one.
     */
    @Test
    public void testGetImagePath() {
        Assert.assertEquals(definition.getPngImagePath(WizardImageType.TREE_ICON_16X16), definition.getImagePath(DefinitionImageType.TREE_ICON_16X16));
        Assert.assertEquals(definition.getPngImagePath(WizardImageType.WIZARD_BANNER_75X66), definition.getImagePath(DefinitionImageType.WIZARD_BANNER_75X66));
        Assert.assertNull(definition.getImagePath(DefinitionImageType.SVG_ICON));
    }
    
    /**
     * Checks {@link SnowflakeConnectionWizardDefinition#getMenuItemName()} returns "Snowflake Connection" for en_US locale
     */
    @Test
    public void testGetMenuItemName() {
        SnowflakeConnectionWizardDefinition definition = new SnowflakeConnectionWizardDefinition();
        Assert.assertEquals("Snowflake Connection", definition.getMenuItemName());
    }
}
