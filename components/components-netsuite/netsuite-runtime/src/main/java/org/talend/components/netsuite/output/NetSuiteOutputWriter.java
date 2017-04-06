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

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.NsWriteResponse;
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

    private final NetSuiteWriteOperation writeOperation;

    private final List<IndexedRecord> successfulWrites = new ArrayList<>();

    private final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    private boolean exceptionForErrors = true;

    private int batchSize = NetSuiteOutputProperties.DEFAULT_BATCH_SIZE;

    // Holds accumulated IndexedRecords for a current batch
    private List<IndexedRecord> inputRecordList = new ArrayList<>();

    private Result result = new Result();

    protected NetSuiteClientService<?> clientService;

    protected MetaDataSource metaDataSource;

    protected TypeDesc typeDesc;

    protected NsObjectOutputTransducer transducer;

    public NetSuiteOutputWriter(NetSuiteWriteOperation writeOperation, MetaDataSource metaDataSource) {
        this.writeOperation = writeOperation;
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
        return successfulWrites;
    }

    @Override
    public Iterable<IndexedRecord> getRejectedWrites() {
        return rejectedWrites;
    }

    @Override
    public void open(String uId) throws IOException {
        try {
            clientService = writeOperation.getSink().getClientService();

            String typeName = writeOperation.getProperties().module.moduleName.getValue();
            typeDesc = metaDataSource.getTypeInfo(typeName);

            initTransducer();

        } catch (NetSuiteException e) {
            throw new IOException(e);
        }
    }

    protected void initTransducer() {
        transducer = new NsObjectOutputTransducer(clientService, typeDesc.getTypeName());
        transducer.setMetaDataSource(metaDataSource);
    }

    @Override
    public void write(Object object) throws IOException {
        IndexedRecord record = (IndexedRecord) object;

        inputRecordList.add(record);

        if (inputRecordList.size() == batchSize) {
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

    private void processWriteResponse(NsWriteResponse<RefT> response, IndexedRecord indexedRecord) {
        if (response.getStatus().isSuccess()) {
            successfulWrites.add(indexedRecord);
        } else {
            if (exceptionForErrors) {
                NetSuiteClientService.checkError(response.getStatus());
            }
            rejectedWrites.add(indexedRecord);
        }

        result.totalCount++;
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

        result.successCount = successfulWrites.size();
        result.rejectCount = rejectedWrites.size();

        return result;
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

}
