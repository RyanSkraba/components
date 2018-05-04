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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.runtime.client.rest.response.BulkImportResult;
import org.talend.components.marketo.runtime.client.rest.type.BulkImport;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties.BulkImportTo;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;

import com.google.gson.Gson;

public class MarketoBulkExecClient extends MarketoCustomObjectClient {

    public static final String API_PATH_BULK_CUSTOMOBJECTS = "/v1/customobjects/%s/import.json";

    public static final String API_PATH_BULK_CUSTOMOBJECTS_RESULT = "/v1/customobjects/%s/import/%d/%s.json";

    public static final String API_PATH_BULK_LEADS = "/v1/leads.json";

    public static final String API_PATH_BULK_LEADS_RESULT_STATUS = "/v1/leads/batch/%d.json";

    public static final String API_PATH_BULK_LEADS_RESULT_FOR = "/v1/leads/batch/%d/%s.json";

    public static final String BULK_STATUS_COMPLETE = "Complete";

    public static final String BULK_STATUS_FAILED = "Failed";

    public static final String URI_FAILURES = "failures";

    public static final String URI_WARNINGS = "warnings";

    private static final Logger LOG = LoggerFactory.getLogger(MarketoBulkExecClient.class);

    public MarketoBulkExecClient(TMarketoConnectionProperties connection) throws MarketoException {
        super(connection);
    }

    public BulkImportResult executePostFileRequest(Class<?> resultClass, String filePath) throws MarketoException {
        String boundary = "Talend_tMarketoBulkExec_" + String.valueOf(System.currentTimeMillis());
        try {
            URL url = new URL(current_uri.toString());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            urlConn.setRequestProperty("accept", "text/json");
            urlConn.setDoOutput(true);
            String requestBody = buildRequest(filePath); // build the request body
            PrintWriter wr = new PrintWriter(new OutputStreamWriter(urlConn.getOutputStream()));
            wr.append("--" + boundary + "\r\n");
            wr.append("Content-Disposition: form-data; name=\"file\";filename=\"" + filePath + "\";\r\n");
            wr.append("Content-type: text/plain; charset=\"utf-8\"\r\n");
            wr.append("Content-Transfer-Encoding: text/plain\r\n");
            wr.append("MIME-Version: 1.0\r\n");
            wr.append("\r\n");
            wr.append(requestBody);
            wr.append("\r\n");
            wr.append("--" + boundary);
            wr.flush();
            wr.close();
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();
                InputStreamReader reader = new InputStreamReader(inStream);
                Gson gson = new Gson();
                return (BulkImportResult) gson.fromJson(reader, resultClass);
            } else {
                LOG.error("POST request failed: {}", responseCode);
                throw new MarketoException(REST, responseCode, "Request failed! Please check your request setting!");
            }
        } catch (IOException e) {
            LOG.error("POST request failed: {}", e.getMessage());
            throw new MarketoException(REST, e.getMessage());
        }
    }

    // read from the file in filepath and return the read data
    private String buildRequest(String filePath) throws IOException {
        FileReader fr = new FileReader(filePath);
        BufferedReader br = new BufferedReader(fr);
        char[] arr = new char[8 * 4096];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = br.read(arr, 0, arr.length)) != -1) {
            buffer.append(arr, 0, numCharsRead);
        }
        br.close();
        return buffer.toString();
    }

    public void executeDownloadFileRequest(File filename) throws MarketoException {
        String err;
        try {
            URL url = new URL(current_uri.toString());
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.setRequestProperty("accept", "text/json");
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 200) {
                InputStream inStream = urlConn.getInputStream();
                FileUtils.copyInputStreamToFile(inStream, filename);
            } else {
                err = String.format("Download failed for %s. Status: %d", filename, responseCode);
                throw new MarketoException(REST, err);
            }
        } catch (IOException e) {
            err = String.format("Download failed for %s. Cause: %s", filename, e.getMessage());
            LOG.error(err);
            throw new MarketoException(REST, err);
        }
    }

    public BulkImport getStatusesForBatch(BulkImport bulk, String downloadPath) throws MarketoException {
        String logFile;
        if (bulk.getNumOfRowsFailed() > 0) {
            current_uri = new StringBuilder(bulkPath);
            if (bulk.isBulkLeadsImport()) {
                current_uri.append(String.format(API_PATH_BULK_LEADS_RESULT_FOR, bulk.getBatchId(), URI_FAILURES));
            } else {
                current_uri.append(String.format(API_PATH_BULK_CUSTOMOBJECTS_RESULT, bulk.getObjectApiName(), bulk.getBatchId(),
                        URI_FAILURES));
            }
            current_uri.append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
            LOG.debug("failures = {}.", current_uri);
            logFile = Paths.get(Paths.get(downloadPath).toString(), bulk.getFailuresOrWarningsFilename(false)).toString();
            bulk.setFailuresLogFile(logFile);
            executeDownloadFileRequest(new File(logFile));
        }
        if (bulk.getNumOfRowsWithWarning() > 0) {
            current_uri = new StringBuilder(bulkPath);
            if (bulk.isBulkLeadsImport()) {
                current_uri.append(String.format(API_PATH_BULK_LEADS_RESULT_FOR, bulk.getBatchId(), URI_WARNINGS));
            } else {
                current_uri.append(String.format(API_PATH_BULK_CUSTOMOBJECTS_RESULT, bulk.getObjectApiName(), bulk.getBatchId(),
                        URI_WARNINGS));
            }
            current_uri.append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
            LOG.debug("warnings = {}.", current_uri);
            logFile = Paths.get(Paths.get(downloadPath).toString(), bulk.getFailuresOrWarningsFilename(true)).toString();
            bulk.setWarningsLogFile(logFile);
            executeDownloadFileRequest(new File(logFile));
        }
        return bulk;
    }

    /**
     * Imports a spreadsheet of leads or custom objects into the target instance
     *
     * POST /bulk/v1/leads/import.json
     *
     * POST /bulk/v1/customobjects/{apiName}/import.json
     *
     * @param parameters
     * @return
     */
    public MarketoRecordResult bulkImport(TMarketoBulkExecProperties parameters) {
        String importFilename = parameters.bulkFilePath.getValue();
        String format = parameters.bulkFileFormat.getValue().name();
        Integer pollWaitTime = parameters.pollWaitTime.getValue();
        String logDownloadPath = parameters.logDownloadPath.getValue();
        String lookupField;
        Integer listId;
        String partitionName;
        String customObjectName;
        current_uri = new StringBuilder(bulkPath);
        Boolean isImportingLeads = parameters.bulkImportTo.getValue().equals(BulkImportTo.Leads);
        if (isImportingLeads) {
            lookupField = parameters.lookupField.getValue().name();
            listId = parameters.listId.getValue();
            partitionName = parameters.partitionName.getValue();
            current_uri.append(API_PATH_BULK_LEADS)//
                    .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true))//
                    .append(fmtParams(FIELD_LOOKUP_FIELD, lookupField));
            if (listId != null) {
                current_uri.append(fmtParams(FIELD_LIST_ID, listId));
            }
            if (!StringUtils.isEmpty(partitionName)) {
                current_uri.append(fmtParams(FIELD_PARTITION_NAME, partitionName));
            }
        } else {
            customObjectName = parameters.customObjectName.getValue();
            current_uri.append(String.format(API_PATH_BULK_CUSTOMOBJECTS, customObjectName))//
                    .append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
        }
        current_uri.append(fmtParams(FIELD_FORMAT, format));
        //
        MarketoRecordResult mkto = new MarketoRecordResult();
        try {
            LOG.debug("bulkImport {}.", current_uri);
            BulkImportResult rs = executePostFileRequest(BulkImportResult.class, importFilename);
            LOG.debug("rs = {}.", rs);
            mkto.setRequestId(REST + "::" + rs.getRequestId());
            mkto.setSuccess(rs.isSuccess());
            if (!mkto.isSuccess()) {
                mkto.setRecordCount(0);
                mkto.setErrors(Arrays
                        .asList(new MarketoError(REST, messages.getMessage("bulkimport.error.import", rs.getErrorsString()))));
                return mkto;
            }
            BulkImport bulkResult = rs.getResult().get(0);
            if (bulkResult.getStatus().equals(BULK_STATUS_FAILED)) { // request Failed
                String err = messages.getMessage("bulkimport.status.failed", bulkResult.getMessage());
                LOG.error("{}.", err);
                mkto.setSuccess(false);
                mkto.setErrors(Arrays.asList(new MarketoError(REST, err)));
            } else if (bulkResult.getStatus().equals(BULK_STATUS_COMPLETE)) { // already Complete
                bulkResult = getStatusesForBatch(bulkResult, logDownloadPath);
            } else { // Importing, Queued
                while (true) {
                    try {
                        LOG.warn(messages.getMessage("bulkimport.status.waiting", pollWaitTime));
                        Thread.sleep(pollWaitTime * 1000L);
                        current_uri = new StringBuilder(bulkPath);
                        if (bulkResult.isBulkLeadsImport()) {
                            current_uri.append(String.format(API_PATH_BULK_LEADS_RESULT_STATUS, bulkResult.getBatchId()));
                        } else {
                            current_uri.append(String.format(API_PATH_BULK_CUSTOMOBJECTS_RESULT, bulkResult.getObjectApiName(),
                                    bulkResult.getBatchId(), "status"));
                        }
                        current_uri.append(fmtParams(FIELD_ACCESS_TOKEN, accessToken, true));
                        LOG.debug("status = {}.", current_uri);
                        rs = (BulkImportResult) executeGetRequest(BulkImportResult.class);
                        if (rs.isSuccess() && rs.getResult().get(0).getStatus().equals(BULK_STATUS_COMPLETE)) {
                            bulkResult = getStatusesForBatch(rs.getResult().get(0), logDownloadPath);
                            break;
                        } else {
                            LOG.warn(messages.getMessage("bulkimport.status.current", rs.getResult().get(0).getStatus()));
                        }
                    } catch (InterruptedException e) {
                        LOG.error("Sleep interrupted : {}", e);
                        throw new MarketoException(REST, "Sleep interrupted : " + e.getLocalizedMessage());
                    }
                }
            }
            mkto.setRecords(Arrays.asList(bulkResult.toIndexedRecord()));
            mkto.setRecordCount(1);
            mkto.setRemainCount(0);
        } catch (MarketoException e) {
            mkto.setSuccess(false);
            mkto.setErrors(Arrays.asList(e.toMarketoError()));
        }
        return mkto;
    }
}
