// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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
import org.talend.components.snowflake.SnowflakeOauthConnectionProperties.GrantType;
import org.talend.daikon.properties.presentation.Form;

public class SnowflakeOauthConnectionPropertiesTest {

    private static final String NAME = "name";

    private SnowflakeOauthConnectionProperties snowflakeOauthConnectionProperties;

    @Before
    public void setUp() throws Exception {
        snowflakeOauthConnectionProperties = new SnowflakeOauthConnectionProperties(NAME);
    }

    @Test
    public void testSetupProperties() {
        Assert.assertNull(snowflakeOauthConnectionProperties.oauthTokenEndpoint.getValue());
        snowflakeOauthConnectionProperties.setupProperties();
        Assert.assertNotNull(snowflakeOauthConnectionProperties.oauthTokenEndpoint.getValue());
    }

    @Test
    public void testSetupLayout() {
        Assert.assertNull(snowflakeOauthConnectionProperties.getForm(Form.MAIN));
        snowflakeOauthConnectionProperties.setupLayout();
        Assert.assertNotNull(snowflakeOauthConnectionProperties.getForm(Form.MAIN));
    }

    @Test
    public void testAfterGrantType() {
        snowflakeOauthConnectionProperties.init();

        Assert.assertFalse(
                snowflakeOauthConnectionProperties
                .getForm(Form.MAIN)
                .getWidget(snowflakeOauthConnectionProperties.oauthUserName.getName())
                .isVisible());
        Assert.assertFalse(
                snowflakeOauthConnectionProperties
                .getForm(Form.MAIN)
                .getWidget(snowflakeOauthConnectionProperties.oauthPassword.getName())
                .isVisible());

        snowflakeOauthConnectionProperties.grantType.setValue(GrantType.PASSWORD);
        snowflakeOauthConnectionProperties.afterGrantType();

        Assert.assertTrue(
                snowflakeOauthConnectionProperties
                .getForm(Form.MAIN)
                .getWidget(snowflakeOauthConnectionProperties.oauthUserName.getName())
                .isVisible());
        Assert.assertTrue(
                snowflakeOauthConnectionProperties
                .getForm(Form.MAIN)
                .getWidget(snowflakeOauthConnectionProperties.oauthPassword.getName())
                .isVisible());
    }
}
