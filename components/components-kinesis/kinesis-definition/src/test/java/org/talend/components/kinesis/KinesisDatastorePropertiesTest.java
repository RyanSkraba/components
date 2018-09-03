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

public class KinesisDatastorePropertiesTest {

    /**
     * Useful constant listing all of the fields in the properties.
     */
    public static final List<String> ALL = Arrays.asList("specifyCredentials", "accessKey", "secretKey",
            "specifyEndpoint", "endpoint", "specifySTS", "roleArn", "roleSessionName", "specifyRoleExternalId",
            "roleExternalId", "specifySTSEndpoint", "stsEndpoint");

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    KinesisDatastoreProperties properties;

    @Before
    public void reset() {
        properties = new KinesisDatastoreProperties("test");
        properties.init();
    }

    @Test
    public void testI18N() {
        ComponentTestUtils.checkAllI18N(properties, errorCollector);
    }

    /**
     * Checks {@link KinesisDatastoreProperties} sets correctly initial schema
     * property
     */
    @Test
    public void testDefaultProperties() {
        assertTrue(properties.specifyCredentials.getValue());
        assertFalse(properties.specifyEndpoint.getValue());
        assertFalse(properties.specifySTS.getValue());
        assertFalse(properties.specifyRoleExternalId.getValue());
        assertFalse(properties.specifySTSEndpoint.getValue());
        assertEquals("kinesis.us-east-1.amazonaws.com", properties.endpoint.getValue());
        assertEquals("sts.amazonaws.com", properties.stsEndpoint.getValue());
    }

    /**
     * Checks {@link KinesisDatastoreProperties} sets correctly initial layout
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

        assertTrue(main.getWidget("specifyCredentials").isVisible());
        assertTrue(main.getWidget("accessKey").isVisible());
        assertTrue(main.getWidget("secretKey").isVisible());
        assertFalse(properties.specifyCredentials.isRequired());
        assertTrue(properties.accessKey.isRequired());
        assertTrue(properties.secretKey.isRequired());
        assertThat(main.getWidget("secretKey").getWidgetType(), is(Widget.HIDDEN_TEXT_WIDGET_TYPE));

        assertTrue(main.getWidget("specifyEndpoint").isVisible());
        assertFalse(main.getWidget("endpoint").isVisible());
        assertFalse(properties.specifyEndpoint.isRequired());
        assertFalse(properties.endpoint.isRequired());

        assertTrue(main.getWidget("specifySTS").isVisible());
        assertFalse(main.getWidget("roleArn").isVisible());
        assertFalse(main.getWidget("roleSessionName").isVisible());
        assertFalse(main.getWidget("specifyRoleExternalId").isVisible());
        assertFalse(main.getWidget("roleExternalId").isVisible());
        assertFalse(main.getWidget("specifySTSEndpoint").isVisible());
        assertFalse(main.getWidget("stsEndpoint").isVisible());
        assertFalse(properties.specifySTS.isRequired());
        assertFalse(properties.roleArn.isRequired());
        assertFalse(properties.roleSessionName.isRequired());
        assertFalse(properties.specifyRoleExternalId.isRequired());
        assertFalse(properties.roleExternalId.isRequired());
        assertFalse(properties.specifySTSEndpoint.isRequired());
        assertFalse(properties.stsEndpoint.isRequired());


    }

    /**
     * Checks {@link KinesisDatastoreProperties} sets correctly layout after refresh
     * properties
     */
    @Test
    public void testRefreshLayout() {
        Form main = properties.getForm(Form.MAIN);

        // set false to specify credentials
        properties.specifyCredentials.setValue(false);
        properties.afterSpecifyCredentials();

        assertTrue(main.getWidget("specifyCredentials").isVisible());
        assertFalse(main.getWidget("accessKey").isVisible());
        assertFalse(main.getWidget("secretKey").isVisible());
        assertFalse(properties.specifyCredentials.isRequired());
        assertFalse(properties.accessKey.isRequired());
        assertFalse(properties.secretKey.isRequired());

        // set back true to specify credentials
        properties.specifyCredentials.setValue(true);
        properties.afterSpecifyCredentials();
        testSetupLayout();

        // set true to specify endpoint
        properties.specifyEndpoint.setValue(true);
        properties.afterSpecifyEndpoint();

        assertTrue(main.getWidget("specifyEndpoint").isVisible());
        assertTrue(main.getWidget("endpoint").isVisible());
        assertFalse(properties.specifyEndpoint.isRequired());
        assertTrue(properties.endpoint.isRequired());

        // set back false to specify endpoint
        properties.specifyEndpoint.setValue(false);
        properties.afterSpecifyEndpoint();
        testSetupLayout();

        // set true to specify STS
        properties.specifySTS.setValue(true);
        properties.afterSpecifySTS();

        assertTrue(main.getWidget("specifySTS").isVisible());
        assertTrue(main.getWidget("roleArn").isVisible());
        assertTrue(main.getWidget("roleSessionName").isVisible());
        assertTrue(main.getWidget("specifyRoleExternalId").isVisible());
        assertFalse(main.getWidget("roleExternalId").isVisible());
        assertTrue(main.getWidget("specifySTSEndpoint").isVisible());
        assertFalse(main.getWidget("stsEndpoint").isVisible());
        assertFalse(properties.specifySTS.isRequired());
        assertTrue(properties.roleArn.isRequired());
        assertTrue(properties.roleSessionName.isRequired());
        assertTrue(properties.specifyRoleExternalId.isRequired());
        assertFalse(properties.roleExternalId.isRequired());
        assertTrue(properties.specifySTSEndpoint.isRequired());
        assertFalse(properties.stsEndpoint.isRequired());

        properties.specifyRoleExternalId.setValue(true);
        properties.afterSpecifyRoleExternalId();

        assertTrue(main.getWidget("specifyRoleExternalId").isVisible());
        assertTrue(main.getWidget("roleExternalId").isVisible());
        assertTrue(properties.specifyRoleExternalId.isRequired());
        assertTrue(properties.roleExternalId.isRequired());

        properties.specifySTSEndpoint.setValue(true);
        properties.afterSpecifySTSEndpoint();

        assertTrue(main.getWidget("specifySTSEndpoint").isVisible());
        assertTrue(main.getWidget("stsEndpoint").isVisible());
        assertTrue(properties.specifySTSEndpoint.isRequired());
        assertTrue(properties.stsEndpoint.isRequired());

        // set back false to specify STS
        properties.specifySTS.setValue(false);
        properties.afterSpecifySTS();
        testSetupLayout();


    }
}
