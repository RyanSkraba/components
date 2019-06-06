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
package org.talend.components.marketo.runtime;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.runtime.client.MarketoRESTClient;
import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation;
import org.talend.daikon.avro.AvroUtils;

import static org.talend.components.marketo.MarketoConstants.FIELD_CAMPAIGN_ID;
import static org.talend.components.marketo.MarketoConstants.FIELD_ERROR_MSG;
import static org.talend.components.marketo.MarketoConstants.FIELD_LEAD_ID;
import static org.talend.components.marketo.MarketoConstants.FIELD_MARKETO_GUID;
import static org.talend.components.marketo.MarketoConstants.FIELD_REASON;
import static org.talend.components.marketo.MarketoConstants.FIELD_SEQ;
import static org.talend.components.marketo.MarketoConstants.FIELD_STATUS;
import static org.talend.components.marketo.MarketoConstants.FIELD_SUCCESS;

public class MarketoOutputWriter extends MarketoWriter {

    public static final String FIELD_ID_REST = "id";

    public static final String FIELD_ID_SOAP = "Id";

    TMarketoOutputProperties properties;

    private OutputOperation operation;

    private Schema dynamicSchema;

    private Boolean isBatchMode = Boolean.FALSE;

    private static final Logger LOG = LoggerFactory.getLogger(MarketoOutputWriter.class);

    public MarketoOutputWriter(WriteOperation writeOperation, RuntimeContainer runtime) {
        super(writeOperation, runtime);
    }

    @Override
    public void open(String uId) throws IOException {
        super.open(uId);

        properties = (TMarketoOutputProperties) sink.getProperties();
        inputSchema = properties.schemaInput.schema.getValue();
        flowSchema = properties.schemaFlow.schema.getValue();
        rejectSchema = properties.schemaReject.schema.getValue();
        //
        operation = properties.outputOperation.getValue();
        dieOnError = properties.dieOnError.getValue();
        batchSize = properties.batchSize.getValue();
    }

    @Override
    public void write(Object object) throws IOException {
        if (object == null) {
            return;
        }
        //
        inputRecord = (IndexedRecord) object;
        result.totalCount++;
        // This for dynamic which would get schema from the first record
        if (inputSchema == null) {
            inputSchema = inputRecord.getSchema();
        }
        //
        recordsToProcess.add(inputRecord);

        if (recordsToProcess.size() == batchSize) {
            flush();
        }
    }

    @Override
    public Result close() throws IOException {
        flush();
        return super.close();
    }

    @Override
    protected void flush() {
        if (recordsToProcess.isEmpty()) {
            return;
        }
        MarketoSyncResult mktoResult = new MarketoSyncResult();
        for (int i = 0; i < getRetryAttemps(); i++) {
            result.apiCalls++;
            switch (operation) {
            case syncLead:
                mktoResult = client.syncLead(properties, recordsToProcess.get(0));
                break;
            case syncMultipleLeads:
                mktoResult = client.syncMultipleLeads(properties, recordsToProcess);
                break;
            case deleteLeads:
                mktoResult = ((MarketoRESTClient) client).deleteLeads(recordsToProcess);
                break;
            case syncCustomObjects:
                mktoResult = ((MarketoRESTClient) client).syncCustomObjects(properties, recordsToProcess);
                break;
            case deleteCustomObjects:
                mktoResult = ((MarketoRESTClient) client).deleteCustomObjects(properties, recordsToProcess);
                break;
            case syncCompanies:
                mktoResult = ((MarketoRESTClient) client).syncCompanies(properties, recordsToProcess);
                break;
            case deleteCompanies:
                mktoResult = ((MarketoRESTClient) client).deleteCompany(properties, recordsToProcess);
                break;
            case syncOpportunities:
            case syncOpportunityRoles:
                mktoResult = ((MarketoRESTClient) client).syncOpportunities(properties, recordsToProcess);
                break;
            case deleteOpportunities:
            case deleteOpportunityRoles:
                mktoResult = ((MarketoRESTClient) client).deleteOpportunities(properties, recordsToProcess);
                break;
            }
            //
            if (!mktoResult.isSuccess()) {
                if (dieOnError) {
                    throw new MarketoRuntimeException(mktoResult.getErrorsString());
                }
                // is recoverable error
                if (client.isErrorRecoverable(mktoResult.getErrors())) {
                    LOG.debug("Recoverable error during operation : `{}`. Retrying...", mktoResult.getErrorsString());
                    waitForRetryAttempInterval();
                    continue;
                } else {
                    LOG.error("Unrecoverable error : `{}`.", mktoResult.getErrorsString());
                    break;
                }
            } else {
                break;
            }
        }
        processResult(mktoResult);
        // clear the list
        recordsToProcess.clear();
    }

    public void processResult(MarketoSyncResult mktoResult) {
        cleanWrites();
        LOG.debug("[processResult] {}.", mktoResult);
        if (!mktoResult.isSuccess()) {
            // build a SyncStatus for record which failed
            SyncStatus status = new SyncStatus();
            status.setStatus("failed");
            status.setErrorMessage(mktoResult.getErrorsString());
            if (mktoResult.getRecords().isEmpty()) {
                LOG.debug("[processResult] Global fault, applying to all records.");
                mktoResult.setRecords(Arrays.asList(status));
                for (IndexedRecord statusRecord : recordsToProcess) {
                    handleReject(fillRecord(status, rejectSchema, statusRecord));
                }
                return;
            }
            List<SyncStatus> tmp = mktoResult.getRecords();
            tmp.add(status);
            mktoResult.setRecords(tmp);
        }
        int idx = 0;
        LOG.debug("[processResult] recordsToProcess: {}.", recordsToProcess);
        for (SyncStatus status : mktoResult.getRecords()) {
            IndexedRecord statusRecord = recordsToProcess.get(idx);
            LOG.debug("[processResult] [idx: {}] statusRecord: {}; status: {} ", idx, statusRecord, status);
            idx++;
            boolean isSuccess = Arrays.asList("created", "updated", "deleted", "scheduled", "triggered")
                    .contains(status.getStatus().toLowerCase());
            if (isSuccess) {
                handleSuccess(fillRecord(status, flowSchema, statusRecord));
            } else {
                if (dieOnError) {
                    throw new MarketoRuntimeException(status.getAvailableReason());
                }
                handleReject(fillRecord(status, rejectSchema, statusRecord));
            }
        }
    }

    private void handleSuccess(IndexedRecord record) {
        if (record != null) {
            result.successCount++;
            successfulWrites.add(record);
        }
    }

    private void handleReject(IndexedRecord record) {
        if (record != null) {
            result.rejectCount++;
            rejectedWrites.add(record);
        }
    }

    public IndexedRecord fillRecord(SyncStatus status, Schema schema, IndexedRecord record) {
        Boolean isDynamic = Boolean.FALSE;
        Schema currentSchema = schema;
        if (AvroUtils.isIncludeAllFields(schema)) {
            isDynamic = true;
            if (dynamicSchema == null) {
                dynamicSchema = MarketoSourceOrSink.mergeDynamicSchemas(record.getSchema(), schema);
            }
            currentSchema = dynamicSchema;
        }
        IndexedRecord outRecord = new Record(currentSchema);
        for (Field f : currentSchema.getFields()) {
            switch (f.name()) {
            case FIELD_LEAD_ID:
            case FIELD_ID_SOAP:
            case FIELD_ID_REST:
            case FIELD_CAMPAIGN_ID:
                // when the request failed, get it from input record
                if (status.getId() == null) {
                    try {
                        outRecord.put(currentSchema.getField(f.name()).pos(), record.get(inputSchema.getField(f.name()).pos()));
                    } catch (NullPointerException e) {
                        LOG.error("Could not find field `{}` in schema : {}.", f.name(), e.getMessage());
                    }
                } else {
                    outRecord.put(f.pos(), status.getId());
                }
                break;
            case FIELD_SUCCESS:
                outRecord.put(f.pos(), Boolean.parseBoolean(status.getStatus()));
                break;
            case FIELD_STATUS:
                outRecord.put(f.pos(), status.getStatus());
                break;
            case FIELD_ERROR_MSG:
            case FIELD_REASON:
                outRecord.put(f.pos(), status.getAvailableReason());
                break;
            case FIELD_MARKETO_GUID:
                outRecord.put(f.pos(), status.getMarketoGUID());
                break;
            case FIELD_SEQ:
                outRecord.put(f.pos(), status.getSeq());
                break;
            default:
                if (isDynamic) {
                    outRecord.put(currentSchema.getField(f.name()).pos(), record.get(f.pos()));
                } else {
                    outRecord.put(currentSchema.getField(f.name()).pos(), record.get(inputSchema.getField(f.name()).pos()));
                }
            }
        }
        return outRecord;
    }
}
