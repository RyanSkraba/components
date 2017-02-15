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

package org.talend.components.simplefileio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties.FieldDelimiterType;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties.RecordDelimiterType;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.serialize.jsonschema.JsonSchemaUtil;

public class SimpleFileIODatasetPropertiesTest {

    /**
     * Useful constant listing all of the fields in the properties.
     */
    public static final Iterable<String> ALL = Arrays.asList("format", "path", "recordDelimiter", "fieldDelimiter");

    /**
     * Instance to test. A new instance is created for each test.
     */
    SimpleFileIODatasetProperties properties = null;

    @Before
    public void setup() {
        properties = new SimpleFileIODatasetProperties("test");
        SimpleFileIODatastoreProperties datastoreProperties = new SimpleFileIODatastoreProperties("test");
        datastoreProperties.init();
        properties.setDatastoreProperties(datastoreProperties);
        properties.init();
    }

    /**
     * Check the correct default values in the properties.
     */
    @Test
    public void testDefaultProperties() {
        assertThat(properties.format.getValue(), is(SimpleFileIOFormat.CSV));
        assertThat(properties.path.getValue(), is(""));
        assertThat(properties.recordDelimiter.getValue(), is(RecordDelimiterType.LF));
        assertThat(properties.specificRecordDelimiter.getValue(), is("\\n"));
        assertThat(properties.fieldDelimiter.getValue(), is(FieldDelimiterType.SEMICOLON));
        assertThat(properties.specificFieldDelimiter.getValue(), is(";"));

        properties.getDatastoreProperties().userName.setValue("ryan");
        String x = JsonSchemaUtil.toJson(properties, Form.MAIN, SimpleFileIODatasetDefinition.NAME);
        System.out.println(x);
    }

    /**
     * Check the setup of the form layout.
     */
    @Test
    public void testSetupLayout() {
        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());
        assertThat(main.getWidgets(), hasSize(6));

        for (String field : ALL) {
            Widget w = main.getWidget(field);
            assertThat(w, notNullValue());
        }
    }

    /**
     * Checks {@link Properties#refreshLayout(Form)}
     */
    @Test
    public void testRefreshLayout() {
        Form main = properties.getForm(Form.MAIN);
        properties.refreshLayout(main);

        for (String field : ALL) {
            Widget w = main.getWidget(field);
            assertThat(w.isVisible(), is(true));
        }

        // Check which properties are visible when the format is changed.
        for (SimpleFileIOFormat format : SimpleFileIOFormat.values()) {
            properties.format.setValue(format);
            properties.afterFormat();

            // Always visible.
            assertThat(main.getWidget("format").isVisible(), is(true));
            assertThat(main.getWidget("path").isVisible(), is(true));

            switch (format) {
            case CSV:
                assertThat(main.getWidget("recordDelimiter").isVisible(), is(true));
                assertThat(main.getWidget("specificRecordDelimiter").isVisible(), is(false));
                assertThat(main.getWidget("fieldDelimiter").isVisible(), is(true));
                assertThat(main.getWidget("specificFieldDelimiter").isVisible(), is(false));
                break;
            case AVRO:
            case PARQUET:
                assertThat(main.getWidget("recordDelimiter").isVisible(), is(false));
                assertThat(main.getWidget("fieldDelimiter").isVisible(), is(false));
                break;
            default:
                throw new RuntimeException("Missing test case for " + format);
            }

        }
    }

    /**
     * Checks {@link Properties#refreshLayout(Form)}
     */
    @Test
    public void testRefreshLayout2() {
        Form main = properties.getForm(Form.MAIN);
        properties.recordDelimiter.setValue(RecordDelimiterType.OTHER);
        properties.fieldDelimiter.setValue(FieldDelimiterType.COMMA);
        properties.refreshLayout(main);

        for (String field : ALL) {
            Widget w = main.getWidget(field);
            assertThat(w.isVisible(), is(true));
        }

        // Check which properties are visible when the format is changed.
        for (SimpleFileIOFormat format : SimpleFileIOFormat.values()) {
            properties.format.setValue(format);
            properties.afterFormat();

            // Always visible.
            assertThat(main.getWidget("format").isVisible(), is(true));
            assertThat(main.getWidget("path").isVisible(), is(true));

            switch (format) {
            case CSV:
                assertThat(main.getWidget("recordDelimiter").isVisible(), is(true));
                assertThat(main.getWidget("specificRecordDelimiter").isVisible(), is(true));
                assertThat(main.getWidget("fieldDelimiter").isVisible(), is(true));
                assertThat(main.getWidget("specificFieldDelimiter").isVisible(), is(false));
                break;
            case AVRO:
            case PARQUET:
                assertThat(main.getWidget("recordDelimiter").isVisible(), is(false));
                assertThat(main.getWidget("fieldDelimiter").isVisible(), is(false));
                break;
            default:
                throw new RuntimeException("Missing test case for " + format);
            }

        }
    }
}
