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

package org.talend.components.netsuite.v2014_2;

import org.talend.components.netsuite.NetSuiteSource;
import org.talend.components.netsuite.v2014_2.client.NetSuiteClientFactoryImpl;

/**
 *
 */
public class NetSuiteSourceImpl extends NetSuiteSource {

    public NetSuiteSourceImpl() {
        clientFactory = NetSuiteClientFactoryImpl.INSTANCE;
    }
}
