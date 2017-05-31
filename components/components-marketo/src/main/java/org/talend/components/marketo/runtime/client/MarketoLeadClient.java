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

import static java.util.Arrays.asList;
import static org.talend.components.marketo.MarketoConstants.FIELD_ERROR_MSG;
import static org.talend.components.marketo.MarketoConstants.FIELD_STATUS;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector.LeadKeySelector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.runtime.client.rest.response.DescribeFieldsResult;
import org.talend.components.marketo.runtime.client.rest.response.LeadActivitiesResult;
import org.talend.components.marketo.runtime.client.rest.response.LeadChangesResult;
import org.talend.components.marketo.runtime.client.rest.response.LeadResult;
import org.talend.components.marketo.runtime.client.rest.response.StaticListResult;
import org.talend.components.marketo.runtime.client.rest.response.SyncResult;
import org.talend.components.marketo.runtime.client.rest.type.FieldDescription;
import org.talend.components.marketo.runtime.client.rest.type.LeadActivityRecord;
import org.talend.components.marketo.runtime.client.rest.type.LeadChangeRecord;
import org.talend.components.marketo.runtime.client.rest.type.ListRecord;
import org.talend.components.marketo.runtime.client.type.ListOperationParameters;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.IncludeExcludeFieldsREST;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.ListParam;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MarketoLeadClient extends MarketoBaseRESTClient implements MarketoClientService {

    public static final String API_PATH_ACTIVITIES = "/v1/activities.json";

    public static final String API_PATH_ACTIVITIES_LEADCHANGES = "/v1/activities/leadchanges.json";

    public static final String API_PATH_ACTIVITIES_TYPES = "/v1/activities/types.json";

    public static final String API_PATH_LEADS = "/v1/leads.json";

    public static final String API_PATH_LEADS_DELETE = "/v1/leads/delete.json";

    public static final String API_PATH_LEADS_ISMEMBER = "/leads/ismember.json";

    public static final String API_PATH_LEADS_JSON = "/leads.json";

    public static final String API_PATH_LISTS = "/v1/lists/";

    public static final String API_PATH_LISTS_JSON = "/v1/lists.json";

    public static final String FIELD_ACTIVITY_DATE = "activityDate";

    public static final String FIELD_ACTIVITY_TYPE_ID = "activityTypeId";

    public static final String FIELD_ACTIVITY_TYPE_IDS = "activityTypeIds";

    public static final String FIELD_ACTIVITY_TYPE_VALUE = "activityTypeValue";

    public static final String FIELD_LEAD_ID = "leadId";

    public static final String FIELD_LEAD_IDS = "leadIds";

    public static final String FIELD_LIST_ID = "listId";

    public static final String FIELD_PRIMARY_ATTRIBUTE_VALUE = "primaryAttributeValue";

    public static final String FIELD_PRIMARY_ATTRIBUTE_VALUE_ID = "primaryAttributeValueId";

    public static final String API_PATH_LIST = "/v1/list/";

    private Map<Integer, String> supportedActivities;

    private static final Logger LOG = LoggerFactory.getLogger(MarketoLeadClient.class);

    public MarketoLeadClient(TMarketoConnectionProperties connection) throws MarketoException {
        super(connection);
    }

    /*
     *
     *
     * MarketoClient implementation - Frontend to REST API.
     *
     *
     */
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

    public Integer getListIdByName(String listName) throws MarketoException {
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_LISTS_JSON)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true)) //
                .append(fmtParams(FIELD_NAME, listName));
        StaticListResult getResponse = (StaticListResult) executeGetRequest(StaticListResult.class);
        if (getResponse == null) {
            throw new MarketoException(REST, messages.getMessage("error.response.null"));
        }
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

    @Override
    public MarketoRecordResult getLead(TMarketoInputProperties parameters, String offset) {
        String filter = parameters.leadKeyTypeREST.getValue().toString();
        String filterValue = parameters.leadKeyValue.getValue();
        String[] fields = parameters.mappingInput.getMarketoColumns(parameters.schemaInput.schema.getValue())
                .toArray(new String[] {});
        int batchLimit = parameters.batchSize.getValue() > REST_API_BATCH_LIMIT ? REST_API_BATCH_LIMIT
                : parameters.batchSize.getValue();
        MarketoRecordResult mkto = new MarketoRecordResult();
        LeadResult result;
        try {
            current_uri = new StringBuilder(basicPath) //
                    .append(API_PATH_LEADS)//
                    .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
            StringBuilder input = new StringBuilder();
            input.append(FIELD_FILTER_TYPE + "=" + filter);
            input.append(fmtParams(FIELD_FILTER_VALUES, filterValue));
            input.append(fmtParams(FIELD_BATCH_SIZE, batchLimit));
            if (offset != null && offset.length() > 0) {
                input.append(fmtParams(FIELD_NEXT_PAGE_TOKEN, offset));
            }
            if (fields != null && fields.length > 0) {
                input.append(fmtParams(FIELD_FIELDS, csvString(fields)));
            }
            LOG.debug("getLead: {} body : {}", current_uri, input);
            result = executeFakeGetRequestForLead(input.toString());
            mkto.setSuccess(result.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(result.getResult().isEmpty() ? 0 : result.getResult().size());
                mkto.setRemainCount((result.getNextPageToken() != null && result.isMoreResult()) ? batchLimit : 0);
                mkto.setStreamPosition(result.getNextPageToken());
                if (mkto.getRecordCount() > 0) {
                    mkto.setRecords(convertLeadRecords(result.getResult(), parameters.schemaInput.schema.getValue(),
                            parameters.mappingInput.getNameMappingsForMarketo()));
                }
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
        String[] fields = parameters.mappingInput.getMarketoColumns(parameters.schemaInput.schema.getValue())
                .toArray(new String[] {});
        int batchLimit = parameters.batchSize.getValue() > REST_API_BATCH_LIMIT ? REST_API_BATCH_LIMIT
                : parameters.batchSize.getValue();
        MarketoRecordResult mkto = new MarketoRecordResult();
        LeadResult result;
        try {
            StringBuilder input = new StringBuilder();

            if (parameters.leadSelectorREST.getValue().equals(LeadKeySelector)) {
                filter = parameters.leadKeyTypeREST.getValue().toString();
                filterValues = parameters.leadKeyValues.getValue().split(",");
                current_uri = new StringBuilder(basicPath)//
                        .append(MarketoRESTClient.API_PATH_LEADS) //
                        .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));

                input.append(FIELD_FILTER_TYPE + "=" + filter);
                if (fields != null && fields.length > 0) {
                    input.append(fmtParams(FIELD_FIELDS, csvString(fields)));
                }
                if (filterValues != null && filterValues.length > 0) {
                    input.append(fmtParams(FIELD_FILTER_VALUES, csvString(filterValues)));
                }
                input.append(fmtParams(FIELD_BATCH_SIZE, batchLimit));
                if (offset != null && offset.length() > 0) {
                    input.append(fmtParams(FIELD_NEXT_PAGE_TOKEN, offset));
                }
                LOG.debug("MultipleLeads: {} body{}", current_uri, input);
                result = executeFakeGetRequestForLead(input.toString());
            } else {
                int listId;
                if (parameters.listParam.getValue().equals(ListParam.STATIC_LIST_NAME)) {
                    listId = getListIdByName(parameters.listParamListName.getValue());
                } else {
                    listId = parameters.listParamListId.getValue();
                }
                current_uri = new StringBuilder(basicPath) //
                        .append(API_PATH_LIST)//
                        .append(listId)//
                        .append(API_PATH_LEADS_JSON)//
                        .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
                if (fields != null && fields.length > 0) {
                    input.append(fmtParams(FIELD_FIELDS, csvString(fields)));
                }
                input.append(fmtParams(FIELD_BATCH_SIZE, batchLimit));
                if (offset != null) {
                    input.append(fmtParams(FIELD_NEXT_PAGE_TOKEN, offset));
                }
                LOG.debug("LeadsByList : {} body: {}.", current_uri, input);
                result = executeFakeGetRequestForLead(input.toString());
            }
            mkto.setSuccess(result.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(result.getResult().isEmpty() ? 0 : result.getResult().size());
                mkto.setRemainCount((result.getNextPageToken() != null && mkto.getRecordCount() > 0) ? batchLimit : 0);
                mkto.setStreamPosition(result.getNextPageToken());
                if (mkto.getRecordCount() > 0) {
                    mkto.setRecords(convertLeadRecords(result.getResult(), parameters.schemaInput.schema.getValue(),
                            parameters.mappingInput.getNameMappingsForMarketo()));
                }
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
        List<String> incs = parameters.includeTypes.type.getValue();
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
                if (limit == REST_API_ACTIVITY_TYPE_IDS_LIMIT) {
                    break;
                }
            }
        }
        // remove unwanted activities
        if (!excs.isEmpty()) {
            for (String s : excs) {
                if (incs.contains(s)) {
                    incs.remove(s);
                }
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
                if (mkto.getRecordCount() > 0) {
                    mkto.setRecords(convertLeadActivityRecords(result.getResult(), parameters.schemaInput.schema.getValue(),
                            parameters.mappingInput.getNameMappingsForMarketo()));
                }
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
                .append(API_PATH_LEADS)//
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
                // skip status & error fields
                if (FIELD_STATUS.equals(f.name()) || FIELD_ERROR_MSG.equals(f.name())) {
                    continue;
                }
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
                .append("/v1/leads")//
                .append(API_PATH_URI_DESCRIBE)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        List<Schema.Field> fields = new ArrayList<>();
        try {
            LOG.debug("describeLead {}.", current_uri);
            DescribeFieldsResult rs = (DescribeFieldsResult) executeGetRequest(DescribeFieldsResult.class);
            if (!rs.isSuccess()) {
                return fields;
            }
            //
            for (FieldDescription d : rs.getResult()) {
                fields.add(d.toAvroField());
            }
        } catch (MarketoException e) {
            LOG.error("describeLeadFields error: {}.", e.toString());
        }
        return fields;
    }

    public MarketoSyncResult deleteLeads(List<IndexedRecord> leadIds) {
        List<Integer> leads = new ArrayList<>();
        for (IndexedRecord r : leadIds) {
            leads.add((Integer) r.get(0));
        }
        return deleteLeads(leads.toArray(new Integer[leads.size()]));
    }

    public MarketoSyncResult deleteLeads(Integer[] leadIds) {
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_LEADS_DELETE)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true))//
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

}
