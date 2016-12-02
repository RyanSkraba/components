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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.talend.components.jira.testutils.JiraTestConstants.DELETE_SCHEMA;
import static org.talend.components.jira.testutils.JiraTestConstants.HOST_PORT;
import static org.talend.components.jira.testutils.JiraTestConstants.PASS;
import static org.talend.components.jira.testutils.JiraTestConstants.USER;
import static org.talend.components.jira.testutils.JiraTestConstants.WRONG_USER;

import java.io.IOException;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.jira.Action;
import org.talend.components.jira.Resource;
import org.talend.components.jira.testutils.JiraTestsHelper;

/**
 * Integration tests for {@link JiraDeleteWriter}
 */
public class JiraDeleteWriterTestIT {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Checks {@link JiraDeleteWriter#write()} throws {@link IOException} which message contains
     * "User is not authenticated. Record wasn't deleted"
     * in case server responses with 401 Unauthorized status code
     * 
     * @throws IOException
     */
    @Test
    public void testWriteUnauthorized() throws IOException {
        String expectedError = "User is not authenticated. Record wasn't deleted";

        IndexedRecord badJsonRecord = new GenericData.Record(DELETE_SCHEMA);
        badJsonRecord.put(0, "TP");

        thrown.expect(IOException.class);
        thrown.expectMessage("Reason: user is not authenticated. Record wasn't deleted");
        thrown.expectMessage("Record: TP");
        thrown.expectMessage("Error: ");

        JiraWriter deleteProjectWriter = JiraTestsHelper.createWriter(HOST_PORT, WRONG_USER, PASS, Resource.PROJECT,
                Action.DELETE);
        deleteProjectWriter.open("del");
        try {
            deleteProjectWriter.write(badJsonRecord);
            fail();
        } catch (DataRejectException e) {
            String rejectError = e.getRejectInfo().get("error").toString();
            assertEquals(expectedError, rejectError);
        } finally {
            deleteProjectWriter.close();
        }
    }

    /**
     * Checks {@link JiraDeleteWriter#write()} throws {@link IOException} which message contains
     * "Record wasn't deleted, because it doesn't exist"
     * in case server responses with 404 Not Found status code
     * 
     * @throws IOException
     */
    @Test
    public void testWriteNotFound() throws IOException {
        IndexedRecord wrongProjectRecord = new GenericData.Record(DELETE_SCHEMA);
        wrongProjectRecord.put(0, "WP");

        thrown.expect(IOException.class);
        thrown.expectMessage("Reason: record wasn't deleted, because it doesnt exist");
        thrown.expectMessage("Record: WP");
        thrown.expectMessage("Error: ");
        thrown.expectMessage("{\"errorMessages\":[\"No project could be found with key \'WP\'.\"],\"errors\":{}}");

        JiraWriter deleteProjectWriter = JiraTestsHelper.createWriter(HOST_PORT, USER, PASS, Resource.PROJECT, Action.DELETE);

        deleteProjectWriter.open("del");
        try {
            deleteProjectWriter.write(wrongProjectRecord);
        } finally {
            deleteProjectWriter.close();
        }
    }

    /**
     * Checks {@link JiraDeleteWriter#write()} throws {@link IOException} with message:
     * "Writer wasn't opened" if {@link JiraDeleteWriter#open()} wasn't called
     * 
     * @throws IOException
     */
    @Test
    public void testWriteNotOpened() throws IOException {
        IndexedRecord wrongProjectRecord = new GenericData.Record(DELETE_SCHEMA);
        wrongProjectRecord.put(0, "WP");

        thrown.expect(IOException.class);
        thrown.expectMessage("Writer wasn't opened");

        JiraWriter deleteProjectWriter = JiraTestsHelper.createWriter(HOST_PORT, USER, PASS, Resource.PROJECT, Action.DELETE);

        deleteProjectWriter.write(wrongProjectRecord);
    }
}
