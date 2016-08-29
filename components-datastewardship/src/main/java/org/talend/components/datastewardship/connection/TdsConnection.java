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
package org.talend.components.datastewardship.connection;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TdsConnection {

    private static final Logger LOG = LoggerFactory.getLogger(TdsConnection.class);

    /**
     * Authorization header
     */
    private Header authorization;

    /**
     * TDS URL
     */
    private String hostPort;

    /**
     * Content type
     */
    private ContentType contentType = ContentType.APPLICATION_JSON;

    /**
     * Request executor
     */
    private Executor executor;

    /**
     * Constructor sets schema(http or https), host(google.com) and port number(8080) using one hostPort parameter
     * 
     * @param hostPort
     * @param username
     * @param password
     */
    public TdsConnection(String hostPort, String username, String password) {
        this.hostPort = !hostPort.endsWith("/") ? hostPort + "/" : hostPort; //$NON-NLS-1$ //$NON-NLS-2$
        String encodedCredentials = Base64.encodeBase64String((username + ":" + password).getBytes()); //$NON-NLS-1$
        authorization = new BasicHeader(HttpHeaders.AUTHORIZATION, AuthPolicy.BASIC + " " + encodedCredentials); //$NON-NLS-1$
        executor = Executor.newInstance().use(new BasicCookieStore());
    }

    /**
     * Checks connection to the host
     * 
     * @return HTTP status code
     * @throws IOException if host is unreachable
     */
    public int checkConnection() throws IOException {
        int statusCode = 0;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpHead httpHead = new HttpHead(hostPort);
            try (CloseableHttpResponse response = httpClient.execute(httpHead)) {
                statusCode = response.getStatusLine().getStatusCode();
            }
        }
        return statusCode;
    }

    /**
     * Executes Http Get request
     * 
     * @param resource REST API resource. E. g. issue/{issueId}
     * @return response result
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public String get(String resource) throws IOException {
        return get(resource, Collections.EMPTY_MAP);
    }

    /**
     * Executes Http Get request
     * 
     * @param resource REST API resource. E. g. issue/{issueId}
     * @param parameters http query parameters
     * @return response result
     * @throws IOException
     */
    public String get(String resource, Map<String, Object> parameters) throws IOException {
        try {
            URIBuilder builder = new URIBuilder(hostPort + resource);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue().toString());
            }
            URI uri = builder.build();
            Request get = Request.Get(uri).addHeader(authorization);
            return executor.execute(get).returnContent().asString();
        } catch (URISyntaxException e) {
            LOG.debug("Wrong URI. {}", e.getMessage()); //$NON-NLS-1$
            throw new IOException("Wrong URI", e); //$NON-NLS-1$
        }
    }

    /**
     * Executes Http Delete request
     * 
     * @param resource REST API resource. E. g. issue/{issueId}
     * @return http status code
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public int delete(String resource) throws IOException {
        return delete(resource, Collections.EMPTY_MAP);
    }

    /**
     * Executes Http Delete request
     * 
     * @param resource REST API resource. E. g. issue/{issueId}
     * @param parameters http query parameters
     * @return http status code
     * @throws IOException
     */
    public int delete(String resource, Map<String, Object> parameters) throws IOException {
        try {
            URIBuilder builder = new URIBuilder(hostPort + resource);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue().toString());
            }
            URI uri = builder.build();
            Request delete = Request.Delete(uri).addHeader(authorization);
            return executor.execute(delete).returnResponse().getStatusLine().getStatusCode();
        } catch (URISyntaxException e) {
            LOG.debug("Wrong URI. {}", e.getMessage()); //$NON-NLS-1$
            throw new IOException("Wrong URI", e); //$NON-NLS-1$
        }
    }

    /**
     * Executes Http Post request
     * 
     * @param resource REST API resource. E. g. issue/{issueId}
     * @param body message body
     * @return response status code
     * @throws ClientProtocolException
     * @throws IOException
     */
    public int post(String resource, String body) throws IOException {
        Request post = Request.Post(hostPort + resource).addHeader(authorization).bodyString(body, contentType);
        return executor.execute(post).returnResponse().getStatusLine().getStatusCode();
    }

    /**
     * Executes Http Put request
     * 
     * @param resource REST API resource. E. g. issue/{issueId}
     * @param body message body
     * @return http status code
     * @throws ClientProtocolException
     * @throws IOException
     */
    public int put(String resource, String body) throws IOException {
        Request put = Request.Put(hostPort + resource).addHeader(authorization).bodyString(body, contentType);
        return executor.execute(put).returnResponse().getStatusLine().getStatusCode();
    }
}
