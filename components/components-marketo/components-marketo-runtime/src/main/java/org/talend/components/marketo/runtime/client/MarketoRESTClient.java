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
package org.talend.components.marketo.runtime.client;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;

public class MarketoRESTClient extends MarketoOpportunityClient implements MarketoClientServiceExtended {

    private static final Logger LOG = LoggerFactory.getLogger(MarketoRESTClient.class);

    public MarketoRESTClient(TMarketoConnectionProperties connection) throws MarketoException {
        super(connection);
    }

    public MarketoRESTClient connect() throws MarketoException {
        try {
            if (endpoint == null) {
                throw new MarketoException(REST, messages.getMessage("error.rest.endpoint.null"));
            }
            URL url = new URL(endpoint);
            if (url.getPath() != null) {
                basicPath = url.toString();
                // check if endpoint is valid
                if (!basicPath.equals(String.format("%s://%s/rest", url.getProtocol(), url.getHost()))) {
                    throw new MarketoException(REST, messages.getMessage("error.rest.endpoint.invalid"));
                }
                bulkPath = basicPath.replaceAll("rest$", "bulk");
            }
            // check credentials
            getToken();
            // dummy call to finally check the connection
            getPageToken("2017-01-01 00:00:00");
            return this;
        } catch (MalformedURLException e) {
            LOG.error(e.toString());
            throw new MarketoException(REST, e.getMessage());
        }
    }

}
