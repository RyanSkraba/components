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

package org.talend.components.salesforce;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.daikon.properties.presentation.Form;

/**
 *
 */
public class SalesforceGetDeletedUpdatedPropertiesTest extends SalesforceTestBase {

    private SalesforceGetDeletedUpdatedProperties properties;

    @Before
    public void setUp() {
        properties = new SalesforceGetDeletedUpdatedProperties("root");
    }

    @Test
    public void testSetupProperties() {
        properties.setupProperties();

        assertNotNull(properties.module);
        assertNotNull(properties.module.connection);
    }

    @Test
    public void testSetupLayout() {
        properties.init();

        Form mainForm = properties.getForm(Form.MAIN);
        assertNotNull(mainForm.getWidget(properties.getConnectionProperties().getName()));
        assertNotNull(mainForm.getChildForm(properties.getConnectionProperties().getName())
                .getChildForm(properties.getConnectionProperties().getName()));
        assertNotNull(mainForm.getWidget(properties.startDate.getName()));
        assertNotNull(mainForm.getWidget(properties.endDate.getName()));

        Form advForm = properties.getForm(Form.ADVANCED);
        assertNotNull(advForm.getWidget(properties.getConnectionProperties().getName()));
        assertNotNull(advForm.getChildForm(properties.getConnectionProperties().getName()).getWidget(
                properties.getConnectionProperties().bulkConnection.getName()));
        assertNotNull(advForm.getChildForm(properties.getConnectionProperties().getName()).getWidget(
                properties.getConnectionProperties().httpTraceMessage.getName()));
    }

    @Test
    public void testRefreshLayout() {
        properties.init();

        properties.refreshLayout(properties.getForm(Form.MAIN));
        Form mainForm = properties.getForm(Form.MAIN);
        assertTrue(mainForm.getChildForm(properties.getConnectionProperties().getName())
                .getChildForm(properties.getConnectionProperties().getName())
                .getWidget(properties.getConnectionProperties().loginType.getName()).isVisible());

        properties.refreshLayout(properties.getForm(Form.ADVANCED));
        Form advForm = properties.getForm(Form.ADVANCED);
        assertFalse(advForm.getChildForm(properties.getConnectionProperties().getName()).getWidget(
                properties.getConnectionProperties().bulkConnection.getName()).isVisible());
        assertFalse(advForm.getChildForm(properties.getConnectionProperties().getName()).getWidget(
                properties.getConnectionProperties().httpTraceMessage.getName()).isVisible());
    }

    @Test
    public void testPropertiesConnectors() {

        assertThat(properties.getPossibleConnectors(true), containsInAnyOrder(
                (Connector) new PropertyPathConnector(Connector.MAIN_NAME, "schema")));

        assertThat(properties.getPossibleConnectors(false), empty());
    }

}
