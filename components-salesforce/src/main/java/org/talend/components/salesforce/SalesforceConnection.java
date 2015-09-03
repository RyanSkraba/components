// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce;

import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectorConfig;

public class SalesforceConnection {

    public void connect(SalesforceConnectionProperties properties) throws Exception {

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(properties.userPassword.userId.getValue());
        config.setPassword(properties.userPassword.password.getValue());
        if (properties.timeout.getValue() > 0)
            config.setConnectionTimeout(properties.timeout.getValue());
        if (properties.needCompression.getValue() != null)
            config.setCompression(properties.needCompression.getValue());
        config.setTraceMessage(true);

        PartnerConnection connection = Connector.newConnection(config);

        System.out.println("conn: " + connection);
    }
}
