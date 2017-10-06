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

package org.talend.components.bigquery.output;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.bigquery.BigQueryDatasetProperties;
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class BigQueryOutputPropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    BigQueryOutputProperties properties;

    @Before
    public void reset() {
        properties = new BigQueryOutputProperties("test");
        properties.init();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(properties, errorCollector);
    }

    /**
     * Checks {@link BigQueryOutputProperties} sets correctly initial schema property
     */
    @Test
    public void testDefaultProperties() {
        assertEquals(BigQueryOutputProperties.TableOperation.NONE, properties.tableOperation.getValue());
        assertEquals(BigQueryOutputProperties.WriteOperation.APPEND, properties.writeOperation.getValue());
    }

    /**
     * Checks {@link BigQueryOutputProperties} sets correctly initial layout properties
     */
    @Test
    public void testSetupLayout() {
        Form main = properties.getForm(Form.MAIN);
        Collection<Widget> mainWidgets = main.getWidgets();

        List<String> ALL = Arrays.asList(properties.tableOperation.getName(), properties.writeOperation.getName());

        Assert.assertThat(main, notNullValue());
        Assert.assertThat(mainWidgets, hasSize(ALL.size()));

        for (String field : ALL) {
            Widget w = main.getWidget(field);
            Assert.assertThat(w, notNullValue());
        }
    }

    /**
     * Checks {@link BigQueryOutputProperties} sets correctly layout after refresh properties
     */
    @Test
    public void testRefreshLayout() throws Throwable {
        Form main = properties.getForm(Form.MAIN);
        assertTrue(main.getWidget(properties.tableOperation).isVisible());
        assertTrue(main.getWidget(properties.writeOperation).isVisible());

        properties.tableOperation.setValue(BigQueryOutputProperties.TableOperation.DROP_IF_EXISTS_AND_CREATE);
        PropertiesDynamicMethodHelper.afterProperty(properties, properties.tableOperation.getName());
        assertTrue(main.getWidget(properties.tableOperation).isVisible());
        assertTrue(main.getWidget(properties.writeOperation).isHidden());

        properties.tableOperation.setValue(BigQueryOutputProperties.TableOperation.NONE);
        PropertiesDynamicMethodHelper.afterProperty(properties, properties.tableOperation.getName());
        assertTrue(main.getWidget(properties.tableOperation).isVisible());
        assertTrue(main.getWidget(properties.writeOperation).isVisible());

        properties.tableOperation.setValue(BigQueryOutputProperties.TableOperation.TRUNCATE);
        PropertiesDynamicMethodHelper.afterProperty(properties, properties.tableOperation.getName());
        assertTrue(main.getWidget(properties.tableOperation).isVisible());
        assertTrue(main.getWidget(properties.writeOperation).isHidden());
    }
}
