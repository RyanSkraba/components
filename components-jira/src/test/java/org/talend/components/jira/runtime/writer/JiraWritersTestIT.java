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

import org.apache.avro.generic.IndexedRecord;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.jira.Action;
import org.talend.components.jira.Resource;
import org.talend.components.jira.testutils.ListIndexedRecord;

/**
 * Integration tests for {@link JiraDeleteWriter}, {@link JiraInsertWriter} and {@link JiraUpdateWriter} Covered Issue and Project
 * resources
 */
public class JiraWritersTestIT extends JiraWriterTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(JiraWritersTestIT.class);

    /**
     * Issue IndexedRecords
     */
    private IndexedRecord insertIssueRecord1;

    private IndexedRecord insertIssueRecord2;

    private IndexedRecord updateIssueRecord1;

    private IndexedRecord updateIssueRecord2;

    private IndexedRecord deleteIssueRecord1;

    private IndexedRecord deleteIssueRecord2;

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
        JiraWriter insertProjectWriter = writeOperation.createWriter(null);
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
        changeActionTo(Action.UPDATE);
        JiraWriter updateProjectWriter = writeOperation.createWriter(null);
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
        changeResourceTo(Resource.ISSUE);
        changeActionTo(Action.INSERT);
        JiraWriter insertIssueWriter = writeOperation.createWriter(null);
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
        changeActionTo(Action.UPDATE);
        JiraWriter updateIssueWriter = writeOperation.createWriter(null);
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
        changeActionTo(Action.DELETE);
        JiraWriter deleteIssueWriter = writeOperation.createWriter(null);
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
        changeActionTo(Action.DELETE);
        changeResourceTo(Resource.PROJECT);
        JiraWriter deleteProjectWriter = writeOperation.createWriter(null);
        deleteProjectWriter.open("delProj");
        try {
            deleteProjectWriter.write(deleteProjectRecord);
        } catch (DataRejectException e) {
            String rejectError = e.getRejectInfo().get("error").toString();
            LOG.error(rejectError);
            collector.addError(new Throwable(rejectError));
        }
    }

    @Override
    protected void setupIndexedRecords() {
        super.setupIndexedRecords();

        insertIssueRecord1 = new ListIndexedRecord(insertSchema);
        String insertIssue1 = "{\"fields\":{\"project\":{\"key\":\"ITP\"},\"summary\":\"Integration test issue 1\",\"issuetype\":{\"id\":\"10000\"}}}";
        insertIssueRecord1.put(0, insertIssue1);

        insertIssueRecord2 = new ListIndexedRecord(insertSchema);
        String insertIssue2 = "{\"fields\":{\"project\":{\"key\":\"ITP\"},\"summary\":\"Integration test issue 2\",\"issuetype\":{\"id\":\"10000\"}}}";
        insertIssueRecord2.put(0, insertIssue2);

        updateIssueRecord1 = new ListIndexedRecord(updateSchema);
        String updateIssue1 = "{\"fields\":{\"summary\":\"Updated test issue 1\"}}";
        updateIssueRecord1.put(0, "ITP-1");
        updateIssueRecord1.put(1, updateIssue1);

        updateIssueRecord2 = new ListIndexedRecord(insertSchema);
        String updateIssue2 = "{\"fields\":{\"summary\":\"Updated test issue 2\"}}";
        updateIssueRecord2.put(0, "ITP-2");
        updateIssueRecord2.put(1, updateIssue2);

        deleteIssueRecord1 = new ListIndexedRecord(deleteSchema);
        deleteIssueRecord1.put(0, "ITP-1");

        deleteIssueRecord2 = new ListIndexedRecord(deleteSchema);
        deleteIssueRecord2.put(0, "ITP-2");
    }

}
