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

package org.talend.components.netsuite.output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl;
import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.NsRef;
import org.talend.components.netsuite.client.NsStatus;
import org.talend.components.netsuite.client.NsWriteResponse;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.TypeDesc;

/**
 * Responsible for bulk writing of records.
 *
 * Subclasses override {@link #doWrite(List)} to perform required output operation:
 * <ul>
 *     <li>Add - {@link NetSuiteAddWriter}</li>
 *     <li>Update - {@link NetSuiteUpdateWriter}</li>
 *     <li>Upsert - {@link NetSuiteUpsertWriter}</li>
 *     <li>Delete - {@link NetSuiteDeleteWriter}</li>
 * </ul>
 *
 * @param <T> type of NetSuite objects that are passed to {@link NetSuiteClientService}
 * @param <RefT> type of NetSuite reference objects
 */
public abstract class NetSuiteOutputWriter<T, RefT> implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    protected transient final Logger logger = LoggerFactory.getLogger(getClass());

    /** Write operation which created this writer. */
    private final NetSuiteWriteOperation writeOperation;

    /** Runtime container for this writer. */
    private final RuntimeContainer container;

    // Holds accumulated write responses for a current batch
    private final List<NsWriteResponse<RefT>> writeResponses = new ArrayList<>();

    // Holds accumulated successful write result records for a current batch
    private final List<IndexedRecord> successfulWrites = new ArrayList<>();

    // Holds accumulated rejected write result records for a current batch
    private final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    /** Specifies whether to throw exception for write errors. */
    private boolean exceptionForErrors = true;

    private int batchSize = NetSuiteOutputProperties.DEFAULT_BATCH_SIZE;

    // Holds accumulated IndexedRecords for a current batch
    private List<IndexedRecord> inputRecordList = new ArrayList<>();

    private Result result = new Result();

    protected NetSuiteClientService<?> clientService;

    /** Source of meta data used. */
    protected MetaDataSource metaDataSource;

    /** Main schema. */
    protected Schema schema;

    /** Schema for outgoing success flow. */
    protected Schema flowSchema;

    /** Schema for outgoing reject flow. */
    protected Schema rejectSchema;

    /** Descriptor of target NetSuite data model object type. */
    protected TypeDesc typeDesc;

    /** Translates {@code IndexedRecord} to NetSuite data object. */
    protected NsObjectOutputTransducer transducer;

    public NetSuiteOutputWriter(NetSuiteWriteOperation writeOperation, RuntimeContainer container, MetaDataSource metaDataSource) {
        this.writeOperation = writeOperation;
        this.container = container;
        this.metaDataSource = metaDataSource;
    }

    public boolean isExceptionForErrors() {
        return exceptionForErrors;
    }

    public void setExceptionForErrors(boolean exceptionForErrors) {
        this.exceptionForErrors = exceptionForErrors;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public Iterable<IndexedRecord> getSuccessfulWrites() {
        // If successful write feedback is requested before submitting of current batch
        // then write accumulated records to provide feedback to a caller.
        // This is required due to bug in DI job which is not aware of bulk writes.
        flush();
        return successfulWrites;
    }

    @Override
    public Iterable<IndexedRecord> getRejectedWrites() {
        // If rejected write feedback is requested before submitting of current batch
        // then write accumulated records to provide feedback to a caller.
        // This is required due to bug in DI job which is not aware of bulk writes.
        flush();
        return rejectedWrites;
    }

    public Iterable<NsWriteResponse<RefT>> getWriteResponses() {
        // If write feedback is requested before submitting of current batch
        // then write accumulated records to provide feedback to a caller.
        // This is required due to bug in DI job which is not aware of bulk writes.
        flush();
        return writeResponses;
    }

    @Override
    public void open(String uId) throws IOException {
        try {
            clientService = writeOperation.getSink().getClientService();

            // Get descriptor of target NetSuite data model object type.
            String typeName = writeOperation.getProperties().module.moduleName.getValue();
            typeDesc = metaDataSource.getTypeInfo(typeName);

            schema = writeOperation.getProperties().module.main.schema.getValue();
            flowSchema = writeOperation.getProperties().module.flowSchema.schema.getValue();
            rejectSchema = writeOperation.getProperties().module.rejectSchema.schema.getValue();

            initTransducer();

        } catch (NetSuiteException e) {
            throw new IOException(e);
        }
    }

    /**
     * Initialize transducer.
     *
     * <p>Subclasses can override this method to customize transducer.
     */
    protected void initTransducer() {
        transducer = new NsObjectOutputTransducer(clientService, typeDesc.getTypeName());
        transducer.setMetaDataSource(metaDataSource);
        transducer.setApiVersion(writeOperation.getProperties().connection.apiVersion.getValue());
    }

    @Override
    public void write(Object object) throws IOException {
        IndexedRecord record = (IndexedRecord) object;

        inputRecordList.add(record);

        if (inputRecordList.size() == batchSize) {
            // If batch is full then submit it.
            flush();
        }
    }

    /**
     * Flush current batch. If batch is empty the method does nothing.
     */
    private void flush() {
        try {
            write(inputRecordList);
        } finally {
            inputRecordList.clear();
        }
    }

    /**
     * Process and write given list of <code>IndexedRecord</code>s.
     *
     * @param indexedRecordList list of records to be processed
     */
    private void write(List<IndexedRecord> indexedRecordList) {
        if (indexedRecordList.isEmpty()) {
            return;
        }

        clearWriteFeedback();

        // Transduce IndexedRecords to NetSuite data model objects

        List<T> nsObjectList = new ArrayList<>(indexedRecordList.size());
        for (IndexedRecord indexedRecord : indexedRecordList) {
            Object nsObject = transducer.write(indexedRecord);
            nsObjectList.add((T) nsObject);
        }

        // Write NetSuite objects and process write responses

        List<NsWriteResponse<RefT>> responseList = doWrite(nsObjectList);

        for (int i = 0; i < responseList.size(); i++) {
            NsWriteResponse<RefT> response = responseList.get(i);
            IndexedRecord indexedRecord = indexedRecordList.get(i);
            processWriteResponse(response, indexedRecord);
        }
    }

    /**
     * Process NetSuite write response and produce result record for outgoing flow.
     *
     * @param response write response to be processed
     * @param indexedRecord indexed record which was submitted
     */
    private void processWriteResponse(NsWriteResponse<RefT> response, IndexedRecord indexedRecord) {
        writeResponses.add(response);
        processReturnVariables(response);
        result.totalCount++;

        if (response.getStatus().isSuccess()) {
            IndexedRecord targetRecord = createSuccessRecord(response, indexedRecord);
            successfulWrites.add(targetRecord);
            result.successCount++;
        } else {
            if (exceptionForErrors) {
                NetSuiteClientService.checkError(response.getStatus());
            }
            IndexedRecord targetRecord = createRejectRecord(response, indexedRecord);
            rejectedWrites.add(targetRecord);
            result.rejectCount++;
        }
    }

    /**
     * Update return variables from given {@code IndexedRecord} after write.
     *
     * @param response write response
     */
    private void processReturnVariables(final NsWriteResponse<RefT> response) {
        if (container != null) {
            String internalId = null;
            if (response.getRef() != null) {
                NsRef ref = NsRef.fromNativeRef(response.getRef());
                internalId = ref.getInternalId();
            }
            try {
                // For compatibility with old component, current internal ID is stored as Integer.
                container.setComponentData(container.getCurrentComponentId(),
                        NetSuiteOutputDefinition.RETURN_LEGACY_CURRENT_INTERNAL_ID,
                        (internalId != null ? Integer.parseInt(internalId) : null));
            } catch (NumberFormatException e) {
                // Normally this should not happen but we need to avoid failure of writer.
                logger.error("Couldn't parse internalId as Integer: {}", internalId);
            }
        }
    }

    /**
     * Clear accumulated write results.
     */
    private void clearWriteFeedback() {
        writeResponses.clear();
        successfulWrites.clear();
        rejectedWrites.clear();
    }

    /**
     * Create record for outgoing {@code success} flow.
     *
     * @param response write response
     * @param record indexed record which was written
     * @return result record
     */
    private IndexedRecord createSuccessRecord(NsWriteResponse<RefT> response, IndexedRecord record) {
        NsRef ref = NsRef.fromNativeRef(response.getRef());

        GenericData.Record targetRecord = new GenericData.Record(flowSchema);

        for (Schema.Field field : schema.getFields()) {
            Schema.Field targetField = flowSchema.getField(field.name());
            if (targetField != null) {
                Object value = record.get(field.pos());
                targetRecord.put(targetField.name(), value);
            }
        }

        Schema.Field internalIdField = NetSuiteDatasetRuntimeImpl.getNsFieldByName(flowSchema, "internalId");
        if (internalIdField != null && targetRecord.get(internalIdField.pos()) == null) {
            targetRecord.put(internalIdField.pos(), ref.getInternalId());
        }
        Schema.Field externalIdField = NetSuiteDatasetRuntimeImpl.getNsFieldByName(flowSchema, "externalId");
        if (externalIdField != null && targetRecord.get(externalIdField.pos()) == null) {
            targetRecord.put(externalIdField.pos(), ref.getExternalId());
        }
        if (ref.getRefType() == RefType.CUSTOMIZATION_REF) {
            Schema.Field scriptIdField = NetSuiteDatasetRuntimeImpl.getNsFieldByName(flowSchema, "scriptId");
            if (scriptIdField != null && targetRecord.get(scriptIdField.pos()) == null) {
                targetRecord.put(scriptIdField.pos(), ref.getScriptId());
            }
        }

        return targetRecord;
    }

    /**
     * Create record for outgoing {@code reject} flow.
     *
     * @param response write response
     * @param record indexed record which was submitted
     * @return result record
     */
    private IndexedRecord createRejectRecord(NsWriteResponse<RefT> response, IndexedRecord record) {
        GenericData.Record targetRecord = new GenericData.Record(rejectSchema);

        for (Schema.Field field : schema.getFields()) {
            Schema.Field targetField = rejectSchema.getField(field.name());
            if (targetField != null) {
                Object value = record.get(field.pos());
                targetRecord.put(targetField.name(), value);
            }
        }

        String errorCode;
        String errorMessage;
        NsStatus status = response.getStatus();
        if (!status.getDetails().isEmpty()) {
            errorCode = status.getDetails().get(0).getCode();
            errorMessage = status.getDetails().get(0).getMessage();
        } else {
            errorCode = "GENERAL_ERROR";
            errorMessage = "Operation failed";
        }

        Schema.Field errorCodeField = NetSuiteDatasetRuntimeImpl.getNsFieldByName(rejectSchema, "errorCode");
        if (errorCodeField != null) {
            targetRecord.put(errorCodeField.pos(), errorCode);
        }
        Schema.Field errorMessageField = NetSuiteDatasetRuntimeImpl.getNsFieldByName(rejectSchema, "errorMessage");
        if (errorMessageField != null) {
            targetRecord.put(errorMessageField.pos(), errorMessage);
        }

        return targetRecord;
    }

    /**
     * Perform <code>write</code> of NetSuite objects.
     *
     * @param nsObjectList list of NetSuite objects to be written
     * @return list of write responses, the order is the same as in written object list
     */
    protected abstract List<NsWriteResponse<RefT>> doWrite(List<T> nsObjectList);

    @Override
    public Result close() throws IOException {
        // Write remaining objects
        flush();
        return result;
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

}
