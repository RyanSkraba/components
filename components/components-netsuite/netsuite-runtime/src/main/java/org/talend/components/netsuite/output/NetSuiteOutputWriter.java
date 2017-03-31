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
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.netsuite.SchemaCustomMetaDataSource;
import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.NsWriteResponse;
import org.talend.components.netsuite.client.model.TypeDesc;

/**
 * TODO Implement bulk Add/Update/Upsert/Delete
 */
public class NetSuiteOutputWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    protected final NetSuiteWriteOperation writeOperation;

    protected final List<IndexedRecord> successfulWrites = new ArrayList<>();

    protected final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    protected boolean exceptionForErrors = true;

    protected NetSuiteClientService<?> clientService;
    protected MetaDataSource metaDataSource;

    protected OutputAction action;

    protected TypeDesc typeDesc;
    protected NsObjectOutputTransducer transducer;

    protected int dataCount = 0;

    public NetSuiteOutputWriter(NetSuiteWriteOperation writeOperation) {
        this.writeOperation = writeOperation;
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

            Schema schema = writeOperation.getSchema();

            MetaDataSource originalMetaDataSource = clientService.getMetaDataSource();
            metaDataSource = clientService.createDefaultMetaDataSource();
            metaDataSource.setCustomizationEnabled(originalMetaDataSource.isCustomizationEnabled());
            SchemaCustomMetaDataSource schemaCustomMetaDataSource = new SchemaCustomMetaDataSource(
                    clientService.getBasicMetaData(), originalMetaDataSource.getCustomMetaDataSource(), schema);
            metaDataSource.setCustomMetaDataSource(schemaCustomMetaDataSource);

            action = writeOperation.getProperties().module.action.getValue();

            String typeName = writeOperation.getProperties().module.moduleName.getValue();
            typeDesc = metaDataSource.getTypeInfo(typeName);

            transducer = new NsObjectOutputTransducer(clientService, typeDesc.getTypeName());
            transducer.setMetaDataSource(metaDataSource);

            if (action == OutputAction.UPDATE || action == OutputAction.UPSERT) {
                transducer.setRecordSource(new NsObjectOutputTransducer.DefaultRecordSource(clientService));
            } else if (action == OutputAction.DELETE) {
                transducer.setReference(true);
            }

        } catch (NetSuiteException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void write(Object object) throws IOException {
        IndexedRecord record = (IndexedRecord) object;
        NsWriteResponse<?> writeResponse;
        if (action == OutputAction.ADD) {
            writeResponse = clientService.add(transduceRecord(record));
        } else if (action == OutputAction.UPDATE) {
            writeResponse = clientService.update(transduceRecord(record));
        } else if (action == OutputAction.UPSERT) {
            writeResponse = clientService.upsert(transduceRecord(record));
        } else if (action == OutputAction.DELETE) {
            writeResponse = clientService.delete(transduceRecord(record));
        } else {
            throw new NetSuiteException("Output operation not implemented: " + action);
        }
        if (writeResponse.getStatus().isSuccess()) {
            successfulWrites.add(record);
        } else {
            if (exceptionForErrors) {
                NetSuiteClientService.checkError(writeResponse.getStatus());
            }
            rejectedWrites.add(record);
        }
        dataCount++;
    }

    @Override
    public Result close() throws IOException {
        Result result = new Result();
        result.totalCount = dataCount;
        result.successCount = successfulWrites.size();
        result.rejectCount = rejectedWrites.size();
        return result;
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

    protected Object transduceRecord(IndexedRecord record) throws IOException {
        return transducer.write(record);
    }

}
