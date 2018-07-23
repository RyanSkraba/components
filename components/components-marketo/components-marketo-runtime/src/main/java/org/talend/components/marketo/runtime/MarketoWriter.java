// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.runtime.client.MarketoClientService;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public abstract class MarketoWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    protected WriteOperation<Result> writeOperation;

    protected RuntimeContainer runtime;

    protected MarketoSink sink;

    protected MarketoClientService client;

    protected MarketoResult result;

    protected Schema inputSchema;

    protected Schema flowSchema;

    protected Schema rejectSchema;

    protected String api;

    protected Boolean use_soap_api = Boolean.FALSE;

    protected IndexedRecord inputRecord;

    protected IndexedRecordConverter<Object, ? extends IndexedRecord> factory = null;

    protected final List<IndexedRecord> successfulWrites = new ArrayList<>();

    protected final List<IndexedRecord> rejectedWrites = new ArrayList<>();

    protected Boolean dieOnError;

    protected int batchSize = 1;

    protected List<IndexedRecord> recordsToProcess = new ArrayList<>();

    protected String API_SOAP = "SOAP";

    protected int retryAttemps = 1;

    protected int retryInterval;

    public MarketoWriter(WriteOperation writeOperation, RuntimeContainer runtime) {
        this.runtime = runtime;
        this.writeOperation = writeOperation;
        this.sink = (MarketoSink) writeOperation.getSink();

        retryAttemps = this.sink.getProperties().getConnectionProperties().maxReconnAttemps.getValue();
        retryInterval = this.sink.getProperties().getConnectionProperties().attemptsIntervalTime.getValue();
    }

    @Override
    public void open(String uId) throws IOException {
        result = new MarketoResult();
        client = sink.getClientService(runtime);
        api = client.getApi();
        use_soap_api = API_SOAP.equals(api);
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

    @Override
    public Result close() throws IOException {
        client = null;
        return result;
    }

    protected abstract void flush();

    @Override
    public List<IndexedRecord> getSuccessfulWrites() {
        return Collections.unmodifiableList(successfulWrites);
    }

    @Override
    public List<IndexedRecord> getRejectedWrites() {
        return Collections.unmodifiableList(rejectedWrites);
    }

    public int getRetryAttemps() {
        return retryAttemps;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    /**
     * Sleeps for retryInterval time
     *
     */
    protected void waitForRetryAttempInterval() {
        try {
            Thread.sleep(getRetryInterval());
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void cleanWrites() {
        successfulWrites.clear();
        rejectedWrites.clear();
    }
}
