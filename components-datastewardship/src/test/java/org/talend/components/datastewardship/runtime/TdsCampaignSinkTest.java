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
import org.talend.components.datastewardship.tdatastewardshipcampaigncreate.TDataStewardshipCampaignCreateProperties;
import org.talend.daikon.avro.AvroRegistry;

/**
 * Unit-tests for {@link TdsCampaignSink} class
 */
@SuppressWarnings({"nls" })
public class TdsCampaignSinkTest {

    /**
     * {@link ComponentProperties} for {@link TdsCampaignSourceOrSink}
     */
    private TDataStewardshipCampaignCreateProperties outputProperties;

    /**
     * {@link Schema}
     */
    private Schema schema;

    /**
     * Prepares required instances for tests
     */
    @SuppressWarnings({ "deprecation"})
    @Before
    public void setUp() {
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();
        Schema.Field testField = new Schema.Field("test", stringSchema, null, null, Order.ASCENDING);
        schema = Schema.createRecord("campaign", null, null, false, Collections.singletonList(testField));
        schema.addProp(TALEND_IS_LOCKED, "true");
        
        outputProperties = new TDataStewardshipCampaignCreateProperties("root");
        outputProperties.connection.url.setValue("urlValue");
        outputProperties.connection.username.setValue("usernameValue");
        outputProperties.connection.password.setValue("passwordValue");
    }

    /**
     * Checks {@link TdsCampaignSink#initialize(RuntimeContainer, ComponentProperties)} sets required fields from
     * {@link ComponentProperties}
     */
    @Test
    public void testInitialize() {
        TdsCampaignSink sink = new TdsCampaignSink();
        sink.initialize(null, outputProperties);
        
        assertEquals("urlValue", sink.getUrl());
        assertEquals("usernameValue", sink.getUsername());
        assertEquals("passwordValue", sink.getPassword());
    }

    /**
     * Checks {@link TdsCampaignSink#createWriteOperation()} creates {@link WriteOperation} of class {@link TdsCampaignWriteOperation}
     */
    @Test
    public void testCreateWriteOperation() {
        TdsCampaignSink sink = new TdsCampaignSink();

        WriteOperation<?> writeOperation = sink.createWriteOperation();
        assertThat(writeOperation, is(instanceOf(TdsCampaignWriteOperation.class)));
    }
}