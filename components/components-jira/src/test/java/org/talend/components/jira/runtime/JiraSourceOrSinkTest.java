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
package org.talend.components.jira.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import org.talend.daikon.properties.ValidationResult;

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

    private JiraSourceOrSink sourceOrSink;

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

        sourceOrSink = new JiraSourceOrSink();
    }

    /**
     * Checks {@link JiraSourceOrSink#initialize(RuntimeContainer, ComponentProperties)} sets required fields from
     * {@link ComponentProperties}
     */
    @Test
    public void testInitialize() {
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

    /**
     * Checks {@link JiraSourceOrSink#validate(RuntimeContainer)} validates connection properties and returns
     * {@link org.talend.daikon.properties.ValidationResult.Result#ERROR} if connection properties are not valid.
     */
    @Test
    public void testValidateConnectionProperties() {
        testValidateConnectionProperties("Host URL is null", null, "user", "test123", false);
        testValidateConnectionProperties("Host URL is empty", "", "user", "test123", false);
        testValidateConnectionProperties("Host URL is blank", "  ", "user", "test123", false);
        testValidateConnectionProperties("Host URL is not valid", "localhost:8080", "user", "test123", false);

        testValidateConnectionProperties("User ID is null", "http://localhost:8080", null, null, false);
        testValidateConnectionProperties("User ID is empty", "http://localhost:8080", "", "", true);
        testValidateConnectionProperties("User ID is blank", "http://localhost:8080", "\t", null, false);

        testValidateConnectionProperties("Password is null", "http://localhost:8080", "user", null, false);
        testValidateConnectionProperties("Password is empty", "http://localhost:8080", "user", "", true);
        testValidateConnectionProperties("Password is blank", "http://localhost:8080", "user", " ", false);
    }

    private void testValidateConnectionProperties(String message, String hostUrl, String userId, String password, boolean valid) {
        JiraSourceOrSink sourceOrSink = new JiraSourceOrSink() {

            @Override
            protected ValidationResult validateConnection() {
                return ValidationResult.OK;
            }
        };

        TJiraOutputProperties outputProperties = new TJiraOutputProperties("root");
        outputProperties.connection.hostUrl.setValue(hostUrl);
        outputProperties.connection.basicAuthentication.userId.setValue(userId);
        outputProperties.connection.basicAuthentication.password.setValue(password);

        sourceOrSink.initialize(null, outputProperties);

        ValidationResult vr = sourceOrSink.validate(null);
        if (!valid) {
            assertEquals(message, ValidationResult.Result.ERROR, vr.getStatus());
            assertNotNull(message, vr.getMessage());
        } else {
            assertEquals(message, ValidationResult.Result.OK, vr.getStatus());
        }
    }
}
