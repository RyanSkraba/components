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
import static java.util.Arrays.asList;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector.LeadKeySelector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.rest.response.CustomObjectResult;
import org.talend.components.marketo.runtime.client.rest.response.DescribeFieldsResult;
import org.talend.components.marketo.runtime.client.rest.response.LeadActivitiesResult;
import org.talend.components.marketo.runtime.client.rest.response.LeadChangesResult;
import org.talend.components.marketo.runtime.client.rest.response.LeadResult;
import org.talend.components.marketo.runtime.client.rest.response.RequestResult;
import org.talend.components.marketo.runtime.client.rest.response.StaticListResult;
import org.talend.components.marketo.runtime.client.rest.response.SyncResult;
import org.talend.components.marketo.runtime.client.rest.type.FieldDescription;
import org.talend.components.marketo.runtime.client.rest.type.LeadActivityRecord;
import org.talend.components.marketo.runtime.client.rest.type.LeadChangeRecord;
import org.talend.components.marketo.runtime.client.rest.type.ListRecord;
import org.talend.components.marketo.runtime.client.type.ListOperationParameters;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.IncludeExcludeFieldsREST;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.ListParam;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.daikon.avro.SchemaConstants;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

public class MarketoRESTClient extends MarketoClient implements MarketoClientServiceExtended {

    public static final String API_PATH_JSON_EXT = ".json";

    public static final String API_PATH_CUSTOMOBJECTS = "/v1/customobjects/";

    public static final String API_PATH_URI_DELETE = "/delete.json";

    public static final String API_PATH_ACTIVITIES = "/v1/activities.json";

    public static final String API_PATH_ACTIVITIES_LEADCHANGES = "/v1/activities/leadchanges.json";

    public static final String API_PATH_ACTIVITIES_TYPES = "/v1/activities/types.json";

    public static final String API_PATH_IDENTITY_OAUTH_TOKEN = "/identity/oauth/token?grant_type=client_credentials";

    public static final String API_PATH_LEADS = "/v1/leads.json";

    public static final String API_PATH_LEADS_DELETE = "/v1/leads/delete.json";

    public static final String API_PATH_LEADS_ISMEMBER = "/leads/ismember.json";

    public static final String API_PATH_LEADS_JSON = "/leads.json";

    public static final String API_PATH_LISTS = "/v1/lists/";

    public static final String API_PATH_LISTS_JSON = "/v1/lists.json";

    public static final String API_PATH_PAGINGTOKEN = "/v1/activities/pagingtoken.json";

    public static final String FIELD_ACCESS_TOKEN = "access_token";

    public static final String FIELD_ACTION = "action";

    public static final String FIELD_ACTIVITY_DATE = "activityDate";

    public static final String FIELD_ACTIVITY_TYPE_ID = "activityTypeId";

    public static final String FIELD_ACTIVITY_TYPE_IDS = "activityTypeIds";

    public static final String FIELD_ACTIVITY_TYPE_VALUE = "activityTypeValue";

    public static final String FIELD_BATCH_SIZE = "batchSize";

    public static final String FIELD_FIELDS = "fields";

    public static final String FIELD_FILTER_TYPE = "filterType";

    public static final String FIELD_FILTER_VALUES = "filterValues";

    public static final String FIELD_ID = "id";

    public static final String FIELD_INPUT = "input";

    public static final String FIELD_LEAD_ID = "leadId";

    public static final String FIELD_LEAD_IDS = "leadIds";

    public static final String FIELD_LIST_ID = "listId";

    public static final String FIELD_LOOKUP_FIELD = "lookupField";

    public static final String FIELD_NAME = "name";

    public static final String FIELD_NEXT_PAGE_TOKEN = "nextPageToken";

    public static final String FIELD_PRIMARY_ATTRIBUTE_VALUE = "primaryAttributeValue";

    public static final String FIELD_PRIMARY_ATTRIBUTE_VALUE_ID = "primaryAttributeValueId";

    public static final String FIELD_SINCE_DATETIME = "sinceDatetime";

    public static final String QUERY_METHOD = "_method";

    public static final String QUERY_METHOD_DELETE = "DELETE";

    public static final String QUERY_METHOD_GET = "GET";

    public static final String QUERY_METHOD_POST = "POST";

    public static final String REST = "REST";

    public static final int REST_API_ACTIVITY_TYPE_IDS_LIMIT = 10;

    public static final int REST_API_BATCH_LIMIT = 300;

    public static final int REST_API_LEAD_IDS_LIMIT = 30;

    public static final String API_PATH_LIST = "/v1/list/";

    private Map<Integer, String> supportedActivities;

    private StringBuilder current_uri;

    private String accessToken;

    private String basicPath = "/rest";

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoRESTClient.class);

    public MarketoRESTClient(TMarketoConnectionProperties connection) throws MarketoException {
        endpoint = connection.endpoint.getValue();
        userId = connection.clientAccessId.getValue();
        secretKey = connection.secretKey.getValue();
        timeout = connection.timeout.getValue();
        retryCount = connection.maxReconnAttemps.getValue();
        retryInterval = connection.attemptsIntervalTime.getValue();
        try {
            if (endpoint == null)
                throw new MarketoException(REST, "The endpoint is null!");
            URI basicURI = new URI(endpoint);
            if (basicURI.getPath() != null) {
                basicPath = basicURI.toString();
            }
            getToken();
        } catch (URISyntaxException e) {
            LOG.error(e.toString());
            throw new MarketoException(REST, e.getMessage());
        } catch (MarketoException e) {
            LOG.error(e.toString());
            throw e;
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
            urlConn.setRequestProperty("accept", "application/json");
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
                    LinkedTreeMap err = (LinkedTreeMap) ((ArrayList) js.get("errors")).get(0);
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
            urlConn.setRequestMethod("GET");
            urlConn.setDoOutput(true);
            urlConn.setRequestProperty("accept", "text/json");
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();
                Reader reader = new InputStreamReader(inStream);
                Gson gson = new Gson();
                return (RequestResult) gson.fromJson(reader, resultClass);
            } else {
                LOG.error("GET request failed: {}", responseCode);
                throw new MarketoException(REST, responseCode, "Request failed! Please check your request setting!");
            }
        } catch (IOException e) {
            // com.google.gson.JsonSyntaxException: java.net.SocketException: Connection reset
            LOG.error("GET request failed: {}", e.getMessage());
            throw new MarketoException(REST, e.getMessage());
        }
    }

    public RequestResult executePostRequest(Class<?> resultClass, JsonObject inputJson) throws MarketoException {
        try {
            URL url = new URL(current_uri.toString());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-type", "application/json");// "application/json" content-type is
            // required.
            urlConn.setRequestProperty("accept", "text/json");
            urlConn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
            wr.write(inputJson.toString());
            wr.flush();
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();
                InputStreamReader reader = new InputStreamReader(inStream);
                Gson gson = new Gson();
                LOG.debug("MarketoRestExecutor.getAccessToken GOT token");
                return (RequestResult) gson.fromJson(reader, resultClass);
                // result = convertStreamToString(inStream);
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

    public Integer getListIdByName(String listName) throws MarketoException {
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_LISTS_JSON)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true)) //
                .append(fmtParams(FIELD_NAME, listName));
        StaticListResult getResponse = (StaticListResult) executeGetRequest(StaticListResult.class);
        if (getResponse != null && getResponse.isSuccess()) {
            if (getResponse.getResult().size() > 0) {
                for (ListRecord listObject : getResponse.getResult()) {
                    return listObject.getId();
                }
            } else {
                throw new MarketoException(REST, "No list match `" + listName + "`.");
            }
        } else if (!getResponse.isSuccess()) {
            throw new MarketoException(REST, getResponse.getErrors().toString());
        }
        return null;
    }

    public String getActivityTypeNameById(int activityId) {
        if (supportedActivities == null) {
            supportedActivities = getLocalActivityTypes();
        }
        return supportedActivities.get(activityId);
    }

    public Map<Integer, String> getLocalActivityTypes() {
        return new HashMap<Integer, String>() {

            {
                put(1, "Visit Webpage");
                put(2, "Fill Out Form");
                put(3, "Click Link");
                put(6, "Send Email");
                put(7, "Email Delivered");
                put(8, "Email Bounced");
                put(9, "Unsubscribe Email");
                put(10, "Open Email");
                put(11, "Click Email");
                put(12, "New Lead");
                put(13, "Change Data Value");
                put(19, "Sync Lead to SFDC");
                put(21, "Convert Lead");
                put(22, "Change Score");
                put(23, "Change Owner");
                put(24, "Add to List");
                put(25, "Remove from List");
                put(26, "SFDC Activity");
                put(27, "Email Bounced Soft");
                put(29, "Delete Lead from SFDC");
                put(30, "SFDC Activity Updated");
                put(32, "Merge Leads");
                put(34, "Add to Opportunity");
                put(35, "Remove from Opportunity");
                put(36, "Update Opportunity");
                put(37, "Delete Lead");
                put(38, "Send Alert");
                put(39, "Send Sales Email");
                put(40, "Open Sales Email");
                put(41, "Click Sales Email");
                put(42, "Add to SFDC Campaign");
                put(43, "Remove from SFDC Campaign");
                put(44, "Change Status in SFDC Campaign");
                put(45, "Receive Sales Email");
                put(46, "Interesting Moment");
                put(47, "Request Campaign");
                put(48, "Sales Email Bounced");
                put(100, "Change Lead Partition");
                put(101, "Change Revenue Stage");
                put(102, "Change Revenue Stage Manually");
                put(104, "Change Status in Progression");
                put(106, "Enrich with Data.com");
                put(108, "Change Segment");
                put(110, "Call Webhook");
                put(111, "Sent Forward to Friend Email");
                put(112, "Received Forward to Friend Email");
                put(113, "Add to Nurture");
                put(114, "Change Nurture Track");
                put(115, "Change Nurture Cadence");
                put(400, "Share Content");
                put(401, "Vote in Poll");
                put(405, "Click Shared Link");
            }
        };
    }

    /*
     *
     *
     * MarketoClient implementation - Frontend to REST API.
     *
     *
     */
    public List<IndexedRecord> convertLeadRecords(List<Map<String, String>> recordList, Schema schema,
            Map<String, String> mappings) {
        List<IndexedRecord> results = new ArrayList<>();

        for (Map<String, String> input : recordList) {
            IndexedRecord record = new Record(schema);
            for (Field f : schema.getFields()) {
                String col = mappings.get(f.name());
                Object tmp = input.get(col);
                if (col != null) {
                    record.put(f.pos(), getValueType(f, tmp));
                }
            }
            results.add(record);
        }
        return results;
    }

    public List<IndexedRecord> convertLeadActivityRecords(List<LeadActivityRecord> recordList, Schema schema,
            Map<String, String> mappings) {
        DateFormat df = new SimpleDateFormat(SCHEMA_DATETIME_PATTERN);
        List<IndexedRecord> results = new ArrayList<>();

        for (LeadActivityRecord input : recordList) {
            IndexedRecord record = new Record(schema);
            for (Field f : schema.getFields()) {
                String col = mappings.get(f.name());
                if (col.equals(FIELD_ID)) {
                    record.put(f.pos(), input.getId());
                } else if (col.equals(FIELD_LEAD_ID)) {
                    record.put(f.pos(), input.getLeadId());
                } else if (col.equals(FIELD_ACTIVITY_DATE)) {
                    record.put(f.pos(), input.getActivityDate());
                } else if (col.equals(FIELD_ACTIVITY_TYPE_ID)) {
                    record.put(f.pos(), input.getActivityTypeId());
                } else if (col.equals(FIELD_ACTIVITY_TYPE_VALUE)) {
                    record.put(f.pos(), getActivityTypeNameById(input.getActivityTypeId()));
                } else if (col.equals(FIELD_PRIMARY_ATTRIBUTE_VALUE_ID)) {
                    record.put(f.pos(), input.getPrimaryAttributeValueId());
                } else if (col.equals(FIELD_PRIMARY_ATTRIBUTE_VALUE)) {
                    record.put(f.pos(), input.getPrimaryAttributeValue());
                } else {
                    for (Map<String, String> attr : input.getAttributes()) {
                        if (attr.get(col) != null) {
                            record.put(f.pos(), attr.get(col));
                        }
                    }
                }
            }
            results.add(record);
        }
        return results;
    }

    public List<IndexedRecord> convertLeadChangesRecords(List<LeadChangeRecord> recordList, Schema schema,
            Map<String, String> mappings) {
        DateFormat df = new SimpleDateFormat(SCHEMA_DATETIME_PATTERN);
        List<IndexedRecord> results = new ArrayList<>();
        Gson gson = new Gson();

        for (LeadChangeRecord input : recordList) {
            IndexedRecord record = new Record(schema);
            for (Field f : schema.getFields()) {
                String col = mappings.get(f.name());
                if (col.equals(FIELD_ID)) {
                    record.put(f.pos(), input.getId());
                } else if (col.equals(FIELD_LEAD_ID)) {
                    record.put(f.pos(), input.getLeadId());
                } else if (col.equals(FIELD_ACTIVITY_DATE)) {
                    record.put(f.pos(), input.getActivityDate());
                } else if (col.equals(FIELD_ACTIVITY_TYPE_ID)) {
                    record.put(f.pos(), input.getActivityTypeId());
                } else if (col.equals(FIELD_ACTIVITY_TYPE_VALUE)) {
                    record.put(f.pos(), getActivityTypeNameById(input.getActivityTypeId()));
                } else if (col.equals(FIELD_FIELDS)) {
                    record.put(f.pos(), gson.toJson(input.getFields()));
                } else {
                    for (Map<String, String> attr : input.getAttributes()) {
                        if (attr.get(col) != null) {
                            record.put(f.pos(), attr.get(col));
                        }
                    }
                }
            }
            results.add(record);
        }
        return results;
    }

    @Override
    public MarketoRecordResult getLead(TMarketoInputProperties parameters, String offset) {
        String filter = parameters.leadKeyTypeREST.getValue().toString();
        String filterValue = parameters.leadKeyValue.getValue();
        String[] fields = parameters.mappingInput.getNameMappingsForMarketo().values().toArray(new String[] {});
        int batchLimit = parameters.batchSize.getValue() > REST_API_BATCH_LIMIT ? REST_API_BATCH_LIMIT
                : parameters.batchSize.getValue();
        MarketoRecordResult mkto = new MarketoRecordResult();
        LeadResult result;
        try {
            current_uri = new StringBuilder(basicPath) //
                    .append(API_PATH_LEADS)//
                    .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
            current_uri.append(fmtParams(FIELD_FILTER_TYPE, filter));
            if (fields != null && fields.length > 0) {
                current_uri.append(fmtParams(FIELD_FIELDS, csvString(fields)));
            }
            if (filterValue != null && !filterValue.isEmpty()) {
                current_uri.append(fmtParams(FIELD_FILTER_VALUES, filterValue));
            }
            current_uri.append(fmtParams(FIELD_BATCH_SIZE, batchLimit));
            if (offset != null && offset.length() > 0) {
                current_uri.append(fmtParams(FIELD_NEXT_PAGE_TOKEN, offset));
            }
            LOG.debug("getLead: {}{}", current_uri);
            result = (LeadResult) executeGetRequest(LeadResult.class);
            mkto.setSuccess(result.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(result.getResult().isEmpty() ? 0 : result.getResult().size());
                mkto.setRemainCount((result.getNextPageToken() != null && result.isMoreResult()) ? batchLimit : 0);
                mkto.setStreamPosition(result.getNextPageToken());
                if (mkto.getRecordCount() > 0)
                    mkto.setRecords(convertLeadRecords(result.getResult(), parameters.schemaInput.schema.getValue(),
                            parameters.mappingInput.getNameMappingsForMarketo()));
            } else {
                mkto.setErrors(Arrays.asList(result.getErrors().get(0)));
            }
        } catch (MarketoException e) {
            LOG.error("Lead error {}.", e.toString());
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
    }

    @Override
    public MarketoRecordResult getMultipleLeads(TMarketoInputProperties parameters, String offset) {
        String filter;
        String[] filterValues;
        String[] fields = parameters.mappingInput.getNameMappingsForMarketo().values().toArray(new String[] {});
        int batchLimit = parameters.batchSize.getValue() > REST_API_BATCH_LIMIT ? REST_API_BATCH_LIMIT
                : parameters.batchSize.getValue();
        MarketoRecordResult mkto = new MarketoRecordResult();
        LeadResult result;
        try {

            if (parameters.leadSelectorREST.getValue().equals(LeadKeySelector)) {
                filter = parameters.leadKeyTypeREST.getValue().toString();
                filterValues = parameters.leadKeyValues.getValue().split(",");
                current_uri = new StringBuilder(basicPath)//
                        .append(MarketoRESTClient.API_PATH_LEADS) //
                        .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));

                current_uri.append(fmtParams(FIELD_FILTER_TYPE, filter));
                if (fields != null && fields.length > 0) {
                    current_uri.append(fmtParams(FIELD_FIELDS, csvString(fields)));
                }
                if (filterValues != null && filterValues.length > 0) {
                    current_uri.append(fmtParams(FIELD_FILTER_VALUES, csvString(filterValues)));
                }
                current_uri.append(fmtParams(FIELD_BATCH_SIZE, batchLimit));
                if (offset != null && offset.length() > 0) {
                    current_uri.append(fmtParams(FIELD_NEXT_PAGE_TOKEN, offset));
                }
                LOG.debug("MultipleLeads: {}{}", current_uri);
                result = (LeadResult) executeGetRequest(LeadResult.class);

            } else {
                int listId;
                if (parameters.listParam.getValue().equals(ListParam.STATIC_LIST_NAME)) {
                    listId = getListIdByName(parameters.listParamValue.getValue());
                } else {
                    listId = Integer.parseInt(parameters.listParamValue.getValue());
                }
                current_uri = new StringBuilder(basicPath) //
                        .append(API_PATH_LIST)//
                        .append(listId)//
                        .append(API_PATH_LEADS_JSON)//
                        .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
                if (fields != null && fields.length > 0) {
                    current_uri.append(fmtParams(FIELD_FIELDS, csvString(fields)));
                }
                current_uri.append(fmtParams(FIELD_BATCH_SIZE, batchLimit));
                if (offset != null) {
                    current_uri.append(fmtParams(FIELD_NEXT_PAGE_TOKEN, offset));
                }
                LOG.debug("LeadsByList : {}.", current_uri);
                result = (LeadResult) executeGetRequest(LeadResult.class);
            }
            mkto.setSuccess(result.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(result.getResult().isEmpty() ? 0 : result.getResult().size());
                mkto.setRemainCount((result.getNextPageToken() != null && mkto.getRecordCount() > 0) ? batchLimit : 0);
                mkto.setStreamPosition(result.getNextPageToken());
                if (mkto.getRecordCount() > 0)
                    mkto.setRecords(convertLeadRecords(result.getResult(), parameters.schemaInput.schema.getValue(),
                            parameters.mappingInput.getNameMappingsForMarketo()));
            } else {
                mkto.setErrors(Arrays.asList(result.getErrors().get(0)));
            }
        } catch (MarketoException e) {
            LOG.error("MultipleLeads error {}.", e.toString());
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
    }

    @Override
    public MarketoRecordResult getLeadActivity(TMarketoInputProperties parameters, String offset) {
        String sinceDateTime = parameters.sinceDateTime.getValue();
        List<String> incs = parameters.setIncludeTypes.getValue() ? parameters.includeTypes.type.getValue()
                : new ArrayList<String>();
        List<String> excs = parameters.setExcludeTypes.getValue() ? parameters.excludeTypes.type.getValue()
                : new ArrayList<String>();
        int batchLimit = parameters.batchSize.getValue() > REST_API_BATCH_LIMIT ? REST_API_BATCH_LIMIT
                : parameters.batchSize.getValue();
        List<Integer> activityTypeIds = new ArrayList<>();
        // no activity provided, we take all
        if (incs.isEmpty()) {
            int limit = 0;
            LOG.warn("No ActivityTypeId provided! Getting 10 first availables (API limit).");
            for (Object s : parameters.includeTypes.type.getPossibleValues()) {
                incs.add(s.toString());
                limit++;
                if (limit == REST_API_ACTIVITY_TYPE_IDS_LIMIT)
                    break;
            }
        }
        // remove unwanted activities
        if (!excs.isEmpty()) {
            for (String s : excs) {
                if (incs.contains(s))
                    incs.remove(s);
            }
        }
        // translate into ids
        for (String i : incs) {
            activityTypeIds.add(IncludeExcludeFieldsREST.valueOf(i).fieldVal);
        }
        MarketoRecordResult mkto = new MarketoRecordResult();
        LeadActivitiesResult result;
        try {
            if (offset == null) {
                offset = getPageToken(sinceDateTime);
            }
            // Marketo API in SOAP and REST return a false estimation of remainCount. Watch out !!!
            current_uri = new StringBuilder(basicPath) //
                    .append(API_PATH_ACTIVITIES) //
                    .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
            if (offset != null && offset.length() > 0) {
                current_uri.append(fmtParams(FIELD_NEXT_PAGE_TOKEN, offset));
            }
            if (activityTypeIds != null) {
                current_uri.append(fmtParams(FIELD_ACTIVITY_TYPE_IDS, csvString(activityTypeIds.toArray())));
            }
            current_uri.append(fmtParams(FIELD_BATCH_SIZE, batchLimit));
            // if (listId != null) { webClient.query(FIELD_LISTID, listId);}
            // if (leadIds != null) { webClient.query(FIELD_LEADIDS, csvString(leadIds)); }
            LOG.debug("Activities: {}.", current_uri);
            result = (LeadActivitiesResult) executeGetRequest(LeadActivitiesResult.class);
            mkto.setSuccess(result.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(result.getResult() == null ? 0 : result.getResult().size());
                mkto.setRemainCount((result.getNextPageToken() != null && result.isMoreResult()) ? batchLimit : 0);
                mkto.setStreamPosition(result.getNextPageToken());
                if (mkto.getRecordCount() > 0)
                    mkto.setRecords(convertLeadActivityRecords(result.getResult(), parameters.schemaInput.schema.getValue(),
                            parameters.mappingInput.getNameMappingsForMarketo()));
            } else {
                mkto.setErrors(Arrays.asList(result.getErrors().get(0)));
            }
        } catch (MarketoException e) {
            LOG.error("LeadActivities error {}.", e.toString());
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
    }

    @Override
    public MarketoRecordResult getLeadChanges(TMarketoInputProperties parameters, String offset) {
        String sinceDateTime = parameters.sinceDateTime.getValue();
        int batchLimit = parameters.batchSize.getValue() > REST_API_BATCH_LIMIT ? REST_API_BATCH_LIMIT
                : parameters.batchSize.getValue();
        String[] fields = parameters.fieldList.getValue().split(",");
        MarketoRecordResult mkto = new MarketoRecordResult();
        LeadChangesResult result;
        try {
            if (offset == null) {
                offset = getPageToken(sinceDateTime);
            }

            current_uri = new StringBuilder(basicPath)//
                    .append(API_PATH_ACTIVITIES_LEADCHANGES)//
                    .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
            if (offset != null && offset.length() > 0) {
                current_uri.append(fmtParams(FIELD_NEXT_PAGE_TOKEN, offset));
            }
            current_uri.append(fmtParams(FIELD_BATCH_SIZE, batchLimit));
            // if (listId != null) { webClient.query(FIELD_LISTID, listId); }
            // if (leadIds != null) { webClient.query(FIELD_LEADIDS, csvString(leadIds)); }
            if (fields != null && fields.length > 0) {
                current_uri.append(fmtParams(FIELD_FIELDS, csvString(fields)));
            }
            LOG.debug("Changes: {}.", current_uri);
            result = (LeadChangesResult) executeGetRequest(LeadChangesResult.class);
            mkto.setSuccess(result.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(result.getResult().isEmpty() ? 0 : result.getResult().size());
                mkto.setRecords(convertLeadChangesRecords(result.getResult(), parameters.schemaInput.schema.getValue(),
                        parameters.mappingInput.getNameMappingsForMarketo()));
                if (result.isMoreResult()) {
                    mkto.setRemainCount(mkto.getRecordCount());// cannot know how many remain...
                    mkto.setStreamPosition(result.getNextPageToken());
                }
            } else {
                mkto.setErrors(Arrays.asList(result.getErrors().get(0)));
            }
        } catch (MarketoException e) {
            LOG.error("LeadChanges error {}.", e.toString());
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
    }
    /*
     *
     * List Operations
     *
     */

    @Override
    public MarketoSyncResult addToList(ListOperationParameters parameters) {
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_LISTS)//
                .append(parameters.getListId())//
                .append(API_PATH_LEADS_JSON)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true))//
                .append(fmtParams(FIELD_ID, csvString(parameters.getLeadIdsValues())))
                .append(fmtParams(QUERY_METHOD, QUERY_METHOD_POST));

        JsonArray json = new JsonArray();
        for (Integer leadId : parameters.getLeadIdsValues()) {
            JsonObject leadKey = new JsonObject();
            leadKey.addProperty(FIELD_ID, leadId);
            json.add(leadKey);
        }
        JsonObject jsonObj = new JsonObject();
        jsonObj.add(FIELD_INPUT, json);

        LOG.debug("addTo: {}.", current_uri);
        SyncResult result = null;
        MarketoSyncResult mkto = new MarketoSyncResult();
        try {
            result = (SyncResult) executePostRequest(SyncResult.class, jsonObj);
            mkto.setSuccess(result.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(result.getResult().isEmpty() ? 0 : result.getResult().size());
                mkto.setRecords(result.getResult());
                if (result.isMoreResult()) {
                    mkto.setRemainCount(mkto.getRecordCount());// cannot know how many remain...
                    mkto.setStreamPosition(result.getNextPageToken());
                }
            } else {
                mkto.setErrors(result.getErrors());
            }
        } catch (MarketoException e) {
            LOG.error("addToList error: {}.", e.toString());
            mkto.setSuccess(false);
            mkto.setErrors(asList(e.toMarketoError()));
        }
        return mkto;
    }

    @Override
    public MarketoSyncResult removeFromList(ListOperationParameters parameters) {

        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_LISTS)//
                .append(parameters.getListId())//
                .append(API_PATH_LEADS_JSON)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true))//
                .append(fmtParams(QUERY_METHOD, QUERY_METHOD_DELETE));
        JsonArray json = new JsonArray();
        for (Integer leadId : parameters.getLeadIdsValues()) {
            JsonObject leadKey = new JsonObject();
            leadKey.addProperty(FIELD_ID, leadId);
            json.add(leadKey);
        }
        JsonObject jsonObj = new JsonObject();
        jsonObj.add(FIELD_INPUT, json);
        LOG.debug("removeFrom: {}{}", current_uri, jsonObj);
        SyncResult result = null;
        MarketoSyncResult mkto = new MarketoSyncResult();
        try {
            result = (SyncResult) executePostRequest(SyncResult.class, jsonObj);
            mkto.setSuccess(result.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(result.getResult().isEmpty() ? 0 : result.getResult().size());
                mkto.setRecords(result.getResult());
                if (result.isMoreResult()) {
                    mkto.setRemainCount(mkto.getRecordCount());// cannot know how many remain...
                    mkto.setStreamPosition(result.getNextPageToken());
                }
            } else {
                mkto.setErrors(result.getErrors());
            }
        } catch (MarketoException e) {
            LOG.error("removeFromList error: {}.", e.toString());
            mkto.setSuccess(false);
            mkto.setErrors(asList(e.toMarketoError()));
        }
        return mkto;
    }

    @Override
    public MarketoSyncResult isMemberOfList(ListOperationParameters parameters) {

        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_LISTS)//
                .append(parameters.getListId())//
                .append(API_PATH_LEADS_ISMEMBER)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true))//
                .append(fmtParams(FIELD_ID, csvString(parameters.getLeadIdsValues())));
        LOG.debug("isMemberOf: {}.", current_uri);
        SyncResult result = null;
        MarketoSyncResult mkto = new MarketoSyncResult();
        try {
            result = (SyncResult) executeGetRequest(SyncResult.class);
            mkto.setSuccess(result.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(result.getResult().isEmpty() ? 0 : result.getResult().size());
                mkto.setRecords(result.getResult());
                if (result.isMoreResult()) {
                    mkto.setRemainCount(mkto.getRecordCount());// cannot know how many remain...
                    mkto.setStreamPosition(result.getNextPageToken());
                }
            } else {
                mkto.setErrors(result.getErrors());
            }
        } catch (MarketoException e) {
            LOG.error("isMemberOfList error: {}.", e.toString());
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
    }
    /*
     *
     * SyncLead Operations
     *
     */

    @Override
    public MarketoSyncResult syncLead(TMarketoOutputProperties parameters, IndexedRecord lead) {
        return syncMultipleLeads(parameters, Arrays.asList(lead));
    }

    @Override
    public MarketoSyncResult syncMultipleLeads(TMarketoOutputProperties parameters, List<IndexedRecord> leads) {
        String action = parameters.operationType.getValue().name();
        String lookupField = parameters.lookupField.getValue().name();
        int batchSize = parameters.batchSize.getValue();

        current_uri = new StringBuilder(basicPath)//
                .append(MarketoRESTClient.API_PATH_LEADS)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        JsonObject inputJson = new JsonObject();
        Gson gson = new Gson();
        // FIXME no partition or asyncProcessing parameters provided by Studio...
        // inputJson.addProperty("asyncProcessing", asyncProcessing);
        // if (partitionName != null) { inputJson.addProperty("partitionName", partitionName); }
        inputJson.addProperty(FIELD_BATCH_SIZE, batchSize);
        if (action != null) {
            inputJson.addProperty(FIELD_ACTION, action);
        }
        if (lookupField != null) {
            inputJson.addProperty(FIELD_LOOKUP_FIELD, lookupField);
        }
        List<Map<String, Object>> leadsObjects = new ArrayList<>();
        for (IndexedRecord r : leads) {
            Map<String, Object> lead = new HashMap<String, Object>();
            for (Field f : r.getSchema().getFields()) {
                lead.put(f.name(), r.get(f.pos()));
            }
            leadsObjects.add(lead);
        }
        inputJson.add(FIELD_INPUT, gson.toJsonTree(leadsObjects));
        MarketoSyncResult mkto = new MarketoSyncResult();
        try {
            LOG.debug("syncMultipleLeads {}{}.", current_uri, inputJson);
            SyncResult rs = (SyncResult) executePostRequest(SyncResult.class, inputJson);
            mkto.setSuccess(rs.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(rs.getResult().isEmpty() ? 0 : rs.getResult().size());
                mkto.setRecords(rs.getResult());
                if (rs.isMoreResult()) {
                    mkto.setRemainCount(mkto.getRecordCount());// cannot know how many remain...
                    mkto.setStreamPosition(rs.getNextPageToken());
                }
            }
        } catch (MarketoException e) {
            LOG.error("syncMultipleLeads error: {}.", e.toString());
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
    }

    /*
     * 
     * management func
     * 
     */

    // public Schema getFieldSchema(){
    // return SchemaBuilder.record("fields").fields()//
    // .name("fieldId").type().
    // .endRecord();
    //
    //
    // }

    public List<Schema.Field> getAllLeadFields() {

        current_uri = new StringBuilder(basicPath)//
                .append("/v1/leads/describe.json")//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        List<Schema.Field> fields = new ArrayList<>();
        try {
            LOG.debug("describeLead {}.", current_uri);
            DescribeFieldsResult rs = (DescribeFieldsResult) executeGetRequest(DescribeFieldsResult.class);
            if (!rs.isSuccess())
                return fields;
            //
            for (FieldDescription d : rs.getResult()) {
                fields.add(d.toAvroField());
            }
        } catch (MarketoException e) {
            LOG.error("describeLeadFields error: {}.", e.toString());
        }
        return fields;
    }

    public MarketoSyncResult deleteLeads(Integer[] leadIds) {

        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_LEADS_DELETE)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true))//
                .append(fmtParams(FIELD_ID, csvString(leadIds)))//
                .append(fmtParams(QUERY_METHOD, QUERY_METHOD_POST));

        JsonArray json = new JsonArray();
        for (Integer leadId : leadIds) {
            JsonObject leadKey = new JsonObject();
            leadKey.addProperty(FIELD_ID, leadId);
            json.add(leadKey);
        }
        JsonObject jsonObj = new JsonObject();
        jsonObj.add(FIELD_INPUT, json);

        MarketoSyncResult mkto = new MarketoSyncResult();
        try {
            LOG.debug("deleteLeads {}{}.", current_uri, jsonObj);
            SyncResult rs = (SyncResult) executePostRequest(SyncResult.class, jsonObj);
            mkto.setSuccess(rs.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(rs.getResult().isEmpty() ? 0 : rs.getResult().size());
                mkto.setRecords(rs.getResult());
                if (rs.isMoreResult()) {
                    mkto.setRemainCount(mkto.getRecordCount());// cannot know how many remain...
                    mkto.setStreamPosition(rs.getNextPageToken());
                }
            }
        } catch (MarketoException e) {
            LOG.error("syncMultipleLeads error: {}.", e.toString());
            mkto.setSuccess(false);
            mkto.setErrors(asList(e.toMarketoError()));
        }
        return mkto;
    }
    /*
     * 
     * Custom Objects
     * 
     */

    /**
     * This method implements
     * http://developers.marketo.com/rest-api/endpoint-reference/lead-database-endpoint-reference/#!/Custom_Objects/describeUsingGET_1
     */
    @Override
    public MarketoRecordResult describeCustomObject(TMarketoInputProperties parameters) {
        String customObjectName = parameters.customObjectName.getValue();
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_CUSTOMOBJECTS)//
                .append(customObjectName)//
                .append("/describe.json")//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        LOG.debug("describeCustomObject : {}.", current_uri);
        MarketoRecordResult mkto = new MarketoRecordResult();
        mkto.setRemainCount(0);
        try {
            CustomObjectResult result = (CustomObjectResult) executeGetRequest(CustomObjectResult.class);
            LOG.debug("result = {}.", result);
            mkto.setSuccess(result.isSuccess());
            mkto.setRequestId(REST + "::" + result.getRequestId());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(1);
                mkto.setRecords(result.getRecords());
            } else {
                if (result.getErrors() != null)
                    mkto.setErrors(result.getErrors());
            }
        } catch (MarketoException e) {
            LOG.error("{}.", e);
            mkto.setSuccess(false);
            mkto.setRecordCount(0);
            mkto.setErrors(Arrays.asList(new MarketoError(REST, e.getMessage())));
        }
        return mkto;
    }

    /**
     * This method implements
     * http://developers.marketo.com/rest-api/endpoint-reference/lead-database-endpoint-reference/#!/Custom_Objects/listCustomObjectsUsingGET
     *
     * FIXME : According the documentation the 'fields' field should be returned but it's not! So, fields is null.
     *
     * TODO : make a BUG REPORT to Marketo.
     */
    @Override
    public MarketoRecordResult listCustomObjects(TMarketoInputProperties parameters) {
        String names = parameters.customObjectNames.getValue();

        current_uri = new StringBuilder(basicPath)//
                .append("/v1/customobjects.json")//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true))//
                .append(fmtParams("names", names))//
                .append(fmtParams(QUERY_METHOD, QUERY_METHOD_GET));
        LOG.debug("listCustomObjects : {}.", current_uri);
        MarketoRecordResult mkto = new MarketoRecordResult();
        mkto.setRemainCount(0);
        try {
            CustomObjectResult result = (CustomObjectResult) executeGetRequest(CustomObjectResult.class);
            LOG.debug("result = {}.", result);
            mkto.setSuccess(result.isSuccess());
            mkto.setRequestId(REST + "::" + result.getRequestId());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(result.getResult().size());
                mkto.setRecords(result.getRecords());
            } else {
                if (result.getErrors() != null)
                    mkto.setErrors(result.getErrors());
            }
        } catch (MarketoException e) {
            LOG.error("{}.", e);
            mkto.setSuccess(false);
            mkto.setRecordCount(0);
            mkto.setErrors(asList(new MarketoError(REST, e.getMessage())));
        }
        return mkto;
    }

    public <T> T getValueType(Field field, Object value) {
        if (value == null)
            return (T) value;
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
            if ((clazz != null && clazz.equals(Date.class.getCanonicalName())) || (pattr != null && !pattr.isEmpty())) {
                Date dt = null;
                try {
                    dt = new SimpleDateFormat(pattr).parse(value.toString());
                    return (T) dt;
                } catch (ParseException e) {
                    LOG.error("Error while parsing date : {} with pattern {}.", e.getMessage(),
                            field.getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
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
        if (customObjectRecords == null || schema == null)
            return records;
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
            urlConn.setRequestProperty("accept", "text/json");
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
                if (!mkr.isSuccess() && ltm.get("errors") != null) {
                    List<LinkedTreeMap> errors = (List<LinkedTreeMap>) ltm.get("errors");
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
            // TODO manage retry on socket execeptions
            LOG.error("GET request failed: {}", e.getMessage());
            throw new MarketoException(REST, e.getMessage());
        }
    }

    public MarketoRecordResult executePostRequest(JsonObject inputJson) throws MarketoException {
        try {
            URL url = new URL(current_uri.toString());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-type", "application/json");// "application/json" content-type is
            // required.
            urlConn.setRequestProperty("accept", "text/json");
            urlConn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
            wr.write(inputJson.toString());
            wr.flush();
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();
                InputStreamReader reader = new InputStreamReader(inStream);
                Gson gson = new Gson();
                LinkedTreeMap ltm = (LinkedTreeMap) gson.fromJson(reader, Object.class);
                MarketoRecordResult mkr = new MarketoRecordResult();
                mkr.setRequestId(REST + "::" + ltm.get("requestId"));
                mkr.setSuccess(Boolean.parseBoolean(ltm.get("success").toString()));
                LOG.warn("ltm = {}.", ltm);
                return mkr;
            } else {
                LOG.error("POST request failed: {}", responseCode);
                throw new MarketoException(REST, responseCode, "Request failed! Please check your request setting!");
            }
        } catch (IOException e) {
            // todo manage retry with socket exceptions
            LOG.error("GET request failed: {}", e.getMessage());
            throw new MarketoException(REST, e.getMessage());
        }
    }

    /**
     * This method implements
     * http://developers.marketo.com/rest-api/endpoint-reference/lead-database-endpoint-reference/#!/Custom_Objects/getCustomObjectsUsingGET
     *
     * Retrieves a list of custom objects records based on filter and set of values. When action is createOnly, idField
     * may not be used as a key and marketoGUID cannot be a member of any object records.
     *
     * 
     * Default :
     *
     * {"requestId":"11883#15a4708d01d","result":[{"seq":0,"marketoGUID":"5ecebec6-1224-4055-a543-2351d1b2feb1","model":"iPhone
     * 7","updatedAt":"2017-02-15T16:25:43Z","createdAt":"2017-02-15T16:25:43Z"},{"seq":1,"marketoGUID":"4ef3c4a9-69f6-4b47-89cd-e71719ec4612","model":"iPhone
     * 7 S","updatedAt":"2017-02-15T16:25:43Z","createdAt":"2017-02-15T16:25:43Z"}],"success":true}
     *
     * With parameter fields=brand,model,customerId
     * {"requestId":"ca9#15a47071363","result":[{"seq":0,"marketoGUID":"5ecebec6-1224-4055-a543-2351d1b2feb1","model":"iPhone
     * 7","customerId":"3113477","brand":"Apple"},{"seq":1,"marketoGUID":"4ef3c4a9-69f6-4b47-89cd-e71719ec4612","model":"iPhone
     * 7 S","customerId":"3113479","brand":"Apple"}],"success":true}
     */
    @Override
    public MarketoRecordResult getCustomObjects(TMarketoInputProperties parameters, String offset) {
        // mandatory fields for request
        String customObjectName = parameters.customObjectName.getValue();
        String filterType = parameters.customObjectFilterType.getValue();
        String filterValues = parameters.customObjectFilterValues.getValue();
        // if fields is unset : marketoGuid, dedupeFields (defined in mkto), updatedAt, createdAt will be returned.
        List<String> fields = new ArrayList<>();
        for (String f : parameters.getSchemaFields()) {
            if (!f.equals(MarketoConstants.FIELD_MARKETO_GUID) && !f.equals(MarketoConstants.FIELD_SEQ))
                fields.add(f);
        }
        //
        int batchLimit = parameters.batchSize.getValue() > REST_API_BATCH_LIMIT ? REST_API_BATCH_LIMIT
                : parameters.batchSize.getValue();
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_CUSTOMOBJECTS)//
                .append(customObjectName)//
                .append(API_PATH_JSON_EXT)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true))//
                .append(fmtParams("filterType", filterType))//
                .append(fmtParams("filterValues", filterValues))//
                .append(fmtParams(FIELD_BATCH_SIZE, batchLimit));
        if (!fields.isEmpty())
            current_uri.append(fmtParams(FIELD_FIELDS, csvString(fields.toArray())));
        if (offset != null)
            current_uri.append(fmtParams(FIELD_NEXT_PAGE_TOKEN, offset));
        // in body :
        // input (Array[CustomObject]): Input list of records. When using a single key, the list is a comma-separated
        // list of values. When using a compound key, the request must be sent as a JSON object, and each record must
        // include each of the fields in the compound key. Compound keys are determined by the contents of
        // 'dedupeFields' in the describe result for the object ,
        LOG.debug("getCustomObjects : {}.", current_uri);
        MarketoRecordResult mkto = new MarketoRecordResult();
        try {
            // Should return:
            // marketoGUID (string): Unique GUID of the custom object records ,
            // reasons (Array[Reason], optional),
            // seq (integer): Integer indicating the sequence of the record in response. This value is
            // correlated to the order of the records included in the request input. Seq should only be part of
            // responses and should not be submitted.
            //
            mkto = executeGetRequest(parameters.schemaInput.schema.getValue());
            LOG.debug("result = {}.", mkto);
        } catch (MarketoException e) {
            LOG.error("{}.", e);
            mkto.setSuccess(false);
            mkto.setRecordCount(0);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
    }

    /**
     * This method implements
     * http://developers.marketo.com/rest-api/endpoint-reference/lead-database-endpoint-reference/#!/Custom_Objects/syncCustomObjectsUsingPOST
     *
     * Inserts, updates, or upserts custom object records to the target instance.
     *
     * 
     */
    @Override
    public MarketoSyncResult syncCustomObjects(TMarketoOutputProperties parameters, List<IndexedRecord> records) {
        // mandatory fields for request
        String customObjectName = parameters.customObjectName.getValue();
        // others
        String action = parameters.customObjectSyncAction.getValue().name();
        String dedupeBy = parameters.customObjectDedupeBy.getValue();
        // input (Array[CustomObject]): List of input records
        JsonObject inputJson = new JsonObject();
        Gson gson = new Gson();
        inputJson.addProperty("action", action);
        if (!dedupeBy.isEmpty()) {
            inputJson.addProperty("dedupeBy", dedupeBy);
        }
        List<Map<String, Object>> leadsObjects = new ArrayList<>();
        for (IndexedRecord r : records) {
            Map<String, Object> lead = new HashMap<String, Object>();
            for (Field f : r.getSchema().getFields()) {
                lead.put(f.name(), r.get(f.pos()));
            }
            leadsObjects.add(lead);
        }
        inputJson.add(FIELD_INPUT, gson.toJsonTree(leadsObjects));
        MarketoSyncResult mkto = new MarketoSyncResult();

        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_CUSTOMOBJECTS)//
                .append(customObjectName)//
                .append(API_PATH_JSON_EXT)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));//

        try {
            LOG.debug("syncCustomObjects {}{}.", current_uri, inputJson);
            SyncResult rs = (SyncResult) executePostRequest(SyncResult.class, inputJson);
            //
            mkto.setRequestId(REST + "::" + rs.getRequestId());
            mkto.setStreamPosition(rs.getNextPageToken());
            mkto.setSuccess(rs.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(rs.getResult().size());
                mkto.setRemainCount(mkto.getStreamPosition() != null ? mkto.getRecordCount() : 0);
                mkto.setRecords(rs.getResult());
            } else {
                mkto.setRecordCount(0);
                mkto.setErrors(Arrays.asList(rs.getErrors().get(0)));
            }
            LOG.debug("rs = {}.", rs);
        } catch (MarketoException e) {
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
    }

    /**
     * This method implements
     * http://developers.marketo.com/rest-api/endpoint-reference/lead-database-endpoint-reference/#!/Custom_Objects/deleteCustomObjectsUsingPOST
     *
     * Deletes a given set of custom object records
     *
     *
     *
     * DeleteCustomObjectRequest
     *
     * { deleteBy (string, optional): Field to delete records by. Permissible values are idField or dedupeFields as
     * indicated by the result of the corresponding describe record,
     *
     * input (Array[CustomObject]): List of input records }
     *
     *
     * CustomObject {
     *
     * marketoGUID (string): Unique GUID of the custom object records ,
     *
     * reasons (Array[Reason], optional),
     *
     * seq (integer): Integer indicating the sequence of the record in response. This value is correlated to the order
     * of the records included in the request input. Seq should only be part of responses and should not be submitted. }
     *
     *
     * Reason {
     *
     * code (string): Integer code of the reason ,
     *
     * message (string): Message describing the reason for the status of the operation }
     *
     *
     */
    @Override
    public MarketoSyncResult deleteCustomObjects(TMarketoOutputProperties parameters, List<IndexedRecord> records) {
        // mandatory fields for request
        String customObjectName = parameters.customObjectName.getValue();
        /*
         * deleteBy : idField || dedupeFields.
         *
         * Sample with describe smartphone : ... "idField": "marketoGUID", "dedupeFields": "[\"model\"]".
         */
        String deleteBy = parameters.customObjectDeleteBy.getValue().name();
        //
        // input (Array[CustomObject]): List of input records
        JsonObject inputJson = new JsonObject();
        Gson gson = new Gson();
        if (!deleteBy.isEmpty()) {
            inputJson.addProperty("deleteBy", deleteBy);
        }
        List<Map<String, Object>> leadsObjects = new ArrayList<>();
        for (IndexedRecord r : records) {
            Map<String, Object> lead = new HashMap<String, Object>();
            for (Field f : r.getSchema().getFields()) {
                lead.put(f.name(), r.get(f.pos()));
            }
            leadsObjects.add(lead);
        }
        inputJson.add(FIELD_INPUT, gson.toJsonTree(leadsObjects));
        //
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_CUSTOMOBJECTS)//
                .append(customObjectName)//
                .append(API_PATH_URI_DELETE)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        MarketoSyncResult mkto = new MarketoSyncResult();
        try {
            LOG.debug("deleteCustomObject {}{}.", current_uri, inputJson);
            SyncResult rs = (SyncResult) executePostRequest(SyncResult.class, inputJson);
            LOG.debug("rs = {}.", rs);
            mkto.setRequestId(REST + "::" + rs.getRequestId());
            mkto.setStreamPosition(rs.getNextPageToken());
            mkto.setSuccess(rs.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(rs.getResult().size());
                mkto.setRemainCount(mkto.getStreamPosition() != null ? mkto.getRecordCount() : 0);
                mkto.setRecords(rs.getResult());
            } else {
                mkto.setRecordCount(0);
                mkto.setErrors(Arrays.asList(new MarketoError(REST, "Could not delete CustomObject.")));
            }
        } catch (MarketoException e) {
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
    }
}
