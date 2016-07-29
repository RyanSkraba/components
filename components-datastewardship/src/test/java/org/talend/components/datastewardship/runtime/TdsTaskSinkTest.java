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
import org.talend.components.datastewardship.CampaignType;
import org.talend.components.datastewardship.tdatastewardshiptaskoutput.TDataStewardshipTaskOutputProperties;
import org.talend.daikon.avro.AvroRegistry;

/**
 * Unit-tests for {@link TdsTaskSink} class
 */
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
        Schema.Field testField = new Schema.Field("test", stringSchema, null, null, Order.ASCENDING); //$NON-NLS-1$
        schema = Schema.createRecord("task", null, null, false, Collections.singletonList(testField));
        schema.addProp(TALEND_IS_LOCKED, "true"); //$NON-NLS-1$
        
        outputProperties = new TDataStewardshipTaskOutputProperties("root"); //$NON-NLS-1$
        outputProperties.connection.url.setValue("urlValue"); //$NON-NLS-1$
        outputProperties.connection.username.setValue("usernameValue"); //$NON-NLS-1$
        outputProperties.connection.password.setValue("passwordValue"); //$NON-NLS-1$
        outputProperties.campaign.campaignName.setValue("campaignNameValue"); //$NON-NLS-1$
        outputProperties.campaign.campaignType.setValue(CampaignType.Arbitration);
    }

    /**
     * Checks {@link TdsTaskSink#initialize(RuntimeContainer, ComponentProperties)} sets required fields from
     * {@link ComponentProperties}
     */
    @Test
    public void testInitialize() {
        TdsTaskSink sink = new TdsTaskSink();
        sink.initialize(null, outputProperties);
        
        assertEquals("urlValue", sink.getUrl()); //$NON-NLS-1$
        assertEquals("usernameValue", sink.getUsername()); //$NON-NLS-1$
        assertEquals("passwordValue", sink.getPassword()); //$NON-NLS-1$
        assertEquals("campaignNameValue", sink.getCampaignName()); //$NON-NLS-1$
        assertEquals(CampaignType.Arbitration.getValue(), sink.getCampaignType());
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