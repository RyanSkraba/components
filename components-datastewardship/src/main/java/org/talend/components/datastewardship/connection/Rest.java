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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Apache Http components library wrapper. It uses Basic authentification by default
 * 
 * @author ivan.honchar
 */
public class Rest {

    private static final Logger LOG = LoggerFactory.getLogger(Rest.class);

    /**
     * A list of common headers
     */
    private List<Header> headers;

    /**
     * Schema, host and port of REST API location
     */
    private String hostPort;

    /**
     * Http authorization type. It is used to set Http Authorization header
     */
    private String authorizationType = "Basic";

    /**
     * 
     */
    private ContentType contentType;

    /**
     * Request executor
     */
    private Executor executor;

    /**
     * Constructor
     */
    public Rest() {
        this(null);
    }

    /**
     * Constructor sets schema(http or https), host(google.com) and port number(8080) using one hostPort parameter
     * 
     * @param hostPort URL
     */
    public Rest(String hostPort) {
        headers = new LinkedList<Header>();
        if (!hostPort.endsWith("/")) {
            hostPort = hostPort + "/";
        }
        this.hostPort = hostPort;
        contentType = ContentType.create("application/json", StandardCharsets.UTF_8);
        executor = Executor.newInstance();
        executor.use(new BasicCookieStore());
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
            Request get = Request.Get(uri);
            for (Header header : headers) {
                get.addHeader(header);
            }
            executor.clearCookies();
            return executor.execute(get).returnContent().asString();
        } catch (URISyntaxException e) {
            LOG.debug("Wrong URI. {}", e.getMessage());
            throw new IOException("Wrong URI", e);
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
            Request delete = Request.Delete(uri);
            for (Header header : headers) {
                delete.addHeader(header);
            }
            executor.clearCookies();
            return executor.execute(delete).returnResponse().getStatusLine().getStatusCode();
        } catch (URISyntaxException e) {
            LOG.debug("Wrong URI. {}", e.getMessage());
            throw new IOException("Wrong URI", e);
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
        Request post = Request.Post(hostPort + resource).bodyString(body, contentType);
        for (Header header : headers) {
            post.addHeader(header);
        }
        executor.clearCookies();
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
        Request put = Request.Put(hostPort + resource).bodyString(body, contentType);
        for (Header header : headers) {
            put.addHeader(header);
        }
        executor.clearCookies();
        return executor.execute(put).returnResponse().getStatusLine().getStatusCode();
    }

    public Rest setAuthorizationType(String type) {
        this.authorizationType = type;
        return this;
    }

    public Rest setCredentials(String username, String password) {
        String credentials = username + ":" + password;
        String encodedCredentials = base64(credentials);
        Header authorization = new BasicHeader("Authorization", authorizationType + " " + encodedCredentials);
        headers.add(authorization);
        return this;
    }

    public Rest setUrl(String url) {
        this.hostPort = url;
        return this;
    }

    private String base64(String str) {
        return Base64.encodeBase64String(str.getBytes());
    }
}
