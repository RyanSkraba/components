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

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.jira.connection.JiraResponse;
import org.talend.components.jira.runtime.JiraWriteOperation;

/**
 * {@link JiraWriter} which inserts resources on Jira server formed from incoming data using REST API
 */
public class JiraInsertWriter extends JiraWriter {

    private static final Logger LOG = LoggerFactory.getLogger(JiraInsertWriter.class);

    /**
     * Schema retrieved from incoming data
     */
    private Schema dataSchema;

    /**
     * Position of json field in record schema
     */
    private int jsonPos;

    /**
     * Constructor sets {@link WriteOperation}
     * 
     * @param writeOperation Jira {@link WriteOperation} instance
     */
    public JiraInsertWriter(JiraWriteOperation writeOperation) {
        super(writeOperation);
    }

    /**
     * Inserts resources into Jira server formed from incoming data input <br>
     * Method should be called only after {@link JiraInsertWriter#open(String)}
     * 
     * @param datum input data
     */
    @Override
    public void write(Object datum) throws IOException {
        if (!opened) {
            throw new IOException(MESSAGES.getMessage("error.writerNotOpened"));
        }
        result.totalCount++;
        if (datum == null) {
            return;
        }
        IndexedRecord record = getFactory(datum).convertToAvro(datum);

        if (dataSchema == null) {
            dataSchema = record.getSchema();
            Field jsonField = dataSchema.getField("json");
            if (jsonField == null) {
                throw new IOException(MESSAGES.getMessage("error.schemaNotContainJson"));
            }
            jsonPos = jsonField.pos();
        }
        String json = (String) record.get(jsonPos);
        JiraResponse response = getConnection().post(resource, json);
        handleResponse(response, json, record);
    }

    /**
     * Handles response according status code
     * See Jira REST documentation for details
     * 
     * @param response Jira response, which contains status code and body
     * @param resourceToCreate JSON of resource to be created
     * @param record current {@link IndexedRecord}
     * @throws IOException in case of status code is not 201 CREATED
     */
    private void handleResponse(JiraResponse response, String resourceToCreate, IndexedRecord record) throws IOException {
        int statusCode = response.getStatusCode();
        String responseError = response.getBody();
        switch (statusCode) {
        case SC_CREATED: {
            LOG.debug("Successfully created {}", resourceToCreate);
            result.successCount++;
            break;
        }
        case SC_BAD_REQUEST: {
            LOG.debug("Input is invalid {}", resourceToCreate);
            throw createRejectException("error.invalidRecordCreate", resourceToCreate, responseError);
        }
        case SC_UNAUTHORIZED: {
            LOG.debug("User is not authenticated. {} wasn't inserted", resourceToCreate);
            throw createRejectException("error.unauthorizedCreate", resourceToCreate, responseError);
        }
        case SC_FORBIDDEN: {
            LOG.debug("User does not have permission to create {}", resourceToCreate);
            throw createRejectException("error.forbiddenCreate", resourceToCreate, responseError);
        }
        default: {
            LOG.debug("Unexpected status code");
        }
        }
    }

}
