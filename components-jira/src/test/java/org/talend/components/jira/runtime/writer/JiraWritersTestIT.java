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

import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.jira.Action;
import org.talend.components.jira.Resource;
import org.talend.components.jira.runtime.JiraSink;
import org.talend.components.jira.runtime.JiraWriteOperation;
import org.talend.components.jira.testutils.ListIndexedRecord;
import org.talend.components.jira.testutils.Utils;
import org.talend.components.jira.tjiraoutput.TJiraOutputProperties;
import org.talend.daikon.avro.AvroRegistry;

/**
 * Integration tests for {@link JiraDeleteWriter}, {@link JiraInsertWriter} and {@link JiraUpdateWriter} Covered Issue and Project
 * resources
 */
public class JiraWritersTestIT {

    private static final Logger LOG = LoggerFactory.getLogger(JiraWritersTestIT.class);

    /**
     * Constants, which describes values to connect real Jira server
     */
    private static final String HOST = "http://192.168.99.100:8080/";

    private static final String USER = "root";

    private static final String PASS = "123456";

    /**
     * Instance used in this test
     */
    private TJiraOutputProperties properties;

    private JiraSink sink;

    private JiraWriteOperation writeOperation;

    /**
     * Schemas
     */
    private Schema deleteSchema;

    private Schema insertSchema;

    private Schema updateSchema;

    /**
     * Project IndexedRecords
     */
    private IndexedRecord insertProjectRecord;

    private IndexedRecord updateProjectRecord;

    private IndexedRecord deleteProjectRecord;

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

    @Before
    public void setup() {
        setupProperties();
        setupSink();
        setupWriteOperation();
        setupSchemas();
        setupIndexedRecords();
    }

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
    @Ignore
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

    private void setupProperties() {
        properties = new TJiraOutputProperties("root");
        properties.init();
        properties.connection.hostUrl.setValue(HOST);
        properties.connection.basicAuthentication.userId.setValue(USER);
        properties.connection.basicAuthentication.password.setValue(PASS);
        properties.resource.setValue(Resource.PROJECT);
        properties.action.setValue(Action.INSERT);
    }

    private void setupSink() {
        sink = new JiraSink();
        sink.initialize(null, properties);
    }

    private void setupWriteOperation() {
        writeOperation = (JiraWriteOperation) sink.createWriteOperation();
        writeOperation.initialize(null);
    }

    private void setupSchemas() {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();

        Schema.Field deleteIdField = new Schema.Field("id", stringSchema, null, null, Order.ASCENDING);
        deleteSchema = Schema.createRecord("jira", null, null, false, Collections.singletonList(deleteIdField));
        deleteSchema.addProp(TALEND_IS_LOCKED, "true");

        Schema.Field insertJsonField = new Schema.Field("json", stringSchema, null, null, Order.ASCENDING);
        insertSchema = Schema.createRecord("jira", null, null, false, Collections.singletonList(insertJsonField));
        insertSchema.addProp(TALEND_IS_LOCKED, "true");

        Schema.Field updateIdField = new Schema.Field("id", stringSchema, null, null, Order.ASCENDING);
        Schema.Field updateJsonField = new Schema.Field("json", stringSchema, null, null, Order.ASCENDING);
        List<Schema.Field> fields = Arrays.asList(updateIdField, updateJsonField);
        updateSchema = Schema.createRecord("jira", null, null, false, fields);
        updateSchema.addProp(TALEND_IS_LOCKED, "true");
    }

    private void setupIndexedRecords() {
        insertProjectRecord = new ListIndexedRecord(insertSchema);
        String insertProject = Utils.readFile("src/test/resources/org/talend/components/jira/runtime/writer/insertProject.json");
        insertProject = insertProject.replace("UserID", USER);
        insertProjectRecord.put(0, insertProject);

        deleteProjectRecord = new ListIndexedRecord(deleteSchema);
        deleteProjectRecord.put(0, "ITP");

        updateProjectRecord = new ListIndexedRecord(updateSchema);
        String updateProject = Utils.readFile("src/test/resources/org/talend/components/jira/runtime/writer/updateProject.json");
        updateProjectRecord.put(0, "ITP");
        updateProjectRecord.put(1, updateProject);

        insertIssueRecord1 = new ListIndexedRecord(insertSchema);
        String insertIssue1 = Utils.readFile("src/test/resources/org/talend/components/jira/runtime/writer/insertIssue1.json");
        insertIssueRecord1.put(0, insertIssue1);

        insertIssueRecord2 = new ListIndexedRecord(insertSchema);
        String insertIssue2 = Utils.readFile("src/test/resources/org/talend/components/jira/runtime/writer/insertIssue2.json");
        insertIssueRecord2.put(0, insertIssue2);

        updateIssueRecord1 = new ListIndexedRecord(updateSchema);
        String updateIssue1 = Utils.readFile("src/test/resources/org/talend/components/jira/runtime/writer/updateIssue1.json");
        updateIssueRecord1.put(0, "ITP-1");
        updateIssueRecord1.put(1, updateIssue1);

        updateIssueRecord2 = new ListIndexedRecord(insertSchema);
        String updateIssue2 = Utils.readFile("src/test/resources/org/talend/components/jira/runtime/writer/updateIssue2.json");
        updateIssueRecord2.put(0, "ITP-2");
        updateIssueRecord2.put(1, updateIssue2);

        deleteIssueRecord1 = new ListIndexedRecord(deleteSchema);
        deleteIssueRecord1.put(0, "ITP-1");

        deleteIssueRecord2 = new ListIndexedRecord(deleteSchema);
        deleteIssueRecord2.put(0, "ITP-2");
    }

    private void changeResourceTo(Resource resource) {
        properties.resource.setValue(resource);
        setupSink();
        setupWriteOperation();
    }

    private void changeActionTo(Action action) {
        properties.action.setValue(action);
        setupSink();
        setupWriteOperation();
    }
}
