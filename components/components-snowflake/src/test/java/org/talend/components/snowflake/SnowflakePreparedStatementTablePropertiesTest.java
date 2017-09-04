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

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.snowflake.SnowflakePreparedStatementTableProperties.Type;
import org.talend.daikon.properties.presentation.Form;

public class SnowflakePreparedStatementTablePropertiesTest {

    private SnowflakePreparedStatementTableProperties properties;

    @Before
    public void setup() {
        properties = new SnowflakePreparedStatementTableProperties("preparedStatementTableProperties");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCheckSetupProperties() {
        Assert.assertTrue(properties.types.getPossibleValues().isEmpty());

        properties.setupProperties();

        List<Type> types = (List<SnowflakePreparedStatementTableProperties.Type>) properties.types.getPossibleValues();
        Assert.assertThat(types, Matchers.containsInAnyOrder(Type.values()));

    }

    @Test
    public void testCheckSetupLayout() {
        Assert.assertNull(properties.getForm(Form.MAIN));

        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);

        Assert.assertNotNull(main);
        Assert.assertNotNull(main.getWidget(properties.indexes));
        Assert.assertNotNull(main.getWidget(properties.types));
        Assert.assertNotNull(main.getWidget(properties.values));
    }
}
