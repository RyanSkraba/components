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

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
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
 * {@link JiraWriter} which updates Jira resources according incoming data
 */
public class JiraUpdateWriter extends JiraWriter {

    private static final Logger LOG = LoggerFactory.getLogger(JiraUpdateWriter.class);

    /**
     * Schema retrieved from incoming data
     */
    private Schema dataSchema;

    /**
     * Position of id field in record schema
     */
    private int idPos;

    /**
     * Position of json field in record schema
     */
    private int jsonPos;

    /**
     * Constructor sets {@link WriteOperation}
     * 
     * @param writeOperation Jira {@link WriteOperation} instance
     */
    public JiraUpdateWriter(JiraWriteOperation writeOperation) {
        super(writeOperation);
    }

    /**
     * Updates Jira resources according incoming data <br>
     * Method should be called only after {@link JiraUpdateWriter#open(String)}
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
            Field idField = dataSchema.getField("id");
            if (idField == null) {
                throw new IOException(MESSAGES.getMessage("error.schemaNotContainId"));
            }
            idPos = idField.pos();
            Field jsonField = dataSchema.getField("json");
            if (jsonField == null) {
                throw new IOException(MESSAGES.getMessage("error.schemaNotContainJson"));
            }
            jsonPos = jsonField.pos();
        }

        String id = (String) record.get(idPos);
        String resourceToUpdate = resource + "/" + id;
        String json = (String) record.get(jsonPos);

        JiraResponse response = getConnection().put(resourceToUpdate, json);
        handleResponse(response, json, record);
    }

    /**
     * Handles response according status code
     * See Jira REST documentation for details
     * 
     * @param response Jira response, which contains status code and body
     * @param resourceToUpdate path of resource to be updated
     * @param record current {@link IndexedRecord}
     * @throws IOException IOException in case of status code is not 201 CREATED or 204 NO CONTENT
     */
    private void handleResponse(JiraResponse response, String resourceToUpdate, IndexedRecord record) throws IOException {
        int statusCode = response.getStatusCode();
        String responseError = response.getBody();
        switch (statusCode) {
        // Jira REST returns different success responses for issue and project
        case SC_CREATED:
        case SC_NO_CONTENT: {
            LOG.debug("Successfully updated {}", resourceToUpdate);
            result.successCount++;
            break;
        }
        case SC_BAD_REQUEST: {
            LOG.debug("{} update failed", resourceToUpdate);
            throw createRejectException("error.invalidRecordUpdate", resourceToUpdate, responseError);
        }
        case SC_UNAUTHORIZED: {
            LOG.debug("User is not authenticated. {} wasn't updated", resourceToUpdate);
            throw createRejectException("error.unauthorizedUpdate", resourceToUpdate, responseError);
        }
        case SC_FORBIDDEN: {
            LOG.debug("User does not have permission to update {}", resourceToUpdate);
            throw createRejectException("error.forbiddenUpdate", resourceToUpdate, responseError);
        }
        case SC_NOT_FOUND: {
            LOG.debug("{} wasn't updated, because it doesn't exist", resourceToUpdate);
            throw createRejectException("error.notFoundUpdate", resourceToUpdate, responseError);
        }
        default: {
            LOG.debug("Unexpected status code");
        }
        }
    }

}
