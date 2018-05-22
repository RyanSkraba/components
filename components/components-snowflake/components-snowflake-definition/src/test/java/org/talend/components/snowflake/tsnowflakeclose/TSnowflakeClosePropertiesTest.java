// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.tsnowflakeclose;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;

/**
 * Unit tests for {@link TSnowflakeCloseProperties} class
 *
 */
public class TSnowflakeClosePropertiesTest {

    TSnowflakeCloseProperties closeProperties;

    @Before
    public void reset() {
        closeProperties = new TSnowflakeCloseProperties("close");
    }

    @Test
    public void testSetupLayout() {
        Assert.assertEquals(0, closeProperties.getForms().size());
        closeProperties.setupLayout();
        Assert.assertEquals(1, closeProperties.getForms().size());
        Assert.assertNotNull(closeProperties.getForm(Form.MAIN).getWidget(closeProperties.referencedComponent.getName()));
    }

    @Test
    public void testGetReferencedComponentId() {
        String expectedStringValue;
        String referencedComponentId;

        expectedStringValue = "SomeStringValue";
        closeProperties.referencedComponent.componentInstanceId.setValue(expectedStringValue);
        referencedComponentId = closeProperties.getReferencedComponentId();

        Assert.assertEquals(referencedComponentId, expectedStringValue);
    }

}
