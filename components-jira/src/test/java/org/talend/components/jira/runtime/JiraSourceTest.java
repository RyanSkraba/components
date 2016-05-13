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

import org.apache.avro.Schema;
import org.junit.Test;
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
     * Prepares required instances before {@link JiraSourceTest#initializeTest()}
     */
    private void beforeInitialize() {
        inputProperties = new TJiraInputProperties("root");
        inputProperties.host.setValue("hostValue");
        inputProperties.userPassword.userId.setValue("userIdValue");
        inputProperties.userPassword.password.setValue("passwordValue");
        inputProperties.resource.setValue("resourceValue");
        schemaValue = Utils.readFile("src/test/resources/org/talend/components/jira/tjirainput/schema.json");
        inputProperties.schema.schema.setValue(schemaValue);
        inputProperties.jql.setValue("jqlValue");
        inputProperties.batchSize.setValue(50);
    }

    /**
     * Checks {@link JiraSource#initialize(RuntimeContainer, ComponentProperties)} sets required fields from
     * {@link ComponentProperties}
     */
    @Test
    public void initializeTest() {
        beforeInitialize();
        JiraSource jiraSource = new JiraSource();
        
        jiraSource.initialize(null, inputProperties);
        
        String hostPort = jiraSource.getHostPort();
        assertEquals("hostValue", hostPort);
        String userId = jiraSource.getUserId();
        assertEquals("userIdValue", userId);
        String password = jiraSource.getPassword();
        assertEquals("passwordValue", password);
        String resourceType = jiraSource.getResourceType();
        assertEquals("resourceValue", resourceType);
        Schema dataSchema = jiraSource.getDataSchema();
        assertEquals(schemaValue, dataSchema.toString());
        String jql = jiraSource.getJql();
        assertEquals("jqlValue", jql);
        int bathcSize = jiraSource.getBatchSize();
        assertEquals(50, bathcSize);
    }
}
