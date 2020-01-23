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

package org.talend.components.simplefileio.s3;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.simplefileio.ExcelFormat;
import org.talend.components.simplefileio.SimpleFileIODatasetDefinition;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties.FieldDelimiterType;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties.RecordDelimiterType;
import org.talend.components.simplefileio.local.EncodingType;
import org.talend.components.simplefileio.SimpleFileIOFormat;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.serialize.jsonschema.JsonSchemaUtil;

public class S3DatasetPropertiesTest {

    /**
     * Useful constant listing all of the fields in the properties.
     */
    public static final Iterable<String> ALL = Arrays.asList("format", "recordDelimiter", "fieldDelimiter");

    /**
     * Instance to test. A new instance is created for each test.
     */
    S3DatasetProperties properties = null;

    @Before
    public void setup() {
        properties = new S3DatasetProperties("test");
        S3DatastoreProperties datastoreProperties = new S3DatastoreProperties("test");
        datastoreProperties.init();
        
        //set a fake one to avoid to trigger the runtime which should not run here
        properties.bucket.setPossibleValues("bucket1");
        
        properties.setDatastoreProperties(datastoreProperties);
        properties.init();
    }

    /**
     * Check the correct default values in the properties.
     */
    @Test
    public void testDefaultProperties() {
        assertThat(properties.format.getValue(), is(SimpleFileIOFormat.CSV));
        assertThat(properties.recordDelimiter.getValue(), is(RecordDelimiterType.LF));
        assertThat(properties.specificRecordDelimiter.getValue(), is("\\n"));
        assertThat(properties.fieldDelimiter.getValue(), is(FieldDelimiterType.SEMICOLON));
        assertThat(properties.specificFieldDelimiter.getValue(), is(";"));
        
        assertThat(properties.encoding.getValue(), is(EncodingType.UTF8));
        assertThat(properties.specificEncoding.getValue(), is(""));
        assertThat(properties.setHeaderLine.getValue(), is(false));
        assertThat(properties.headerLine.getValue(), is(1));
        assertThat(properties.textEnclosureCharacter.getValue(), is(""));
        assertThat(properties.escapeCharacter.getValue(), is(""));
        
        assertThat(properties.sheet.getValue(), is(""));
        assertThat(properties.setFooterLine.getValue(), is(false));
        assertThat(properties.footerLine.getValue(), is(IsNull.nullValue()));

        String x = JsonSchemaUtil.toJson(properties, Form.MAIN, SimpleFileIODatasetDefinition.NAME);
    }

    /**
     * Check the setup of the form layout.
     */
    @Test
    public void testSetupLayout() {
        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());

        assertThat(main.getWidgets(), hasSize(19));

        for (String field : ALL) {
            Widget w = main.getWidget(field);
            assertThat(w, notNullValue());
        }

        assertThat(properties.bucket.isRequired(), is(true));
        assertThat(properties.object.isRequired(), is(true));
        assertThat(main.getWidget("bucket").getWidgetType(), is(Widget.DATALIST_WIDGET_TYPE));
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

            switch (format) {
            case CSV:
                assertThat(main.getWidget("recordDelimiter").isVisible(), is(true));
                assertThat(main.getWidget("specificRecordDelimiter").isVisible(), is(false));
                assertThat(main.getWidget("fieldDelimiter").isVisible(), is(true));
                assertThat(main.getWidget("specificFieldDelimiter").isVisible(), is(false));
                
                assertThat(main.getWidget("encoding").isVisible(), is(true));
                assertThat(main.getWidget("specificEncoding").isVisible(), is(false));
                assertThat(main.getWidget("setHeaderLine").isVisible(), is(true));
                assertThat(main.getWidget("headerLine").isVisible(), is(false));
                assertThat(main.getWidget("textEnclosureCharacter").isVisible(), is(true));
                assertThat(main.getWidget("escapeCharacter").isVisible(), is(true));
                
                properties.setHeaderLine.setValue(true);
                properties.afterSetHeaderLine();
                assertThat(main.getWidget("headerLine").isVisible(), is(true));
                
                properties.encoding.setValue(EncodingType.OTHER);
                properties.afterEncoding();
                assertThat(main.getWidget("specificEncoding").isVisible(), is(true));
                
                break;
            case AVRO:
            case PARQUET:
                assertThat(main.getWidget("recordDelimiter").isVisible(), is(false));
                assertThat(main.getWidget("fieldDelimiter").isVisible(), is(false));
                
                assertThat(main.getWidget("encoding").isVisible(), is(false));
                assertThat(main.getWidget("specificEncoding").isVisible(), is(false));
                assertThat(main.getWidget("setHeaderLine").isVisible(), is(false));
                assertThat(main.getWidget("headerLine").isVisible(), is(false));
                assertThat(main.getWidget("textEnclosureCharacter").isVisible(), is(false));
                assertThat(main.getWidget("escapeCharacter").isVisible(), is(false));
                break;
            case EXCEL:
                //reset back as above CSV change it
                properties.setHeaderLine.setValue(false);
                properties.afterSetHeaderLine();
                properties.encoding.setValue(EncodingType.UTF8);
                properties.afterEncoding();
                
                assertThat(main.getWidget("recordDelimiter").isVisible(), is(false));
                assertThat(main.getWidget("specificRecordDelimiter").isVisible(), is(false));
                assertThat(main.getWidget("fieldDelimiter").isVisible(), is(false));
                assertThat(main.getWidget("specificFieldDelimiter").isVisible(), is(false));
                
                assertThat(main.getWidget("encoding").isVisible(), is(false));
                assertThat(main.getWidget("specificEncoding").isVisible(), is(false));
                assertThat(main.getWidget("setHeaderLine").isVisible(), is(true));
                assertThat(main.getWidget("headerLine").isVisible(), is(false));
                assertThat(main.getWidget("textEnclosureCharacter").isVisible(), is(false));
                assertThat(main.getWidget("escapeCharacter").isVisible(), is(false));
                
                assertThat(main.getWidget("sheet").isVisible(), is(true));
                assertThat(main.getWidget("setFooterLine").isVisible(), is(true));
                assertThat(main.getWidget("footerLine").isVisible(), is(false));
                
                properties.setHeaderLine.setValue(true);
                properties.afterSetHeaderLine();
                assertThat(main.getWidget("headerLine").isVisible(), is(true));
                
                properties.setFooterLine.setValue(true);
                properties.afterSetFooterLine();
                assertThat(main.getWidget("footerLine").isVisible(), is(true));
                
                properties.excelFormat.setValue(ExcelFormat.HTML);
                properties.encoding.setValue(EncodingType.OTHER);
                properties.afterEncoding();
                assertThat(main.getWidget("specificEncoding").isVisible(), is(true));
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
            case EXCEL:
                break;
            default:
                throw new RuntimeException("Missing test case for " + format);
            }

        }
    }
}
