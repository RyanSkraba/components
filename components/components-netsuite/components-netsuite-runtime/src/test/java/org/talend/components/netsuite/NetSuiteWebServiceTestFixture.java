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

package org.talend.components.netsuite;

import static org.talend.components.netsuite.client.NetSuiteClientService.MESSAGE_LOGGING_ENABLED_PROPERTY_NAME;

import java.util.Arrays;
import java.util.Properties;

import org.talend.components.common.test.TestFixture;
import org.talend.components.netsuite.client.NetSuiteClientFactory;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteCredentials;
import org.talend.components.netsuite.test.TestUtils;

/**
 *
 */
public class NetSuiteWebServiceTestFixture<T> implements TestFixture {

    protected NetSuiteClientFactory<T> clientFactory;
    protected String apiVersion;
    protected Properties properties;
    protected NetSuiteCredentials credentials;
    protected NetSuiteClientService<T> clientService;

    public NetSuiteWebServiceTestFixture(NetSuiteClientFactory<T> clientFactory, String apiVersion) {
        this.clientFactory = clientFactory;
        this.apiVersion = apiVersion;
    }

    @Override
    public void setUp() throws Exception {
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        properties = TestUtils.loadProperties(System.getProperties(), Arrays.asList(
                "netsuite.endpoint.url",
                "netsuite.endpoint." + apiVersion + ".url",
                "netsuite.email", "netsuite.password",
                "netsuite.account", "netsuite.roleId",
                "netsuite.applicationId"
        ));

        credentials = NetSuiteCredentials.loadFromProperties(properties, "netsuite.");

        clientService = clientFactory.createClient();
        clientService.setEndpointUrl(getEndpointUrl());
        clientService.setCredentials(credentials);

        boolean messageLoggingEnabled = Boolean.valueOf(
                System.getProperty(MESSAGE_LOGGING_ENABLED_PROPERTY_NAME, "false"));
        clientService.setMessageLoggingEnabled(messageLoggingEnabled);
    }

    @Override
    public void tearDown() throws Exception {

    }

    public Properties getProperties() {
        return properties;
    }

    public NetSuiteCredentials getCredentials() {
        return credentials;
    }

    public String getEndpointUrl() {
        return properties.getProperty("netsuite.endpoint." + apiVersion + ".url",
                properties.getProperty("netsuite.endpoint.url"));
    }

    public NetSuiteClientService<T> getClientService() {
        return clientService;
    }
}
