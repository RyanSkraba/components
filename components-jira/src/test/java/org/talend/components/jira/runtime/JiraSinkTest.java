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
import static org.junit.Assert.assertTrue;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.Action;
import org.talend.components.jira.Resource;
import org.talend.components.jira.testutils.Utils;
import org.talend.components.jira.tjiraoutput.TJiraOutputProperties;

/**
 * Unit-tests for {@link JiraSink} class
 */
public class JiraSinkTest {

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
        outputProperties.connection.hostUrl.setValue("hostValue");
        outputProperties.connection.basicAuthentication.userId.setValue("userIdValue");
        outputProperties.connection.basicAuthentication.password.setValue("passwordValue");
        outputProperties.resource.setValue(Resource.ISSUE);
        schemaValue = Utils.readFile("src/test/resources/org/talend/components/jira/tjirainput/schema.json");
        outputProperties.schema.schema.setValue(new Schema.Parser().parse(schemaValue));
        outputProperties.action.setValue(Action.INSERT);
        outputProperties.deleteSubtasks.setValue(true);
    }

    /**
     * Checks {@link JiraSink#initialize(RuntimeContainer, ComponentProperties)} sets required fields from
     * {@link ComponentProperties}
     */
    @Test
    public void testInitialize() {
        JiraSink sink = new JiraSink();

        sink.initialize(null, outputProperties);

        Action action = sink.getAction();
        assertEquals(Action.INSERT, action);
        boolean deleteSubtasks = sink.doDeleteSubtasks();
        assertTrue(deleteSubtasks);
    }

    /**
     * Checks {@link JiraSink#createWriteOperation()} creates {@link WriteOperation} of class {@link JiraWriteOperation}
     */
    @Test
    public void testCreateWriteOperation() {
        JiraSink sink = new JiraSink();

        WriteOperation<?> writeOperation = sink.createWriteOperation();
        assertThat(writeOperation, is(instanceOf(JiraWriteOperation.class)));
    }
}
