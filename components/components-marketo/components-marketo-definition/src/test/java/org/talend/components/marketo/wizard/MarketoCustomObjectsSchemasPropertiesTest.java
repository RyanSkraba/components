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
package org.talend.components.marketo.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.MarketoTestBase;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.ValidationResult.Result;

public class MarketoCustomObjectsSchemasPropertiesTest extends MarketoTestBase {

    MarketoCustomObjectsSchemasProperties properties;

    TMarketoConnectionProperties connection;

    @Before
    public void setUp() throws Exception {
        properties = new MarketoCustomObjectsSchemasProperties("test");
        properties.setupProperties();
        connection = new TMarketoConnectionProperties("testconn");
        connection.setupProperties();
        connection.endpoint.setValue("http://fakeendpoint.com");
        connection.clientAccessId.setValue("user0000");
        connection.secretKey.setValue("secretk");
        properties.setupLayout();
    }

    @Test
    public void testGetConnectionProperties() throws Exception {
        assertNotNull(properties.getConnectionProperties());
    }

    @Test
    public void testSetConnection() throws Exception {
        assertEquals(properties, properties.setConnection(connection));
        assertEquals(connection, properties.getConnectionProperties());
    }

    @Test
    public void testSetRepositoryLocation() throws Exception {
        properties.setRepositoryLocation("___DRI");
        assertEquals("___DRI", properties.getRepositoryLocation());
    }

    @Test
    public void testBeforeFormPresentCustomObjects() throws Exception {
        try (SandboxedInstanceTestFixture sandboxedInstanceTestFixture = new SandboxedInstanceTestFixture()) {
            sandboxedInstanceTestFixture.setUp();
            properties.beforeFormPresentCustomObjects();
            assertEquals(CO_SCHEMA_NAMES, properties.selectedCustomObjectsNames.getPossibleValues());
            assertTrue(properties.getForm(MarketoCustomObjectsSchemasProperties.FORM_CUSTOMOBJECTS).isAllowBack());
            assertTrue(properties.getForm(MarketoCustomObjectsSchemasProperties.FORM_CUSTOMOBJECTS).isAllowFinish());
            when(sandboxedInstanceTestFixture.runtimeSourceOrSink.getSchemaNames(any()))
                    .thenThrow(new IOException("ERROR"));
            try {
                properties.beforeFormPresentCustomObjects();
                fail("Should not be here");
            } catch (Exception e) {
                assertNotNull(e.getMessage());
            }
            sandboxedInstanceTestFixture.tearDown();
        }
    }

    @Test
    public void testAfterFormFinishCustomObjects() throws Exception {
        try (SandboxedInstanceTestFixture sandboxedInstanceTestFixture = new SandboxedInstanceTestFixture()) {
            sandboxedInstanceTestFixture.setUp();
            properties.beforeFormPresentCustomObjects();
            List<NamedThing> o = new ArrayList<>();
            o.add(new SimpleNamedThing("car_c", "car_c"));
            properties.selectedCustomObjectsNames.setValue(o);
            assertEquals(Result.OK, properties.afterFormFinishCustomObjects(repo).getStatus());
            //
            o.add(new SimpleNamedThing("car_except", "car_except"));
            properties.selectedCustomObjectsNames.setValue(o);
            assertEquals(Result.ERROR, properties.afterFormFinishCustomObjects(repo).getStatus());
            sandboxedInstanceTestFixture.tearDown();
        }
    }

}
