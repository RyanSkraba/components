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
package org.talend.components.datastewardship.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.util.Collections;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.datastewardship.common.CampaignType;
import org.talend.components.datastewardship.common.TdsConstants;
import org.talend.components.datastewardship.tdatastewardshiptaskoutput.TDataStewardshipTaskOutputProperties;
import org.talend.daikon.avro.AvroRegistry;

/**
 * Unit-tests for {@link TdsTaskSink} class
 */
@SuppressWarnings("nls")
public class TdsTaskSinkTest {

    /**
     * {@link ComponentProperties} for {@link TdsTaskSourceOrSink}
     */
    private TDataStewardshipTaskOutputProperties outputProperties;

    /**
     * {@link Schema}
     */
    private Schema schema;

    /**
     * Prepares required instances for tests
     */
    @SuppressWarnings("deprecation")
    @Before
    public void setUp() {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field testField = new Schema.Field("test", stringSchema, null, null, Order.ASCENDING);
        schema = Schema.createRecord("task", null, null, false, Collections.singletonList(testField));
        schema.addProp(TALEND_IS_LOCKED, "true");
        
        outputProperties = new TDataStewardshipTaskOutputProperties("root");
        outputProperties.connection.url.setValue("urlValue");
        outputProperties.connection.username.setValue("usernameValue");
        outputProperties.connection.password.setValue("passwordValue");
        outputProperties.campaignName.setValue("campaignNameValue");
        outputProperties.campaignType.setValue(CampaignType.MERGING);
        
        outputProperties.tasksMetadata.taskPriority.setValue(TdsConstants.PRIORITY_MEDIUM);
        outputProperties.tasksMetadata.taskTags.setValue("tagValue");
        outputProperties.tasksMetadata.taskState.setValue("stateValue");
        outputProperties.tasksMetadata.taskAssignee.setValue("user1");
        outputProperties.tasksMetadata.taskComment.setValue("comment");
        
        outputProperties.batchSize.setValue(0);
        outputProperties.advancedMappings.groupIdColumn.setValue("groupIdColumn");
        outputProperties.advancedMappings.sourceColumn.setValue("sourceColumn");
        outputProperties.advancedMappings.masterColumn.setValue("masterColumn");
        outputProperties.advancedMappings.scoreColumn.setValue("scoreColumn");        
    }

    /**
     * Checks {@link TdsTaskSink#initialize(RuntimeContainer, ComponentProperties)} sets required fields from
     * {@link ComponentProperties}
     */
    @Test
    public void testInitialize() {
        TdsTaskSink sink = new TdsTaskSink();
        sink.initialize(null, outputProperties);
        
        assertEquals("urlValue", sink.getUrl());
        assertEquals("usernameValue", sink.getUsername());
        assertEquals("passwordValue", sink.getPassword());
        assertEquals("campaignNameValue", sink.getCampaignName());
        assertEquals(CampaignType.MERGING.toString(), sink.getCampaignType());
        assertEquals("stateValue", sink.getTaskState());
        assertEquals("user1", sink.getTaskAssignee());
        assertEquals("groupIdColumn", sink.getGroupIdColumn());
        assertEquals("sourceColumn", sink.getSourceColumn());
        assertEquals("masterColumn", sink.getMasterColumn());
        assertEquals("scoreColumn", sink.getScoreColumn());      
    }

    /**
     * Checks {@link TdsTaskSink#createWriteOperation()} creates {@link WriteOperation} of class {@link TdsTaskWriteOperation}
     */
    @Test
    public void testCreateWriteOperation() {
        TdsTaskSink sink = new TdsTaskSink();

        WriteOperation<?> writeOperation = sink.createWriteOperation();
        assertThat(writeOperation, is(instanceOf(TdsTaskWriteOperation.class)));
    }
}