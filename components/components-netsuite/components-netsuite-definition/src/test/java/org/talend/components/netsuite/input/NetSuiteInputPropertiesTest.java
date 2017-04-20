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

package org.talend.components.netsuite.input;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
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
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;

/**
 *
 */
public class NetSuiteInputPropertiesTest extends NetSuitePropertiesTestBase {
    private DatasetMockTestFixture datasetMockTestFixture;
    private TestDataset dataset;
    private NetSuiteRuntime runtime;
    private NetSuiteDatasetRuntime datasetRuntime;

    private NetSuiteInputProperties properties;

    @Before
    public void setUp() throws Exception {
        datasetMockTestFixture = new DatasetMockTestFixture(createTestDataset1());
        datasetMockTestFixture.setUp();
        dataset = datasetMockTestFixture.getTestDataset();
        runtime = datasetMockTestFixture.getRuntime();
        datasetRuntime = datasetMockTestFixture.getDatasetRuntime();

        properties = new NetSuiteInputProperties("input");

        when(runtime.getDatasetRuntime(eq(properties.connection))).thenReturn(datasetRuntime);
    }

    @After
    public void tearDown() throws Exception {
        datasetMockTestFixture.tearDown();
    }

    @Test
    public void testSetupProperties() {
        properties.connection.setupProperties();
        properties.module.main.setupProperties();
        properties.module.setupProperties();
        properties.setupProperties();

        assertNotNull(properties.getConnectionProperties());
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() {
        properties.init();

        assertEquals(1, properties.getAllSchemaPropertiesConnectors(true).size());
        assertEquals(0, properties.getAllSchemaPropertiesConnectors(false).size());
        PropertyPathConnector connector = properties.getAllSchemaPropertiesConnectors(true).iterator().next();
        assertThat(connector.getName(), is(Connector.MAIN_NAME));
        assertThat(connector.getPropertyPath(), is("module.main"));
    }

    @Test
    public void testSetupLayout() {
        properties.connection.setupLayout();
        properties.module.main.setupLayout();
        properties.module.setupLayout();
        properties.setupLayout();

        Form mainForm = properties.getForm(Form.MAIN);
        assertNotNull(mainForm);
        assertNotNull(mainForm.getWidget("connection"));
        assertNotNull(mainForm.getWidget("module"));
    }

    @Test
    public void testSelectSearchTarget() throws Exception {
        properties.init();

        // Before module select

        ValidationResult validationResult1 = properties.module.beforeModuleName();

        verify(datasetRuntime, times(1)).getSearchableTypes();

        assertNotNull(validationResult1);
        assertEquals(ValidationResult.OK.getStatus(), validationResult1.getStatus());

        assertEquals(dataset.getSearchableTypes().size(),
                properties.module.moduleName.getPossibleValues().size());

        // Select module

        properties.module.moduleName.setValue("Account");

        // After module select

        ValidationResult validationResult2 = properties.module.afterModuleName();

        verify(datasetRuntime, times(1)).getSearchFieldOperators();
        verify(datasetRuntime, times(1)).getSearchInfo(eq("Account"));
        verify(datasetRuntime, times(1)).getSchema(eq("Account"));

        assertNotNull(validationResult2);
        assertEquals(ValidationResult.OK.getStatus(), validationResult2.getStatus());

        assertEquals(dataset.getSearchInfoMap().get("Account").getFields().size(),
                properties.module.searchQuery.field.getPossibleValues().size());
        assertEquals(dataset.getSearchOperators().size(),
                properties.module.searchQuery.operator.getPossibleValues().size());
    }
}
