// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.MarketoTestBase;
import org.talend.components.marketo.data.MarketoDatasetProperties.Operation;
import org.talend.components.marketo.runtime.MarketoSourceOrSinkSchemaProvider;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;

public class MarketoDatasetPropertiesTest extends MarketoTestBase {

    private MarketoDatasetProperties properties;

    private MarketoDatastoreProperties datastoreProperties;

    @Before
    public void setUp() throws Exception {
        properties = new MarketoDatasetProperties("test");
        properties.setupProperties();
        properties.setupLayout();
        datastoreProperties = new MarketoDatastoreProperties("test");
        datastoreProperties.init();
    }

    @Test
    public void testSetGetDatastoreProperties() throws Exception {
        assertNull(properties.getDatastoreProperties());
        properties.setDatastoreProperties(datastoreProperties);
        assertEquals(datastoreProperties, properties.getDatastoreProperties());
    }

    @Test
    public void testGetSchema() throws Exception {
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads(), properties.getSchema());
        properties.operation.setValue(Operation.getLeadChanges);
        properties.afterOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadChanges(), properties.getSchema());
        properties.operation.setValue(Operation.getLeadActivities);
        properties.afterOperation();
        assertEquals(MarketoConstants.getRESTSchemaForGetLeadActivity(), properties.getSchema());
        properties.operation.setValue(Operation.getCustomObjects);

        try (SandboxedInstanceTestFixture sandboxedInstanceTestFixture = new SandboxedInstanceTestFixture()) {
            sandboxedInstanceTestFixture.setUp();
            properties.afterOperation();
            assertEquals(MarketoConstants.getCustomObjectRecordSchema(), properties.getSchema());
            properties.customObjectName.setValue("smartphone_c");
            properties.afterOperation();
            assertEquals(MarketoConstants.getCustomObjectRecordSchema(), properties.getSchema());
        }
    }

    @Test
    public void testRefreshLayout() throws Exception {
        Form f = properties.getForm(Form.MAIN);
        properties.refreshLayout(f);
        assertFalse(f.getWidget(properties.customObjectName.getName()).isVisible());
        assertFalse(f.getWidget(properties.filterType.getName()).isVisible());
        assertFalse(f.getWidget(properties.filterValue.getName()).isVisible());
        properties.operation.setValue(Operation.getLeadChanges);
        properties.afterOperation();
        assertFalse(f.getWidget(properties.customObjectName.getName()).isVisible());
        properties.operation.setValue(Operation.getLeadActivities);
        properties.afterOperation();
        assertFalse(f.getWidget(properties.customObjectName.getName()).isVisible());
        properties.operation.setValue(Operation.getCustomObjects);
        try (SandboxedInstanceTestFixture sandboxedInstanceTestFixture = new SandboxedInstanceTestFixture()) {
            sandboxedInstanceTestFixture.setUp();
            properties.afterOperation();
            assertTrue(f.getWidget(properties.customObjectName.getName()).isVisible());
            assertTrue(f.getWidget(properties.filterType.getName()).isVisible());
            assertTrue(f.getWidget(properties.filterValue.getName()).isVisible());
        }
    }

    @Test
    public void testRetrieveCustomObjectList() throws Exception {
        try (SandboxedInstanceTestFixture sandboxedInstanceTestFixture = new SandboxedInstanceTestFixture()) {
            sandboxedInstanceTestFixture.setUp();
            when(((MarketoSourceOrSinkSchemaProvider) sandboxedInstanceTestFixture.runtimeSourceOrSink).getSchemaNames(null))
                    .thenThrow(new RuntimeException());
            properties.operation.setValue(Operation.getCustomObjects);
            properties.afterOperation();
            assertEquals(MarketoConstants.getCustomObjectRecordSchema(), properties.getSchema());
            when(((MarketoSourceOrSinkSchemaProvider) sandboxedInstanceTestFixture.runtimeSourceOrSink).validate(null))
                    .thenReturn(new ValidationResult(Result.ERROR, "Not validated"));
            properties.afterOperation();
            assertEquals(MarketoConstants.getCustomObjectRecordSchema(), properties.getSchema());
            properties.customObjectName.setValue("smartphone_c");
            properties.afterCustomObjectName();
            assertEquals(MarketoConstants.getCustomObjectRecordSchema(), properties.getSchema());
        }
    }

}
