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
package org.talend.components.salesforce.runtime;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.components.salesforce.integration.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class SalesforceSourceOrSinkTestIT extends SalesforceTestBase {

    @Test
    public void testInitialize() {
        SalesforceSourceOrSink salesforceSourceOrSink = new SalesforceSourceOrSink();
        TSalesforceInputProperties properties = new TSalesforceInputProperties(null);
        salesforceSourceOrSink.initialize(null, properties);
        assertEquals(properties.connection, salesforceSourceOrSink.getConnectionProperties());
    }

    @Test
    public void testValidate() {
        // check validate is OK with proper credentials
        SalesforceConnectionProperties props = setupProps(null, !ADD_QUOTES);
        SalesforceSourceOrSink salesforceSourceOrSink = new SalesforceSourceOrSink();
        salesforceSourceOrSink.initialize(null, props);
        assertEquals(Result.OK, salesforceSourceOrSink.validate(null).getStatus());
        // check validate is ERROR with wrong creadentials
        props.userPassword.userId.setValue("");
        assertEquals(Result.ERROR, salesforceSourceOrSink.validate(null).getStatus());
    }

    @Test
    public void testIsolatedClassLoader() {
        ClassLoader classLoader = SalesforceDefinition.class.getClassLoader();
        RuntimeInfo runtimeInfo = SalesforceDefinition.getCommonRuntimeInfo(SalesforceSourceOrSink.class.getCanonicalName());
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClassWithCurrentJVMProperties(runtimeInfo,
                classLoader)) {
            sandboxedInstance.getInstance();
            System.setProperty("key", "value");
        }
        Assert.assertNull("The system property should not exist, but not", System.getProperty("key"));
    }

    @Test
    public void testGetConnectionProperties() {
        // using SalesforceConnectionProperties
        SalesforceConnectionProperties scp = setupProps(null, !ADD_QUOTES);
        SalesforceSourceOrSink salesforceSourceOrSink = new SalesforceSourceOrSink();
        salesforceSourceOrSink.initialize(null, scp);
        assertEquals(scp, salesforceSourceOrSink.getConnectionProperties());

        // using SalesforceConnectionProperties
        SalesforceConnectionModuleProperties scmp = new SalesforceConnectionModuleProperties(null) {

            @Override
            protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
                // TODO Auto-generated method stub
                return null;
            }

        };
        salesforceSourceOrSink.initialize(null, scmp);
        assertEquals(scmp.connection, salesforceSourceOrSink.getConnectionProperties());
    }

    @Test
    public void testGetSchemaNames() throws IOException {
        SalesforceConnectionProperties scp = setupProps(null, !ADD_QUOTES);
        List<NamedThing> schemaNames = SalesforceSourceOrSink.getSchemaNames(null, scp);
        assertTrue(schemaNames.size() > 50);
    }

    @Test
    public void testGetSchema() throws IOException {
        SalesforceConnectionProperties scp = setupProps(null, !ADD_QUOTES);
        Schema schema = SalesforceSourceOrSink.getSchema(null, scp, EXISTING_MODULE_NAME);
        assertNotNull(schema);
        assertThat(schema.getFields(), hasSize(greaterThan(10)));
        // assertTrue(schema.getRoot().getChildren().size() > 10);
    }

    @Test
    public void testGetSchemaFail() throws IOException {
        SalesforceConnectionProperties scp = setupProps(null, !ADD_QUOTES);
        try {
            Schema schema = SalesforceSourceOrSink.getSchema(null, scp, "module that does not exist");
            fail("Should have throw an exception when not finding the module");
        } catch (IOException ce) {
            assertTrue(ce.getMessage().contains("does not exist"));
        }
    }

}
