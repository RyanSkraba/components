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

import com.sforce.async.BulkConnection;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectorConfig;

public class SalesforceConnection {

    PartnerConnection connection;

    BulkConnection bulkConnection;

    public void connect(SalesforceConnectionProperties properties) throws Exception {

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(properties.userPassword.userId.getValue());
        config.setPassword(properties.userPassword.password.getValue());
        config.setAuthEndpoint(properties.url.getValue());

        if (properties.timeout.getValue() > 0)
            config.setConnectionTimeout(properties.timeout.getValue());
        if (properties.needCompression.getValue() != null)
            config.setCompression(properties.needCompression.getValue());

        config.setTraceMessage(true);

        // FIXME - awkward
        if (properties.bulkConnection.getValue() != null && properties.bulkConnection.getValue()) {
            new PartnerConnection(config);
            // When PartnerConnection is instantiated, a login is implicitly
            // executed and, if successful,
            // a valid session is stored in the ConnectorConfig instance.
            // Use this key to initialize a BulkConnection:
            ConnectorConfig bulkConfig = new ConnectorConfig();
            bulkConfig.setSessionId(config.getSessionId());
            // The endpoint for the Bulk API service is the same as for the normal
            // SOAP uri until the /Soap/ part. From here it's '/async/versionNumber'
            String soapEndpoint = config.getServiceEndpoint();
            String apiVersion = "34.0";
            String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + apiVersion;
            bulkConfig.setRestEndpoint(restEndpoint);
            // This should only be false when doing debugging.
            bulkConfig.setCompression(true);
            // Set this to true to see HTTP requests and responses on stdout
            bulkConfig.setTraceMessage(false);
            bulkConnection = new BulkConnection(bulkConfig);
        } else {
            connection = Connector.newConnection(config);
        }

        System.out.println("Connection: " + connection);
        System.out.println("Bulk Connection: " + bulkConnection);
    }
}
