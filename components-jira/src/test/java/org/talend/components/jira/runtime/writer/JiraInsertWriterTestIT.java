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

import static org.talend.components.jira.testutils.JiraTestConstants.HOST_PORT;
import static org.talend.components.jira.testutils.JiraTestConstants.INSERT_SCHEMA;
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
 * Integration tests for {@link JiraInsertWriter}
 */
public class JiraInsertWriterTestIT {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Checks {@link JiraInsertWriter#write()} throws {@link IOException} which message contains
     * "Reason: record is invalid" 
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
        thrown.expectMessage("Reason: record is invalid");
        thrown.expectMessage("Record: " + badProject);
        thrown.expectMessage("Error: ");
        thrown.expectMessage("{\"errorMessages\":[\"Unexpected character (\'\\\"\' (code 34)):");

        JiraWriter insertProjectWriter = JiraTestsHelper.createWriter(HOST_PORT, USER, PASS, Resource.PROJECT, Action.INSERT);

        insertProjectWriter.open("ins");
        try {
            insertProjectWriter.write(badJsonRecord);
        } finally {
            insertProjectWriter.close();
        }
    }

    /**
     * Checks {@link JiraInsertWriter#write()} throws {@link IOException} which message contains
     * "Reason: user is not authenticated. Record wasn't created" 
     * in case server responses with 401 Unauthorized status code
     * 
     * @throws IOException
     */
    @Test
    public void testWriteUnauthorized() throws IOException {
        IndexedRecord badJsonRecord = new GenericData.Record(INSERT_SCHEMA);
        String badProject = "{\"name\":\"Updated Integration Test Project\"\"assigneeType\":\"PROJECT_LEAD\"}";
        badJsonRecord.put(0, badProject);
        
        thrown.expect(IOException.class);
        thrown.expectMessage("Reason: user is not authenticated. Record wasn't created");
        thrown.expectMessage("Record: " + badProject);
        thrown.expectMessage("Error: ");
        
        JiraWriter insertProjectWriter = JiraTestsHelper.createWriter(HOST_PORT, WRONG_USER, PASS, Resource.PROJECT,
                Action.INSERT);

        insertProjectWriter.open("ins");
        try {
            insertProjectWriter.write(badJsonRecord);
        } finally {
            insertProjectWriter.close();
        }
    }

    /**
     * Checks {@link JiraInsertWriter#write()} throws {@link IOException} with 
     * "Writer wasn't opened" message if {@link JiraInsertWriter#open()} wasn't called
     * 
     * @throws IOException
     */
    @Test
    public void testWriteNotOpened() throws IOException {
        IndexedRecord badJsonRecord = new GenericData.Record(INSERT_SCHEMA);
        String badProject = "{\"name\":\"Updated Integration Test Project\"\"assigneeType\":\"PROJECT_LEAD\"}";
        badJsonRecord.put(0, badProject);
        
        thrown.expect(IOException.class);
        thrown.expectMessage("Writer wasn't opened");

        JiraWriter insertProjectWriter = JiraTestsHelper.createWriter(HOST_PORT, WRONG_USER, PASS, Resource.PROJECT,
                Action.INSERT);
        insertProjectWriter.write(badJsonRecord);
    }

    /**
     * Checks {@link JiraInsertWriter#write()} throws {@link IOException} which error message contains:
     * "Reason: record is invalid"
     * 
     * @throws IOException
     */
    @Test
    public void testWriteErrorMessage() throws IOException {
        IndexedRecord badIssueTypeRecord = new GenericData.Record(INSERT_SCHEMA);
        String insertIssue1 = "{\"fields\":{\"project\":{\"key\":\"TP\"},\"summary\":\"Integration test issue 1\",\"issuetype\":{\"id\":\"12345\"}}}";
        badIssueTypeRecord.put(0, insertIssue1);
        
        thrown.expect(IOException.class);
        thrown.expectMessage("Reason: record is invalid");
        thrown.expectMessage("Record: " + insertIssue1);
        thrown.expectMessage("Error: ");
        thrown.expectMessage("{\"errorMessages\":[],\"errors\":{\"issuetype\":\"valid issue type is required\"}}");

        JiraWriter insertIssueWriter = JiraTestsHelper.createWriter(HOST_PORT, USER, PASS, Resource.ISSUE, Action.INSERT);

        insertIssueWriter.open("ins");
        try {
            insertIssueWriter.write(badIssueTypeRecord);
        } finally {
            insertIssueWriter.close();
        }
    }
}
