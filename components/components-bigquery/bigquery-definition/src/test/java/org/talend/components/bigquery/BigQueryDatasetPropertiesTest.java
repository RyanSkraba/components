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

package org.talend.components.bigquery;

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
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class BigQueryDatasetPropertiesTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    BigQueryDatasetProperties properties;

    @Before
    public void reset() {
        properties = new BigQueryDatasetProperties("test");
        properties.init();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(properties, errorCollector);
    }

    @Test
    public void testVisible() throws Throwable {
        Form main = properties.getForm(Form.MAIN);
        assertTrue(main.getWidget(properties.bqDataset).isVisible());
        assertTrue(main.getWidget(properties.sourceType).isVisible());
        assertTrue(main.getWidget(properties.tableName).isVisible());
        assertTrue(main.getWidget(properties.query).isHidden());
        assertTrue(main.getWidget(properties.useLegacySql).isHidden());
        assertTrue(main.getWidget(properties.main).isVisible());

        properties.sourceType.setValue(BigQueryDatasetProperties.SourceType.QUERY);
        PropertiesDynamicMethodHelper.afterProperty(properties, properties.sourceType.getName());
        assertTrue(main.getWidget(properties.bqDataset).isVisible());
        assertTrue(main.getWidget(properties.sourceType).isVisible());
        assertTrue(main.getWidget(properties.tableName).isHidden());
        assertTrue(main.getWidget(properties.query).isVisible());
        assertTrue(main.getWidget(properties.useLegacySql).isVisible());
        assertTrue(main.getWidget(properties.main).isVisible());

    }

    /**
     * Checks {@link BigQueryDatasetProperties} sets correctly initial schema property
     */
    @Test
    public void testDefaultProperties() {
        assertEquals(BigQueryDatasetProperties.SourceType.TABLE_NAME, properties.sourceType.getValue());
        assertEquals(false, properties.useLegacySql.getValue());
    }

    /**
     * Checks {@link BigQueryDatasetProperties} sets correctly initial layout properties
     */
    @Test
    public void testSetupLayout() {
        Form main = properties.getForm(Form.MAIN);
        Collection<Widget> mainWidgets = main.getWidgets();

        List<String> ALL = Arrays.asList(properties.bqDataset.getName(), properties.sourceType.getName(),
                properties.tableName.getName(), properties.query.getName(), properties.useLegacySql.getName(),
                properties.main.getName());

        Assert.assertThat(main, notNullValue());
        Assert.assertThat(mainWidgets, hasSize(ALL.size()));

        for (String field : ALL) {
            Widget w = main.getWidget(field);
            Assert.assertThat(w, notNullValue());
        }
    }
}
