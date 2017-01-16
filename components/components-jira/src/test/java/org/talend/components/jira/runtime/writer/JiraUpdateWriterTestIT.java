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

import static org.talend.components.jira.testutils.JiraTestConstants.HOST_PORT;
import static org.talend.components.jira.testutils.JiraTestConstants.PASS;
import static org.talend.components.jira.testutils.JiraTestConstants.UPDATE_SCHEMA;
import static org.talend.components.jira.testutils.JiraTestConstants.USER;
import static org.talend.components.jira.testutils.JiraTestConstants.WRONG_USER;

import java.io.IOException;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.components.jira.Action;
import org.talend.components.jira.Resource;
import org.talend.components.jira.testutils.JiraTestsHelper;

/**
 * Integration tests for {@link JiraUpdateWriter}
 */
public class JiraUpdateWriterTestIT {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Checks {@link JiraUpdateWriter#write()} throws {@link IOException} which message contains
     * "Reason: record update failed"
     * in case server responses with 400 Bad Request status code
     * 
     * @throws IOException
     */
    @Test
    public void testWriteBadRequest() throws IOException {
        IndexedRecord badJsonRecord = new GenericData.Record(UPDATE_SCHEMA);
        String badProject = "{\"name\":\"Updated Integration Test Project\"\"assigneeType\":\"PROJECT_LEAD\"}";
        badJsonRecord.put(0, "TP");
        badJsonRecord.put(1, badProject);

        thrown.expect(IOException.class);
        thrown.expectMessage("Reason: record update failed");
        thrown.expectMessage("Record: " + badProject);
        thrown.expectMessage("Error: ");
        thrown.expectMessage("{\"errorMessages\":[\"Unexpected character (\'\\\"\' (code 34)):");

        JiraWriter updateProjectWriter = JiraTestsHelper.createWriter(HOST_PORT, USER, PASS, Resource.PROJECT, Action.UPDATE);

        updateProjectWriter.open("upd");
        try {
            updateProjectWriter.write(badJsonRecord);
        } finally {
            updateProjectWriter.close();
        }
    }

    /**
     * Checks {@link JiraUpdateWriter#write()} throws {@link IOException} which message contains
     * "Reason: user is not authenticated. Record wasn't updated"
     * in case server responses with 401 Unauthorized status code
     * 
     * @throws IOException
     */
    @Test
    public void testWriteUnauthorized() throws IOException {
        IndexedRecord badJsonRecord = new GenericData.Record(UPDATE_SCHEMA);
        String badProject = "{\"name\":\"Updated Integration Test Project\"\"assigneeType\":\"PROJECT_LEAD\"}";
        badJsonRecord.put(0, "TP");
        badJsonRecord.put(1, badProject);

        JiraWriter updateProjectWriter = JiraTestsHelper.createWriter(HOST_PORT, WRONG_USER, PASS, Resource.PROJECT,
                Action.UPDATE);

        thrown.expect(IOException.class);
        thrown.expectMessage("Reason: user is not authenticated. Record wasn't updated");
        thrown.expectMessage("Record: " + badProject);
        thrown.expectMessage("Error: ");

        updateProjectWriter.open("upd");
        try {
            updateProjectWriter.write(badJsonRecord);
        } finally {
            updateProjectWriter.close();
        }
    }

    /**
     * Checks {@link JiraUpdateWriter#write()} throws {@link IOException} which message contains
     * "Reason: record wasn't updated, because it doesn't exist"
     * in case server responses with 404 Not Found status code
     * 
     * @throws IOException
     */
    @Test
    public void testWriteNotFound() throws IOException {
        IndexedRecord wrongProjectRecord = new GenericData.Record(UPDATE_SCHEMA);
        String wrongProject = "{\"name\":\"Updated Integration Test Project\",\"assigneeType\":\"PROJECT_LEAD\"}";
        wrongProjectRecord.put(0, "WP");
        wrongProjectRecord.put(1, wrongProject);

        thrown.expect(IOException.class);
        thrown.expectMessage("Reason: record wasn't updated, because it doesn't exist");
        thrown.expectMessage("Record: " + wrongProject);
        thrown.expectMessage("Error: ");
        thrown.expectMessage("{\"errorMessages\":[\"No project could be found with key \'WP\'.\"],\"errors\":{}}");

        JiraWriter updateProjectWriter = JiraTestsHelper.createWriter(HOST_PORT, USER, PASS, Resource.PROJECT, Action.UPDATE);

        updateProjectWriter.open("upd");
        try {
            updateProjectWriter.write(wrongProjectRecord);
        } finally {
            updateProjectWriter.close();
        }
    }

    /**
     * Checks {@link JiraUpdateWriter#write()} {@link IOException} which message is
     * 
     * if {@link JiraUpdateWriter#open()} wasn't called
     * 
     * @throws IOException
     */
    @Test
    public void testWriteNotOpened() throws IOException {
        IndexedRecord wrongProjectRecord = new GenericData.Record(UPDATE_SCHEMA);
        String wrongProject = "{\"name\":\"Updated Integration Test Project\",\"assigneeType\":\"PROJECT_LEAD\"}";
        wrongProjectRecord.put(0, "WP");
        wrongProjectRecord.put(1, wrongProject);

        thrown.expect(IOException.class);
        thrown.expectMessage("Writer wasn't opened");

        JiraWriter updateProjectWriter = JiraTestsHelper.createWriter(HOST_PORT, WRONG_USER, PASS, Resource.PROJECT,
                Action.UPDATE);

        updateProjectWriter.write(wrongProjectRecord);
    }

}
