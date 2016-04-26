// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.splunk.connection;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * created by dmytro.chmyga on Apr 26, 2016
 */
public class TSplunkEventCollectorConnection {

    private HttpClient client = null;
    
    /**
     * create http client to be used for connection with Splunk server
     */
    public void connect() {
        if(client == null) {
            try {
                client = new DefaultHttpClient();
            } catch (Exception e) { 
            }
        }
    }
    
    /**
     * Send a post request to splunk server, including event to be logged, in JSON String. 
     * @param schema to be used. Possible values are http, https
     * @param uri server uri(hostname or ip-address)
     * @param portNumber which is listened by Splunk server http event collector
     * @param request to be sent
     * @return response from the server
     * @throws ClientProtocolException
     * @throws IOException
     */
    public HttpResponse sendRequest(String schema, String uri, int portNumber, HttpPost request) throws ClientProtocolException, IOException {
        HttpHost target = new HttpHost(uri, portNumber, schema);
        return client.execute(target, request);
    }
    
    /**
     * Send a post request to splunk server, including event to be logged, in JSON String.
     * @param request to be sent
     * @return response from the server
     * @throws ClientProtocolException
     * @throws IOException
     */
    public HttpResponse sendRequest(HttpPost request) throws ClientProtocolException, IOException {
        return client.execute(request);
    }
    
    /**
     * Close connection manager of http client.
     */
    public void close() {
        if(client != null) {
            client = null;
        }
    }
    
}
