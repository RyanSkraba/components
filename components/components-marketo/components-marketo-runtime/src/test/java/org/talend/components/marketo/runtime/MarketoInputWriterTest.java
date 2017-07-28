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
package org.talend.components.marketo.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;

public class MarketoInputWriterTest extends MarketoRuntimeTestBase {

    MarketoInputWriter writer;

    MarketoWriteOperation wop;

    TMarketoInputProperties props;

    IndexedRecord record;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.setupProperties();
        props.leadKeyValues.setValue("email");
        props.updateSchemaRelated();
        when(sink.getProperties()).thenReturn(props);
        wop = new MarketoWriteOperation(sink);
        writer = new MarketoInputWriter(wop, null);
        writer.properties = props;
        assertTrue(writer instanceof MarketoInputWriter);

        record = new Record(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        record.put(1, "toto@toto.com");
    }

    @Test
    public void testOpen() throws Exception {
        writer.open("test");
        assertNotNull(writer.close());
    }

    @Test
    public void testWrite() throws Exception {
        writer.open("test");
        writer.write(null);
        writer.write(record);
    }

    @Test
    public void testWriteDynamic() throws Exception {
        props.schemaInput.schema.setValue(getLeadDynamicSchema());
        when(sink.getProperties()).thenReturn(props);
        when(sink.getDynamicSchema(any(String.class), any(Schema.class)))
                .thenReturn(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());

        writer.open("test");
        writer.write(record);
    }

    @Test
    public void testFlush() throws Exception {
        // nop
        writer.flush();
    }

    @Test
    public void testRetryOperationSuccess() throws Exception {
        doReturn(getLeadRecordResult(false)).when(client).getMultipleLeads(any(TMarketoInputProperties.class), anyString());
        doReturn(false).when(client).isErrorRecoverable(any(List.class));
        writer.open("test");
        writer.write(record);
        MarketoResult result = (MarketoResult) writer.close();
        assertEquals(1, result.apiCalls);
    }

    @Test
    public void testRetryOperationFailDieOnError() throws Exception {
        doReturn(getFailedRecordResult("REST", "902", "Invalid operation")).when(client)
                .getMultipleLeads(any(TMarketoInputProperties.class), anyString());
        writer.open("test");
        try {
            writer.write(record);
            writer.close();
            fail("Should not be here");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("902"));
        }
    }

    @Test
    public void testRetryOperationFailNonRecoverableErrror() throws Exception {
        doReturn(getFailedRecordResult("REST", "902", "Invalid operation")).when(client)
                .getMultipleLeads(any(TMarketoInputProperties.class), anyString());
        props.dieOnError.setValue(false);
        when(sink.getProperties()).thenReturn(props);
        writer.open("test");
        writer.write(record);
        MarketoResult result = (MarketoResult) writer.close();
        assertEquals(1, result.apiCalls);
        assertEquals(Collections.emptyList(), writer.getSuccessfulWrites());
    }

    @Test
    public void testRetryOperationFailRecoverableErrror() throws Exception {
        doReturn(getFailedRecordResult("REST", "602", "expired header")).when(client)
                .getMultipleLeads(any(TMarketoInputProperties.class), anyString());
        doReturn(true).when(client).isErrorRecoverable(any(List.class));
        props.dieOnError.setValue(false);
        when(sink.getProperties()).thenReturn(props);
        int minDelay = props.connection.maxReconnAttemps.getValue() * props.connection.attemptsIntervalTime.getValue();
        long start = System.currentTimeMillis();
        writer.open("test");
        writer.write(record);
        MarketoResult result = (MarketoResult) writer.close();
        long end = System.currentTimeMillis();
        assertEquals((long) props.connection.maxReconnAttemps.getValue(), result.apiCalls);
        assertEquals(Collections.emptyList(), writer.getSuccessfulWrites());
        assertTrue(minDelay <= (end - start));
    }

}
