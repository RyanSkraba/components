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

import static org.talend.components.marketo.MarketoConstants.FIELD_ERROR_MSG;
import static org.talend.components.marketo.MarketoConstants.FIELD_LEAD_ID;
import static org.talend.components.marketo.MarketoConstants.FIELD_STATUS;
import static org.talend.components.marketo.MarketoConstants.FIELD_SUCCESS;

import java.io.IOException;
import java.util.Arrays;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.MarketoRESTClient;
import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.OutputOperation;

public class MarketoOutputWriter extends MarketoWriter {

    public static final String FIELD_ID_REST = "id";

    public static final String FIELD_ID_SOAP = "Id";

    TMarketoOutputProperties properties;

    private OutputOperation operation;

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
        switch (operation) {
        case syncLead:
            processResult(client.syncLead(properties, inputRecord));
            break;
        case syncMultipleLeads:
            recordsToProcess.add(inputRecord);
            if (recordsToProcess.size() >= batchSize) {
                processResult(client.syncMultipleLeads(properties, recordsToProcess));
                recordsToProcess.clear();
            }
            break;
        case deleteLeads:
            recordsToProcess.add(inputRecord);
            if (recordsToProcess.size() >= batchSize) {
                processResult(((MarketoRESTClient) client).deleteLeads(recordsToProcess));
                recordsToProcess.clear();
            }
            break;
        case syncCustomObjects:
            processResult(((MarketoRESTClient) client).syncCustomObjects(properties, Arrays.asList(inputRecord)));
            break;
        case deleteCustomObjects:
            processResult(((MarketoRESTClient) client).deleteCustomObjects(properties, Arrays.asList(inputRecord)));
            break;
        case syncCompanies:
            processResult(((MarketoRESTClient) client).syncCompanies(properties, Arrays.asList(inputRecord)));
            break;
        case deleteCompanies:
            processResult(((MarketoRESTClient) client).deleteCompany(properties, Arrays.asList(inputRecord)));
            break;
        case syncOpportunities:
        case syncOpportunityRoles:
            processResult(((MarketoRESTClient) client).syncOpportunities(properties, Arrays.asList(inputRecord)));
            break;
        case deleteOpportunities:
        case deleteOpportunityRoles:
            processResult(((MarketoRESTClient) client).deleteOpportunities(properties, Arrays.asList(inputRecord)));
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
            LOG.error(mktoResult.getErrorsString());
        }
        for (SyncStatus status : mktoResult.getRecords()) {
            if (Arrays.asList("created", "updated", "deleted", "scheduled", "triggered")
                    .contains(status.getStatus().toLowerCase())) {
                handleSuccess(fillRecord(status, flowSchema));
            } else {
                if (dieOnError) {
                    throw new IOException(status.getAvailableReason());
                }
                handleReject(fillRecord(status, rejectSchema));
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

    public IndexedRecord fillRecord(SyncStatus status, Schema schema) {
        IndexedRecord record = new Record(schema);
        for (Field f : schema.getFields()) {
            if (f.name().equals(FIELD_ID_SOAP)) {
                record.put(f.pos(), status.getId());
            } else if (f.name().equals(FIELD_ID_REST)) {
                record.put(f.pos(), status.getId());
            } else if (f.name().equals(FIELD_LEAD_ID)) {
                record.put(f.pos(), status.getId());
            } else if (f.name().equals(MarketoConstants.FIELD_CAMPAIGN_ID)) {
                record.put(f.pos(), status.getId());
            } else if (f.name().equals(FIELD_SUCCESS)) {
                record.put(f.pos(), Boolean.parseBoolean(status.getStatus()));
            } else if (f.name().equals(FIELD_STATUS)) {
                record.put(f.pos(), status.getStatus());
            } else if (f.name().equals(FIELD_ERROR_MSG)) {
                record.put(f.pos(), status.getAvailableReason());
                // manage CO fields
            } else if (f.name().equals(MarketoConstants.FIELD_MARKETO_GUID)) {
                record.put(f.pos(), status.getMarketoGUID());
            } else if (f.name().equals(MarketoConstants.FIELD_SEQ)) {
                record.put(f.pos(), status.getSeq());
            } else if (f.name().equals(MarketoConstants.FIELD_REASON)) {
                record.put(f.pos(), status.getAvailableReason());
            } else {
                record.put(schema.getField(f.name()).pos(), inputRecord.get(inputSchema.getField(f.name()).pos()));
            }
        }
        return record;
    }
}
