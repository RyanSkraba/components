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

import com.sforce.soap.partner.*;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.RuntimeHelper;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

final class SalesforceWriter implements Writer<WriterResult> {

    private SalesforceWriteOperation salesforceWriteOperation;

    private PartnerConnection connection;

    private String uId;

    private SalesforceSink sink;

    private RuntimeContainer adaptor;

    private TSalesforceOutputProperties sprops;

    private String upsertKeyColumn;

    protected List<String> deleteItems;

    protected List<SObject> insertItems;

    protected List<SObject> upsertItems;

    protected List<SObject> updateItems;

    protected int commitLevel;

    protected boolean exceptionForErrors;

    private int dataCount;

    private transient IndexedRecordAdapterFactory<Object, ? extends IndexedRecord> factory;

    private transient Schema schema;

    public SalesforceWriter(SalesforceWriteOperation salesforceWriteOperation, RuntimeContainer adaptor) {
        this.salesforceWriteOperation = salesforceWriteOperation;
        this.adaptor = adaptor;
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
        connection = sink.connect().connection;
        schema = RuntimeHelper.resolveSchema(adaptor, sink,
                new Schema.Parser().parse(sprops.module.schema.schema.getStringValue()));
        upsertKeyColumn = sprops.upsertKeyColumn.getStringValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(Object datum) throws IOException {
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

        if (!TSalesforceOutputProperties.ACTION_DELETE.equals(sprops.outputAction.getValue())) {
            SObject so = new SObject();
            so.setType(sprops.module.moduleName.getStringValue());

            for (Schema.Field f : input.getSchema().getFields()) {
                Object value = input.get(f.pos());
                if (value != null) {
                    Schema.Field se = schema.getField(f.name());
                    if (se != null) {
                        addSObjectField(so, f, se, value);
                    }
                }
            }

            switch (TSalesforceOutputProperties.OutputAction.valueOf(sprops.outputAction.getStringValue())) {
                case INSERT:
                    insert(so);
                    break;
                case UPDATE:
                    update(so);
                    break;
                case UPSERT:
                    upsert(so);
                    break;
                case DELETE:
                    // See below
                    throw new RuntimeException("Impossible");
            }
        } else { // DELETE
            String id = getIdValue(input);
            if (id != null) {
                delete(id);
            }
        }
        dataCount++;
    }

    protected String getIdValue(IndexedRecord input) {
        String ID = "Id";
        Schema.Field idField = input.getSchema().getField(ID);
        if (null != idField) {
            return (String) input.get(idField.pos());
        }
        throw new RuntimeException(ID + " not found");
    }

    protected void addSObjectField(SObject sObject, Schema.Field actual, Schema.Field expected, Object value) {
        Object valueToAdd = null;
        // Convert stuff here
        switch (expected.schema().getType()) {
            case BYTES:
                valueToAdd = Charset.defaultCharset().decode(ByteBuffer.wrap((byte[]) value)).toString();
                break;
            // case DATE:
            // case DATETIME:
            // valueToAdd = adaptor.formatDate((Date) value, se.getPattern());
            // break;
            default:
                valueToAdd = value;
                break;
        }
        sObject.setField(expected.name(), valueToAdd);
    }

    protected SaveResult[] insert(SObject sObject) throws IOException {
        insertItems.add(sObject);
        return doInsert();
    }

    protected SaveResult[] doInsert() throws IOException {
        if (insertItems.size() >= commitLevel) {
            SObject[] accs = insertItems.toArray(new SObject[insertItems.size()]);
            String[] changedItemKeys = new String[accs.length];
            SaveResult[] sr;
            try {
                sr = connection.create(accs);
                insertItems.clear();
                if (sr != null && sr.length != 0) {
                    int batch_idx = -1;
                    for (SaveResult result : sr) {
                        handleResults(result.getSuccess(), result.getErrors(), changedItemKeys, ++batch_idx);
                    }
                }
                return sr;
            } catch (ConnectionException e) {
                throw new IOException(e);
            }
        }
        return null;
    }

    protected SaveResult[] update(SObject sObject) throws IOException {
        updateItems.add(sObject);
        return doUpdate();
    }

    protected SaveResult[] doUpdate() throws IOException {
        if (updateItems.size() >= commitLevel) {
            SObject[] upds = updateItems.toArray(new SObject[updateItems.size()]);
            String[] changedItemKeys = new String[upds.length];
            for (int ix = 0; ix < upds.length; ++ix) {
                changedItemKeys[ix] = upds[ix].getId();
            }
            SaveResult[] saveResults;
            try {
                saveResults = connection.update(upds);
                updateItems.clear();
                upds = null;

                if (saveResults != null && saveResults.length != 0) {
                    int batch_idx = -1;
                    for (SaveResult result : saveResults) {
                        handleResults(result.getSuccess(), result.getErrors(), changedItemKeys, ++batch_idx);
                    }
                }
                return saveResults;
            } catch (ConnectionException e) {
                throw new IOException(e);
            }
        }
        return null;
    }

    protected UpsertResult[] upsert(SObject sObject) throws IOException {
        upsertItems.add(sObject);
        return doUpsert();
    }

    protected UpsertResult[] doUpsert() throws IOException {
        if (upsertItems.size() >= commitLevel) {
            SObject[] upds = upsertItems.toArray(new SObject[upsertItems.size()]);
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
                upsertItems.clear();
                upds = null;

                if (upsertResults != null && upsertResults.length != 0) {
                    int batch_idx = -1;
                    for (UpsertResult result : upsertResults) {
                        handleResults(result.getSuccess(), result.getErrors(), changedItemKeys, ++batch_idx);
                    }
                }
                return upsertResults;
            } catch (ConnectionException e) {
                new IOException(e);
            }
        }
        return null;

    }

    protected void handleResults(boolean success, Error[] resultErrors, String[] changedItemKeys, int batchIdx)
            throws IOException {
        StringBuilder errors = new StringBuilder("");
        if (success) {
            // TODO: send back the ID
        } else {
            errors = SalesforceRuntime.addLog(resultErrors, batchIdx < changedItemKeys.length ? changedItemKeys[batchIdx]
                    : "Batch index out of bounds", null);
        }
        if (exceptionForErrors && errors.toString().length() > 0) {
            throw new IOException(errors.toString());
        }
    }

    protected DeleteResult[] delete(String id) throws IOException {
        if (id == null) {
            return null;
        }
        deleteItems.add(id);
        return doDelete();
    }

    protected DeleteResult[] doDelete() throws IOException {
        if (deleteItems.size() >= commitLevel) {
            String[] delIDs = deleteItems.toArray(new String[deleteItems.size()]);
            String[] changedItemKeys = new String[delIDs.length];
            for (int ix = 0; ix < delIDs.length; ++ix) {
                changedItemKeys[ix] = delIDs[ix];
            }
            DeleteResult[] dr;
            try {
                dr = connection.delete(delIDs);
                deleteItems.clear();

                if (dr != null && dr.length != 0) {
                    int batch_idx = -1;
                    for (DeleteResult result : dr) {
                        handleResults(result.getSuccess(), result.getErrors(), changedItemKeys, ++batch_idx);
                    }
                }
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
        return new WriterResult(uId, dataCount);
    }

    protected void logout() throws IOException {
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
}