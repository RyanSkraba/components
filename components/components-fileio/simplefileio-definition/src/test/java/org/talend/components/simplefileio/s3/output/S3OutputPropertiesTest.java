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

package org.talend.components.simplefileio.s3.output;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.simplefileio.s3.S3DatasetProperties;
import org.talend.components.simplefileio.s3.S3DatastoreProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.serialize.jsonschema.JsonSchemaUtil;

/**
 * Unit tests for {@link S3OutputProperties}.
 */
public class S3OutputPropertiesTest {

    /**
     * Useful constant listing all of the fields in the properties.
     */
    public static final Iterable<String> ALL = Arrays.asList("overwrite", "mergeOutput");

    /**
     * Instance to test. A new instance is created for each test.
     */
    S3OutputProperties properties = null;

    @Before
    public void setup() {
        properties = new S3OutputProperties("test");
        S3DatastoreProperties datastoreProperties = new S3DatastoreProperties("test");
        datastoreProperties.init();
        S3DatasetProperties datasetProperties = new S3DatasetProperties("test");
        datasetProperties.init();
        datasetProperties.setDatastoreProperties(datastoreProperties);
        properties.setDatasetProperties(datasetProperties);
        properties.init();
    }

    /**
     * Check the correct default values in the properties.
     */
    @Test
    public void testDefaultProperties() {
        assertThat(properties.mergeOutput.getValue(), is(false));

        String x = JsonSchemaUtil.toJson(properties, Form.MAIN, S3OutputDefinition.NAME);
    }

    /**
     * Check the setup of the form layout.
     */
    @Test
    public void testSetupLayout() {
        properties.setupLayout();

        Form main = properties.getForm(Form.MAIN);
        assertThat(main, notNullValue());
        assertThat(main.getWidgets(), hasSize(2));

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

    }

}
