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
import static org.talend.components.marketo.MarketoConstants.FIELD_LEAD_KEY_TYPE;
import static org.talend.components.marketo.MarketoConstants.FIELD_LEAD_KEY_VALUE;
import static org.talend.components.marketo.MarketoConstants.FIELD_LIST_ID;
import static org.talend.components.marketo.MarketoConstants.FIELD_LIST_KEY_TYPE;
import static org.talend.components.marketo.MarketoConstants.FIELD_LIST_KEY_VALUE;
import static org.talend.components.marketo.MarketoConstants.FIELD_STATUS;
import static org.talend.components.marketo.MarketoConstants.FIELD_SUCCESS;
import static org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties.ListOperation.addTo;
import static org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties.ListOperation.isMemberOf;

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
import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;
import org.talend.components.marketo.runtime.client.type.ListOperationParameters;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties.ListOperation;

public class MarketoListOperationWriter extends MarketoWriter {

    private TMarketoListOperationProperties properties;

    private Boolean multipleOperations = Boolean.FALSE;

    private ListOperationParameters listOpeParms;

    private ListOperation operation;

    private static final Logger LOG = LoggerFactory.getLogger(MarketoListOperationWriter.class);

    public MarketoListOperationWriter(WriteOperation writeOperation, RuntimeContainer runtime) {
        super(writeOperation, runtime);
    }

    @Override
    public void open(String uId) throws IOException {
        super.open(uId);

        properties = (TMarketoListOperationProperties) sink.getProperties();
        inputSchema = properties.schemaInput.schema.getValue();
        flowSchema = properties.schemaFlow.schema.getValue();
        rejectSchema = properties.schemaReject.schema.getValue();
        multipleOperations = properties.multipleOperation.getValue();
        listOpeParms = new ListOperationParameters();
        listOpeParms.setApiMode(api);
        operation = properties.listOperation.getValue();
        dieOnError = properties.dieOnError.getValue();
        //
        listOpeParms = new ListOperationParameters();
        listOpeParms.setApiMode(api);
        listOpeParms.setOperation(operation.name());
        listOpeParms.setStrict(dieOnError);
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
        addRecordToListOperationParameters(inputRecord);
        //
    }

    @Override
    public Result close() throws IOException {
        // check if we have processed every record...
        flush();
        return super.close();
    }

    @Override
    protected void flush() {
        if (!listOpeParms.isValid()) {
            return;
        }
        // process the list operation
        switch (operation) {
        case addTo:
            processResult(client.addToList(listOpeParms));
            break;
        case isMemberOf:
            processResult(client.isMemberOfList(listOpeParms));
            break;
        case removeFrom:
            processResult(client.removeFromList(listOpeParms));
            break;
        }
        // clear the list
        listOpeParms.reset();
    }

    public Boolean isSameListForListOperation(IndexedRecord record) {
        if (use_soap_api) {
            return listOpeParms.getListKeyType().equals(record.get(inputSchema.getField(FIELD_LIST_KEY_TYPE).pos()).toString())
                    && listOpeParms.getListKeyValue()
                            .equals(record.get(inputSchema.getField(FIELD_LIST_KEY_VALUE).pos()).toString());
        } else {
            return listOpeParms.getListId().equals(record.get(inputSchema.getField(FIELD_LIST_ID).pos()));
        }
    }

    public void addRecordToListOperationParameters(IndexedRecord record) {
        if (!listOpeParms.isValid() || !multipleOperations || operation.equals(isMemberOf)) {
            buildListOperationParameters(record);
            return;
        }
        // while the List doesn't change we accumulate the Lead keys...
        if (isSameListForListOperation(inputRecord)) {
            addLeadKeyToListOperationParameters(inputRecord);
        } else {
            flush();
            buildListOperationParameters(inputRecord);
        }
    }

    public ListOperationParameters buildListOperationParameters(IndexedRecord record) {
        listOpeParms.reset();
        if (use_soap_api) {
            listOpeParms.setListKeyType(String.valueOf(record.get(inputSchema.getField(FIELD_LIST_KEY_TYPE).pos())));
            listOpeParms.setListKeyValue(String.valueOf(record.get(inputSchema.getField(FIELD_LIST_KEY_VALUE).pos())));
            listOpeParms.setLeadKeyType(String.valueOf(record.get(inputSchema.getField(FIELD_LEAD_KEY_TYPE).pos())));
            listOpeParms.setLeadKeyValue(
                    new String[] { String.valueOf(record.get(inputSchema.getField(FIELD_LEAD_KEY_VALUE).pos())) });
        } else {
            listOpeParms.setListId((Integer) record.get(inputSchema.getField(FIELD_LIST_ID).pos()));
            listOpeParms.setLeadIds(new Integer[] { (Integer) record.get(inputSchema.getField(FIELD_LEAD_ID).pos()) });
        }
        LOG.debug("[buildParameters] {}.", listOpeParms);
        return listOpeParms;
    }

    public ListOperationParameters addLeadKeyToListOperationParameters(IndexedRecord record) {
        if (use_soap_api) {
            listOpeParms.getLeadKeyValue().add(String.valueOf(record.get(inputSchema.getField(FIELD_LEAD_KEY_VALUE).pos())));
        } else {
            listOpeParms.getLeadIds().add((Integer) record.get(inputSchema.getField(FIELD_LEAD_ID).pos()));
        }
        return listOpeParms;
    }

    public IndexedRecord fillRecord(SyncStatus status, Schema schema) {
        IndexedRecord record = new Record(schema);
        for (Field f : schema.getFields()) {
            if (f.name().equals(FIELD_LIST_KEY_TYPE)) {
                record.put(f.pos(), listOpeParms.getListKeyType());
            }
            if (f.name().equals(FIELD_LIST_KEY_VALUE)) {
                record.put(f.pos(), listOpeParms.getListKeyValue());
            }
            if (f.name().equals(FIELD_LEAD_KEY_TYPE)) {
                record.put(f.pos(), listOpeParms.getLeadKeyType());
            }
            if (f.name().equals(FIELD_LEAD_KEY_VALUE)) {
                record.put(f.pos(), status.getId());
            }
            if (f.name().equals(FIELD_LIST_ID)) {
                record.put(f.pos(), listOpeParms.getListId());
            }
            if (f.name().equals(FIELD_LEAD_ID)) {
                record.put(f.pos(), status.getId());
            }
            if (f.name().equals(FIELD_SUCCESS)) {
                record.put(f.pos(), Boolean.parseBoolean(status.getStatus()));
            }
            if (f.name().equals(FIELD_STATUS)) {
                record.put(f.pos(), status.getStatus());
            }
            if (f.name().equals(FIELD_ERROR_MSG)) {
                record.put(f.pos(), status.getAvailableReason());
            }
        }
        return record;
    }

    private void processResult(MarketoSyncResult mktoResult) {
        successfulWrites.clear();
        rejectedWrites.clear();
        result.apiCalls++;
        if (!mktoResult.isSuccess()) {
            if (dieOnError) {
                throw new MarketoRuntimeException(mktoResult.getErrorsString());
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
        for (SyncStatus status : mktoResult.getRecords()) {
            if (Arrays.asList("true", "added", "removed", "notmemberof", "memberof").contains(status.getStatus().toLowerCase())
                    || (properties.isApiSOAP() && !addTo.equals(operation))) {
                handleSuccess(fillRecord(status, flowSchema));
            } else {
                if (dieOnError) {
                    throw new MarketoRuntimeException(status.getAvailableReason());
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
}
