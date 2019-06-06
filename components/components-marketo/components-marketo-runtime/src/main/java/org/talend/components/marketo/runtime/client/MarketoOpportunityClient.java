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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static org.talend.components.marketo.MarketoConstants.REST_API_LIMIT;

/**
 * Marketers deliver leads to sales in the form of an opportunity. An opportunity represents a potential sales deal and
 * is associated with a lead or contact and an organization in Marketo. An opportunity role is the intersection between
 * a given lead and an organization. The opportunity role pertains to a lead’s function within the organization.
 */
public class MarketoOpportunityClient extends MarketoCompanyClient {

    public static final String API_PATH_OPPORTUNITIES = "/v1/opportunities";

    public static final String API_PATH_OPPORTUNITY_ROLE = API_PATH_OPPORTUNITIES + "/roles";

    private static final Logger LOG = LoggerFactory.getLogger(MarketoOpportunityClient.class);

    public MarketoOpportunityClient(TMarketoConnectionProperties connection) throws MarketoException {
        super(connection);
    }

    /**
     * Returns object and field metadata for Opportunity type records in the target instance.
     *
     * @param parameters
     * @return
     */
    public MarketoRecordResult describeOpportunity(TMarketoInputProperties parameters) {
        String resource = API_PATH_OPPORTUNITIES;
        if (parameters.inputOperation.getValue().equals(InputOperation.OpportunityRole)) {
            resource = API_PATH_OPPORTUNITY_ROLE;
        }
        String customObjectName = parameters.customObjectName.getValue();
        current_uri = new StringBuilder(basicPath)//
                .append(resource)//
                .append(API_PATH_URI_DESCRIBE)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        LOG.debug("describeOpportunity : {}.", current_uri);

        return getRecordResultForFromRequestBySchema(parameters.schemaInput.schema.getValue(), false, null);
    }

    /**
     * Returns a list of opportunities based on a filter and set of values.
     *
     * @param parameters
     * @return
     */
    public MarketoRecordResult getOpportunities(TMarketoInputProperties parameters, String offset) {
        String resource = API_PATH_OPPORTUNITIES;
        if (parameters.inputOperation.getValue().equals(InputOperation.OpportunityRole)) {
            resource = API_PATH_OPPORTUNITY_ROLE;
        }
        String filterType = parameters.customObjectFilterType.getValue();
        String filterValues = parameters.customObjectFilterValues.getValue();
        Boolean useCompoundKey = parameters.useCompoundKey.getValue();
        List<String> fields = new ArrayList<>();
        for (String f : parameters.getSchemaFields()) {
            if (!f.equals(MarketoConstants.FIELD_MARKETO_GUID) && !f.equals(MarketoConstants.FIELD_SEQ)) {
                fields.add(f);
            }
        }
        int batchLimit = parameters.batchSize.getValue() > REST_API_LIMIT ? REST_API_LIMIT : parameters.batchSize.getValue();
        //
        current_uri = new StringBuilder(basicPath)//
                .append(resource)//
                .append(API_PATH_JSON_EXT)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true))//
        ;
        //
        if (useCompoundKey) {
            JsonObject inputJson = new JsonObject();
            Gson gson = new Gson();
            if (offset != null) {
                inputJson.addProperty(FIELD_NEXT_PAGE_TOKEN, offset);
            }
            inputJson.addProperty(FIELD_BATCH_SIZE, batchLimit);

            inputJson.addProperty("filterType", "dedupeFields");
            if (!fields.isEmpty()) {
                inputJson.add(FIELD_FIELDS, gson.toJsonTree(fields));
            }
            inputJson.add(FIELD_INPUT, parameters.compoundKey.getKeyValuesAsJson().getAsJsonArray());
            LOG.debug("getOpportunities: {} body : {}", current_uri, inputJson);
            return getRecordResultForFromRequestBySchema(parameters.schemaInput.schema.getValue(), true, inputJson.toString());

        } else {
            current_uri.append(fmtParams(FIELD_BATCH_SIZE, batchLimit)).append(fmtParams("filterType", filterType))//
                    .append(fmtParams("filterValues", filterValues));//
            if (offset != null) {
                current_uri.append(fmtParams(FIELD_NEXT_PAGE_TOKEN, offset));
            }
            if (!fields.isEmpty()) {
                current_uri.append(fmtParams(FIELD_FIELDS, csvString(fields.toArray())));
            }
            LOG.debug("getOpportunities : {}.", current_uri);
            return getRecordResultForFromRequestBySchema(parameters.schemaInput.schema.getValue(), false, null);
        }
    }

    /**
     * Allows inserting, updating, or upserting of opportunity records into the target instance.
     *
     * @param parameters
     * @param records
     * @return
     */
    public MarketoSyncResult syncOpportunities(TMarketoOutputProperties parameters, List<IndexedRecord> records) {
        String resource = API_PATH_OPPORTUNITIES;
        if (parameters.outputOperation.getValue().equals(OutputOperation.syncOpportunityRoles)) {
            resource = API_PATH_OPPORTUNITY_ROLE;
        }
        String action = parameters.customObjectSyncAction.getValue().name();
        String dedupeBy = parameters.customObjectDedupeBy.getValue();
        JsonObject inputJson = new JsonObject();
        Gson gson = new Gson();
        inputJson.addProperty("action", action);
        if (!dedupeBy.isEmpty()) {
            inputJson.addProperty("dedupeBy", dedupeBy);
        }
        List<Map<String, Object>> opportunities = new ArrayList<>();
        for (IndexedRecord r : records) {
            Map<String, Object> opportunity = new HashMap<>();
            for (Field f : r.getSchema().getFields()) {
                opportunity.put(f.name(), r.get(f.pos()));
            }
            opportunities.add(opportunity);
        }
        inputJson.add(FIELD_INPUT, gson.toJsonTree(opportunities));
        current_uri = new StringBuilder(basicPath)//
                .append(resource)//
                .append(API_PATH_JSON_EXT)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));//
        LOG.debug("syncOpportunities {}{}.", current_uri, inputJson);
        return getSyncResultFromRequest(true, inputJson);
    }

    /**
     * Deletes a list of opportunity records from the target instance. Input records should only have one member, based on
     * the value of 'dedupeBy'.
     *
     * @param parameters
     * @param records
     * @return
     */
    public MarketoSyncResult deleteOpportunities(TMarketoOutputProperties parameters, List<IndexedRecord> records) {
        String resource = API_PATH_OPPORTUNITIES;
        if (parameters.outputOperation.getValue().equals(OutputOperation.deleteOpportunityRoles)) {
            resource = API_PATH_OPPORTUNITY_ROLE;
        }
        String deleteBy = parameters.customObjectDeleteBy.getValue().name();
        JsonObject inputJson = new JsonObject();
        Gson gson = new Gson();
        if (!deleteBy.isEmpty()) {
            inputJson.addProperty("deleteBy", deleteBy);
        }
        List<Map<String, Object>> opportunities = new ArrayList<>();
        for (IndexedRecord r : records) {
            Map<String, Object> opportunity = new HashMap<>();
            for (Field f : r.getSchema().getFields()) {
                opportunity.put(f.name(), r.get(f.pos()));
            }
            opportunities.add(opportunity);
        }
        inputJson.add(FIELD_INPUT, gson.toJsonTree(opportunities));
        //
        current_uri = new StringBuilder(basicPath)//
                .append(resource)//
                .append(API_PATH_URI_DELETE)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        LOG.debug("deleteOpportunities {}{}.", current_uri, inputJson);
        return getSyncResultFromRequest(true, inputJson);
    }

}
