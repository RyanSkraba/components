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

package org.talend.components.kinesis;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

public class KinesisDatasetPropertiesTest {

    /**
     * Useful constant listing all of the fields in the properties.
     */
    public static final List<String> ALL = Arrays.asList("region", "unknownRegion", "streamName", "valueFormat",
            "fieldDelimiter", "specificFieldDelimiter", "avroSchema");

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    KinesisDatasetProperties properties;

    @Before
    public void reset() {
        properties = new KinesisDatasetProperties("test");
        properties.init();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(properties, errorCollector);
    }

    /**
     * Checks {@link KinesisDatasetProperties} sets correctly initial schema
     * property
     */
    @Test
    public void testDefaultProperties() {
        assertEquals(properties.region.getValue(), KinesisRegion.DEFAULT);
        assertEquals(properties.unknownRegion.getValue(), KinesisRegion.DEFAULT.getValue());
        assertEquals(properties.valueFormat.getValue(), KinesisDatasetProperties.ValueFormat.CSV);
        assertEquals(properties.fieldDelimiter.getValue(), KinesisDatasetProperties.FieldDelimiterType.SEMICOLON);
        assertEquals(properties.specificFieldDelimiter.getValue(), ";");
    }

    /**
     * Checks {@link KinesisDatasetProperties} sets correctly initial layout
     * properties
     */
    @Test
    public void testSetupLayout() {
        Form main = properties.getForm(Form.MAIN);
        Collection<Widget> mainWidgets = main.getWidgets();
        assertThat(main, notNullValue());
        assertThat(main.getWidgets(), Matchers.<Widget> hasSize(ALL.size()));

        for (String field : ALL) {
            Widget w = main.getWidget(field);
            Assert.assertThat(w, notNullValue());
        }

        assertTrue(main.getWidget("region").isVisible());
        assertFalse(main.getWidget("unknownRegion").isVisible());
        assertTrue(properties.region.isRequired());
        assertTrue(properties.unknownRegion.isRequired());

        assertTrue(main.getWidget("streamName").isVisible());
        assertTrue(properties.streamName.isRequired());
        assertThat(main.getWidget("streamName").getWidgetType(), is(Widget.DATALIST_WIDGET_TYPE));

        assertTrue(main.getWidget("valueFormat").isVisible());
        assertTrue(main.getWidget("fieldDelimiter").isVisible());
        assertFalse(main.getWidget("specificFieldDelimiter").isVisible());
        assertFalse(main.getWidget("avroSchema").isVisible());
        assertTrue(properties.valueFormat.isRequired());
        assertTrue(properties.fieldDelimiter.isRequired());
        assertFalse(properties.specificFieldDelimiter.isRequired());
        assertFalse(properties.avroSchema.isRequired());
        assertThat(main.getWidget("avroSchema").getWidgetType(), is(Widget.CODE_WIDGET_TYPE));
        assertThat(main.getWidget("avroSchema").getConfigurationValue(Widget.CODE_SYNTAX_WIDGET_CONF).toString(),
                is("json"));
    }

    /**
     * Checks {@link KinesisDatasetProperties} sets correctly layout after refresh
     * properties
     */
    @Test
    public void testRefreshLayout() {
        Form main = properties.getForm(Form.MAIN);

        // set false to specify credentials
        properties.region.setValue(KinesisRegion.OTHER);
        // properties.afterRegion(); can't call it as it contains runtime invoke
        properties.refreshLayout(main);

        assertTrue(main.getWidget("region").isVisible());
        assertTrue(main.getWidget("unknownRegion").isVisible());
        assertTrue(properties.region.isRequired());
        assertTrue(properties.unknownRegion.isRequired());

        // set back true to specify credentials
        properties.region.setValue(KinesisRegion.DEFAULT);
        // properties.afterRegion(); can't call it as it contains runtime invoke
        properties.refreshLayout(main);
        testSetupLayout();

        // set true to specify STS
        properties.valueFormat.setValue(KinesisDatasetProperties.ValueFormat.AVRO);
        properties.afterValueFormat();

        assertTrue(main.getWidget("valueFormat").isVisible());
        assertFalse(main.getWidget("fieldDelimiter").isVisible());
        assertFalse(main.getWidget("specificFieldDelimiter").isVisible());
        assertTrue(main.getWidget("avroSchema").isVisible());
        assertTrue(properties.valueFormat.isRequired());
        assertFalse(properties.fieldDelimiter.isRequired());
        assertFalse(properties.specificFieldDelimiter.isRequired());
        assertTrue(properties.avroSchema.isRequired());

        // set back false to specify STS
        properties.valueFormat.setValue(KinesisDatasetProperties.ValueFormat.CSV);
        properties.afterValueFormat();
        testSetupLayout();

        // set Other to fieldDelimiter
        properties.fieldDelimiter.setValue(KinesisDatasetProperties.FieldDelimiterType.OTHER);
        properties.afterFieldDelimiter();

        assertTrue(main.getWidget("valueFormat").isVisible());
        assertTrue(main.getWidget("fieldDelimiter").isVisible());
        assertTrue(main.getWidget("specificFieldDelimiter").isVisible());
        assertFalse(main.getWidget("avroSchema").isVisible());
        assertTrue(properties.valueFormat.isRequired());
        assertTrue(properties.fieldDelimiter.isRequired());
        assertTrue(properties.specificFieldDelimiter.isRequired());
        assertFalse(properties.avroSchema.isRequired());

        // set back build-in value to fieldDelimiter
        properties.fieldDelimiter.setValue(KinesisDatasetProperties.FieldDelimiterType.SEMICOLON);
        properties.afterFieldDelimiter();
        testSetupLayout();
    }
}
