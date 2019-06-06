// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.data;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.marketo.MarketoTestBase;
import org.talend.components.marketo.data.MarketoDatasetProperties.Operation;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.daikon.properties.presentation.Form;

public class MarketoInputPropertiesTest extends MarketoTestBase {

    private MarketoInputProperties properties;

    private MarketoDatasetProperties dataset;

    private MarketoDatastoreProperties datastore;

    @Before
    public void setUp() throws Exception {
        datastore = new MarketoDatastoreProperties("test");
        datastore.init();
        dataset = new MarketoDatasetProperties("test");
        dataset.setupProperties();
        dataset.setupLayout();
        dataset.setDatastoreProperties(datastore);
        properties = new MarketoInputProperties("test");
        properties.setupProperties();
        properties.setupLayout();
        properties.setDatasetProperties(dataset);
    }

    @Test
    public void testRefreshLayout() throws Exception {
        Form f = properties.getForm(Form.MAIN);
        properties.refreshLayout(f);
        assertTrue(f.getWidget(properties.leadKeyType.getName()).isVisible());
        assertTrue(f.getWidget(properties.leadKeyValue.getName()).isVisible());
        assertFalse(f.getWidget(properties.sinceDateTime.getName()).isVisible());
        assertFalse(f.getWidget(properties.fieldList.getName()).isVisible());
        properties.getDatasetProperties().operation.setValue(Operation.getLeadChanges);
        properties.getDatasetProperties().afterOperation();
        properties.refreshLayout(f);
        assertFalse(f.getWidget(properties.leadKeyType.getName()).isVisible());
        assertFalse(f.getWidget(properties.leadKeyValue.getName()).isVisible());
        assertTrue(f.getWidget(properties.sinceDateTime.getName()).isVisible());
        assertTrue(f.getWidget(properties.fieldList.getName()).isVisible());
        properties.getDatasetProperties().operation.setValue(Operation.getLeadActivities);
        properties.getDatasetProperties().afterOperation();
        properties.refreshLayout(f);
        assertFalse(f.getWidget(properties.leadKeyType.getName()).isVisible());
        assertFalse(f.getWidget(properties.leadKeyValue.getName()).isVisible());
        assertTrue(f.getWidget(properties.sinceDateTime.getName()).isVisible());
        assertFalse(f.getWidget(properties.fieldList.getName()).isVisible());
        properties.getDatasetProperties().operation.setValue(Operation.getCustomObjects);
        try (SandboxedInstanceTestFixture sandboxedInstanceTestFixture = new SandboxedInstanceTestFixture()) {
            sandboxedInstanceTestFixture.setUp();
            properties.getDatasetProperties().afterOperation();
            properties.getDatasetProperties().customObjectName.setValue("smartphone_c");
            properties.getDatasetProperties().afterCustomObjectName();
            properties.refreshLayout(f);
            assertFalse(f.getWidget(properties.leadKeyType.getName()).isVisible());
            assertFalse(f.getWidget(properties.leadKeyValue.getName()).isVisible());
            assertFalse(f.getWidget(properties.sinceDateTime.getName()).isVisible());
            assertFalse(f.getWidget(properties.fieldList.getName()).isVisible());
        }
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() throws Exception {
        assertEquals(Collections.emptySet(), properties.getAllSchemaPropertiesConnectors(false));
        assertEquals(Collections.singleton(new PropertyPathConnector(Connector.MAIN_NAME, "dataset.main")),
                properties.getAllSchemaPropertiesConnectors(true));
    }

    @Test
    public void testGetConnectionProperties() throws Exception {
        assertEquals(datastore, properties.getConnectionProperties());
    }
}
