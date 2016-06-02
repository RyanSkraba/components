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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.Action;
import org.talend.components.jira.Resource;
import org.talend.components.jira.testutils.Utils;
import org.talend.components.jira.tjiraoutput.TJiraOutputProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

/**
 * Integration tests for {@link JiraSourceOrSink} class
 */
public class JiraSourceOrSinkTestIT {
    
    /**
     * Jira server host and port
     */
    private static final String CORRECT_HOST_PORT = "http://192.168.99.100:8080/";
    
    /**
     * Incorrect host and port
     */
    private static final String INCORRECT_HOST_PORT = "http://incorrecthost.com/";
    
    /**
     * {@link ComponentProperties} for {@link JiraSourceOrSink}
     */
    private TJiraOutputProperties outputProperties;
    
    /**
     * JSON string, which describes {@link Schema}
     */
    private String schemaValue;
    
    /**
     * Prepares required instances for tests
     */
    @Before
    public void setUp() {
        outputProperties = new TJiraOutputProperties("root");
        outputProperties.connection.hostUrl.setValue(CORRECT_HOST_PORT);
        outputProperties.connection.basicAuthentication.userId.setValue("userIdValue");
        outputProperties.connection.basicAuthentication.password.setValue("passwordValue");
        outputProperties.resource.setValue(Resource.ISSUE);
        schemaValue = Utils.readFile("src/test/resources/org/talend/components/jira/tjirainput/schema.json");
        outputProperties.schema.schema.setValue(new Schema.Parser().parse(schemaValue));
        outputProperties.action.setValue(Action.INSERT);
    }
    
    /**
     * Checks {@link JiraSourceOrSink#validate(RuntimeContainer)} returns {@link ValidationResult.Result#OK} in case of
     * established connection
     */
    @Ignore
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
    @Ignore
    @Test
    public void testValidateError() {
        JiraSourceOrSink sourceOrSink = new JiraSourceOrSink();
        outputProperties.connection.hostUrl.setValue(INCORRECT_HOST_PORT);
        sourceOrSink.initialize(null, outputProperties);

        ValidationResult result = sourceOrSink.validate(null);
        Result actualStatus = result.getStatus();
        String actualMessage = result.getMessage();

        assertEquals(Result.ERROR, actualStatus);
        assertEquals("Wrong host URL: " + INCORRECT_HOST_PORT + " or host is unreachable", actualMessage);
    }

}
