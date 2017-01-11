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

import static org.talend.components.jira.testutils.JiraTestConstants.DELETE_SCHEMA;
import static org.talend.components.jira.testutils.JiraTestConstants.HOST_PORT;
import static org.talend.components.jira.testutils.JiraTestConstants.INSERT_SCHEMA;
import static org.talend.components.jira.testutils.JiraTestConstants.PASS;
import static org.talend.components.jira.testutils.JiraTestConstants.UPDATE_SCHEMA;
import static org.talend.components.jira.testutils.JiraTestConstants.USER;

import java.io.IOException;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.jira.Action;
import org.talend.components.jira.Resource;
import org.talend.components.jira.testutils.JiraTestsHelper;

/**
 * Integration tests for {@link JiraDeleteWriter}, {@link JiraInsertWriter} and {@link JiraUpdateWriter} Covered Issue and Project
 * resources
 */
public class JiraWritersTestIT {

    private static final Logger LOG = LoggerFactory.getLogger(JiraWritersTestIT.class);

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    /**
     * Checks following scenario: <br>
     * 1. Create project "Test Project"
     * 2. Update project "Test Project"
     * 3. Create 2 issues in "Test Project"
     * 4. Update 2 issues in "Test Project"
     * 5. Delete 2 issues from "Test Project"
     * 6. Delete project "Test Project"
     * 
     * @throws IOException
     */
    @Test
    public void testWrite() throws IOException {
        // 1. Create project "Test Project"
        testInsertProject();

        // 2. Update project "Test Project"
        testUpdateProject();

        // 3. Create 2 issues in "Test Project"
        testInsertIssues();

        // 4. Update 2 issues in "Test Project"
        testUpdateIssues();

        // 5. Delete 2 issues from "Test Project"
        testDeleteIssues();

        // 6. Delete project "Test Project"
        testDeleteProject();
    }

    /**
     * Checks {@link JiraInsertWriter#write(Object)} creates project on Jira server
     * 
     * @throws IOException
     */
    public void testInsertProject() throws IOException {
        IndexedRecord insertProjectRecord = new GenericData.Record(INSERT_SCHEMA);
        String insertProject = "{\"key\":\"ITP\",\"name\":\"Integration Test Project\",\"projectTemplateKey\":"
                + "\"com.atlassian.jira-core-project-templates:jira-core-project-management\",\"lead\":\"UserID\"}";
        insertProject = insertProject.replace("UserID", USER);
        insertProjectRecord.put(0, insertProject);
        
        JiraWriter insertProjectWriter = JiraTestsHelper.createWriter(HOST_PORT, USER, PASS, Resource.PROJECT, Action.INSERT);
        
        insertProjectWriter.open("insProj");
        try {
            insertProjectWriter.write(insertProjectRecord);
            insertProjectWriter.close();
        } catch (DataRejectException e) {
            String rejectError = e.getRejectInfo().get("error").toString();
            LOG.error(rejectError);
            collector.addError(new Throwable(rejectError));
        }
    }

    /**
     * Checks {@link JiraUpdateWriter#write(Object)} updates project on Jira server
     * 
     * @throws IOException
     */
    public void testUpdateProject() throws IOException {
        IndexedRecord updateProjectRecord = new GenericData.Record(UPDATE_SCHEMA);
        String updateProject = "{\"name\":\"Updated Integration Test Project\",\"assigneeType\":\"PROJECT_LEAD\"}";
        updateProjectRecord.put(0, "ITP");
        updateProjectRecord.put(1, updateProject);
        
        JiraWriter updateProjectWriter = JiraTestsHelper.createWriter(HOST_PORT, USER, PASS, Resource.PROJECT, Action.UPDATE);
        
        updateProjectWriter.open("updProj");
        try {
            updateProjectWriter.write(updateProjectRecord);
            updateProjectWriter.close();
        } catch (DataRejectException e) {
            String rejectError = e.getRejectInfo().get("error").toString();
            LOG.error(rejectError);
            collector.addError(new Throwable(rejectError));
        }
    }

    /**
     * Checks {@link JiraInsertWriter#write(Object)} inserts issues on Jira server
     * 
     * @throws IOException
     */
    public void testInsertIssues() throws IOException {
        IndexedRecord insertIssueRecord1 = new GenericData.Record(INSERT_SCHEMA);
        String insertIssue1 = "{\"fields\":{\"project\":{\"key\":\"ITP\"},\"summary\":\"Integration test issue 1\",\"issuetype\":{\"id\":\"10000\"}}}";
        insertIssueRecord1.put(0, insertIssue1);

        IndexedRecord insertIssueRecord2 = new GenericData.Record(INSERT_SCHEMA);
        String insertIssue2 = "{\"fields\":{\"project\":{\"key\":\"ITP\"},\"summary\":\"Integration test issue 2\",\"issuetype\":{\"id\":\"10000\"}}}";
        insertIssueRecord2.put(0, insertIssue2);
        
        JiraWriter insertIssueWriter = JiraTestsHelper.createWriter(HOST_PORT, USER, PASS, Resource.ISSUE, Action.INSERT);
        
        insertIssueWriter.open("insIss");
        try {
            insertIssueWriter.write(insertIssueRecord1);
            insertIssueWriter.write(insertIssueRecord2);
            insertIssueWriter.close();

        } catch (DataRejectException e) {
            String rejectError = e.getRejectInfo().get("error").toString();
            LOG.error(rejectError);
            collector.addError(new Throwable(rejectError));
        }
    }

    /**
     * Checks {@link JiraUpdateWriter#write(Object)} updates issues on Jira server
     * 
     * @throws IOException
     */
    public void testUpdateIssues() throws IOException {
        IndexedRecord updateIssueRecord1 = new GenericData.Record(UPDATE_SCHEMA);
        String updateIssue1 = "{\"fields\":{\"summary\":\"Updated test issue 1\"}}";
        updateIssueRecord1.put(0, "ITP-1");
        updateIssueRecord1.put(1, updateIssue1);

        IndexedRecord updateIssueRecord2 = new GenericData.Record(UPDATE_SCHEMA);
        String updateIssue2 = "{\"fields\":{\"summary\":\"Updated test issue 2\"}}";
        updateIssueRecord2.put(0, "ITP-2");
        updateIssueRecord2.put(1, updateIssue2);
        
        JiraWriter updateIssueWriter = JiraTestsHelper.createWriter(HOST_PORT, USER, PASS, Resource.ISSUE, Action.UPDATE);
        
        updateIssueWriter.open("updIss");
        try {
            updateIssueWriter.write(updateIssueRecord1);
            updateIssueWriter.write(updateIssueRecord2);
            updateIssueWriter.close();
        } catch (DataRejectException e) {
            String rejectError = e.getRejectInfo().get("error").toString();
            LOG.error(rejectError);
            collector.addError(new Throwable(rejectError));
        }
    }

    /**
     * Checks {@link JiraDeleteWriter#write(Object)} deletes issues from Jira server
     * 
     * @throws IOException
     */
    public void testDeleteIssues() throws IOException {
        IndexedRecord deleteIssueRecord1 = new GenericData.Record(DELETE_SCHEMA);
        deleteIssueRecord1.put(0, "ITP-1");

        IndexedRecord deleteIssueRecord2 = new GenericData.Record(DELETE_SCHEMA);
        deleteIssueRecord2.put(0, "ITP-2");
        
        JiraWriter deleteIssueWriter = JiraTestsHelper.createWriter(HOST_PORT, USER, PASS, Resource.ISSUE, Action.DELETE);
        
        deleteIssueWriter.open("delIss");
        try {
            deleteIssueWriter.write(deleteIssueRecord1);
            deleteIssueWriter.write(deleteIssueRecord2);
        } catch (DataRejectException e) {
            String rejectError = e.getRejectInfo().get("error").toString();
            LOG.error(rejectError);
            collector.addError(new Throwable(rejectError));
        }
    }

    /**
     * Checks {@link JiraDeleteWriter#write(Object)} deletes project from Jira server
     * 
     * @throws IOException
     */
    public void testDeleteProject() throws IOException {
        IndexedRecord deleteProjectRecord = new GenericData.Record(DELETE_SCHEMA);
        deleteProjectRecord.put(0, "ITP");
        
        JiraWriter deleteProjectWriter = JiraTestsHelper.createWriter(HOST_PORT, USER, PASS, Resource.PROJECT, Action.DELETE);
        
        deleteProjectWriter.open("delProj");
        try {
            deleteProjectWriter.write(deleteProjectRecord);
        } catch (DataRejectException e) {
            String rejectError = e.getRejectInfo().get("error").toString();
            LOG.error(rejectError);
            collector.addError(new Throwable(rejectError));
        }
    }

}
