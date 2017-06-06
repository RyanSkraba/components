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
package org.talend.components.marketo.runtime;

import static org.talend.components.marketo.MarketoConstants.FIELD_CAMPAIGN_ID;
import static org.talend.components.marketo.MarketoConstants.FIELD_ERROR_MSG;
import static org.talend.components.marketo.MarketoConstants.FIELD_LEAD_ID;
import static org.talend.components.marketo.MarketoConstants.FIELD_MARKETO_GUID;
import static org.talend.components.marketo.MarketoConstants.FIELD_REASON;
import static org.talend.components.marketo.MarketoConstants.FIELD_SEQ;
import static org.talend.components.marketo.MarketoConstants.FIELD_STATUS;
import static org.talend.components.marketo.MarketoConstants.FIELD_SUCCESS;

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
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.OutputOperation;
import org.talend.daikon.avro.AvroUtils;

public class MarketoOutputWriter extends MarketoWriter {

    public static final String FIELD_ID_REST = "id";

    public static final String FIELD_ID_SOAP = "Id";

    TMarketoOutputProperties properties;

    private OutputOperation operation;

    private Schema dynamicSchema;

    private static final Logger LOG = LoggerFactory.getLogger(MarketoOutputWriter.class);

    public MarketoOutputWriter(WriteOperation writeOperation, RuntimeContainer runtime) {
        super(writeOperation, runtime);
    }

    @Override
    public void open(String uId) throws IOException {
        super.open(uId);

        properties = (TMarketoOutputProperties) sink.properties;
        inputSchema = properties.schemaInput.schema.getValue();
        flowSchema = properties.schemaFlow.schema.getValue();
        rejectSchema = properties.schemaReject.schema.getValue();
        //
        operation = properties.outputOperation.getValue();
        dieOnError = properties.dieOnError.getValue();
        if (operation.equals(OutputOperation.syncMultipleLeads) || operation.equals(OutputOperation.deleteLeads)) {
            batchSize = properties.batchSize.getValue();
        }
        if (operation.equals(OutputOperation.deleteLeads) && !properties.deleteLeadsInBatch.getValue()) {
            batchSize = 1;
        }
    }

    @Override
    public void write(Object object) throws IOException {
        if (object == null) {
            return;
        }
        //
        successfulWrites.clear();
        rejectedWrites.clear();
        //
        inputRecord = (IndexedRecord) object;
        result.totalCount++;
        // This for dynamic which would get schema from the first record
        if (inputSchema == null) {
            inputSchema = inputRecord.getSchema();
        }
        //
        recordsToProcess.add(inputRecord);
        switch (operation) {
        case syncLead:
            processResult(client.syncLead(properties, recordsToProcess.get(0)));
            recordsToProcess.clear();
            break;
        case syncMultipleLeads:
            if (recordsToProcess.size() >= batchSize) {
                processResult(client.syncMultipleLeads(properties, recordsToProcess));
                recordsToProcess.clear();
            }
            break;
        case deleteLeads:
            if (recordsToProcess.size() >= batchSize) {
                processResult(((MarketoRESTClient) client).deleteLeads(recordsToProcess));
                recordsToProcess.clear();
            }
            break;
        case syncCustomObjects:
            processResult(((MarketoRESTClient) client).syncCustomObjects(properties, recordsToProcess));
            recordsToProcess.clear();
            break;
        case deleteCustomObjects:
            processResult(((MarketoRESTClient) client).deleteCustomObjects(properties, recordsToProcess));
            recordsToProcess.clear();
            break;
        }
    }

    @Override
    public Result close() throws IOException {
        if (!recordsToProcess.isEmpty()) {
            if (operation.equals(OutputOperation.syncMultipleLeads)) {
                processResult(client.syncMultipleLeads(properties, recordsToProcess));
            }
            if (operation.equals(OutputOperation.deleteLeads)) {
                processResult(((MarketoRESTClient) client).deleteLeads(recordsToProcess));
            }
            recordsToProcess.clear();
        }
        return super.close();
    }

    public void processResult(MarketoSyncResult mktoResult) throws IOException {
        result.apiCalls++;
        if (!mktoResult.isSuccess()) {
            if (dieOnError) {
                throw new IOException(mktoResult.getErrorsString());
            }
            // build a SyncStatus for record which failed
            SyncStatus status = new SyncStatus();
            status.setStatus("failed");
            status.setErrorMessage(mktoResult.getErrorsString());
            if (mktoResult.getRecords().isEmpty()) {
                mktoResult.setRecords(Arrays.asList(status));
            } else {
                List<SyncStatus> tmp = mktoResult.getRecords();
                tmp.add(status);
                mktoResult.setRecords(tmp);
            }
        }
        int idx = 0;
        for (SyncStatus status : mktoResult.getRecords()) {
            IndexedRecord statusRecord = recordsToProcess.get(idx);
            idx++;
            if (Arrays.asList("created", "updated", "deleted", "scheduled", "triggered")
                    .contains(status.getStatus().toLowerCase())) {
                handleSuccess(fillRecord(status, flowSchema, statusRecord));
            } else {
                if (dieOnError) {
                    throw new IOException(status.getAvailableReason());
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
