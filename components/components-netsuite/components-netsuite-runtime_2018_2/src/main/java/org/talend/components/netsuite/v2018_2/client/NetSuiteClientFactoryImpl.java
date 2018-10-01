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

package org.talend.components.netsuite.v2018_2.client;

import org.talend.components.netsuite.NetSuiteVersion;
import org.talend.components.netsuite.client.NetSuiteClientFactory;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;

import com.netsuite.webservices.v2018_2.platform.NetSuitePortType;

/**
 *
 */
public class NetSuiteClientFactoryImpl implements NetSuiteClientFactory<NetSuitePortType> {

    public static final NetSuiteClientFactoryImpl INSTANCE = new NetSuiteClientFactoryImpl();

    @Override
    public NetSuiteClientService<NetSuitePortType> createClient() throws NetSuiteException {
        return new NetSuiteClientServiceImpl();
    }

    @Override
    public NetSuiteVersion getApiVersion() {
        return new NetSuiteVersion(2018, 2);
    }
}
