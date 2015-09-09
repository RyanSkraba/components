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
import com.sforce.soap.partner.*;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import org.talend.components.api.ComponentRuntime;
import org.talend.components.api.ComponentSchemaElement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SalesforceRuntime extends ComponentRuntime {

    PartnerConnection connection;

    BulkConnection bulkConnection;

    public void connect(SalesforceConnectionProperties properties) throws Exception {

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(properties.userPassword.userId.getValue());
        config.setPassword(properties.userPassword.password.getValue());
        config.setAuthEndpoint(properties.url.getValue());

        if (properties.timeout.getValue() > 0) {
            config.setConnectionTimeout(properties.timeout.getValue());
        }
        if (properties.needCompression.getValue() != null) {
            config.setCompression(properties.needCompression.getValue());
        }

        config.setTraceMessage(true);

        connection = new PartnerConnection(config);

        // FIXME - awkward
        if (properties.bulkConnection.getValue() != null && properties.bulkConnection.getValue()) {
            // When PartnerConnection is instantiated, a login is implicitly
            // executed and, if successful,
            // a valid session is stored in the ConnectorConfig instance.
            // Use this key to initialize a BulkConnection:
            ConnectorConfig bulkConfig = new ConnectorConfig();
            bulkConfig.setSessionId(config.getSessionId());
            // The endpoint for the Bulk API service is the same as for the normal
            // SOAP uri until the /Soap/ part. From here it's '/async/versionNumber'
            String soapEndpoint = config.getServiceEndpoint();
            // FIXME - fix hardcoded version
            String apiVersion = "34.0";
            String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + apiVersion;
            bulkConfig.setRestEndpoint(restEndpoint);
            // This should only be false when doing debugging.
            bulkConfig.setCompression(true);
            // Set this to true to see HTTP requests and responses on stdout
            bulkConfig.setTraceMessage(false);
            bulkConnection = new BulkConnection(bulkConfig);
        }

        System.out.println("Connection: " + connection);
        System.out.println("Bulk Connection: " + bulkConnection);
    }

    public List<String> getModuleNames() throws ConnectionException {
        List<String> returnList = new ArrayList();
        DescribeGlobalResult result = connection.describeGlobal();
        DescribeGlobalSObjectResult[] objects = result.getSobjects();
        for (DescribeGlobalSObjectResult obj : objects) {
            System.out.println("module label: " + obj.getLabel() + " name: " + obj.getName());
            returnList.add(obj.getName());
        }
        return returnList;
    }

    public ComponentSchemaElement getSchema(String module) throws ConnectionException {
        ComponentSchemaElement root = getComponentSchemaElement();
        root.setName("Root");
        List<ComponentSchemaElement> children = new ArrayList<ComponentSchemaElement>();

        DescribeSObjectResult[] describeSObjectResults = connection.describeSObjects(new String[] { module });
        Field fields[] = describeSObjectResults[0].getFields();
        for (Field field : fields) {
            ComponentSchemaElement child = getComponentSchemaElement();
            child.setName(field.getName());
            children.add(child);
        }
        root.setChildren(children);
        return root;
    }

    public Calendar getServerTimestamp() throws ConnectionException {
        GetServerTimestampResult result = connection.getServerTimestamp();
        return result.getTimestamp();
    }

}
