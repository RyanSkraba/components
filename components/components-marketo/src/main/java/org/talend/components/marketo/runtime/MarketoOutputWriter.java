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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.runtime.GenericAvroRegistry;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.MarketoRESTClient;
import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.OperationType;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.OutputOperation;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class MarketoOutputWriter extends MarketoWriter {

    public static final String FIELD_ID_REST = "id";

    public static final String FIELD_ID_SOAP = "Id";

    public static final String FIELD_EMAIL_REST = "email";

    public static final String FIELD_EMAIL_SOAP = "Email";

    TMarketoOutputProperties properties;

    private OutputOperation operation;

    private OperationType operationType;

    private String lookupField;

    private Map<String, String> mappings;

    private Boolean deDupeEnabled = Boolean.FALSE;

    private int batchSize = 1;

    private List<IndexedRecord> recordsToProcess = new ArrayList<>();

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoOutputWriter.class);

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
        operationType = properties.operationType.getValue();// REST only
        lookupField = properties.lookupField.getValue().name(); // REST only
        mappings = properties.mappingInput.getNameMappingsForMarketo(); // SOAP only
        deDupeEnabled = properties.deDupeEnabled.getValue(); // syncMultipleLeads only
        batchSize = properties.batchSize.getValue(); // syncMultipleLeads only
    }

    @Override
    public void write(Object object) throws IOException {
        LOG.debug("[write] object = {}.", object);
        if (object == null) {
            return;
        }
        if (null == factory) {
            factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) GenericAvroRegistry.get()
                    .createIndexedRecordConverter(object.getClass());
        }
        inputRecord = factory.convertToAvro(object);
        result.totalCount++;
        // This for dynamic which would get schema from the first record
        if (inputSchema == null) {
            inputSchema = ((IndexedRecord) object).getSchema();
        }
        //
        MarketoSyncResult syncResult = null;
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
        // TODO see if we manage the list...
        case syncCustomObjects:
            processResult(((MarketoRESTClient) client).syncCustomObjects(properties, Arrays.asList(inputRecord)));
            break;
        case deleteCustomObjects:
            processResult(((MarketoRESTClient) client).deleteCustomObjects(properties, Arrays.asList(inputRecord)));
            break;
        }
    }

    @Override
    public Result close() throws IOException {
        if (recordsToProcess.size() > 0) {
            processResult(client.syncMultipleLeads(properties, recordsToProcess));
            recordsToProcess.clear();
        }
        return super.close();
    }

    public void processResult(MarketoSyncResult mktoResult) {
        result.apiCalls++;
        LOG.debug("[processResult] = {}.", mktoResult);
        if (mktoResult.isSuccess()) {
            for (SyncStatus r : mktoResult.getRecords()) {
                handleSuccess(fillSuccessRecord(r));
            }
        } else {
            for (MarketoError e : mktoResult.getErrors()) {
                handleReject(inputRecord, e);
            }
        }
    }

    private void handleSuccess(IndexedRecord record) {
        LOG.debug("[handleSuccess] record={}.", record);
        successfulWrites.clear();
        if (record != null) {
            result.successCount++;
            successfulWrites.add(record);
        }
    }

    private void handleReject(IndexedRecord record, MarketoError error) {
        LOG.debug("[handleReject] record={}. Error: {}.", record, error);
        rejectedWrites.clear();
        IndexedRecord reject = new GenericData.Record(rejectSchema);
        reject.put(rejectSchema.getField(FIELD_ERROR_MSG).pos(), error.getMessage());
        for (Schema.Field outField : reject.getSchema().getFields()) {
            Object outValue;
            Schema.Field inField = record.getSchema().getField(outField.name());
            if (inField != null) {
                outValue = record.get(inField.pos());
                reject.put(outField.pos(), outValue);
            }
        }
        LOG.debug("reject = {}.", reject);
        result.rejectCount++;
        rejectedWrites.add(reject);
    }

    public IndexedRecord fillSuccessRecord(SyncStatus status) {
        IndexedRecord record = new Record(flowSchema);
        for (Field f : flowSchema.getFields()) {
            if (f.name().equals(FIELD_ID_SOAP)) {
                record.put(f.pos(), status.getId());
            } else if (f.name().equals(FIELD_ID_REST)) {
                record.put(f.pos(), status.getId());
            } else if (f.name().equals(FIELD_LEAD_ID)) {
                record.put(f.pos(), status.getId());
            } else if (f.name().equals(FIELD_SUCCESS)) {
                record.put(f.pos(), Boolean.parseBoolean(status.getStatus()));
            } else if (f.name().equals(FIELD_STATUS)) {
                record.put(f.pos(), status.getStatus());
            } else if (f.name().equals(FIELD_ERROR_MSG)) {
                record.put(f.pos(), status.getReasons());
                // manage CO fields
            } else if (f.name().equals(MarketoConstants.FIELD_MARKETO_GUID)) {
                record.put(f.pos(), status.getMarketoGUID());
            } else if (f.name().equals(MarketoConstants.FIELD_SEQ)) {
                record.put(f.pos(), status.getSeq());
            } else if (f.name().equals(MarketoConstants.FIELD_REASON)) {
                record.put(f.pos(), status.getAvailableReason());
            } else {
                LOG.debug("f = {}.", f);
                record.put(flowSchema.getField(f.name()).pos(), inputRecord.get(inputSchema.getField(f.name()).pos()));
            }
        }

        return record;
    }
}
