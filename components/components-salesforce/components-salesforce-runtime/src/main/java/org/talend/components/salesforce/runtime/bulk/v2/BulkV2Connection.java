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

package org.talend.components.salesforce.runtime.bulk.v2;

import static org.apache.oltu.oauth2.common.OAuth.OAUTH_HEADER_NAME;
import static org.talend.components.salesforce.runtime.bulk.v2.type.HttpMethod.GET;
import static org.talend.components.salesforce.runtime.bulk.v2.type.HttpMethod.PATCH;
import static org.talend.components.salesforce.runtime.bulk.v2.type.HttpMethod.POST;
import static org.talend.components.salesforce.runtime.bulk.v2.type.HttpMethod.PUT;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.talend.components.salesforce.runtime.bulk.v2.error.BulkV2ClientException;
import org.talend.components.salesforce.runtime.bulk.v2.request.CreateJobRequest;
import org.talend.components.salesforce.runtime.bulk.v2.request.GetQueryJobResultRequest;
import org.talend.components.salesforce.runtime.bulk.v2.request.UpdateJobRequest;
import org.talend.components.salesforce.runtime.bulk.v2.type.HttpMethod;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.sforce.async.JobStateEnum;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.bind.CalendarCodec;
import com.sforce.ws.tools.VersionInfo;

/**
 * Bulk Connection for Bulk API V2
 */

public class BulkV2Connection {

    public static final String ACCESS_TOKEN = "Authorization";

    public static final String CSV_CONTENT_TYPE = "text/csv";

    private static final I18nMessages MESSAGES =
            GlobalI18N.getI18nMessageProvider().getI18nMessages(BulkV2Connection.class);

    private ConnectorConfig config;

    private CloseableHttpClient httpclient;

    private OperationType operationType;

    public BulkV2Connection(ConnectorConfig config, OperationType operationType) throws BulkV2ClientException {
        if (config == null) {
            throw new BulkV2ClientException(MESSAGES.getMessage("error.config.empty"));
        }

        if (config.getRestEndpoint() == null) {
            throw new BulkV2ClientException(MESSAGES.getMessage("error.endpoint.empty"));
        }

        this.config = adaptBulkV2Config(config);

        this.httpclient = getHttpClient();

        this.operationType = operationType;

        if (config.getSessionId() == null) {
            throw new BulkV2ClientException(MESSAGES.getMessage("error.token.notfound"));
        }
    }

    static String serializeToJson(Object value) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.setDateFormat(CalendarCodec.getDateFormat());
        return mapper.writeValueAsString(value);
    }

    static <T> T deserializeJsonToObject(InputStream in, Class<T> tmpClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return mapper.readValue(in, tmpClass);
    }

    private ConnectorConfig adaptBulkV2Config(ConnectorConfig bulkConfig) {
        String restEndpoint = bulkConfig.getRestEndpoint();
        // Check whether it has been adapted
        if (restEndpoint.indexOf("/data/v") != -1) {
            return bulkConfig;
        }
        ConnectorConfig bulkV2Config = new ConnectorConfig();
        bulkV2Config.setSessionId(bulkConfig.getSessionId());

        String apiVersion = restEndpoint.substring(restEndpoint.lastIndexOf("/services/async/") + 16);
        // Replace rest endpoint with bulk v2 rest one.
        restEndpoint = restEndpoint.substring(0, restEndpoint.lastIndexOf("/async/")) + "/data/v" + apiVersion;
        bulkV2Config.setRestEndpoint(restEndpoint);
        bulkV2Config.setTraceMessage(bulkConfig.isTraceMessage());
        bulkV2Config.setValidateSchema(bulkConfig.isValidateSchema());
        bulkV2Config.setProxy(bulkConfig.getProxy());
        bulkV2Config.setProxyUsername(bulkConfig.getProxyUsername());
        bulkV2Config.setProxyPassword(bulkConfig.getProxyPassword());
        if (bulkConfig.getConnectionTimeout() > 0) {
            config.setConnectionTimeout(bulkConfig.getConnectionTimeout());
        }
        return bulkV2Config;
    }

    public JobInfoV2 createJob(CreateJobRequest request) throws BulkV2ClientException {
        String endpoint = getRestEndpoint();
        if (operationType == OperationType.LOAD) {
            endpoint = endpoint + "jobs/ingest";
        } else {
            endpoint = endpoint + "jobs/query";
        }
        return createJob(request, endpoint);
    }

    public JobInfoV2 closeJob(String jobId) throws BulkV2ClientException {
        return updateJob(jobId, JobStateEnum.UploadComplete);
    }

    private JobInfoV2 createJob(CreateJobRequest request, String endpoint) throws BulkV2ClientException {
        try {
            HttpPost httpPost = (HttpPost) createRequest(endpoint, HttpMethod.POST);
            StringEntity entity =
                    new StringEntity(serializeToJson(request), org.apache.http.entity.ContentType.APPLICATION_JSON);

            httpPost.setEntity(entity);
            HttpResponse response = httpclient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                if (response.getStatusLine().getStatusCode() == 404) {
                    throw new BulkV2ClientException(MESSAGES.getMessage("error.resource.not.found"));
                }
                throw new BulkV2ClientException(response.getStatusLine().getReasonPhrase());
            }
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                return deserializeJsonToObject(responseEntity.getContent(), JobInfoV2.class);
            } else {
                throw new IOException(MESSAGES.getMessage("error.job.info"));
            }
        } catch (BulkV2ClientException bec) {
            throw bec;
        } catch (IOException e) {
            throw new BulkV2ClientException(MESSAGES.getMessage("error.query.job"), e);
        }
    }

    private String getRestEndpoint() {
        String endpoint = config.getRestEndpoint();
        endpoint = endpoint.endsWith("/") ? endpoint : endpoint + "/";
        return endpoint;
    }

    public void uploadDataFromStream(String jobId, InputStream input) throws IOException {
        if (operationType != OperationType.LOAD) {
            throw new IOException(MESSAGES.getMessage("error.unsupported.method"));
        }
        try {
            String endpoint = getRestEndpoint();
            endpoint = endpoint + "jobs/ingest/" + jobId + "/batches";
            HttpPut httpPut = (HttpPut) createRequest(endpoint, HttpMethod.PUT);

            InputStreamEntity entity = new InputStreamEntity(input, -1);
            entity.setContentType(CSV_CONTENT_TYPE);
            entity.setChunked(config.useChunkedPost());

            httpPut.setEntity(entity);
            HttpResponse response = httpclient.execute(httpPut);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                throw new BulkV2ClientException(response.getStatusLine().getReasonPhrase());
            }
        } catch (BulkV2ClientException bec) {
            throw bec;
        } catch (IOException e) {
            throw new BulkV2ClientException(MESSAGES.getMessage("error.job.upload"), e);
        }
    }

    public InputStream getUnprocessedRecordsStream(String jobId) throws IOException {
        if (operationType != OperationType.LOAD) {
            throw new IOException(MESSAGES.getMessage("error.unsupported.method"));
        }
        String endpoint = getRestEndpoint() + "jobs/ingest/" + jobId + "/unprocessedrecords/";
        return doHttpGet(endpoint);
    }

    public InputStream getFailedRecordsStream(String jobId) throws IOException {
        if (operationType != OperationType.LOAD) {
            throw new IOException(MESSAGES.getMessage("error.unsupported.method"));
        }
        String endpoint = getRestEndpoint() + "jobs/ingest/" + jobId + "/failedResults/";
        return doHttpGet(endpoint);
    }

    public InputStream getResult(String jobId) throws BulkV2ClientException {
        String endpoint = null;
        if (operationType == OperationType.LOAD) {
            endpoint = getRestEndpoint() + "jobs/ingest/" + jobId + "/successfulResults/";
        } else {
            endpoint = getRestEndpoint() + "jobs/query/" + jobId + "/results/";
        }
        return doHttpGet(endpoint);
    }

    public InputStream getResult(GetQueryJobResultRequest request) throws BulkV2ClientException {
        String endpoint = getRestEndpoint() + "jobs/query/" + request.getQueryJobId() + "/results/";
        if (request.getLocator() == null && request.getMaxRecords() != null && request.getMaxRecords() > 0) {
            endpoint += "?maxRecords=" + request.getMaxRecords();
        } else if (request.getLocator() != null && request.getMaxRecords() != null && request.getMaxRecords() > 0) {
            endpoint += "?locator=" + request.getLocator() + "&maxRecords=" + request.getMaxRecords();
        }
        try {
            HttpGet httpGet = (HttpGet) createRequest(endpoint, HttpMethod.GET);
            httpGet.addHeader("Accept", CSV_CONTENT_TYPE);

            HttpResponse response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new BulkV2ClientException(response.getStatusLine().getReasonPhrase());
            }
            Header locator = response.getFirstHeader("Sforce-Locator");
            if (locator != null && locator.getValue() != null && !"null".equals(locator.getValue())) {
                request.setLocator(locator.getValue());
            } else {
                request.setLocator(null);
            }

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                return responseEntity.getContent();
            } else {
                throw new IOException(MESSAGES.getMessage("error.job.info"));
            }
        } catch (BulkV2ClientException bec) {
            throw bec;
        } catch (IOException e) {
            throw new BulkV2ClientException(MESSAGES.getMessage("error.query.job"), e);
        }
    }

    private InputStream doHttpGet(String endpoint) throws BulkV2ClientException {
        try {
            HttpGet httpGet = (HttpGet) createRequest(endpoint, HttpMethod.GET);
            httpGet.addHeader("Accept", CSV_CONTENT_TYPE);

            HttpResponse response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new BulkV2ClientException(response.getStatusLine().getReasonPhrase());
            }
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                return responseEntity.getContent();
            } else {
                throw new IOException(MESSAGES.getMessage("error.job.info"));
            }
        } catch (BulkV2ClientException bec) {
            throw bec;
        } catch (IOException e) {
            throw new BulkV2ClientException(MESSAGES.getMessage("error.query.job"), e);
        }
    }

    public JobInfoV2 getJobStatus(String jobId) throws IOException {
        String endpoint = null;
        if (operationType == OperationType.LOAD) {
            endpoint = getRestEndpoint() + "jobs/ingest/" + jobId;
        } else {
            endpoint = getRestEndpoint() + "jobs/query/" + jobId;
        }
        return deserializeJsonToObject(doHttpGet(endpoint), JobInfoV2.class);
    }

    public JobInfoV2 updateJob(String jobId, JobStateEnum state) throws BulkV2ClientException {
        String endpoint = getRestEndpoint();
        if (operationType == OperationType.LOAD) {
            endpoint += "jobs/ingest/" + jobId;
        } else {
            endpoint += "jobs/query/" + jobId;
        }
        UpdateJobRequest request = new UpdateJobRequest.Builder(state).build();
        try {
            HttpPatch httpPatch = (HttpPatch) createRequest(endpoint, HttpMethod.PATCH);
            StringEntity entity = new StringEntity(serializeToJson(request), ContentType.APPLICATION_JSON);

            httpPatch.setEntity(entity);
            HttpResponse response = httpclient.execute(httpPatch);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new BulkV2ClientException(response.getStatusLine().getReasonPhrase());
            }
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                return deserializeJsonToObject(responseEntity.getContent(), JobInfoV2.class);
            } else {
                throw new IOException(MESSAGES.getMessage("error.job.info"));
            }
        } catch (BulkV2ClientException bec) {
            throw bec;
        } catch (IOException e) {
            throw new BulkV2ClientException(MESSAGES.getMessage("error.query.job"), e);
        }
    }

    public HttpRequestBase createRequest(String uri, String httpMethod) throws IOException {

        Proxy proxy = config.getProxy();

        if (config.isTraceMessage()) {
            config
                    .getTraceStream()
                    .println("WSC: Creating a new connection to " + uri + " Proxy = " + proxy + " username "
                            + config.getProxyUsername());
        }

        HttpRequestBase request = null;
        switch (httpMethod) {
        case PATCH:
            request = new HttpPatch(uri);
            break;
        case POST:
            request = new HttpPost(uri);
            break;
        case GET:
            request = new HttpGet(uri);
            break;
        case PUT:
            request = new HttpPut(uri);
            break;
        default:
            throw new BulkV2ClientException(MESSAGES.getMessage("error.create.request", httpMethod));
        }

        Map<String, String> httpHeaders = config.getHeaders();

        if (httpHeaders == null || (httpHeaders.get("User-Agent") == null && httpHeaders.get("user-agent") == null)) {
            request.setHeader("User-Agent", VersionInfo.info());
        }

        if (proxy != null && !Proxy.Type.DIRECT.equals(proxy.type())) {
            HttpHost proxyHost = new HttpHost(((InetSocketAddress) proxy.address()).getHostName(),
                    ((InetSocketAddress) proxy.address()).getPort());
            RequestConfig requestConfig = RequestConfig.custom().setProxy(proxyHost).build();
            request.setConfig(requestConfig);
        }

        /*
         * Add all the client specific headers here
         */
        if (httpHeaders != null) {
            for (Map.Entry<String, String> entry : httpHeaders.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }

        request.setHeader(ACCESS_TOKEN, OAUTH_HEADER_NAME + " " + config.getSessionId());

        if (httpHeaders != null) {
            for (Map.Entry<String, String> entry : httpHeaders.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }

        if (config.getReadTimeout() != 0 || config.getConnectionTimeout() != 0) {
            RequestConfig.Builder builder = RequestConfig.custom();
            if (config.getConnectionTimeout() != 0) {
                builder.setConnectionRequestTimeout(config.getConnectionTimeout());
                builder.setConnectTimeout(config.getConnectionTimeout());
                builder.setSocketTimeout(config.getConnectionTimeout());
            }
            RequestConfig requestConfig = builder.build();
            request.setConfig(requestConfig);
        }

        if (config.isTraceMessage()) {
            config
                    .getTraceStream()
                    .println("WSC: Connection configured to have request properties " + request.toString());
        }

        return request;
    }

    public ConnectorConfig getConfig() {
        return config;
    }

    private CloseableHttpClient getHttpClient() {
        if (httpclient == null) {
            if (config != null && config.getProxy() != null) {
                Proxy proxy = config.getProxy();
                if (!Proxy.Type.DIRECT.equals(proxy.type()) && config.getProxyUsername() != null
                        && config.getProxyPassword() != null) {
                    Credentials credentials =
                            new UsernamePasswordCredentials(config.getProxyUsername(), config.getProxyPassword());
                    AuthScope authScope = new AuthScope(((InetSocketAddress) proxy.address()).getHostName(),
                            ((InetSocketAddress) proxy.address()).getPort());
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(authScope, credentials);
                    return HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
                }
            }
            return HttpClients.createDefault();
        }
        return httpclient;
    }

    public enum OperationType {
        LOAD,
        QUERY,
    }
}
