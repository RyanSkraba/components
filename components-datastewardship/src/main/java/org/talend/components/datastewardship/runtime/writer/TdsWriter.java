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
package org.talend.components.datastewardship.runtime.writer;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.datastewardship.connection.TdsConnection;
import org.talend.components.datastewardship.runtime.TdsWriteOperation;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 * TDS {@link Writer}
 */
public abstract class TdsWriter implements Writer<Result> {

    private static final Logger LOG = LoggerFactory.getLogger(TdsWriter.class);

    /**
     * IndexedRecord converter
     */
    protected IndexedRecordConverter<Object, ? extends IndexedRecord> factory;

    /**
     * Http connection
     */
    protected TdsConnection tdsConn;

    /**
     * {@link WriteOperation} of this {@link Writer}
     */
    protected final TdsWriteOperation writeOperation;

    /**
     * Defines whether {@link Writer} was already opened
     */
    protected boolean opened;

    /**
     * Return results
     */
    protected Result result;

    /**
     * Constructor sets {@link WriteOperation}
     * 
     * @param writeOperation TDS {@link WriteOperation} instance
     */
    public TdsWriter(TdsWriteOperation writeOperation) {
        this.writeOperation = writeOperation;
    }

    /**
     * Initializes connection of this {@link Writer}
     * 
     * @param uId Unique ID of this {@link Writer}
     */
    @Override
    public void open(String uId) {
        if (opened) {
            LOG.debug("Writer is already opened"); //$NON-NLS-1$
            return;
        }
        result = new Result(uId);

        String url = writeOperation.getSink().getUrl();
        String username = writeOperation.getSink().getUsername();
        String password = writeOperation.getSink().getPassword();

        tdsConn = new TdsConnection(url);
        tdsConn.setCredentials(username, password);
        opened = true;
    }

    /**
     * Closes connection and resets instance to initial state Successors should also reset data counter
     * 
     * @return {@link Result} with {@link Writer} ID and number of data written
     * @throws IOException
     */
    @Override
    public Result close() throws IOException {
        if (!opened) {
            LOG.debug("Writer closed without opening");
        }

        // Rest connection doesn't require closing
        tdsConn = null;
        opened = false;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TdsWriteOperation getWriteOperation() {
        return writeOperation;
    }

    /**
     * Returns connection of this {@link Writer}
     * 
     * @return {@link TdsConnection} connection instance
     */
    protected TdsConnection getConnection() {
        return tdsConn;
    }

    /**
     * Returns IndexedRecord converter
     * 
     * @param datum data object
     * @return IndexedRecord converter
     */
    @SuppressWarnings("unchecked")
    protected IndexedRecordConverter<Object, ? extends IndexedRecord> getFactory(Object datum) {
        if (null == factory) {
            factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) new AvroRegistry()
                    .createIndexedRecordConverter(datum.getClass());
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
    protected void handleReject(String error, String json) {
        result.rejectCount++;
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("error", error);
        info.put("talend_record", json);
        throw new DataRejectException(info);
    }

    /**
     * Handles response according status code See Jira REST documentation for details
     * 
     * @param statusCode HTTP response status code
     * @param resourceToCreate JSON of resource to be created
     * @param record current {@link IndexedRecord}
     */
    protected void handleResponse(int statusCode, String resourceToCreate, String json) {
        switch (statusCode) {
            case SC_OK: {
                LOG.debug("Successfully processed {}", resourceToCreate);
                result.successCount++;
                break;
            }
            case SC_CREATED: {
                LOG.debug("Successfully created {}", resourceToCreate);
                result.successCount++;
                break;
            }
            case SC_BAD_REQUEST: {
                LOG.debug("Input is invalid {}", resourceToCreate);
                handleReject("Record is invalid", json);
            }
            case SC_UNAUTHORIZED: {
                LOG.debug("User is not authenticated. {} wasn't inserted", resourceToCreate);
                handleReject("User is not authenticated. Record wasn't inserted", json);
            }
            case SC_FORBIDDEN: {
                LOG.debug("User does not have permission to create {}", resourceToCreate);
                handleReject("User does not have permission to create record", json);
            }
            default: {
                LOG.debug("Unexpected status code");
            }
        }
    }
}
