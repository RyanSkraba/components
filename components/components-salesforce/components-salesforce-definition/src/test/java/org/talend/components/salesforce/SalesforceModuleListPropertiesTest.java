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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.service.PropertiesService;
import org.talend.daikon.properties.service.PropertiesServiceImpl;

/**
 *
 */
public class SalesforceModuleListPropertiesTest extends SalesforceTestBase {

    private final String connectionName = "___TEST";

    private final String repoLocation = "___DRI";

    private PropertiesService propertiesService;

    private SalesforceConnectionProperties connectionProperties;

    private SalesforceModuleListProperties properties;

    @Before
    public void setUp() {
        propertiesService = new PropertiesServiceImpl();

        connectionProperties = new SalesforceConnectionProperties("connection");
        connectionProperties.name.setValue(connectionName);

        properties = new SalesforceModuleListProperties("root");
        properties.setConnection(connectionProperties);
        properties.setRepositoryLocation(repoLocation);
    }

    @Test
    public void testSetupProperties() {
        properties.setupProperties();

        assertNotNull(properties.getConnectionProperties());
        assertEquals(connectionProperties, properties.getConnectionProperties());
        assertNotNull(properties.getRepositoryLocation());
        assertEquals(repoLocation, properties.getRepositoryLocation());
    }

    @Test
    public void testSetupLayout() {
        properties.init();

        Form mainForm = properties.getForm(Form.MAIN);
        assertNotNull(mainForm.getWidget(properties.selectedModuleNames.getName()));
    }

    @Test
    public void testRefreshLayout() {
        properties.init();

        properties.refreshLayout(properties.getForm(Form.MAIN));
        Form mainForm = properties.getForm(Form.MAIN);
        assertNotNull(mainForm.getWidget(properties.selectedModuleNames.getName()));
    }

    @Test
    public void testBeforeFormPresentMain() throws Throwable {
        properties.init();

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture(
                equalTo(properties), createDefaultTestDataset())) {
            testFixture.setUp();

            propertiesService.beforeFormPresent("Main", properties);

            assertThat((Iterable<NamedThing>) properties.selectedModuleNames.getPossibleValues(),
                    containsInAnyOrder((NamedThing) new SimpleNamedThing("Account"), new SimpleNamedThing("Customer")));

            Form mainForm = properties.getForm(Form.MAIN);
            assertTrue(mainForm.isAllowBack());
            assertTrue(mainForm.isAllowFinish());
        }
    }

    @Test
    public void testAfterFormFinishMain() throws Throwable {
        properties.init();

        List<TestRepository.Entry> repoEntries = new ArrayList<>();
        TestRepository repository = new TestRepository(repoEntries);
        propertiesService.setRepository(repository);

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture(
                equalTo(properties), createDefaultTestDataset())) {
            testFixture.setUp();

            properties.selectedModuleNames.setValue(Arrays.<NamedThing>asList(
                    new SimpleNamedThing("Account"), new SimpleNamedThing("Customer")));

            propertiesService.afterFormFinish("Main", properties);

            assertEquals(3, repoEntries.size());

            // Connection entry

            TestRepository.Entry connRepoEntry = repoEntries.get(0);
            assertEquals(connectionName, connRepoEntry.getName());
            assertThat(connRepoEntry.getProperties(), instanceOf(SalesforceConnectionProperties.class));

            // Module entry 1

            TestRepository.Entry repoEntry1 = repoEntries.get(1);

            assertEquals("Account", repoEntry1.getName());
            assertEquals("main.schema", repoEntry1.getSchemaPropertyName());
            assertEquals(testFixture.getTestDataset().getSchema("Account"), repoEntry1.getSchema());
            assertThat(repoEntry1.getProperties(), instanceOf(SalesforceModuleProperties.class));

            SalesforceModuleProperties modProps1 = (SalesforceModuleProperties) repoEntry1.getProperties();
            assertEquals(connectionProperties, modProps1.getConnectionProperties());
            assertEquals("Account", modProps1.moduleName.getValue());
            assertEquals(testFixture.getTestDataset().getSchema("Account"), modProps1.main.schema.getValue());
            assertNotNull(modProps1.getForm(Form.MAIN));

            // Module entry 2

            TestRepository.Entry repoEntry2 = repoEntries.get(2);

            assertEquals("Customer", repoEntry2.getName());
            assertEquals("main.schema", repoEntry2.getSchemaPropertyName());
            assertEquals(testFixture.getTestDataset().getSchema("Customer"), repoEntry2.getSchema());
            assertThat(repoEntry2.getProperties(), instanceOf(SalesforceModuleProperties.class));

            SalesforceModuleProperties modProps2 = (SalesforceModuleProperties) repoEntry2.getProperties();
            assertEquals(connectionProperties, modProps2.getConnectionProperties());
            assertEquals("Customer", modProps2.moduleName.getValue());
            assertEquals(testFixture.getTestDataset().getSchema("Customer"), modProps2.main.schema.getValue());
            assertNotNull(modProps2.getForm(Form.MAIN));
        }
    }

}
