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

package org.talend.components.netsuite.output;

import java.util.Collection;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.netsuite.NetSuiteDatasetRuntime;
import org.talend.components.netsuite.NetSuitePropertiesTestBase;
import org.talend.components.netsuite.NetSuiteRuntime;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;

/**
 *
 */
public class NetSuiteOutputPropertiesTest extends NetSuitePropertiesTestBase {
    private DatasetMockTestFixture datasetMockTestFixture;
    private TestDataset dataset;
    private NetSuiteRuntime runtime;
    private NetSuiteDatasetRuntime datasetRuntime;

    private NetSuiteOutputProperties properties;

    @Before
    public void setUp() throws Exception {
        datasetMockTestFixture = new DatasetMockTestFixture(createTestDataset1());
        datasetMockTestFixture.setUp();
        dataset = datasetMockTestFixture.getTestDataset();
        runtime = datasetMockTestFixture.getRuntime();
        datasetRuntime = datasetMockTestFixture.getDatasetRuntime();

        properties = new NetSuiteOutputProperties("test");

        when(runtime.getDatasetRuntime(eq(properties.connection))).thenReturn(datasetRuntime);
    }

    @After
    public void tearDown() throws Exception {
        datasetMockTestFixture.tearDown();
    }

    @Test
    public void testSetupProperties() {
        properties.connection.setupProperties();
        properties.module.setupProperties();
        properties.setupProperties();

        assertEquals(OutputAction.ADD, properties.module.action.getValue());
        assertEquals(Boolean.FALSE, properties.module.useNativeUpsert.getValue());
        assertEquals((Integer) NetSuiteOutputProperties.DEFAULT_BATCH_SIZE, properties.batchSize.getValue());
        assertEquals(Boolean.TRUE, properties.dieOnError.getValue());

        assertNotNull(properties.getConnectionProperties());
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() {
        properties.setupProperties();

        assertEquals(1, properties.getAllSchemaPropertiesConnectors(false).size());

        Set<PropertyPathConnector> connectors = properties.getAllSchemaPropertiesConnectors(false);
        PropertyPathConnector mainConnector = getByName(connectors, Connector.MAIN_NAME);
        assertNotNull(mainConnector);
        assertThat(mainConnector.getPropertyPath(), is("module.main"));
    }

    @Test
    public void testGetAllSchemaPropertiesConnectorsForOutgoing() {
        properties.setupProperties();

        assertEquals(2, properties.getAllSchemaPropertiesConnectors(true).size());

        Set<PropertyPathConnector> connectors = properties.getAllSchemaPropertiesConnectors(true);

        PropertyPathConnector flowConnector = getByName(connectors, Connector.MAIN_NAME);
        assertNotNull(flowConnector);
        assertThat(flowConnector.getPropertyPath(), is("module.flowSchema"));

        PropertyPathConnector rejectConnector = getByName(connectors, Connector.REJECT_NAME);
        assertNotNull(rejectConnector);
        assertThat(rejectConnector.getPropertyPath(), is("module.rejectSchema"));
    }

    private <T extends NamedThing> T getByName(Collection<T> objects, String name) {
        for (T obj : objects) {
            if (name.equals(obj.getName())) {
                return obj;
            }
        }
        return null;
    }

    @Test
    public void testSetupLayout() {
        properties.connection.setupLayout();
        properties.module.main.setupLayout();
        properties.module.setupLayout();
        properties.setupLayout();

        Form mainForm = properties.getForm(Form.MAIN);
        assertNotNull(mainForm);
    }

    @Test
    public void testSelectTargetForUpdate() throws Exception {
        properties.init();

        // Before module select

        ValidationResult validationResult1 = properties.module.beforeModuleName();

        verify(datasetRuntime, times(1)).getRecordTypes();

        assertNotNull(validationResult1);
        assertEquals(ValidationResult.OK.getStatus(), validationResult1.getStatus());

        assertEquals(dataset.getRecordTypes().size(),
                properties.module.moduleName.getPossibleValues().size());

        // Select module

        properties.module.moduleName.setValue("Account");

        // After module select

        ValidationResult validationResult2 = properties.module.afterModuleName();

        verify(datasetRuntime, times(1)).getSchemaForUpdate(eq("Account"));

        assertNotNull(validationResult2);
        assertEquals(ValidationResult.OK.getStatus(), validationResult2.getStatus());

        // Select action

        properties.module.action.setValue(OutputAction.UPDATE);

        // After action select

        ValidationResult validationResult3 = properties.module.afterAction();

        verify(datasetRuntime, times(2)).getSchemaForUpdate(eq("Account"));

        assertNotNull(validationResult3);
        assertEquals(ValidationResult.OK.getStatus(), validationResult3.getStatus());
    }

    @Test
    public void testSelectTargetForDelete() throws Exception {
        properties.init();

        // Before module select

        ValidationResult validationResult1 = properties.module.beforeModuleName();

        verify(datasetRuntime, times(1)).getRecordTypes();

        assertNotNull(validationResult1);
        assertEquals(ValidationResult.OK.getStatus(), validationResult1.getStatus());

        assertEquals(dataset.getRecordTypes().size(),
                properties.module.moduleName.getPossibleValues().size());

        // Select module

        properties.module.moduleName.setValue("Account");

        // After module select

        ValidationResult validationResult2 = properties.module.afterModuleName();

        verify(datasetRuntime, times(1)).getSchemaForUpdate(eq("Account"));

        assertNotNull(validationResult2);
        assertEquals(ValidationResult.OK.getStatus(), validationResult2.getStatus());

        // Select action

        properties.module.action.setValue(OutputAction.DELETE);

        // After action select

        ValidationResult validationResult3 = properties.module.afterAction();

        verify(datasetRuntime, times(1)).getSchemaForDelete(eq("Account"));

        assertNotNull(validationResult3);
        assertEquals(ValidationResult.OK.getStatus(), validationResult3.getStatus());
    }
}
