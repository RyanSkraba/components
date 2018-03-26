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

package org.talend.components.salesforce.dataset;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.dataprep.SalesforceInputProperties;
import org.talend.components.salesforce.datastore.SalesforceDatastoreProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.service.PropertiesService;
import org.talend.daikon.properties.service.PropertiesServiceImpl;

/**
 *
 */
public class SalesforceDatasetPropertiesTest extends SalesforceTestBase {

    private PropertiesService propertiesService;

    private SalesforceDatastoreProperties datastoreProperties;

    private SalesforceDatasetProperties properties;

    @Before
    public void setUp() {
        propertiesService = new PropertiesServiceImpl();

        datastoreProperties = new SalesforceDatastoreProperties("datastore");

        properties = spy(new SalesforceDatasetProperties("dataset"));
    }

    @Test
    public void testSetupProperties() {
        properties.setupProperties();

        assertEquals(SalesforceDatasetProperties.SourceType.SOQL_QUERY, properties.sourceType.getValue());
    }

    @Test
    public void testSetupLayout() {
        properties.init();

        Form mainForm = properties.getForm(Form.MAIN);
        assertNotNull(mainForm);
        assertNotNull(mainForm.getWidget(properties.sourceType.getName()));
        assertEquals(SalesforceDatasetProperties.SourceType.SOQL_QUERY, properties.sourceType.getValue());
        assertNotNull(mainForm.getWidget(properties.query.getName()));
        assertTrue(mainForm.getWidget(properties.query.getName()).isVisible());
        assertNotNull(mainForm.getWidget(properties.moduleName.getName()));
        assertFalse(mainForm.getWidget(properties.moduleName.getName()).isVisible());
        assertNotNull(mainForm.getWidget(properties.selectColumnIds.getName()));
        assertFalse(mainForm.getWidget(properties.selectColumnIds.getName()).isVisible());
        assertNotNull(mainForm.getWidget(properties.condition.getName()));
        assertFalse(mainForm.getWidget(properties.condition.getName()).isVisible());
    }

    @Test
    public void testSetDatastoreProperties() throws Exception {
        datastoreProperties.init();
        properties.init();
        properties.sourceType.setValue(SalesforceDatasetProperties.SourceType.MODULE_SELECTION);

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture(
                isA(SalesforceInputProperties.class), createDefaultTestDataset())) {
            testFixture.setUp();

            properties.setDatastoreProperties(datastoreProperties);

            assertThat((Iterable<String>) properties.moduleName.getPossibleValues(),
                    containsInAnyOrder("Account", "Customer"));

            assertThat(properties.selectColumnIds.getPossibleValues(), empty());

        }
    }

    @Test
    public void testSetDataStorePropertiesWithModuleNameSpecified() throws Exception {
        datastoreProperties.init();
        properties.init();

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture(
                isA(SalesforceInputProperties.class), createDefaultTestDataset())) {
            testFixture.setUp();

            properties.sourceType.setValue(SalesforceDatasetProperties.SourceType.MODULE_SELECTION);

            properties.moduleName.setValue("Account");

            properties.setDatastoreProperties(datastoreProperties);

            assertThat((Iterable<String>) properties.moduleName.getPossibleValues(),
                    containsInAnyOrder("Account", "Customer"));

            assertThat((Iterable<NamedThing>) properties.selectColumnIds.getPossibleValues(),
                    contains((NamedThing) new SimpleNamedThing("Id", "Id"), new SimpleNamedThing("Name", "Name")));

        }
    }

    @Test(expected = TalendRuntimeException.class)
    public void testSetDatastorePropertiesException() throws Exception {
        datastoreProperties.init();
        properties.init();

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture(
                isA(SalesforceInputProperties.class), createDefaultTestDataset())) {
            testFixture.setUp();

            properties.sourceType.setValue(SalesforceDatasetProperties.SourceType.MODULE_SELECTION);

            when(testFixture.runtimeSourceOrSink.getSchemaNames(any(RuntimeContainer.class)))
                    .thenThrow(new IOException("ERROR"));

            properties.setDatastoreProperties(datastoreProperties);

        }
    }

    @Test(expected = TalendRuntimeException.class)
    public void testSetDatastorePropertiesValidationResultError() throws Exception {
        datastoreProperties.init();
        properties.init();

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture(
                isA(SalesforceInputProperties.class), createDefaultTestDataset())) {
            testFixture.setUp();

            properties.sourceType.setValue(SalesforceDatasetProperties.SourceType.MODULE_SELECTION);

            when(testFixture.runtimeSourceOrSink.validate(any(RuntimeContainer.class)))
                    .thenReturn(new ValidationResult(ValidationResult.Result.ERROR, "CONNECTION ERROR"));

            properties.setDatastoreProperties(datastoreProperties);
        }
    }

    @Test
    public void testRefreshLayoutForSourceTypeModuleSelection() throws Exception {
        datastoreProperties.init();
        properties.init();

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture(
                isA(SalesforceInputProperties.class), createDefaultTestDataset())) {
            testFixture.setUp();

            properties.sourceType.setValue(SalesforceDatasetProperties.SourceType.MODULE_SELECTION);
            properties.setDatastoreProperties(datastoreProperties);

            Form mainForm = properties.getForm(Form.MAIN);

            // Module name not set

            properties.refreshLayout(mainForm);

            assertTrue(mainForm.getWidget(properties.moduleName.getName()).isVisible());
            assertFalse(mainForm.getWidget(properties.query.getName()).isVisible());
            assertFalse(mainForm.getWidget(properties.selectColumnIds.getName()).isVisible());
            assertFalse(properties.selectColumnIds.isRequired());

            // Module name set

            properties.moduleName.setValue("Account");

            properties.refreshLayout(mainForm);

            assertTrue(mainForm.getWidget(properties.moduleName.getName()).isVisible());
            assertFalse(mainForm.getWidget(properties.query.getName()).isVisible());
            assertTrue(mainForm.getWidget(properties.selectColumnIds.getName()).isVisible());
            assertTrue(properties.selectColumnIds.isRequired());
        }
    }

    @Test
    public void testRefreshLayoutForSourceTypeQuery() throws Exception {
        datastoreProperties.init();
        properties.init();

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture(
                isA(SalesforceInputProperties.class), createDefaultTestDataset())) {
            testFixture.setUp();

            properties.setDatastoreProperties(datastoreProperties);

            Form mainForm = properties.getForm(Form.MAIN);

            properties.sourceType.setValue(SalesforceDatasetProperties.SourceType.SOQL_QUERY);

            properties.refreshLayout(mainForm);

            assertTrue(mainForm.getWidget(properties.query.getName()).isVisible());
            assertTrue(properties.query.isRequired());
            assertFalse(mainForm.getWidget(properties.moduleName.getName()).isVisible());
            assertFalse(properties.moduleName.isRequired());
            assertFalse(mainForm.getWidget(properties.selectColumnIds.getName()).isVisible());
            assertFalse(properties.selectColumnIds.isRequired());
        }
    }

    @Test
    public void testAfterSourceType() throws Throwable {
        datastoreProperties.init();
        properties.init();

        reset(properties);

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture(
                isA(SalesforceInputProperties.class), createDefaultTestDataset())) {
            testFixture.setUp();

            properties.setDatastoreProperties(datastoreProperties);

            properties.sourceType.setValue(SalesforceDatasetProperties.SourceType.MODULE_SELECTION);

            propertiesService.afterProperty("sourceType", properties);

            assertThat((Iterable<String>) properties.moduleName.getPossibleValues(),
                    containsInAnyOrder("Account", "Customer"));

            assertNull(properties.moduleName.getValue());
            assertNull(properties.selectColumnIds.getValue());
            assertNull(properties.query.getValue());

            Form mainForm = properties.getForm(Form.MAIN);
            verify(properties, times(1)).refreshLayout(eq(mainForm));

        }
    }

    @Test
    public void testAfterModuleName() throws Throwable {
        datastoreProperties.init();
        properties.init();

        reset(properties);

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture(
                isA(SalesforceInputProperties.class), createDefaultTestDataset())) {
            testFixture.setUp();

            properties.setDatastoreProperties(datastoreProperties);
            properties.sourceType.setValue(SalesforceDatasetProperties.SourceType.MODULE_SELECTION);

            properties.moduleName.setValue("Account");

            propertiesService.afterProperty("moduleName", properties);

            assertThat((Iterable<String>) properties.moduleName.getPossibleValues(),
                    containsInAnyOrder("Account", "Customer"));

            assertNotNull(properties.moduleName.getValue());
            assertNull(properties.selectColumnIds.getValue());
            assertNull(properties.query.getValue());

            Form mainForm = properties.getForm(Form.MAIN);
            verify(properties, times(1)).refreshLayout(eq(mainForm));
        }
    }
}
