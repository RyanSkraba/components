// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.rest.response.SyncResult;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static org.talend.components.marketo.MarketoConstants.REST_API_LIMIT;

public class MarketoCampaignClient extends MarketoBulkExecClient {

    public static final String API_PATH_CAMPAIGNS = "/v1/campaigns";

    public static final String API_PATH_CAMPAIGNS_SCHEDULE = API_PATH_CAMPAIGNS + "/%s/schedule.json";

    public static final String API_PATH_CAMPAIGNS_TRIGGER = API_PATH_CAMPAIGNS + "/%s/trigger.json";

    public static final String API_PATH_CAMPAIGN_TRIGGER_DESACTIVATE_ACTIVATE = "/asset/v1/smartCampaign/%s/%s.json";

    public static final String FIELD_PROGRAM_NAME = "programName";

    public static final String FIELD_WORKSPACE_NAME = "workspaceName";

    public static final String FIELD_CLONE_TO_PROGRAM_NAME = "cloneToProgramName";

    public static final String FIELD_RUN_AT = "runAt";

    public static final String FIELD_LEADS = "leads";

    public static final String FIELD_TOKENS = "tokens";

    private static final Logger LOG = LoggerFactory.getLogger(MarketoCampaignClient.class);

    public MarketoCampaignClient(TMarketoConnectionProperties connection) throws MarketoException {
        super(connection);
    }

    /**
     * Returns a list of campaign records.
     *
     * @param parameters
     * @return
     */
    public MarketoRecordResult getCampaigns(TMarketoCampaignProperties parameters, String offset) {
        String ids = parameters.campaignIds.getValue();
        String names = parameters.campaignNames.getValue();
        String programNames = parameters.programNames.getValue();
        String workspaceNames = parameters.workspaceNames.getValue();
        int batchLimit = parameters.batchSize.getValue() > REST_API_LIMIT ? REST_API_LIMIT : parameters.batchSize.getValue();
        //
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_CAMPAIGNS)//
                .append(API_PATH_JSON_EXT)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true))//
                .append(fmtParams(FIELD_BATCH_SIZE, batchLimit));
        if (!StringUtils.isEmpty(ids)) {
            current_uri.append(fmtParams(FIELD_ID, csvString(ids.split(","))));
        }
        if (!StringUtils.isEmpty(names)) {
            current_uri.append(fmtParams(FIELD_NAME, csvString(names.split(","))));
        }
        if (!StringUtils.isEmpty(programNames)) {
            current_uri.append(fmtParams(FIELD_PROGRAM_NAME, csvString(programNames.split(","))));
        }
        if (!StringUtils.isEmpty(workspaceNames)) {
            current_uri.append(fmtParams(FIELD_WORKSPACE_NAME, csvString(workspaceNames.split(","))));
        }
        if (!StringUtils.isEmpty(offset)) {
            current_uri.append(fmtParams(FIELD_NEXT_PAGE_TOKEN, offset));
        }
        LOG.debug("getCampaigns : {}.", current_uri);
        return getRecordResultForFromRequestBySchema(parameters.schemaInput.schema.getValue(), false, null);
    }

    /**
     * Returns the record of a campaign by its id.
     *
     * @param parameters
     * @return
     */
    public MarketoRecordResult getCampaignById(TMarketoCampaignProperties parameters) {
        int cId = parameters.campaignId.getValue();
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_CAMPAIGNS)//
                .append("/")//
                .append(cId)//
                .append(API_PATH_JSON_EXT)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        LOG.debug("getCampaignById : {}.", current_uri);
        return getRecordResultForFromRequestBySchema(parameters.schemaInput.schema.getValue(), false, null);
    }

    /**
     * ScheduleCampaignRequest
     * <p>
     * Remotely schedules a batch campaign to run at a given time. My tokens local to the campaign's parent program call be
     * overridden for the run to customize content.
     */
    public MarketoRecordResult scheduleCampaign(TMarketoCampaignProperties parameters) {
        String campaignId = parameters.campaignId.getStringValue();
        String clone = parameters.cloneToProgramName.getStringValue();
        String runat = parameters.runAt.getStringValue();
        JsonObject inputJson = new JsonObject();
        Gson gson = new Gson();
        if (!StringUtils.isEmpty(clone)) {
            inputJson.addProperty(FIELD_CLONE_TO_PROGRAM_NAME, parameters.cloneToProgramName.getValue());
        }
        if (!StringUtils.isEmpty(runat)) {
            SimpleDateFormat sdf = new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_REST);
            inputJson.addProperty(FIELD_RUN_AT, sdf.format(parameters.runAt.getValue()));
        }
        if (parameters.campaignTokens.size() > 0) {
            inputJson.add(FIELD_TOKENS, gson.toJsonTree(parameters.campaignTokens.getTokensAsJson()));
        }
        JsonObject jsonObj = new JsonObject();
        jsonObj.add(FIELD_INPUT, inputJson);
        MarketoRecordResult mkto = new MarketoRecordResult();
        current_uri = new StringBuilder(basicPath)//
                .append(String.format(API_PATH_CAMPAIGNS_SCHEDULE, campaignId))//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));//
        LOG.debug("scheduleCampaign {}{}.", current_uri, jsonObj);
        try {
            SyncResult rs = (SyncResult) executePostRequest(SyncResult.class, jsonObj);
            LOG.debug("[scheduleCampaign] {}", rs);
            //
            mkto.setRequestId(REST + "::" + rs.getRequestId());
            mkto.setSuccess(rs.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(rs.getResult().size());
                mkto.setRemainCount(0);
                IndexedRecord record = new Record(MarketoConstants.scheduleCampaignSchema());
                record.put(0, rs.getResult().get(0).getId());
                record.put(1, "scheduled");
                mkto.setRecords(Collections.singletonList(record));
            } else {
                mkto.setRecordCount(0);
                mkto.setErrors(rs.getErrors());
            }
        } catch (MarketoException e) {
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
    }

    /**
     * triggerCampaign (aka requestCampaign)
     * <p>
     * Passes a set of leads to a trigger campaign to run through the campaign's flow. The designated campaign must have a
     * Campaign is Requested: Web Service API trigger, and must be active. My tokens local to the campaign's parent program
     * can be overridden for the run to customize content
     */

    public MarketoRecordResult activateDeactivateCampaign(TMarketoCampaignProperties parameters) {
        String campaignId = parameters.campaignId.getStringValue();
        String action = parameters.triggerAction.getValue().name();
        current_uri = new StringBuilder(basicPath)//
                .append(String.format(API_PATH_CAMPAIGN_TRIGGER_DESACTIVATE_ACTIVATE, campaignId, action))//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));//
        LOG.debug("[activateDeactivateCampaign] ({}) {}.", action, current_uri);
        return getRecordResultFromPostRequest(parameters.schemaFlow.schema.getValue(), new JsonObject());
    }

    public MarketoSyncResult requestCampaign(TMarketoCampaignProperties parameters, List<IndexedRecord> records) {
        String campaignId = parameters.campaignId.getStringValue();
        JsonObject inputJson = new JsonObject();
        Gson gson = new Gson();
        List<Map<String, Integer>> leads = new ArrayList<>();
        for (IndexedRecord r : records) {
            Map<String, Integer> lead = new HashMap<>();
            lead.put("id", Integer.valueOf(r.get(r.getSchema().getField("leadId").pos()).toString()));
            leads.add(lead);
        }
        inputJson.add(FIELD_LEADS, gson.toJsonTree(leads));
        if (parameters.campaignTokens.size() > 0) {
            inputJson.add(FIELD_TOKENS, gson.toJsonTree(parameters.campaignTokens.getTokensAsJson()));
        }
        JsonObject jsonObj = new JsonObject();
        jsonObj.add(FIELD_INPUT, inputJson);
        MarketoSyncResult mkto = new MarketoSyncResult();
        current_uri = new StringBuilder(basicPath)//
                .append(String.format(API_PATH_CAMPAIGNS_TRIGGER, campaignId))//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));//
        LOG.debug("scheduleCampaign {}{}.", current_uri, jsonObj);
        return getSyncResultFromRequest(true, jsonObj);
    }

}
