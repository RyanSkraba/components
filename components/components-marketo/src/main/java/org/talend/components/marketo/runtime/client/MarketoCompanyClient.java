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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.rest.response.CustomObjectResult;
import org.talend.components.marketo.runtime.client.rest.response.SyncResult;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MarketoCompanyClient extends MarketoCampaignClient {

    public static final String API_PATH_COMPANIES = "/v1/companies";

    public static final String API_PATH_COMPANIES_DESCRIBE = API_PATH_COMPANIES + API_PATH_URI_DESCRIBE;

    public static final String API_PATH_COMPANIES_GET = API_PATH_COMPANIES + API_PATH_JSON_EXT;

    public static final String API_PATH_COMPANIES_SYNC = API_PATH_COMPANIES + API_PATH_JSON_EXT;

    public static final String API_PATH_COMPANIES_DELETE = API_PATH_COMPANIES + API_PATH_URI_DELETE;

    private static final Logger LOG = LoggerFactory.getLogger(MarketoCompanyClient.class);

    public MarketoCompanyClient(TMarketoConnectionProperties connection) throws MarketoException {
        super(connection);
    }

    public MarketoRecordResult describeCompanies(TMarketoInputProperties parameters) {
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_COMPANIES_DESCRIBE)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        LOG.debug("describeCompanies : {}.", current_uri);
        MarketoRecordResult mkto = new MarketoRecordResult();
        mkto.setRemainCount(0);
        try {
            CustomObjectResult result = (CustomObjectResult) executeGetRequest(CustomObjectResult.class);
            mkto.setSuccess(result.isSuccess());
            mkto.setRequestId(REST + "::" + result.getRequestId());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(1);
                mkto.setRecords(result.getRecords());
            } else {
                if (result.getErrors() != null) {
                    mkto.setErrors(result.getErrors());
                }
            }
        } catch (MarketoException e) {
            LOG.error("{}.", e);
            mkto.setSuccess(false);
            mkto.setRecordCount(0);
            mkto.setErrors(Arrays.asList(new MarketoError(REST, e.getMessage())));
        }
        return mkto;
    }

    public MarketoRecordResult getCompanies(TMarketoInputProperties parameters, String offset) {
        String filterType = parameters.customObjectFilterType.getValue();
        String filterValues = parameters.customObjectFilterValues.getValue();
        // if fields is unset : id, dedupeFields (defined in mkto), updatedAt, createdAt will be returned.
        List<String> fields = new ArrayList<>();
        for (String f : parameters.getSchemaFields()) {
            if (!f.equals(FIELD_ID) && !f.equals(MarketoConstants.FIELD_SEQ)) {
                fields.add(f);
            }
        }
        //
        int batchLimit = parameters.batchSize.getValue() > REST_API_BATCH_LIMIT ? REST_API_BATCH_LIMIT
                : parameters.batchSize.getValue();
        current_uri = new StringBuilder(basicPath)//
                .append(API_PATH_COMPANIES_GET)//
                .append(API_PATH_JSON_EXT)//
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
        MarketoRecordResult mkto = new MarketoRecordResult();
        try {
            mkto = executeGetRequest(parameters.schemaInput.schema.getValue());
        } catch (MarketoException e) {
            LOG.error("{}.", e);
            mkto.setSuccess(false);
            mkto.setRecordCount(0);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
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
                .append(API_PATH_JSON_EXT)//
                .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));//
        try {
            LOG.debug("syncCompanies {}{}.", current_uri, inputJson);
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
        } catch (MarketoException e) {
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
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
        MarketoSyncResult mkto = new MarketoSyncResult();
        try {
            LOG.debug("deleteCompanies {}{}.", current_uri, inputJson);
            SyncResult rs = (SyncResult) executePostRequest(SyncResult.class, inputJson);
            mkto.setRequestId(REST + "::" + rs.getRequestId());
            mkto.setStreamPosition(rs.getNextPageToken());
            mkto.setSuccess(rs.isSuccess());
            if (mkto.isSuccess()) {
                mkto.setRecordCount(rs.getResult().size());
                mkto.setRemainCount(mkto.getStreamPosition() != null ? mkto.getRecordCount() : 0);
                mkto.setRecords(rs.getResult());
            } else {
                mkto.setRecordCount(0);
                mkto.setErrors(Arrays.asList(new MarketoError(REST, "Could not delete Company.")));
            }
        } catch (MarketoException e) {
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
    }

}
