// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jira.runtime.writer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.jira.connection.Rest;
import org.talend.components.jira.runtime.JiraWriteOperation;
import org.talend.components.jira.runtime.result.DataCountResult;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

/**
 * Jira server {@link Writer}
 */
public class JiraWriter implements Writer<DataCountResult> {

    private static final Logger LOG = LoggerFactory.getLogger(JiraWriter.class);
    
    /**
     * IndexedRecord converter
     */
    private IndexedRecordAdapterFactory<Object, ? extends IndexedRecord> factory;

    /**
     * Http connection
     */
    private Rest rest;

    /**
     * {@link WriteOperation} of this {@link Writer}
     */
    private final JiraWriteOperation writeOperation;

    /**
     * Unique ID of this {@link Writer} to distinguish it from other Writers
     */
    private String uId;

    /**
     * Defines whether {@link Writer} was already opened
     */
    protected boolean opened;
    
    /**
     * Number of data passed to {@link Writer#write(Object)}
     */
    protected int dataCount = 0;
    
    /**
     * Number of data written
     */
    protected int successCount = 0;
    
    /**
     * Number of data rejected
     */
    protected int rejectCount = 0;
    
    /**
     * Jira REST resource
     */
    protected final String resource;

    /**
     * Constructor sets {@link WriteOperation}
     * 
     * @param writeOperation Jira {@link WriteOperation} instance
     */
    public JiraWriter(JiraWriteOperation writeOperation) {
        this.writeOperation = writeOperation;
        this.resource = writeOperation.getSink().getResource();
    }

    /**
     * Initializes connection of this {@link Writer}
     * 
     * @param uId Unique ID of this {@link Writer}
     */
    @Override
    public void open(String uId) {
        if (opened) {
            LOG.debug("Writer is already opened");
            return;
        }
        this.uId = uId;

        String hostPort = writeOperation.getSink().getHostPort();
        String userId = writeOperation.getSink().getUserId();
        String userPassword = writeOperation.getSink().getUserPassword();

        rest = new Rest(hostPort);
        rest.setCredentials(userId, userPassword);
        opened = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(Object datum) throws IOException {
        // Nothing to be done. Should be overridden in successors
    }

    /**
     * Closes connection and resets instance to initial state Successors should also reset data counter
     * 
     * @return {@link WriterResult} with {@link Writer} ID and number of data written
     */
    @Override
    public DataCountResult close() {
        if (!opened) {
            LOG.debug("Writer closed without opening");
        }

        // Rest connection doesn't require closing
        rest = null;
        opened = false;

        DataCountResult result = new DataCountResult(uId, dataCount, successCount, rejectCount);
        dataCount = 0;
        successCount = 0;
        rejectCount = 0;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JiraWriteOperation getWriteOperation() {
        return writeOperation;
    }

    /**
     * Returns connection of this {@link Writer}
     * 
     * @return {@link Rest} connection instance
     */
    protected Rest getConnection() {
        return rest;
    }
    
    /**
     * Returns IndexedRecord converter
     * 
     * @param datum data object
     * @return IndexedRecord converter
     */
    protected IndexedRecordAdapterFactory<Object, ? extends IndexedRecord> getFactory(Object datum) {
        if (null == factory) {
            factory = (IndexedRecordAdapterFactory<Object, ? extends IndexedRecord>) new AvroRegistry()
                    .createAdapterFactory(datum.getClass());
        }
        return factory;
    }
    
    
    /**
     * Throws {@link DataRejectException} with specified error message
     * 
     * @param error error message
     * @param record current {@link IndexedRecord}
     * @throws DataRejectException with specified error and current {@link IndexedRecord}
     */
    protected void handleReject(String error, IndexedRecord record) {
        rejectCount++;
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("error", error);
        info.put("talend_record", record);
        throw new DataRejectException(info);
    }
}
