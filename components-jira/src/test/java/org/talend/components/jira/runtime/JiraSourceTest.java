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
package org.talend.components.jira.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.util.Collections;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.Resource;
import org.talend.components.jira.runtime.reader.JiraProjectIdReader;
import org.talend.components.jira.runtime.reader.JiraProjectsReader;
import org.talend.components.jira.runtime.reader.JiraSearchReader;
import org.talend.components.jira.tjirainput.TJiraInputProperties;
import org.talend.daikon.avro.AvroRegistry;

/**
 * Unit-tests for {@link JiraSource} class
 */
public class JiraSourceTest {

    /**
     * {@link ComponentProperties} for {@link JiraSource}
     */
    private TJiraInputProperties inputProperties;

    /**
     * {@link Schema}
     */
    private Schema schema;

    /**
     * Prepares required instances for tests
     */
    @Before
    public void setUp() {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field jsonField = new Schema.Field("json", stringSchema, null, null, Order.ASCENDING);
        schema = Schema.createRecord("jira", null, null, false, Collections.singletonList(jsonField));
        schema.addProp(TALEND_IS_LOCKED, "true");
    	
        inputProperties = new TJiraInputProperties("root");
        inputProperties.connection.hostUrl.setValue("hostValue");
        inputProperties.connection.basicAuthentication.userId.setValue("userIdValue");
        inputProperties.connection.basicAuthentication.password.setValue("passwordValue");
        inputProperties.resource.setValue(Resource.ISSUE);
       
        inputProperties.schema.schema.setValue(schema);
        inputProperties.jql.setValue("jqlValue");
        inputProperties.batchSize.setValue(50);
        inputProperties.projectId.setValue("projectIdValue");
    }

    /**
     * Checks {@link JiraSource#initialize(RuntimeContainer, ComponentProperties)} sets required fields from
     * {@link ComponentProperties}
     */
    @Test
    public void testInitialize() {
        JiraSource jiraSource = new JiraSource();

        jiraSource.initialize(null, inputProperties);

        String jql = jiraSource.getJql();
        assertEquals("jqlValue", jql);
        int bathcSize = jiraSource.getBatchSize();
        assertEquals(50, bathcSize);
        String projectId = jiraSource.getProjectId();
        assertEquals("projectIdValue", projectId);
    }

    /**
     * Checks {@link JiraSource#createReader(RuntimeContainer)} creates {@link JiraSearchReader}
     */
    @Test
    public void testCreateReaderSearch() {
        JiraSource jiraSource = new JiraSource();
        jiraSource.initialize(null, inputProperties);

        Reader<IndexedRecord> reader = jiraSource.createReader(null);

        assertThat(reader, is(instanceOf(JiraSearchReader.class)));
    }

    /**
     * Checks {@link JiraSource#createReader(RuntimeContainer)} creates {@link JiraProjectsReader}
     */
    @Test
    public void testCreateReaderProjects() {
        JiraSource jiraSource = new JiraSource();
        inputProperties.resource.setValue(Resource.PROJECT);
        inputProperties.projectId.setValue(null);
        jiraSource.initialize(null, inputProperties);

        Reader<IndexedRecord> reader = jiraSource.createReader(null);

        assertThat(reader, is(instanceOf(JiraProjectsReader.class)));
    }

    /**
     * Checks {@link JiraSource#createReader(RuntimeContainer)} creates {@link JiraProjectIdReader}
     */
    @Test
    public void testCreateReaderProjectId() {
        JiraSource jiraSource = new JiraSource();
        inputProperties.resource.setValue(Resource.PROJECT);
        inputProperties.projectId.setValue("TP");
        jiraSource.initialize(null, inputProperties);

        Reader<IndexedRecord> reader = jiraSource.createReader(null);

        assertThat(reader, is(instanceOf(JiraProjectIdReader.class)));
    }
}
