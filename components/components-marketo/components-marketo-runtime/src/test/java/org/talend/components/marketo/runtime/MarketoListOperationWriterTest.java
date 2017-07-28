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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.type.ListOperationParameters;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties.ListOperation;

public class MarketoListOperationWriterTest extends MarketoRuntimeTestBase {

    TMarketoListOperationProperties props;

    MarketoListOperationWriter writer;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        props = new TMarketoListOperationProperties("test");
        props.connection.setupProperties();
        props.setupProperties();

        when(sink.getProperties()).thenReturn(props);
        writer = new MarketoListOperationWriter(new MarketoWriteOperation(sink), null);
        assertNotNull(writer);
    }

    @Test
    public void testOpen() throws Exception {
        writer.open("test");
        assertNotNull(writer.close());
    }

    @Test
    public void testWriteNull() throws Exception {
        writer.open("test");
        writer.write(null);
        assertEquals(0, writer.result.totalCount);
    }

    @Test
    public void testWriteREST() throws Exception {
        doReturn(getFailedSyncResult(true)).when(client).addToList(any(ListOperationParameters.class));
        doReturn(getSuccessSyncResult("added")).when(client).removeFromList(any(ListOperationParameters.class));
        doReturn(getFailedSyncResult(true)).when(client).isMemberOfList(any(ListOperationParameters.class));
        IndexedRecord record = new Record(MarketoConstants.getListOperationRESTSchema());
        record.put(0, 12345);
        record.put(1, 54321);
        try {
            writer.write(record);
            fail("Should not be here");
        } catch (Exception e) {
        }
        props.dieOnError.setValue(false);
        props.multipleOperation.setValue(true);
        when(sink.getProperties()).thenReturn(props);
        writer.open("test");
        writer.write(record);
        writer.write(record);
        writer.write(record);
        writer.write(record);
        writer.write(record);
        record.put(0, 66666);
        writer.write(record);
        assertNotNull(writer.close());
        //
        doReturn(getSuccessSyncResult("added")).when(client).addToList(any(ListOperationParameters.class));
        writer.open("test");
        writer.write(record);
        assertNotNull(writer.close());
        //
        props.listOperation.setValue(ListOperation.isMemberOf);
        when(sink.getProperties()).thenReturn(props);
        writer.open("test");
        writer.write(record);
        assertNotNull(writer.close());
        //
        props.listOperation.setValue(ListOperation.removeFrom);
        when(sink.getProperties()).thenReturn(props);
        writer.open("test");
        writer.write(record);
        assertNotNull(writer.close());
    }

    @Test
    public void testWriteSOAP() throws Exception {
        doReturn(getSuccessSyncResult("added")).when(client).addToList(any(ListOperationParameters.class));
        IndexedRecord record = new Record(MarketoConstants.getListOperationSOAPSchema());
        record.put(0, "MKTOLISTNAME");
        record.put(1, "TESTS");
        record.put(2, "ID");
        record.put(3, "12345");
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.dieOnError.setValue(false);
        props.multipleOperation.setValue(true);
        props.schemaInput.schema.setValue(MarketoConstants.getListOperationSOAPSchema());
        props.updateOutputSchemas();
        when(client.getApi()).thenReturn("SOAP");
        when(sink.getProperties()).thenReturn(props);
        writer.open("test");
        writer.write(record);
        assertEquals(1, writer.getSuccessfulWrites().size());
        record.put(1, "TEST2");
        writer.write(record);
        assertNotNull(writer.close());
        assertEquals(1, writer.getSuccessfulWrites().size());
        //
    }

    @Test
    public void testRetryOperationSuccess() throws Exception {
        IndexedRecord record = new Record(MarketoConstants.getListOperationRESTSchema());
        record.put(0, 12345);
        record.put(1, 54321);
        doReturn(getSuccessSyncResult("added")).when(client).addToList(any(ListOperationParameters.class));
        doReturn(false).when(client).isErrorRecoverable(any(List.class));
        writer.open("test");
        writer.write(record);
        MarketoResult result = (MarketoResult) writer.close();
        assertEquals(1, result.apiCalls);
    }

    @Test
    public void testRetryOperationFailDieOnError() throws Exception {
        IndexedRecord record = new Record(MarketoConstants.getListOperationRESTSchema());
        record.put(0, 12345);
        record.put(1, 54321);
        doReturn(false).when(client).isErrorRecoverable(any(List.class));
        doReturn(getFailedSyncResult("REST", "902", "Invalid operation")).when(client)
                .addToList(any(ListOperationParameters.class));
        writer.open("test");
        writer.write(record);
        try {
            writer.close();
            fail("Should not be here");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("902"));
        }
    }

    @Test
    public void testRetryOperationFailNonRecoverableErrror() throws Exception {
        IndexedRecord record = new Record(MarketoConstants.getListOperationRESTSchema());
        record.put(0, 12345);
        record.put(1, 54321);
        doReturn(false).when(client).isErrorRecoverable(any(List.class));
        doReturn(getFailedSyncResult("REST", "902", "Invalid operation")).when(client)
                .addToList(any(ListOperationParameters.class));
        props.dieOnError.setValue(false);
        when(sink.getProperties()).thenReturn(props);
        writer.open("test");
        writer.write(record);
        MarketoResult result = (MarketoResult) writer.close();
        assertEquals(1, result.apiCalls);
        assertEquals(Collections.emptyList(), writer.getSuccessfulWrites());
        List<IndexedRecord> rejects = writer.getRejectedWrites();
        IndexedRecord reject = rejects.get(0);
        assertNotNull(reject);
        assertEquals("failed", reject.get(2));
        assertTrue(String.valueOf(reject.get(3)).contains("902"));
    }

    @Test
    public void testRetryOperationFailRecoverableErrror() throws Exception {
        IndexedRecord record = new Record(MarketoConstants.getListOperationRESTSchema());
        record.put(0, 12345);
        record.put(1, 54321);
        doReturn(getFailedSyncResult("REST", "602", "expired header")).when(client).addToList(any(ListOperationParameters.class));
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
        List<IndexedRecord> rejects = writer.getRejectedWrites();
        IndexedRecord reject = rejects.get(0);
        assertNotNull(reject);
        assertEquals("failed", reject.get(2));
        assertTrue(String.valueOf(reject.get(3)).contains("602"));
    }

}
