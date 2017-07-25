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
package org.talend.components.jira.runtime.writer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.jira.connection.Rest;
import org.talend.components.jira.runtime.JiraWriteOperation;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

/**
 * Jira server {@link Writer}
 */
public class JiraWriter implements Writer<Result> {

    private static final Logger LOG = LoggerFactory.getLogger(JiraWriter.class);

    protected static final I18nMessages MESSAGES = GlobalI18N.getI18nMessageProvider().getI18nMessages(JiraWriter.class);

    /**
     * IndexedRecord converter
     */
    private IndexedRecordConverter<Object, ? extends IndexedRecord> factory;

    /**
     * Http connection
     */
    private Rest rest;

    /**
     * {@link WriteOperation} of this {@link Writer}
     */
    private final JiraWriteOperation writeOperation;

    /**
     * Defines whether {@link Writer} was already opened
     */
    protected boolean opened;

    /**
     * Jira REST resource
     */
    protected final String resource;

    /**
     * Return results
     */
    protected Result result;

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
        result = new Result(uId);

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
     * @return {@link Result} with {@link Writer} ID and number of data written
     */
    @Override
    public Result close() {
        if (!opened) {
            LOG.debug("Writer closed without opening");
        }

        // Rest connection doesn't require closing
        rest = null;
        opened = false;
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
    protected IndexedRecordConverter<Object, ? extends IndexedRecord> getFactory(Object datum) {
        if (null == factory) {
            factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) new AvroRegistry()
                    .createIndexedRecordConverter(datum.getClass());
        }
        return factory;
    }

    /**
     * Constructs {@link DataRejectException}, includes error message information and
     * {@link IndexedRecord}
     * Iterates reject records count
     *
     * @param error  error message
     * @param record current {@link IndexedRecord}
     * @throws DataRejectException with specified error and current {@link IndexedRecord}
     */
    protected DataRejectException createRejectException(String error, IndexedRecord record) {
        result.rejectCount++;
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("error", error);
        info.put("talend_record", record);
        return new DataRejectException(info);
    }

    /**
     * Constructs error message, which includes information about error reason, record content
     * and message from Jira server, returns {@link IOException} with this message
     * Iterates reject records count
     *
     * @param reasonKey     reason message i18n key
     * @param json          record json
     * @param serverMessage error message from Jira server
     * @return {@link IOException} which provides error message
     */
    protected IOException createRejectException(String reasonKey, String json, String serverMessage) {
        result.rejectCount++;
        StringBuilder sb = new StringBuilder();
        sb.append(MESSAGES.getMessage("error.reason"));
        sb.append(MESSAGES.getMessage(reasonKey));
        sb.append(System.lineSeparator());
        sb.append(MESSAGES.getMessage("error.record"));
        sb.append(json);
        sb.append(System.lineSeparator());
        sb.append(MESSAGES.getMessage("error.error"));
        sb.append(serverMessage);
        return new IOException(sb.toString());
    }

    /**
     * Validate request body (json content) before submitting of a request.
     *
     * <p>The method checks JSON for {@code null}.
     *
     * @param json json content to be validated
     * @throws IOException if json content is not valid
     */
    protected void validateRequestBody(String json) throws IOException {
        if (StringUtils.isEmpty(json)) {
            throw createRejectException("error.invalidRecordCreate", json, MESSAGES.getMessage("error.jsonEmpty"));
        }
    }
}
