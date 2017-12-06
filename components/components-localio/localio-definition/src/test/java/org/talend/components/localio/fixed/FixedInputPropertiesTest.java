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
import org.talend.daikon.serialize.jsonschema.JsonSchemaUtil;

import java.util.Arrays;

/**
 * Unit tests for {@link FixedInputProperties}.
 */
public class FixedInputPropertiesTest {

    /**
     * Instance to test. A new instance is created for each test.
     */
    private FixedInputProperties properties = null;

    @Before
    public void setup() {
        properties = new FixedInputProperties("test");
        properties.init();
    }

     /**
     * Check the correct default values in the properties.
     */
     @Test
     public void testDefaultProperties() {
         assertThat(properties.repeat.getValue(), is(1));
         assertThat(properties.useOverrideValues.getValue(), is(false));
         assertThat(properties.overrideValuesAction.getValue(), is(FixedInputProperties.OverrideValuesAction.NONE));
         assertThat(properties.overrideValues.getValue(), is(""));
     }

    /**
     * Check the setup of the form layout.
     */
    @Test
    public void testSetupLayout() {
        properties.setupLayout();
        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());
        assertThat(main.getWidgets(), hasSize(3));
        // Check the widgets.
        Widget w = main.getWidget(properties.repeat.getName());
        assertThat(w.getWidgetType(), is(Widget.DEFAULT_WIDGET_TYPE));
        assertThat(w.isVisible(), is(true));
        w = main.getWidget(properties.overrideValuesAction.getName());
        assertThat(w.getWidgetType(), is(Widget.DEFAULT_WIDGET_TYPE));
        assertThat(w.isVisible(), is(false));
        w = main.getWidget(properties.overrideValues.getName());
        assertThat(w.getWidgetType(), is(Widget.CODE_WIDGET_TYPE));
        assertThat(w.isVisible(), is(false));
    }

    /**
     * Check the changes to the form.
     */
    @Test
    public void testRefreshLayout() {
        properties.setupLayout();
        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());
        assertThat(main.getWidgets(), hasSize(3));

        // Turn the override values on.
        properties.useOverrideValues.setValue(true);
        properties.refreshLayout(main);
        assertThat(main.getWidget(properties.repeat.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.overrideValuesAction.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.overrideValues.getName()).isVisible(), is(false));

        // Check the different values for APPEND and REPLACE and NONE
        properties.overrideValuesAction.setValue(FixedInputProperties.OverrideValuesAction.APPEND);
        properties.refreshLayout(main);
        assertThat(main.getWidget(properties.repeat.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.overrideValuesAction.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.overrideValues.getName()).isVisible(), is(true));

        properties.overrideValuesAction.setValue(FixedInputProperties.OverrideValuesAction.REPLACE);
        properties.refreshLayout(main);
        assertThat(main.getWidget(properties.repeat.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.overrideValuesAction.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.overrideValues.getName()).isVisible(), is(true));

        properties.overrideValuesAction.setValue(FixedInputProperties.OverrideValuesAction.NONE);
        properties.refreshLayout(main);
        assertThat(main.getWidget(properties.repeat.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.overrideValuesAction.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.overrideValues.getName()).isVisible(), is(false));

        // Turn the override values off.
        properties.useOverrideValues.setValue(false);
        properties.refreshLayout(main);
        assertThat(main.getWidget(properties.repeat.getName()).isVisible(), is(true));
        assertThat(main.getWidget(properties.overrideValuesAction.getName()).isVisible(), is(false));
        assertThat(main.getWidget(properties.overrideValues.getName()).isVisible(), is(false));
    }

    @Test
    public void testJsonSchemaSerialization() throws JSONException {
        String jsonString = JsonSchemaUtil.toJson(properties, MAIN, FixedInputDefinition.NAME);
        assertThat(jsonString, notNullValue());

        JSONObject node = new JSONObject(jsonString);
        assertThat(node, notNullValue());
        JSONAssert.assertEquals("{\"jsonSchema\": {},\"uiSchema\": {},\"properties\": {}} ", node, false);

        // Check the properties.
        JSONObject jsonProps = node.getJSONObject("properties");
        JSONAssert.assertEquals("{\"@definitionName\": \"FixedInput\",\"outgoing\": {},\"repeat\": 1} ", jsonProps, false);
    }

}