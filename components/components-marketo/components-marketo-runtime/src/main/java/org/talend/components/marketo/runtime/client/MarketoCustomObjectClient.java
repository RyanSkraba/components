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

import java.util.ArrayList;
import java.util.List;

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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static org.talend.components.marketo.MarketoConstants.REST_API_LIMIT;

public class MarketoCustomObjectClient extends MarketoLeadClient {

    public static final String API_PATH_CUSTOMOBJECTS = "/v1/customobjects/";

    private static final Logger LOG = LoggerFactory.getLogger(MarketoCustomObjectClient.class);

    public MarketoCustomObjectClient(TMarketoConnectionProperties connection) throws MarketoException {
        super(connection);
    }

    /**
     * This method implements
     * http://developers.marketo.com/rest-api/endpoint-reference/lead-database-endpoint-reference/#!/Custom_Objects/describeUsingGET_1
     */
    public MarketoRecordResult describeCustomObject(TMarketoInputProperties parameters) {
        String customObjectName = parameters.customObjectName.getValue();
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_CUSTOMOBJECTS)//
                .append(customObjectName)//
                .append(API_PATH_URI_DESCRIBE)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        LOG.debug("describeCustomObject : {}.", current_uri);

        return getRecordResultForFromRequestBySchema(parameters.schemaInput.schema.getValue(), false, null);
    }

    /**
     * This method implements
     * http://developers.marketo.com/rest-api/endpoint-reference/lead-database-endpoint-reference/#!/Custom_Objects/listCustomObjectsUsingGET
     */
    public MarketoRecordResult listCustomObjects(TMarketoInputProperties parameters) {
        String names = parameters.customObjectNames.getValue();

        current_uri = new StringBuilder(basicPath)//
                .append("/v1/customobjects.json")//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true))//
                .append(fmtParams("names", names))//
                .append(fmtParams(QUERY_METHOD, QUERY_METHOD_GET));
        LOG.debug("listCustomObjects : {}.", current_uri);

        return getRecordResultForFromRequestBySchema(parameters.schemaInput.schema.getValue(), false, null);
    }

    /**
     * This method implements
     * http://developers.marketo.com/rest-api/endpoint-reference/lead-database-endpoint-reference/#!/Custom_Objects/getCustomObjectsUsingGET
     * <p>
     * Retrieves a list of custom objects records based on filter and set of values. When action is createOnly, idField may
     * not be used as a key and marketoGUID cannot be a member of any object records.
     */
    public MarketoRecordResult getCustomObjects(TMarketoInputProperties parameters, String offset) {
        // mandatory fields for request
        String customObjectName = parameters.customObjectName.getValue();
        String filterType = parameters.customObjectFilterType.getValue();
        String filterValues = parameters.customObjectFilterValues.getValue();
        // if fields is unset : marketoGuid, dedupeFields (defined in mkto), updatedAt, createdAt will be returned.
        List<String> fields = new ArrayList<>();
        for (String f : parameters.getSchemaFields()) {
            if (!f.equals(MarketoConstants.FIELD_MARKETO_GUID) && !f.equals(MarketoConstants.FIELD_SEQ)) {
                fields.add(f);
            }
        }
        //
        int batchLimit = parameters.batchSize.getValue() > REST_API_LIMIT ? REST_API_LIMIT : parameters.batchSize.getValue();
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_CUSTOMOBJECTS)//
                .append(customObjectName)//
                .append(API_PATH_JSON_EXT)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));//
        // Compound Key use
        if (parameters.useCompoundKey.getValue()) {
            JsonObject inputJson = new JsonObject();
            Gson gson = new Gson();
            if (offset != null) {
                inputJson.addProperty(FIELD_NEXT_PAGE_TOKEN, offset);
            }
            inputJson.addProperty("filterType", "dedupeFields");
            if (!fields.isEmpty()) {
                inputJson.add(FIELD_FIELDS, gson.toJsonTree(fields));
            }
            inputJson.add(FIELD_INPUT, parameters.compoundKey.getKeyValuesAsJson().getAsJsonArray());
            LOG.debug("getCustomObjects : {} body : {}", current_uri, inputJson);

            return getRecordResultForFromRequestBySchema(parameters.schemaInput.schema.getValue(), true, inputJson.toString());
        } else {
            current_uri.append(fmtParams(FIELD_BATCH_SIZE, batchLimit))//
                    .append(fmtParams("filterType", filterType))//
                    .append(fmtParams("filterValues", filterValues));
            if (offset != null) {
                current_uri.append(fmtParams(FIELD_NEXT_PAGE_TOKEN, offset));
            }
            if (!fields.isEmpty()) {
                current_uri.append(fmtParams(FIELD_FIELDS, csvString(fields.toArray())));
            }
            LOG.debug("getCustomObjects : {}.", current_uri);

            return getRecordResultForFromRequestBySchema(parameters.schemaInput.schema.getValue(), false, null);
        }
    }

    /**
     * This method implements
     * http://developers.marketo.com/rest-api/endpoint-reference/lead-database-endpoint-reference/#!/Custom_Objects/syncCustomObjectsUsingPOST
     * <p>
     * Inserts, updates, or upserts custom object records to the target instance.
     */
    public MarketoSyncResult syncCustomObjects(TMarketoOutputProperties parameters, List<IndexedRecord> records) {
        // mandatory fields for request
        String customObjectName = parameters.customObjectName.getValue();
        // others
        String action = parameters.customObjectSyncAction.getValue().name();
        String dedupeBy = parameters.customObjectDedupeBy.getValue();
        JsonObject inputJson = new JsonObject();
        inputJson.addProperty("action", action);
        if (!dedupeBy.isEmpty()) {
            inputJson.addProperty("dedupeBy", dedupeBy);
        }
        inputJson.add(FIELD_INPUT, convertIndexedRecordsToJson(records));
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_CUSTOMOBJECTS)//
                .append(customObjectName)//
                .append(API_PATH_JSON_EXT)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));//
        LOG.debug("syncCustomObjects {}{}.", current_uri, inputJson);

        return getSyncResultFromRequest(true, inputJson);
    }

    /**
     * This method implements
     * http://developers.marketo.com/rest-api/endpoint-reference/lead-database-endpoint-reference/#!/Custom_Objects/deleteCustomObjectsUsingPOST
     */
    public MarketoSyncResult deleteCustomObjects(TMarketoOutputProperties parameters, List<IndexedRecord> records) {
        // mandatory fields for request
        String customObjectName = parameters.customObjectName.getValue();
        String deleteBy = parameters.customObjectDeleteBy.getValue().name();
        //
        JsonObject inputJson = new JsonObject();
        Gson gson = new Gson();
        if (!deleteBy.isEmpty()) {
            inputJson.addProperty("deleteBy", deleteBy);
        }
        inputJson.add(FIELD_INPUT, convertIndexedRecordsToJson(records));
        //
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_CUSTOMOBJECTS)//
                .append(customObjectName)//
                .append(API_PATH_URI_DELETE)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        LOG.debug("deleteCustomObject {}{}.", current_uri, inputJson);

        return getSyncResultFromRequest(true, inputJson);
    }
}
