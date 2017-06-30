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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.talend.components.netsuite.test.NetSuitePortTypeMockAdapterImpl;

import com.netsuite.webservices.test.platform.NetSuitePortType;
import com.netsuite.webservices.test.platform.messages.LoginRequest;
import com.netsuite.webservices.test.platform.messages.LoginResponse;
import com.netsuite.webservices.test.platform.messages.SessionResponse;

/**
 *
 */
public class NetSuiteComponentMockTestFixture
        extends AbstractNetSuiteComponentMockTestFixture<NetSuitePortType> {

    public NetSuiteComponentMockTestFixture(
            NetSuiteWebServiceMockTestFixture<NetSuitePortType, ?> webServiceMockTestFixture) {
        super(webServiceMockTestFixture);
    }

    @Override
    protected void mockLoginResponse(NetSuitePortType port) throws Exception {
        SessionResponse sessionResponse = new SessionResponse();
        sessionResponse.setStatus(NetSuitePortTypeMockAdapterImpl.createSuccessStatus());
        LoginResponse response = new LoginResponse();
        response.setSessionResponse(sessionResponse);

        when(port.login(any(LoginRequest.class))).thenReturn(response);
    }

}
