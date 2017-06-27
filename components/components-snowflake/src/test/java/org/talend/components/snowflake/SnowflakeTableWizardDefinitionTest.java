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
 * Unit tests for {@link SnowflakeTableWizardDefinition} class
 */
public class SnowflakeTableWizardDefinitionTest {

    private SnowflakeTableWizardDefinition definition;

    @Before
    public void setup() {
        definition = new SnowflakeTableWizardDefinition();
    }

    @Test
    public void testCreateWizardWithSetConnectionProperties() {
        SnowflakeConnectionProperties connectionProperties = new SnowflakeConnectionProperties("connection");
        SnowflakeTableWizard snowflakeTableWizard = (SnowflakeTableWizard) definition.createWizard(connectionProperties,
                "location");
        Assert.assertEquals(connectionProperties, snowflakeTableWizard.tProps.getConnectionProperties());
        Assert.assertTrue(snowflakeTableWizard.supportsProperties(connectionProperties));
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
        Assert.assertEquals(definition.getPngImagePath(WizardImageType.TREE_ICON_16X16),
                definition.getImagePath(DefinitionImageType.TREE_ICON_16X16));
        Assert.assertEquals(definition.getPngImagePath(WizardImageType.WIZARD_BANNER_75X66),
                definition.getImagePath(DefinitionImageType.WIZARD_BANNER_75X66));
        Assert.assertNull(definition.getImagePath(DefinitionImageType.SVG_ICON));
    }

}
