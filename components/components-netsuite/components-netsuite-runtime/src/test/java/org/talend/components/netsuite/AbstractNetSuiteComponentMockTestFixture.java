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

import static org.mockito.Mockito.spy;

import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.components.netsuite.test.TestFixture;

/**
 *
 */
public abstract class AbstractNetSuiteComponentMockTestFixture<PortT> implements TestFixture {
    protected NetSuiteWebServiceMockTestFixture<PortT, ?> webServiceMockTestFixture;
    protected boolean reinstall;
    protected RuntimeContainer runtimeContainer;
    protected NetSuiteConnectionProperties connectionProperties;

    public AbstractNetSuiteComponentMockTestFixture(
            NetSuiteWebServiceMockTestFixture<PortT, ?> webServiceMockTestFixture) {
        this.webServiceMockTestFixture = webServiceMockTestFixture;
    }

    public boolean isReinstall() {
        return reinstall;
    }

    public void setReinstall(boolean reinstall) {
        this.reinstall = reinstall;
    }

    @Override
    public void setUp() throws Exception {
        if (reinstall) {
            webServiceMockTestFixture.reinstall();
        }

        final PortT port = webServiceMockTestFixture.getPortMock();

        mockLoginResponse(port);

        runtimeContainer = spy(new DefaultComponentRuntimeContainerImpl());

        connectionProperties = new NetSuiteConnectionProperties("test");
        connectionProperties.init();
        connectionProperties.endpoint.setValue(webServiceMockTestFixture.getEndpointAddress().toString());
        connectionProperties.apiVersion.setValue(webServiceMockTestFixture.getClientFactory().getApiVersion().getMajorAsString("."));
        connectionProperties.email.setValue("test@test.com");
        connectionProperties.password.setValue("123");
        connectionProperties.role.setValue(3);
        connectionProperties.account.setValue("test");
        connectionProperties.applicationId.setValue("00000000-0000-0000-0000-000000000000");
        connectionProperties.customizationEnabled.setValue(true);
    }

    @Override
    public void tearDown() throws Exception {
        // do nothing
    }

    public RuntimeContainer getRuntimeContainer() {
        return runtimeContainer;
    }

    public NetSuiteConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }

    protected abstract void mockLoginResponse(PortT port) throws Exception;
}
