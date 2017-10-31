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

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.components.salesforce.integration.SalesforceTestBase;
import org.talend.components.salesforce.runtime.common.ConnectionHolder;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class SalesforceSourceOrSinkTestIT extends SalesforceTestBase {

    public static final String USER_ID_EXPIRED = System.getProperty("salesforce.user.expired");

    public static final String PASSWORD_EXPIRED = System.getProperty("salesforce.password.expired");

    public static final String SECURITY_KEY_EXPIRED = System.getProperty("salesforce.key.expired");

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
        // check validate is ERROR with wrong credentials
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
    public void testGetSharedPartnerConnection() throws Exception {
        prepareAndGetConnectionHolder(false);
    }

    @Test
    public void testGetSharedBulkConnection() throws Exception {
        prepareAndGetConnectionHolder(true);
    }

    private void prepareAndGetConnectionHolder(boolean isBulk) throws Exception {
        final SalesforceConnectionProperties connectionProperties = setupProps(null, false);
        connectionProperties.bulkConnection.setValue(isBulk);
        RuntimeContainer container = new DefaultComponentRuntimeContainerImpl() {

            @Override
            public String getCurrentComponentId() {
                return connectionProperties.getName();
            }
        };

        SalesforceSourceOrSink sourceOrSink = new SalesforceSourceOrSink();
        sourceOrSink.initialize(container, connectionProperties);
        // Creating and saving PartnerConnection + BulkConnection(holder) inside container.
        sourceOrSink.validate(container);
        ConnectionHolder expectedHolder = (ConnectionHolder) container.getComponentData(connectionProperties.getName(),
                SalesforceSourceOrSink.KEY_CONNECTION);
        TSalesforceInputProperties inputProperties = new TSalesforceInputProperties("input");
        inputProperties.connection.referencedComponent.componentInstanceId.setValue(connectionProperties.getName());
        sourceOrSink.initialize(container, inputProperties);
        ConnectionHolder actualHolder = sourceOrSink.connect(container);

        Assert.assertEquals(expectedHolder, actualHolder);
        if (isBulk) {
            Assert.assertNotNull(actualHolder.bulkConnection);
        } else {
            //Check if bulk connection was not chosen but created.
            Assert.assertNull(actualHolder.bulkConnection);
        }
    }

    @Ignore
    @Test(expected = ConnectionException.class)
    public void testSalesForcePasswordExpired() throws ConnectionException {
        SalesforceSourceOrSink salesforceSourceOrSink = new SalesforceSourceOrSink();
        TSalesforceInputProperties properties = (TSalesforceInputProperties) new TSalesforceInputProperties(null).init();
        salesforceSourceOrSink.initialize(null, properties);

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(StringUtils.strip(USER_ID_EXPIRED, "\""));
        String password = StringUtils.strip(PASSWORD_EXPIRED, "\"");
        String securityKey = StringUtils.strip(SECURITY_KEY_EXPIRED, "\"");
        if (StringUtils.isNotEmpty(securityKey)) {
            password = password + securityKey;
        }
        config.setPassword(password);

        PartnerConnection connection = null;
        try {
            connection = salesforceSourceOrSink.doConnection(config, true);
        } catch (LoginFault ex) {
            Assert.fail("Must be an exception related to expired password, not the Login Fault.");
        } finally {
            if (null != connection) {
                connection.logout();
            }
        }
    }

}
