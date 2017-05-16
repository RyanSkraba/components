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
import static org.talend.components.marketo.MarketoConstants.FIELD_STATUS;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.MarketoRESTClient;
import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties;
import org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties.CampaignAction;

public class MarketoCampaignWriter extends MarketoWriter {

    TMarketoCampaignProperties properties;

    CampaignAction operation;

    public MarketoCampaignWriter(MarketoWriteOperation marketoWriteOperation, RuntimeContainer adaptor) {
        super(marketoWriteOperation, adaptor);
    }

    @Override
    public void open(String uId) throws IOException {
        super.open(uId);

        properties = (TMarketoCampaignProperties) sink.properties;
        inputSchema = properties.schemaInput.schema.getValue();
        flowSchema = properties.schemaFlow.schema.getValue();
        rejectSchema = properties.schemaReject.schema.getValue();
        operation = properties.campaignAction.getValue();
        dieOnError = properties.dieOnError.getValue();
        batchSize = properties.batchSize.getValue();
        if (operation.equals(CampaignAction.trigger) && !properties.triggerCampaignForLeadsInBatch.getValue()) {
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
        if (recordsToProcess.size() >= batchSize) {
            processResult(((MarketoRESTClient) client).triggerCampaign(properties, recordsToProcess));
            recordsToProcess.clear();
        }
    }

    @Override
    public Result close() throws IOException {
        if (!recordsToProcess.isEmpty()) {
            processResult(((MarketoRESTClient) client).triggerCampaign(properties, recordsToProcess));
        }
        recordsToProcess.clear();
        return super.close();
    }

    public void processResult(MarketoSyncResult mktoResult) throws IOException {
        result.apiCalls++;
        if (!mktoResult.isSuccess()) {
            throw new IOException(mktoResult.getErrorsString());
        }
        for (SyncStatus status : mktoResult.getRecords()) {
            handleSuccess(fillRecord(status, flowSchema));
        }
    }

    private void handleSuccess(IndexedRecord record) {
        if (record != null) {
            result.successCount++;
            successfulWrites.add(record);
        }
    }

    public IndexedRecord fillRecord(SyncStatus status, Schema schema) {
        IndexedRecord record = new Record(schema);
        for (Field f : schema.getFields()) {
            if (f.name().equals(MarketoConstants.FIELD_CAMPAIGN_ID)) {
                record.put(f.pos(), status.getId());
            } else if (f.name().equals(FIELD_STATUS)) {
                record.put(f.pos(), status.getStatus());
            } else if (f.name().equals(FIELD_ERROR_MSG)) {
                record.put(f.pos(), status.getAvailableReason());
            } else {
                record.put(schema.getField(f.name()).pos(), inputRecord.get(inputSchema.getField(f.name()).pos()));
            }
        }
        return record;
    }

}
