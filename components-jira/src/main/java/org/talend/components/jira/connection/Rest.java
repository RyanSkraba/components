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
package org.talend.components.jira.connection;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
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
        this.hostPort = hostPort;
        contentType = ContentType.create("application/json", StandardCharsets.UTF_8);
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
    public String get(String resource, Map<String, String> parameters) throws IOException {
        try {
            URIBuilder builder = new URIBuilder(hostPort + resource);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
            URI uri = builder.build();
            Request get = Request.Get(uri);
            for (Header header : headers) {
                get.addHeader(header);
            }
            return get.execute().returnContent().asString();
        } catch (URISyntaxException e) {
            LOG.debug("Wrong URI. {}", e.getMessage());
        }
        return null;
    }

    /**
     * Executes Http Delete request
     * 
     * @param resource REST API resource. E. g. issue/{issueId}
     * @return http status code
     * @throws IOException
     * @throws ClientProtocolException
     */
    public int delete(String resource) throws ClientProtocolException, IOException {
        Request delete = Request.Delete(hostPort + resource);
        for (Header header : headers) {
            delete.addHeader(header);
        }
        return delete.execute().returnResponse().getStatusLine().getStatusCode();
    }

    /**
     * Executes Http Post request
     * 
     * @param resource REST API resource. E. g. issue/{issueId}
     * @param body message body
     * @return response result
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String post(String resource, String body) throws ClientProtocolException, IOException {
        Request post = Request.Post(hostPort + resource).bodyString(body, contentType);
        for (Header header : headers) {
            post.addHeader(header);
        }
        Content content = post.execute().returnContent();
        if (content != null) {
            return content.asString();
        }
        return "204 no content";
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
    public int put(String resource, String body) throws ClientProtocolException, IOException {
        Request put = Request.Put(hostPort + resource).bodyString(body, contentType);
        for (Header header : headers) {
            put.addHeader(header);
        }
        return put.execute().returnResponse().getStatusLine().getStatusCode();
    }

    public Rest setAuthorizationType(String type) {
        this.authorizationType = type;
        return this;
    }

    public Rest setCredentials(String user, String password) {

        String credentials = user + ":" + password;
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
        return Base64.getEncoder().encodeToString(str.getBytes());
    }
}
