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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.talend.components.jira.testutils.JiraTestConstants.HOST_PORT;
import static org.talend.components.jira.testutils.JiraTestConstants.INCORRECT_HOST_PORT;
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
import org.talend.daikon.properties.ValidationResult.Result;

/**
 * Integration tests for {@link JiraSourceOrSink} class
 */
public class JiraSourceOrSinkTestIT {

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
        outputProperties.connection.hostUrl.setValue(HOST_PORT);
        outputProperties.connection.basicAuthentication.userId.setValue("userIdValue");
        outputProperties.connection.basicAuthentication.password.setValue("passwordValue");
        outputProperties.resource.setValue(Resource.ISSUE);
        outputProperties.schema.schema.setValue(schema);
        outputProperties.action.setValue(Action.INSERT);
    }

    /**
     * Checks {@link JiraSourceOrSink#validate(RuntimeContainer)} returns {@link ValidationResult.Result#OK} in case of
     * established connection
     */
    @Test
    public void testValidateOk() {
        JiraSourceOrSink sourceOrSink = new JiraSourceOrSink();
        sourceOrSink.initialize(null, outputProperties);

        ValidationResult result = sourceOrSink.validate(null);
        Result actualStatus = result.getStatus();

        assertEquals(Result.OK, actualStatus);
    }

    /**
     * Checks {@link JiraSourceOrSink#validate(RuntimeContainer)} returns {@link ValidationResult.Result#ERROR} and
     * {@link ValidationResult} contains correct message in case of connection wasn't established
     */
    @Test
    public void testValidateError() {
        JiraSourceOrSink sourceOrSink = new JiraSourceOrSink();
        outputProperties.connection.hostUrl.setValue(INCORRECT_HOST_PORT);
        sourceOrSink.initialize(null, outputProperties);

        ValidationResult result = sourceOrSink.validate(null);
        Result actualStatus = result.getStatus();
        String actualMessage = result.getMessage();

        assertEquals(Result.ERROR, actualStatus);
        assertThat(actualMessage, containsString("Host validation failed for URL: " + INCORRECT_HOST_PORT));
        assertThat(actualMessage, containsString("Exception during connection: "));
    }

    /**
     * Checks {@link JiraSourceOrSink#validate(RuntimeContainer)} returns {@link ValidationResult.Result#ERROR} and
     * {@link ValidationResult} contains correct message in case of connection was established, but there is no such resource
     * on server
     */
    @Test
    public void testValidateWrongStatus() {
        String expectedMessage = "Host validation failed for URL: " + HOST_PORT + "notFoundPage" + System.lineSeparator()
                + "Connection is established, but status code is 404";

        JiraSourceOrSink sourceOrSink = new JiraSourceOrSink();
        outputProperties.connection.hostUrl.setValue(HOST_PORT + "notFoundPage");
        sourceOrSink.initialize(null, outputProperties);

        ValidationResult result = sourceOrSink.validate(null);
        Result actualStatus = result.getStatus();
        String actualMessage = result.getMessage();

        assertEquals(Result.ERROR, actualStatus);
        assertEquals(expectedMessage, actualMessage);
    }

}
