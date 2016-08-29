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

import static org.junit.Assert.assertEquals;
import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

import java.util.Collections;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.datastewardship.common.CampaignType;
import org.talend.components.datastewardship.tdatastewardshiptaskinput.TDataStewardshipTaskInputProperties;
import org.talend.components.datastewardship.tdatastewardshiptaskoutput.TDataStewardshipTaskOutputProperties;
import org.talend.daikon.avro.AvroRegistry;

/**
 * Unit-tests for {@link TdsTaskSink} class
 */
@SuppressWarnings("nls")
public class TdsTaskSourceTest {

    /**
     * {@link ComponentProperties} for {@link TdsTaskSourceOrSink}
     */
    private TDataStewardshipTaskInputProperties inputProperties;

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
        
        inputProperties = new TDataStewardshipTaskInputProperties("root");
        inputProperties.connection.url.setValue("urlValue");
        inputProperties.connection.username.setValue("usernameValue");
        inputProperties.connection.password.setValue("passwordValue");
        inputProperties.campaignName.setValue("perf-review-resolution");
        inputProperties.searchCriteria.taskState.setValue("Performance_review_request");
        inputProperties.batchSize.setValue(0);      
    }

    /**
     * Checks {@link TdsTaskSink#initialize(RuntimeContainer, ComponentProperties)} sets required fields from
     * {@link ComponentProperties}
     */
    @Test
    public void testInitialize() {
        TdsTaskSource source = new TdsTaskSource();
        source.initialize(null, inputProperties);
        
        assertEquals("urlValue", source.getUrl());
        assertEquals("usernameValue", source.getUsername());
        assertEquals("passwordValue", source.getPassword());
        assertEquals("perf-review-resolution", source.getCampaignName());
        assertEquals("Performance_review_request", source.getTaskState());
        assertEquals(0, source.getBatchSize());    
    }

}