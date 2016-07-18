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
import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.util.Collections;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.Action;
import org.talend.components.jira.Resource;
import org.talend.components.jira.tjiraoutput.TJiraOutputProperties;
import org.talend.daikon.avro.AvroRegistry;

/**
 * Unit-tests for {@link JiraSourceOrSink} class
 */
public class JiraSourceOrSinkTest {

    /**
     * {@link ComponentProperties} for {@link JiraSourceOrSink}
     */
    private TJiraOutputProperties outputProperties;

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
    	
        outputProperties = new TJiraOutputProperties("root");
        outputProperties.connection.hostUrl.setValue("hostValue");
        outputProperties.connection.basicAuthentication.userId.setValue("userIdValue");
        outputProperties.connection.basicAuthentication.password.setValue("passwordValue");
        outputProperties.resource.setValue(Resource.ISSUE);
        outputProperties.schema.schema.setValue(schema);
        outputProperties.action.setValue(Action.INSERT);
    }

    /**
     * Checks {@link JiraSourceOrSink#initialize(RuntimeContainer, ComponentProperties)} sets required fields from
     * {@link ComponentProperties}
     */
    @Test
    public void testInitialize() {
        JiraSourceOrSink sourceOrSink = new JiraSourceOrSink();

        sourceOrSink.initialize(null, outputProperties);

        String hostPort = sourceOrSink.getHostPort();
        assertEquals("hostValue", hostPort);
        String userId = sourceOrSink.getUserId();
        assertEquals("userIdValue", userId);
        String password = sourceOrSink.getUserPassword();
        assertEquals("passwordValue", password);
        String resource = sourceOrSink.getResource();
        assertEquals("rest/api/2/issue", resource);
        Schema actualSchema = sourceOrSink.getSchema();
        assertEquals(schema, actualSchema);
    }

}
