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
import static org.talend.components.jira.testutils.JiraTestConstants.PASS;
import static org.talend.components.jira.testutils.JiraTestConstants.USER;
import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.talend.components.jira.Action;
import org.talend.components.jira.Resource;
import org.talend.components.jira.runtime.JiraSink;
import org.talend.components.jira.runtime.JiraWriteOperation;
import org.talend.components.jira.testutils.ListIndexedRecord;
import org.talend.components.jira.tjiraoutput.TJiraOutputProperties;
import org.talend.daikon.avro.AvroRegistry;

/**
 * Base class for all classes which test {@link JiraWriter} successors
 */
public class JiraWriterTestBase {

    /**
     * Instance used in tests
     */
    protected TJiraOutputProperties properties;

    protected JiraSink sink;

    protected JiraWriteOperation writeOperation;

    /**
     * Schemas
     */
    protected Schema deleteSchema;

    protected Schema insertSchema;

    protected Schema updateSchema;

    /**
     * Project IndexedRecords
     */
    protected IndexedRecord insertProjectRecord;

    protected IndexedRecord updateProjectRecord;

    protected IndexedRecord deleteProjectRecord;

    protected IndexedRecord badJsonRecord;

    protected IndexedRecord wrongProjectRecord;

    @Before
    public void setup() {
        setupProperties();
        setupSink();
        setupWriteOperation();
        setupSchemas();
        setupIndexedRecords();
    }

    protected void setupProperties() {
        properties = new TJiraOutputProperties("root");
        properties.init();
        properties.connection.hostUrl.setValue(HOST_PORT);
        properties.connection.basicAuthentication.userId.setValue(USER);
        properties.connection.basicAuthentication.password.setValue(PASS);
        properties.resource.setValue(Resource.PROJECT);
        properties.action.setValue(Action.INSERT);
    }

    protected void setupSink() {
        sink = new JiraSink();
        sink.initialize(null, properties);
    }

    protected void setupWriteOperation() {
        writeOperation = (JiraWriteOperation) sink.createWriteOperation();
        writeOperation.initialize(null);
    }

    protected void setupSchemas() {
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

    protected void setupIndexedRecords() {
        insertProjectRecord = new ListIndexedRecord(insertSchema);
        String insertProject = "{\"key\":\"ITP\",\"name\":\"Integration Test Project\",\"projectTemplateKey\":"
        		+ "\"com.atlassian.jira-core-project-templates:jira-core-project-management\",\"lead\":\"UserID\"}";
        insertProject = insertProject.replace("UserID", USER);
        insertProjectRecord.put(0, insertProject);

        deleteProjectRecord = new ListIndexedRecord(deleteSchema);
        deleteProjectRecord.put(0, "ITP");

        updateProjectRecord = new ListIndexedRecord(updateSchema);
        String updateProject = "{\"name\":\"Updated Integration Test Project\",\"assigneeType\":\"PROJECT_LEAD\"}";
        updateProjectRecord.put(0, "ITP");
        updateProjectRecord.put(1, updateProject);

        badJsonRecord = new ListIndexedRecord(updateSchema);
        String badProject = "{\"name\":\"Updated Integration Test Project\"\"assigneeType\":\"PROJECT_LEAD\"}";
        badJsonRecord.put(0, "TP");
        badJsonRecord.put(1, badProject);

        wrongProjectRecord = new ListIndexedRecord(updateSchema);
        String wrongProject = "{\"name\":\"Updated Integration Test Project\",\"assigneeType\":\"PROJECT_LEAD\"}";
        wrongProjectRecord.put(0, "WP");
        wrongProjectRecord.put(1, wrongProject);
    }

    protected void changeResourceTo(Resource resource) {
        properties.resource.setValue(resource);
        setupSink();
        setupWriteOperation();
    }

    protected void changeActionTo(Action action) {
        properties.action.setValue(action);
        setupSink();
        setupWriteOperation();
    }

    protected void changeUserTo(String user) {
        properties.connection.basicAuthentication.userId.setValue(user);
        setupSink();
        setupWriteOperation();
    }
}
