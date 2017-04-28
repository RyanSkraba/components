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

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;

/**
 * Unit tests for {@link S3DatastoreProperties}.
 */
public class S3DatastorePropertiesTest {

    /**
     * Useful constant listing all of the fields in the properties.
     */
    public static final Iterable<String> ALL = Arrays.asList("specifyCredentials", "accessKey", "secretKey");
    /**
     * Instance to test. A new instance is created for each test.
     */
    S3DatastoreProperties properties = null;

    @Before
    public void setup() {
        properties = new S3DatastoreProperties("test");
        properties.init();
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
        // All of the fields are visible.
        for (String field : ALL) {
            assertThat(main.getWidget(field).isVisible(), is(true));
        }

        properties.specifyCredentials.setValue(false);
        properties.afterSpecifyCredentials();

        assertThat(main.getWidget("specifyCredentials").isVisible(), is(true));
        assertThat(main.getWidget("accessKey").isVisible(), is(false));
        assertThat(main.getWidget("secretKey").isVisible(), is(false));

    }
}
