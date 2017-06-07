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
package org.talend.components.marketo.runtime.client;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.runtime.client.rest.response.LeadResult;
import org.talend.components.marketo.runtime.client.rest.response.RequestResult;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

public abstract class MarketoBaseRESTClient extends MarketoClient {

    public static final int REST_API_ACTIVITY_TYPE_IDS_LIMIT = 10;

    public static final int REST_API_BATCH_LIMIT = 300;

    public static final int REST_API_LEAD_IDS_LIMIT = 30;

    public static final String API_PATH_PAGINGTOKEN = "/v1/activities/pagingtoken.json";

    public static final String API_PATH_IDENTITY_OAUTH_TOKEN = "/identity/oauth/token?grant_type=client_credentials";

    public static final String API_PATH_JSON_EXT = ".json";

    public static final String API_PATH_URI_DESCRIBE = "/describe.json";

    public static final String API_PATH_URI_DELETE = "/delete.json";

    public static final String API_PATH_URI_IMPORT = "import.json";

    public static final String FIELD_ACCESS_TOKEN = "access_token";

    public static final String FIELD_ACTION = "action";

    public static final String FIELD_BATCH_SIZE = "batchSize";

    public static final String FIELD_FIELDS = "fields";

    public static final String FIELD_FILTER_TYPE = "filterType";

    public static final String FIELD_FILTER_VALUES = "filterValues";

    public static final String FIELD_FORMAT = "format";

    public static final String FIELD_ID = "id";

    public static final String FIELD_INPUT = "input";

    public static final String FIELD_LOOKUP_FIELD = "lookupField";

    public static final String FIELD_NAME = "name";

    public static final String FIELD_NEXT_PAGE_TOKEN = "nextPageToken";

    public static final String FIELD_PARTITION_NAME = "partitionName";

    public static final String FIELD_SINCE_DATETIME = "sinceDatetime";

    public static final String QUERY_METHOD = "_method";

    public static final String QUERY_METHOD_DELETE = "DELETE";

    public static final String QUERY_METHOD_GET = "GET";

    public static final String QUERY_METHOD_POST = "POST";

    public static final String REST = "REST";

    public static final String REQUEST_VALUE_TEXT_JSON = "text/json";

    public static final String REQUEST_PROPERTY_ACCEPT = "accept";

    public static final String REQUEST_VALUE_APPLICATION_JSON = "application/json";

    public static final String FIELD_ERRORS = "errors";

    public static final String REQUEST_PROPERTY_CONTENT_TYPE = "Content-type";

    public static final String REQUEST_VALUE_APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private Map<Integer, String> supportedActivities;

    protected StringBuilder current_uri;

    protected String accessToken;

    protected String basicPath = "/rest";

    protected String bulkPath = "/bulk";

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoBaseRESTClient.class);

    protected static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(MarketoBaseRESTClient.class);

    public MarketoBaseRESTClient(TMarketoConnectionProperties connection) throws MarketoException {
        endpoint = connection.endpoint.getValue();
        userId = connection.clientAccessId.getValue();
        secretKey = connection.secretKey.getValue();
        timeout = connection.timeout.getValue();
        retryCount = connection.maxReconnAttemps.getValue();
        retryInterval = connection.attemptsIntervalTime.getValue();
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
        } catch (MalformedURLException e) {
            LOG.error(e.toString());
            throw new MarketoException(REST, e.getMessage());
        }
    }

    @Override
    public String getApi() {
        return REST;
    }

    @Override
    public String toString() {
        return format("Marketo REST API Client [%s].", endpoint);
    }

    public void getToken() throws MarketoException {
        try {
            URL basicURI = new URL(endpoint);
            current_uri = new StringBuilder(basicURI.getProtocol())//
                    .append("://")//
                    .append(basicURI.getHost())//
                    .append(API_PATH_IDENTITY_OAUTH_TOKEN)//
                    .append(fmtParams("client_id", userId))//
                    .append(fmtParams("client_secret", secretKey));
            URL url = new URL(current_uri.toString());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.setRequestProperty(REQUEST_PROPERTY_ACCEPT, REQUEST_VALUE_APPLICATION_JSON);
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();
                Reader reader = new InputStreamReader(inStream);
                Gson gson = new Gson();
                LinkedTreeMap js = (LinkedTreeMap) gson.fromJson(reader, Object.class);
                Object ac = js.get("access_token");
                if (ac != null) {
                    accessToken = ac.toString();
                    LOG.debug("MarketoRestExecutor.getAccessToken GOT token");
                } else {
                    LinkedTreeMap err = (LinkedTreeMap) ((ArrayList) js.get(FIELD_ERRORS)).get(0);
                    throw new MarketoException(REST, err.get("code").toString(), err.get("message").toString());
                }
            } else {
                throw new MarketoException(REST, responseCode, "Marketo Authentication failed! Please check your " + "setting!");
            }
        } catch (ProtocolException | SocketTimeoutException | SocketException e) {
            // TODO mangage SocketTimeoutException and SocketException with timeout and retry properties
            LOG.error("AccessToken error: {}.", e.getMessage());
            throw new MarketoException(REST, "Marketo Authentication failed : " + e.getMessage());
        } catch (IOException e) {
            LOG.error("AccessToken error: {}.", e.getMessage());
            throw new MarketoException(REST, "Marketo Authentication failed : " + e.getMessage());
        }
    }

    public boolean isAvailable() {
        return accessToken != null;
    }

    public boolean isAccessTokenExpired(List<MarketoError> errors) {
        if (errors != null) {
            for (MarketoError error : errors) {
                if ("602".equals(error.getCode())) {
                    return true;
                }
            }
        }
        return false;
    }

    public RequestResult executeGetRequest(Class<?> resultClass) throws MarketoException {
        try {
            URL url = new URL(current_uri.toString());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod(QUERY_METHOD_GET);
            urlConn.setDoOutput(true);
            urlConn.setRequestProperty(REQUEST_PROPERTY_ACCEPT, REQUEST_VALUE_TEXT_JSON);
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();
                Reader reader = new InputStreamReader(inStream);
                Gson gson = new Gson();
                return (RequestResult) gson.fromJson(reader, resultClass);
            } else {
                LOG.error("GET request failed: {}.", responseCode);
                throw new MarketoException(REST, responseCode, "Request failed! Please check your request setting!");
            }
        } catch (IOException e) {
            LOG.error("GET request failed: {}", e.getMessage());
            throw new MarketoException(REST, e.getMessage());
        }
    }

    private String convertStreamToString(InputStream inputStream) {
        try {
            return new Scanner(inputStream).useDelimiter("\\A").next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    public InputStreamReader httpFakeGet(String content, boolean isForLead) throws MarketoException {
        try {
            current_uri.append(fmtParams(QUERY_METHOD, QUERY_METHOD_GET));
            URL url = new URL(current_uri.toString());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod(QUERY_METHOD_POST);
            if (isForLead) {
                urlConn.setRequestProperty(REQUEST_PROPERTY_CONTENT_TYPE, REQUEST_VALUE_APPLICATION_X_WWW_FORM_URLENCODED);
            } else {
                urlConn.setRequestProperty(REQUEST_PROPERTY_CONTENT_TYPE, REQUEST_VALUE_APPLICATION_JSON);
            }
            urlConn.setRequestProperty(REQUEST_PROPERTY_ACCEPT, REQUEST_VALUE_TEXT_JSON);
            urlConn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
            wr.write(content);
            wr.flush();
            wr.close();
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();
                InputStreamReader reader = new InputStreamReader(inStream);
                return reader;
            } else {
                LOG.error("POST request failed: {}", responseCode);
                throw new MarketoException(REST, responseCode, "Request failed! Please check your request setting!");
            }
        } catch (IOException e) {
            LOG.error("POST request failed: {}", e.getMessage());
            throw new MarketoException(REST, e.getMessage());
        }
    }

    public LeadResult executeFakeGetRequestForLead(String input) throws MarketoException {
        return new Gson().fromJson(httpFakeGet(input, true), LeadResult.class);
    }

    public RequestResult executeFakeGetRequest(Class<?> resultClass, String input) throws MarketoException {
        return (RequestResult) new Gson().fromJson(httpFakeGet(input, false), resultClass);
    }

    public MarketoRecordResult executeFakeGetRequest(Schema schema, String input) throws MarketoException {
        InputStreamReader reader = httpFakeGet(input, false);
        // TODO refactor this part with method executeGetRequest(Schema s);
        Gson gson = new Gson();
        MarketoRecordResult mkr = new MarketoRecordResult();
        LinkedTreeMap ltm = (LinkedTreeMap) gson.fromJson(reader, Object.class);
        LOG.debug("ltm = {}.", ltm);
        mkr.setRequestId(REST + "::" + ltm.get("requestId"));
        mkr.setSuccess(Boolean.parseBoolean(ltm.get("success").toString()));
        mkr.setStreamPosition((String) ltm.get("nextPageToken"));
        if (!mkr.isSuccess() && ltm.get(FIELD_ERRORS) != null) {
            List<LinkedTreeMap> errors = (List<LinkedTreeMap>) ltm.get(FIELD_ERRORS);
            for (LinkedTreeMap err : errors) {
                MarketoError error = new MarketoError(REST, (String) err.get("code"), (String) err.get("message"));
                mkr.setErrors(Arrays.asList(error));
            }
        }
        if (mkr.isSuccess()) {
            List<LinkedTreeMap> tmp = (List<LinkedTreeMap>) ltm.get("result");
            if (tmp != null) {
                mkr.setRecordCount(tmp.size());
                mkr.setRecords(parseCustomObjectRecords(tmp, schema));
            }
            if (mkr.getStreamPosition() != null) {
                mkr.setRemainCount(mkr.getRecordCount());
            }
        }
        return mkr;
    }

    public RequestResult executePostRequest(Class<?> resultClass, JsonObject inputJson) throws MarketoException {
        try {
            URL url = new URL(current_uri.toString());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod(QUERY_METHOD_POST);
            urlConn.setRequestProperty(REQUEST_PROPERTY_CONTENT_TYPE, REQUEST_VALUE_APPLICATION_JSON);
            urlConn.setRequestProperty(REQUEST_PROPERTY_ACCEPT, REQUEST_VALUE_TEXT_JSON);
            urlConn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
            wr.write(inputJson.toString());
            wr.flush();
            wr.close();
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();
                InputStreamReader reader = new InputStreamReader(inStream);
                Gson gson = new Gson();
                // LOG.error("{}", convertStreamToString(inStream));
                return (RequestResult) gson.fromJson(reader, resultClass);
            } else {
                LOG.error("POST request failed: {}", responseCode);
                throw new MarketoException(REST, responseCode, "Request failed! Please check your request setting!");
            }
        } catch (IOException e) {
            LOG.error("GET request failed: {}", e.getMessage());
            throw new MarketoException(REST, e.getMessage());
        }
    }

    public String fmtParams(String paramName, Object paramValue, boolean first) {
        return String.format(first ? "?%s=%s" : "&%s=%s", paramName, paramValue);
    }

    public String fmtParams(String paramName, Object paramValue) {
        return fmtParams(paramName, paramValue, false);
    }

    public static String csvString(Object[] fields) {
        StringBuilder fieldCsv = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            fieldCsv.append(fields[i]);
            if (i + 1 != fields.length) {
                fieldCsv.append(",");
            }
        }
        return fieldCsv.toString();
    }

    public String getPageToken(String sinceDatetime) throws MarketoException {
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_PAGINGTOKEN)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true))//
                .append(fmtParams(FIELD_SINCE_DATETIME, sinceDatetime));
        LeadResult getResponse = (LeadResult) executeGetRequest(LeadResult.class);
        if (getResponse != null) {
            return getResponse.getNextPageToken();
        }
        return null;
    }

    public <T> T getValueType(Field field, Object value) {
        if (value == null) {
            return (T) value;
        }
        Schema convSchema = field.schema();
        Schema.Type type = field.schema().getType();
        if (convSchema.getType().equals(Type.UNION)) {
            for (Schema s : field.schema().getTypes()) {
                if (s.getType() != Type.NULL) {
                    type = s.getType();
                    break;
                }
            }
        }
        switch (type) {
        case STRING:
            return (T) value;
        case INT:
            return (T) (Integer) Float.valueOf(value.toString()).intValue();
        case BOOLEAN:
            return (T) Boolean.valueOf(value.toString());
        case FLOAT:
            return (T) Float.valueOf(value.toString());
        case DOUBLE:
            return (T) Double.valueOf(value.toString());
        case LONG:
            String clazz = field.getProp(SchemaConstants.JAVA_CLASS_FLAG);
            String pattr = field.getProp(SchemaConstants.TALEND_COLUMN_PATTERN);
            if ((clazz != null && clazz.equals(Date.class.getCanonicalName())) || !StringUtils.isEmpty(pattr)) {
                Date dt = null;
                try {
                    // Mkto returns datetime in UTC and Follows W3C format (ISO 8601).
                    dt = new DateTime(value.toString(), DateTimeZone.forID("UTC")).toDate();
                    return (T) dt;
                } catch (Exception e) {
                    LOG.error("Error while parsing date : {}.", e.getMessage());
                }
            } else {
                return (T) Long.valueOf(value.toString());
            }
            break;
        default:
            LOG.warn("Not managed -> type: {}, value: {} for field: {}.", convSchema.getType(), value, field);
            return (T) value;
        }
        return null;
    }

    public List<IndexedRecord> parseCustomObjectRecords(List<LinkedTreeMap> customObjectRecords, Schema schema) {
        List<IndexedRecord> records = new ArrayList<>();
        if (customObjectRecords == null || schema == null) {
            return records;
        }
        for (LinkedTreeMap cor : customObjectRecords) {
            IndexedRecord record = new GenericData.Record(schema);
            for (Field f : schema.getFields()) {
                Object o = cor.get(f.name());
                record.put(f.pos(), getValueType(f, o));
            }
            records.add(record);
        }
        return records;
    }

    public MarketoRecordResult executeGetRequest(Schema schema) throws MarketoException {
        try {
            URL url = new URL(current_uri.toString());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.setDoOutput(true);
            urlConn.setRequestProperty(REQUEST_PROPERTY_ACCEPT, REQUEST_VALUE_TEXT_JSON);
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();
                Reader reader = new InputStreamReader(inStream);
                Gson gson = new Gson();
                MarketoRecordResult mkr = new MarketoRecordResult();
                LinkedTreeMap ltm = (LinkedTreeMap) gson.fromJson(reader, Object.class);
                mkr.setRequestId(REST + "::" + ltm.get("requestId"));
                mkr.setSuccess(Boolean.parseBoolean(ltm.get("success").toString()));
                mkr.setStreamPosition((String) ltm.get("nextPageToken"));
                if (!mkr.isSuccess() && ltm.get(FIELD_ERRORS) != null) {
                    List<LinkedTreeMap> errors = (List<LinkedTreeMap>) ltm.get(FIELD_ERRORS);
                    for (LinkedTreeMap err : errors) {
                        MarketoError error = new MarketoError(REST, (String) err.get("code"), (String) err.get("message"));
                        mkr.setErrors(Arrays.asList(error));
                    }
                }
                if (mkr.isSuccess()) {
                    List<LinkedTreeMap> tmp = (List<LinkedTreeMap>) ltm.get("result");
                    if (tmp != null) {
                        mkr.setRecordCount(tmp.size());
                        mkr.setRecords(parseCustomObjectRecords(tmp, schema));
                    }
                    if (mkr.getStreamPosition() != null) {
                        mkr.setRemainCount(mkr.getRecordCount());
                    }
                }
                return mkr;
            } else {
                LOG.error("GET request failed: {}", responseCode);
                throw new MarketoException(REST, responseCode, "Request failed! Please check your request setting!");
            }

        } catch (IOException e) {
            LOG.error("GET request failed: {}", e.getMessage());
            throw new MarketoException(REST, e.getMessage());
        }
    }

    public MarketoRecordResult executePostRequest(JsonObject inputJson) throws MarketoException {
        try {
            URL url = new URL(current_uri.toString());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty(REQUEST_PROPERTY_CONTENT_TYPE, REQUEST_VALUE_APPLICATION_JSON);// "application/json"
            // content-type is
            // required.
            urlConn.setRequestProperty(REQUEST_PROPERTY_ACCEPT, REQUEST_VALUE_TEXT_JSON);
            urlConn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
            wr.write(inputJson.toString());
            wr.flush();
            wr.close();
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();
                InputStreamReader reader = new InputStreamReader(inStream);
                Gson gson = new Gson();
                LinkedTreeMap ltm = (LinkedTreeMap) gson.fromJson(reader, Object.class);
                MarketoRecordResult mkr = new MarketoRecordResult();
                mkr.setRequestId(REST + "::" + ltm.get("requestId"));
                mkr.setSuccess(Boolean.parseBoolean(ltm.get("success").toString()));
                return mkr;
            } else {
                LOG.error("POST request failed: {}", responseCode);
                throw new MarketoException(REST, responseCode, "Request failed! Please check your request setting!");
            }
        } catch (IOException e) {
            LOG.error("GET request failed: {}", e.getMessage());
            throw new MarketoException(REST, e.getMessage());
        }
    }

}
