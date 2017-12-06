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
package org.talend.components.localio.fixed;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.talend.daikon.properties.presentation.Form.MAIN;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.serialize.jsonschema.JsonSchemaUtil;

/**
 * Unit tests for {@link FixedDatasetProperties}.
 */
public class FixedDatasetPropertiesTest {

    public Property<FixedDatasetProperties.RecordFormat> format = PropertyFactory
            .newEnum("format", FixedDatasetProperties.RecordFormat.class).setRequired();

    public Property<FixedDatasetProperties.RecordDelimiterType> recordDelimiter = PropertyFactory
            .newEnum("recordDelimiter", FixedDatasetProperties.RecordDelimiterType.class)
            .setValue(FixedDatasetProperties.RecordDelimiterType.LF);

    public Property<String> specificRecordDelimiter = PropertyFactory.newString("specificRecordDelimiter", "\\n");

    public Property<FixedDatasetProperties.FieldDelimiterType> fieldDelimiter = PropertyFactory
            .newEnum("fieldDelimiter", FixedDatasetProperties.FieldDelimiterType.class)
            .setValue(FixedDatasetProperties.FieldDelimiterType.SEMICOLON);

    public Property<String> specificFieldDelimiter = PropertyFactory.newString("specificFieldDelimiter", ";");

    public Property<String> schema = PropertyFactory.newString("schema", "").setRequired();

    public Property<String> csvSchema = PropertyFactory.newString("csvSchema", "");

    public Property<String> values = PropertyFactory.newString("values", "");

    /**
     * Instance to test. A new instance is created for each test.
     */
    private FixedDatasetProperties properties = null;

    @Before
    public void setup() {
        properties = new FixedDatasetProperties("test");
        properties.init();
    }

    /**
     * Check the correct default values in the properties.
     */
    @Test
    public void testDefaultProperties() {
        assertThat(properties.format.getValue(), is(FixedDatasetProperties.RecordFormat.CSV));
        assertThat(properties.recordDelimiter.getValue(), is(FixedDatasetProperties.RecordDelimiterType.LF));
        assertThat(properties.specificRecordDelimiter.getValue(), is("\\n"));
        assertThat(properties.fieldDelimiter.getValue(), is(FixedDatasetProperties.FieldDelimiterType.SEMICOLON));
        assertThat(properties.specificFieldDelimiter.getValue(), is(";"));
        assertThat(properties.schema.getValue(), is(""));
        assertThat(properties.csvSchema.getValue(), is(""));
        assertThat(properties.values.getValue(), is(""));
    }

    /**
     * Check the setup of the form layout.
     */
    @Test
    public void testSetupLayout() {
        properties.setupLayout();
        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());
        assertThat(main.getWidgets(), hasSize(8));

        // Check the widgets.
        Widget w = main.getWidget(properties.format.getName());
        assertThat(w.getWidgetType(), is(Widget.DEFAULT_WIDGET_TYPE));
        assertThat(w.isVisible(), is(true));
        w = main.getWidget(properties.recordDelimiter.getName());
        assertThat(w.getWidgetType(), is(Widget.DEFAULT_WIDGET_TYPE));
        assertThat(w.isVisible(), is(true));
        w = main.getWidget(properties.specificRecordDelimiter.getName());
        assertThat(w.getWidgetType(), is(Widget.DEFAULT_WIDGET_TYPE));
        assertThat(w.isVisible(), is(false));
        w = main.getWidget(properties.fieldDelimiter.getName());
        assertThat(w.getWidgetType(), is(Widget.DEFAULT_WIDGET_TYPE));
        assertThat(w.isVisible(), is(true));
        w = main.getWidget(properties.specificFieldDelimiter.getName());
        assertThat(w.getWidgetType(), is(Widget.DEFAULT_WIDGET_TYPE));
        assertThat(w.isVisible(), is(false));
        w = main.getWidget(properties.schema.getName());
        assertThat(w.getWidgetType(), is(Widget.CODE_WIDGET_TYPE));
        assertThat(w.isVisible(), is(false));
        w = main.getWidget(properties.csvSchema.getName());
        assertThat(w.getWidgetType(), is(Widget.DEFAULT_WIDGET_TYPE));
        assertThat(w.isVisible(), is(true));
        w = main.getWidget(properties.values.getName());
        assertThat(w.getWidgetType(), is(Widget.CODE_WIDGET_TYPE));
        assertThat(w.isVisible(), is(true));
    }

    /**
     * Check the changes to the form.
     */
    @Test
    public void testRefreshLayout() {
        properties.setupLayout();
        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());
        assertThat(main.getWidgets(), hasSize(8));

        // Turn on specific delimiters on and off
        properties.recordDelimiter.setValue(FixedDatasetProperties.RecordDelimiterType.OTHER);
        properties.refreshLayout(main);
        assertThat(main.getWidget(properties.format.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.recordDelimiter.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.specificRecordDelimiter.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.fieldDelimiter.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.specificFieldDelimiter.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.schema.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.csvSchema.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.values.getName()).isVisible(), is(true));

        properties.fieldDelimiter.setValue(FixedDatasetProperties.FieldDelimiterType.OTHER);
        properties.refreshLayout(main);
        assertThat(main.getWidget(properties.format.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.recordDelimiter.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.specificRecordDelimiter.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.fieldDelimiter.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.specificFieldDelimiter.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.schema.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.csvSchema.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.values.getName()).isVisible(), is(true));

        properties.recordDelimiter.setValue(FixedDatasetProperties.RecordDelimiterType.CR);
        properties.fieldDelimiter.setValue(FixedDatasetProperties.FieldDelimiterType.COMMA);
        properties.refreshLayout(main);
        assertThat(main.getWidget(properties.format.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.recordDelimiter.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.specificRecordDelimiter.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.fieldDelimiter.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.specificFieldDelimiter.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.schema.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.csvSchema.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.values.getName()).isVisible(), is(true));

        // Turn on Avro format
        properties.format.setValue(FixedDatasetProperties.RecordFormat.AVRO);
        properties.refreshLayout(main);
        assertThat(main.getWidget(properties.format.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.recordDelimiter.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.specificRecordDelimiter.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.fieldDelimiter.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.specificFieldDelimiter.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.schema.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.csvSchema.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.values.getName()).isVisible(), is(true));

        // Turn on JSON format
        properties.format.setValue(FixedDatasetProperties.RecordFormat.JSON);
        properties.fieldDelimiter.setValue(FixedDatasetProperties.FieldDelimiterType.OTHER);
        properties.refreshLayout(main);
        assertThat(main.getWidget(properties.format.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.recordDelimiter.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.specificRecordDelimiter.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.fieldDelimiter.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.specificFieldDelimiter.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.schema.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.csvSchema.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.values.getName()).isVisible(), is(true));
    }

    @Test
    public void testJsonSchemaSerialization() throws JSONException {
        String jsonString = JsonSchemaUtil.toJson(properties, MAIN, FixedDatasetDefinition.NAME);
        assertThat(jsonString, notNullValue());

        JSONObject node = new JSONObject(jsonString);
        assertThat(node, notNullValue());
        JSONAssert.assertEquals("{\"jsonSchema\": {},\"uiSchema\": {},\"properties\": {}} ", node, false);

        // Check the properties.
        JSONObject jsonProps = node.getJSONObject("properties");
        JSONAssert.assertEquals("{\"@definitionName\": \"FixedDataset\"} ", jsonProps, false);
    }

}