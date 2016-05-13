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

import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.testutils.Utils;
import org.talend.components.jira.tjirainput.TJiraInputProperties;

/**
 * Unit-tests for {@link JiraSource} class
 */
public class JiraSourceTest {
    
    /**
     * {@link ComponentProperties} for {@link JiraSource}
     */
    private TJiraInputProperties inputProperties;
    
    /**
     * JSON string, which describes {@link Schema}
     */
    private String schemaValue;
    
    /**
     * Prepares required instances for tests
     */
    @Before
    public void setUp() {
        inputProperties = new TJiraInputProperties("root");
        inputProperties.host.setValue("hostValue");
        inputProperties.userPassword.userId.setValue("userIdValue");
        inputProperties.userPassword.password.setValue("passwordValue");
        inputProperties.resource.setValue("issue");
        schemaValue = Utils.readFile("src/test/resources/org/talend/components/jira/tjirainput/schema.json");
        inputProperties.schema.schema.setValue(schemaValue);
        inputProperties.jql.setValue("jqlValue");
        inputProperties.batchSize.setValue(50);
        inputProperties.projectId.setValue("projectIdValue");
    }

    /**
     * Checks {@link JiraSource#initialize(RuntimeContainer, ComponentProperties)} sets required fields from
     * {@link ComponentProperties}
     */
    @Test
    public void initializeTest() {
        JiraSource jiraSource = new JiraSource();
        
        jiraSource.initialize(null, inputProperties);
        
        String hostPort = jiraSource.getHostPort();
        assertEquals("hostValue", hostPort);
        String userId = jiraSource.getUserId();
        assertEquals("userIdValue", userId);
        String password = jiraSource.getPassword();
        assertEquals("passwordValue", password);
        String resourceType = jiraSource.getResourceType();
        assertEquals("issue", resourceType);
        Schema dataSchema = jiraSource.getDataSchema();
        assertEquals(schemaValue, dataSchema.toString());
        String jql = jiraSource.getJql();
        assertEquals("jqlValue", jql);
        int bathcSize = jiraSource.getBatchSize();
        assertEquals(50, bathcSize);
        String projectId = jiraSource.getProjectId();
        assertEquals("projectIdValue", projectId);
    }
    
    /**
     * Checks {@link JiraSource#createReader(RuntimeContainer)} creates
     * {@link JiraSearchReader}
     */
    @Test
    public void createReaderSearchTest() {
        JiraSource jiraSource = new JiraSource();
        jiraSource.initialize(null, inputProperties);
        
        Reader<IndexedRecord> reader = jiraSource.createReader(null);
        
        assertThat(reader, is(instanceOf(JiraSearchReader.class)));
    }
    
    /**
     * Checks {@link JiraSource#createReader(RuntimeContainer)} creates
     * {@link JiraProjectsReader}
     */
    @Test
    public void createReaderProjectsTest() {
        JiraSource jiraSource = new JiraSource();
        inputProperties.resource.setValue("project");
        inputProperties.projectId.setValue(null);
        jiraSource.initialize(null, inputProperties);
        
        Reader<IndexedRecord> reader = jiraSource.createReader(null);
        
        assertThat(reader, is(instanceOf(JiraProjectsReader.class)));
    }
    
    /**
     * Checks {@link JiraSource#createReader(RuntimeContainer)} creates
     * {@link JiraProjectIdReader}
     */
    @Test
    public void createReaderProjectIdTest() {
        JiraSource jiraSource = new JiraSource();
        inputProperties.resource.setValue("project");
        inputProperties.projectId.setValue("TP");
        jiraSource.initialize(null, inputProperties);
        
        Reader<IndexedRecord> reader = jiraSource.createReader(null);
        
        assertThat(reader, is(instanceOf(JiraProjectIdReader.class)));
    }
}
