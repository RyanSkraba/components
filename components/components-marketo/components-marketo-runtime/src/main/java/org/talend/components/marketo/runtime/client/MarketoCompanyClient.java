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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MarketoCompanyClient extends MarketoCampaignClient {

    private static final String API_PATH_COMPANIES = "/v1/companies";

    private static final String API_PATH_COMPANIES_DESCRIBE = API_PATH_COMPANIES + API_PATH_URI_DESCRIBE;

    private static final String API_PATH_COMPANIES_GET = API_PATH_COMPANIES + API_PATH_JSON_EXT;

    private static final String API_PATH_COMPANIES_SYNC = API_PATH_COMPANIES + API_PATH_JSON_EXT;

    private static final String API_PATH_COMPANIES_DELETE = API_PATH_COMPANIES + API_PATH_URI_DELETE;

    private static final Logger LOG = LoggerFactory.getLogger(MarketoCompanyClient.class);

    public MarketoCompanyClient(TMarketoConnectionProperties connection) throws MarketoException {
        super(connection);
    }

    public MarketoRecordResult describeCompanies(TMarketoInputProperties parameters) {

        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_COMPANIES_DESCRIBE)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        String batchLimit = String.valueOf(parameters.batchSize.getValue());
        String filterType = parameters.customObjectFilterType.getValue();
        String filterValues = parameters.customObjectFilterValues.getValue();
        //
        current_uri.append(fmtParams(FIELD_BATCH_SIZE, batchLimit))//
                .append(fmtParams("filterType", filterType))//
                .append(fmtParams("filterValues", filterValues));
        LOG.debug("describeCompanies : {}.", current_uri);
        return getRecordResultForFromRequestBySchema(parameters.schemaInput.schema.getValue(), false, null);
    }

    public MarketoRecordResult getCompanies(TMarketoInputProperties parameters, String offset) {
        String filterType = parameters.customObjectFilterType.getValue();
        String filterValues = parameters.customObjectFilterValues.getValue();
        List<String> fields = new ArrayList<>();
        for (String f : parameters.getSchemaFields()) {
            if (!f.equals(FIELD_ID) && !f.equals(MarketoConstants.FIELD_SEQ)) {
                fields.add(f);
            }
        }
        int batchLimit = parameters.batchSize.getValue() > REST_API_BATCH_LIMIT ? REST_API_BATCH_LIMIT
                : parameters.batchSize.getValue();
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_COMPANIES_GET)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true))//
                .append(fmtParams("filterType", filterType))//
                .append(fmtParams("filterValues", filterValues))//
                .append(fmtParams(FIELD_BATCH_SIZE, batchLimit));
        if (!fields.isEmpty()) {
            current_uri.append(fmtParams(FIELD_FIELDS, csvString(fields.toArray())));
        }
        if (offset != null) {
            current_uri.append(fmtParams(FIELD_NEXT_PAGE_TOKEN, offset));
        }
        LOG.debug("getCompanies : {}.", current_uri);
        return getRecordResultForFromRequestBySchema(parameters.schemaInput.schema.getValue(), false, null);
    }

    public MarketoSyncResult syncCompanies(TMarketoOutputProperties parameters, List<IndexedRecord> records) {
        String action = parameters.customObjectSyncAction.getValue().name();
        String dedupeBy = parameters.customObjectDedupeBy.getValue();
        JsonObject inputJson = new JsonObject();
        Gson gson = new Gson();
        inputJson.addProperty("action", action);
        if (!dedupeBy.isEmpty()) {
            inputJson.addProperty("dedupeBy", dedupeBy);
        }
        List<Map<String, Object>> companies = new ArrayList<>();
        for (IndexedRecord r : records) {
            Map<String, Object> company = new HashMap<>();
            for (Field f : r.getSchema().getFields()) {
                company.put(f.name(), r.get(f.pos()));
            }
            companies.add(company);
        }
        inputJson.add(FIELD_INPUT, gson.toJsonTree(companies));
        MarketoSyncResult mkto = new MarketoSyncResult();
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_COMPANIES_SYNC)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));//

        LOG.debug("syncCompanies {}{}.", current_uri, inputJson);
        return getSyncResultFromRequest(true, inputJson);
    }

    public MarketoSyncResult deleteCompany(TMarketoOutputProperties parameters, List<IndexedRecord> records) {
        String deleteBy = parameters.customObjectDeleteBy.getValue().name();
        JsonObject inputJson = new JsonObject();
        Gson gson = new Gson();
        if (!deleteBy.isEmpty()) {
            inputJson.addProperty("deleteBy", deleteBy);
        }
        List<Map<String, Object>> companies = new ArrayList<>();
        for (IndexedRecord r : records) {
            Map<String, Object> company = new HashMap<>();
            for (Field f : r.getSchema().getFields()) {
                company.put(f.name(), r.get(f.pos()));
            }
            companies.add(company);
        }
        inputJson.add(FIELD_INPUT, gson.toJsonTree(companies));
        //
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_COMPANIES_DELETE)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        LOG.debug("deleteCompany {}{}.", current_uri, inputJson);
        return getSyncResultFromRequest(true, inputJson);
    }

}
