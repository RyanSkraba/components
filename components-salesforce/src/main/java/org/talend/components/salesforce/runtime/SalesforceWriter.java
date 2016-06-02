// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.runtime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;
import org.talend.daikon.avro.util.AvroUtils;

import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

final class SalesforceWriter implements Writer<WriterResult> {

    private final SalesforceWriteOperation salesforceWriteOperation;

    private PartnerConnection connection;

    private String uId;

    private final SalesforceSink sink;

    private final RuntimeContainer container;

    private final TSalesforceOutputProperties sprops;

    private String upsertKeyColumn;

    protected final List<IndexedRecord> deleteItems;

    protected final List<IndexedRecord> insertItems;

    protected final List<IndexedRecord> upsertItems;

    protected final List<IndexedRecord> updateItems;

    protected final int commitLevel;

    protected boolean exceptionForErrors;

    private int dataCount;

    private int successCount;

    private int rejectCount;

    private int deleteFieldId = -1;

    private transient IndexedRecordAdapterFactory<Object, ? extends IndexedRecord> factory;

    private transient Schema schema;

    public SalesforceWriter(SalesforceWriteOperation salesforceWriteOperation, RuntimeContainer container) {
        this.salesforceWriteOperation = salesforceWriteOperation;
        this.container = container;
        sink = (SalesforceSink) salesforceWriteOperation.getSink();
        sprops = sink.getSalesforceOutputProperties();
        commitLevel = 1;
        int arraySize = commitLevel * 2;
        deleteItems = new ArrayList<>(arraySize);
        insertItems = new ArrayList<>(arraySize);
        updateItems = new ArrayList<>(arraySize);
        upsertItems = new ArrayList<>(arraySize);
        upsertKeyColumn = "";

    }

    @Override
    public void open(String uId) throws IOException {
        this.uId = uId;
        connection = sink.connect(container).connection;
        if (null == schema) {
            schema = sprops.module.main.schema.getValue();
            if (AvroUtils.isIncludeAllFields(schema)) {
                schema = sink.getSchema(connection, sprops.module.moduleName.getStringValue());
            } // else schema is fully specified
        }
        upsertKeyColumn = sprops.upsertKeyColumn.getStringValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(Object datum) throws IOException {
        dataCount++;
        // Ignore empty rows.
        if (null == datum) {
            return;
        }

        // This is all we need to do in order to ensure that we can process the incoming value as an IndexedRecord.
        if (null == factory) {
            factory = (IndexedRecordAdapterFactory<Object, ? extends IndexedRecord>) SalesforceAvroRegistry.get()
                    .createAdapterFactory(datum.getClass());
        }
        IndexedRecord input = factory.convertToAvro(datum);

        // Clean the feedback records at each write.
        salesforceWriteOperation.getSink().getSuccessfulWrites().clear();
        salesforceWriteOperation.getSink().getRejectedWrites().clear();

        switch (sprops.outputAction.getValue()) {
        case INSERT:
            insert(input);
            break;
        case UPDATE:
            update(input);
            break;
        case UPSERT:
            upsert(input);
            break;
        case DELETE:
            delete(input);
        }
    }

    private SObject createSObject(IndexedRecord input) {
        SObject so = new SObject();
        so.setType(sprops.module.moduleName.getStringValue());
        for (Schema.Field f : input.getSchema().getFields()) {
            Object value = input.get(f.pos());
            if (value != null) {
                Schema.Field se = schema.getField(f.name());
                if (se != null) {
                    addSObjectField(so, se.schema().getType(), se.name(), value);
                }
            }
        }
        return so;
    }

    private SObject createSObjectForUpsert(IndexedRecord input) {
        SObject so = new SObject();
        so.setType(sprops.module.moduleName.getStringValue());
        Map<String, Map<String, String>> referenceFieldsMap = getReferenceFieldsMap();
        for (Schema.Field f : input.getSchema().getFields()) {
            Object value = input.get(f.pos());
            if (value != null) {
                Schema.Field se = schema.getField(f.name());
                if (se != null) {
                    if (referenceFieldsMap != null && referenceFieldsMap.get(se.name()) != null) {
                        Map<String, String> relationMap = referenceFieldsMap.get(se.name());
                        String lookupFieldName = relationMap.get("lookupFieldName");
                        so.setField(lookupFieldName, null);
                        so.getChild(lookupFieldName).setField("type", relationMap.get("lookupFieldModuleName"));
                        addSObjectField(so.getChild(lookupFieldName), se.schema().getType(),
                                relationMap.get("lookupFieldExternalIdName"), value);
                    } else {
                        addSObjectField(so, se.schema().getType(), se.name(), value);
                    }
                }
            }
        }
        return so;
    }

    private void addSObjectField(XmlObject xmlObject, Schema.Type expected, String fieldName, Object value) {
        Object valueToAdd = null;
        // Convert stuff here
        switch (expected) {
        case BYTES:
            valueToAdd = Charset.defaultCharset().decode(ByteBuffer.wrap((byte[]) value)).toString();
            break;
        default:
            valueToAdd = value;
            break;
        }
        xmlObject.setField(fieldName, valueToAdd);
    }

    private SaveResult[] insert(IndexedRecord input) throws IOException {
        insertItems.add(input);
        return doInsert();
    }

    private SaveResult[] doInsert() throws IOException {
        if (insertItems.size() >= commitLevel) {
            SObject[] accs = new SObject[insertItems.size()];
            for (int i = 0; i < insertItems.size(); i++)
                accs[i] = createSObject(insertItems.get(i));

            String[] changedItemKeys = new String[accs.length];
            SaveResult[] saveResults;
            try {
                saveResults = connection.create(accs);
                if (saveResults != null && saveResults.length != 0) {
                    int batch_idx = -1;
                    for (int i = 0; i < saveResults.length; i++) {
                        if (saveResults[i].getSuccess())
                            handleSuccess(insertItems.get(i), saveResults[i].getId());
                        else
                            handleReject(insertItems.get(i), saveResults[i].getErrors(), changedItemKeys, ++batch_idx);
                    }
                }
                insertItems.clear();
                return saveResults;
            } catch (ConnectionException e) {
                throw new IOException(e);
            }
        }
        return null;
    }

    private SaveResult[] update(IndexedRecord input) throws IOException {
        updateItems.add(input);
        return doUpdate();
    }

    private SaveResult[] doUpdate() throws IOException {
        if (updateItems.size() >= commitLevel) {
            SObject[] upds = new SObject[updateItems.size()];
            for (int i = 0; i < updateItems.size(); i++)
                upds[i] = createSObject(updateItems.get(i));

            String[] changedItemKeys = new String[upds.length];
            for (int ix = 0; ix < upds.length; ++ix) {
                changedItemKeys[ix] = upds[ix].getId();
            }
            SaveResult[] saveResults;
            try {
                saveResults = connection.update(upds);
                upds = null;
                if (saveResults != null && saveResults.length != 0) {
                    int batch_idx = -1;
                    for (int i = 0; i < saveResults.length; i++) {
                        if (saveResults[i].getSuccess())
                            handleSuccess(updateItems.get(i), saveResults[i].getId());
                        else
                            handleReject(updateItems.get(i), saveResults[i].getErrors(), changedItemKeys, ++batch_idx);
                    }
                }
                updateItems.clear();
                return saveResults;
            } catch (ConnectionException e) {
                throw new IOException(e);
            }
        }
        return null;
    }

    private UpsertResult[] upsert(IndexedRecord input) throws IOException {
        upsertItems.add(input);
        return doUpsert();
    }

    private UpsertResult[] doUpsert() throws IOException {
        if (upsertItems.size() >= commitLevel) {
            SObject[] upds = new SObject[upsertItems.size()];
            for (int i = 0; i < upsertItems.size(); i++)
                upds[i] = createSObjectForUpsert(upsertItems.get(i));

            String[] changedItemKeys = new String[upds.length];
            for (int ix = 0; ix < upds.length; ++ix) {
                Object value = upds[ix].getField(upsertKeyColumn);
                if (value == null) {
                    changedItemKeys[ix] = "No value for " + upsertKeyColumn + " ";
                } else {
                    changedItemKeys[ix] = upsertKeyColumn;
                }
            }
            UpsertResult[] upsertResults;
            try {
                upsertResults = connection.upsert(upsertKeyColumn, upds);
                upds = null;
                if (upsertResults != null && upsertResults.length != 0) {
                    int batch_idx = -1;
                    for (int i = 0; i < upsertResults.length; i++) {
                        if (upsertResults[i].getSuccess())
                            handleSuccess(upsertItems.get(i), upsertResults[i].getId());
                        else
                            handleReject(upsertItems.get(0), upsertResults[i].getErrors(), changedItemKeys, ++batch_idx);
                    }
                }
                upsertItems.clear();
                return upsertResults;
            } catch (ConnectionException e) {
                new IOException(e);
            }
        }
        return null;

    }

    private void handleSuccess(IndexedRecord input, String id) {
        successCount++;
        Schema outSchema = sprops.schemaFlow.schema.getValue();
        if (outSchema == null || outSchema.getFields().size() == 0)
            return;
        if (input.getSchema().equals(outSchema)) {
            sink.getSuccessfulWrites().add(input);
        } else {
            IndexedRecord successful = new GenericData.Record(outSchema);
            for (Schema.Field outField : successful.getSchema().getFields()) {
                Object outValue = null;
                Schema.Field inField = input.getSchema().getField(outField.name());
                if (inField != null)
                    outValue = input.get(inField.pos());
                else if (TSalesforceOutputProperties.FIELD_SALESFORCE_ID.equals(outField.name()))
                    outValue = id;
                successful.put(outField.pos(), outValue);
            }
            sink.getSuccessfulWrites().add(successful);
        }
    }

    private void handleReject(IndexedRecord input, Error[] resultErrors, String[] changedItemKeys, int batchIdx)
            throws IOException {

        rejectCount++;
        Schema outSchema = sprops.schemaReject.schema.getValue();
        if (outSchema == null || outSchema.getFields().size() == 0)
            return;
        if (input.getSchema().equals(outSchema)) {
            sink.getRejectedWrites().add(input);
        } else {
            IndexedRecord reject = new GenericData.Record(outSchema);
            for (Schema.Field outField : reject.getSchema().getFields()) {
                Object outValue = null;
                Schema.Field inField = input.getSchema().getField(outField.name());
                if (inField != null)
                    outValue = input.get(inField.pos());
                else if (resultErrors.length > 0) {
                    Error error = resultErrors[0];
                    if (TSalesforceOutputProperties.FIELD_ERROR_CODE.equals(outField.name()))
                        outValue = error.getStatusCode() != null ? error.getStatusCode().toString() : null;
                    else if (TSalesforceOutputProperties.FIELD_ERROR_FIELDS.equals(outField.name())) {
                        StringBuffer fields = new StringBuffer();
                        for (String field : error.getFields()) {
                            fields.append(field);
                            fields.append(",");
                        }
                        if (fields.length() > 0) {
                            fields.deleteCharAt(fields.length() - 1);
                        }
                        outValue = fields.toString();
                    } else if (TSalesforceOutputProperties.FIELD_ERROR_MESSAGE.equals(outField.name()))
                        outValue = error.getMessage();
                }
                reject.put(outField.pos(), outValue);
            }
            sink.getRejectedWrites().add(reject);
        }
    }

    private DeleteResult[] delete(IndexedRecord input) throws IOException {
        // Calculate the field position of the Id the first time that it is used. The Id field must be present in the
        // schema to delete rows.
        if (deleteFieldId == -1) {
            String ID = "Id";
            Schema.Field idField = input.getSchema().getField(ID);
            if (null == idField)
                throw new RuntimeException(ID + " not found");
            deleteFieldId = idField.pos();
        }
        String id = (String) input.get(deleteFieldId);
        if (id == null) {
            return null;
        }
        deleteItems.add(input);
        return doDelete();
    }

    private DeleteResult[] doDelete() throws IOException {
        if (deleteItems.size() >= commitLevel) {
            String[] delIDs = new String[deleteItems.size()];
            String[] changedItemKeys = new String[delIDs.length];
            for (int ix = 0; ix < delIDs.length; ++ix) {
                delIDs[ix] = (String) deleteItems.get(ix).get(deleteFieldId);
                changedItemKeys[ix] = delIDs[ix];
            }
            DeleteResult[] dr;
            try {
                dr = connection.delete(delIDs);
                if (dr != null && dr.length != 0) {
                    int batch_idx = -1;
                    for (int i = 0; i < dr.length; i++) {
                        if (dr[i].getSuccess())
                            handleSuccess(deleteItems.get(i), dr[i].getId());
                        else
                            handleReject(deleteItems.get(i), dr[i].getErrors(), changedItemKeys, ++batch_idx);
                    }
                }
                deleteItems.clear();
                return dr;
            } catch (ConnectionException e) {
                throw new IOException(e);
            }
        }
        return null;
    }

    @Override
    public WriterResult close() throws IOException {
        logout();
        // this should be computed according to the result of the write I guess but I don't know yet how exceptions are
        // handled by Beam.
        if (container != null) {
            container.setComponentData(container.getCurrentComponentId(), SalesforceOutputProperties.NB_LINE_NAME, dataCount);
            container.setComponentData(container.getCurrentComponentId(), SalesforceOutputProperties.NB_SUCCESS_NAME, successCount);
            container.setComponentData(container.getCurrentComponentId(), SalesforceOutputProperties.NB_REJECT_NAME, rejectCount);
        }
        return new WriterResult(uId, dataCount);
    }

    private void logout() throws IOException {
        // Finish anything uncommitted
        doInsert();
        doDelete();
        doUpdate();
        doUpsert();
    }

    @Override
    public WriteOperation<WriterResult> getWriteOperation() {
        return salesforceWriteOperation;
    }

    private Map<String, Map<String, String>> getReferenceFieldsMap() {
        List<String> columns = sprops.upsertRelationTable.columnName.getValue();
        Map<String, Map<String, String>> referenceFieldsMap = null;
        if (columns != null) {
            referenceFieldsMap = new HashMap<>();
            List<String> lookupFieldModuleNames = sprops.upsertRelationTable.lookupFieldModuleName.getValue();
            List<String> lookupFieldNames = sprops.upsertRelationTable.lookupFieldName.getValue();
            List<String> externalIdFromLookupFields = sprops.upsertRelationTable.lookupFieldExternalIdName.getValue();
            for (int index = 0; index < columns.size(); index++) {
                Map<String, String> relationMap = new HashMap<>();
                relationMap.put("lookupFieldModuleName", lookupFieldModuleNames.get(index));
                relationMap.put("lookupFieldName", lookupFieldNames.get(index));
                relationMap.put("lookupFieldExternalIdName", externalIdFromLookupFields.get(index));
                referenceFieldsMap.put(columns.get(index), relationMap);
            }
        }
        return referenceFieldsMap;
    }

}
