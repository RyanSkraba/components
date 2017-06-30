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

package org.talend.components.netsuite.test.client;

import java.net.URL;

import org.talend.components.netsuite.NetSuiteVersion;
import org.talend.components.netsuite.NetSuiteWebServiceMockTestFixture;
import org.talend.components.netsuite.client.NetSuiteClientFactory;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.test.NetSuitePortTypeMockAdapterImpl;

import com.netsuite.webservices.test.platform.NetSuitePortType;

/**
 *
 */
public class TestNetSuiteClientFactory implements NetSuiteClientFactory<NetSuitePortType>,
        TestNetSuiteClientService.NetSuitePortProvider<NetSuitePortType>,
        NetSuiteWebServiceMockTestFixture.NetSuitePortMockInstallAware<NetSuitePortType> {

    private NetSuitePortType portMock;

    public TestNetSuiteClientFactory() {
    }

    public TestNetSuiteClientFactory(NetSuitePortType portMock) {
        this.portMock = portMock;
    }

    @Override
    public NetSuiteClientService<NetSuitePortType> createClient() throws NetSuiteException {
        TestNetSuiteClientService clientService = new TestNetSuiteClientService();
        clientService.setPortProvider(this);
        return clientService;
    }

    @Override
    public NetSuitePortType getPort(URL endpointUrl, String account) {
        if (portMock == null) {
            throw new IllegalStateException("NetSuitePortType mock not installed");
        }
        NetSuitePortTypeMockAdapterImpl portTypeMockAdapter = new NetSuitePortTypeMockAdapterImpl();
        portTypeMockAdapter.setPort(portMock);
        return portTypeMockAdapter;
    }

    @Override
    public NetSuiteVersion getApiVersion() {
        return new NetSuiteVersion(2016, 2);
    }

    @Override
    public void portMockInstalled(NetSuitePortType portMock) {
        this.portMock = portMock;
    }
}
