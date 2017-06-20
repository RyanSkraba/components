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

package org.talend.components.salesforce.datastore;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.talend.components.salesforce.SalesforceDefinition.DATASTORE_RUNTIME_CLASS;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.dataprep.SalesforceInputDefinition;
import org.talend.components.salesforce.dataprep.SalesforceInputProperties;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 *
 */
public class SalesforceDatastoreDefinitionTest extends SalesforceTestBase {

    private SalesforceDatastoreDefinition definition;

    private SalesforceDatastoreProperties properties;

    @Before
    public void setUp() {
        definition = new SalesforceDatastoreDefinition();

        properties = new SalesforceDatastoreProperties("root");
        properties.init();
    }

    @Test
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(properties);
        assertRuntimeInfo(runtimeInfo);
    }

    private void assertRuntimeInfo(RuntimeInfo runtimeInfo) {
        assertNotNull(runtimeInfo);
        assertThat(runtimeInfo, instanceOf(JarRuntimeInfo.class));

        JarRuntimeInfo jarRuntimeInfo = (JarRuntimeInfo) runtimeInfo;
        assertNotNull(jarRuntimeInfo.getJarUrl());
        assertNotNull(jarRuntimeInfo.getDepTxtPath());
        assertEquals(DATASTORE_RUNTIME_CLASS, jarRuntimeInfo.getRuntimeClassName());
    }

    @Test
    public void testCreateDatasetProperties() throws Exception {
        try (SalesforceTestBase.MockRuntimeSourceOrSinkTestFixture testFixture = new SalesforceTestBase.MockRuntimeSourceOrSinkTestFixture(
                isA(SalesforceInputProperties.class), createDefaultTestDataset())) {
            testFixture.setUp();

            DatasetProperties datasetProperties = definition.createDatasetProperties(properties);
            assertEquals(properties, datasetProperties.getDatastoreProperties());

            Form mainForm = properties.getForm(Form.MAIN);
            assertNotNull(mainForm);
            assertNotNull(mainForm.getWidget(properties.userId.getName()));
            assertNotNull(mainForm.getWidget(properties.password.getName()));
            assertNotNull(mainForm.getWidget(properties.securityKey.getName()));
        }
    }

    @Test
    public void testInputComponentDefinitionName() {
        assertEquals(SalesforceInputDefinition.NAME, definition.getInputCompDefinitionName());
    }

    @Test
    public void testOutputComponentDefinitionName() {
        assertNull(definition.getOutputCompDefinitionName());
    }

    @Test
    public void testImagePath() {
        assertNotNull(definition.getImagePath(DefinitionImageType.PALETTE_ICON_32X32));
        assertNull(definition.getImagePath(DefinitionImageType.SVG_ICON));
    }
}
