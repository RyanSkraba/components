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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.talend.components.api.component.ComponentDefinition.RETURN_ERROR_MESSAGE;
import static org.talend.components.marketo.MarketoComponentDefinition.RETURN_NB_CALL;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.MarketoRESTClient;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties;

public class MarketoBulkExecReaderTest {

    MarketoBulkExecReader reader;

    MarketoRESTClient client;

    @Before
    public void setUp() throws Exception {
        TMarketoBulkExecProperties props = new TMarketoBulkExecProperties("test");
        props.connection.setupProperties();
        props.setupProperties();
        props.connection.maxReconnAttemps.setValue(2);
        props.connection.attemptsIntervalTime.setValue(500);
        client = mock(MarketoRESTClient.class);
        when(client.bulkImport(any(TMarketoBulkExecProperties.class))).thenReturn(new MarketoRecordResult());
        MarketoSource source = mock(MarketoSource.class);
        source.initialize(null, props);
        when(source.getClientService(any())).thenReturn(client);
        when(source.createReader(null)).thenReturn(new MarketoBulkExecReader(null, source, props));
        reader = (MarketoBulkExecReader) source.createReader(null);
        assertTrue(reader instanceof MarketoBulkExecReader);
    }

    @Test
    public void testStart() throws Exception {
        try {
            assertFalse(reader.start());
            fail("Should not be here");
        } catch (Exception e) {
        }
        reader.properties.dieOnError.setValue(false);
        MarketoRecordResult mkto = new MarketoRecordResult();
        mkto.setErrors(Arrays.asList(new MarketoError("REST", "error")));
        when(client.bulkImport(any(TMarketoBulkExecProperties.class))).thenReturn(mkto);
        assertFalse(reader.start());
        IndexedRecord record = new GenericData.Record(MarketoConstants.getEmptySchema());
        mkto.setSuccess(true);
        mkto.setRecords(Arrays.asList(record));
        when(client.bulkImport(any(TMarketoBulkExecProperties.class))).thenReturn(mkto);
        assertTrue(reader.start());
    }

    @Test
    public void testAdvance() throws Exception {
        assertFalse(reader.advance());
    }

    @Test
    public void testGetCurrent() throws Exception {
        assertNull(reader.getCurrent());
    }

    @Test
    public void testGetReturnValues() throws Exception {
        Map<String, Object> r = reader.getReturnValues();
        assertNotNull(r);
        assertEquals(0, r.get(RETURN_NB_CALL));
        assertNull(r.get(RETURN_ERROR_MESSAGE));
    }

    @Test
    public void testRetryOperationSuccess() throws Exception {
        IndexedRecord record = new GenericData.Record(MarketoConstants.getEmptySchema());
        MarketoRecordResult mkto = new MarketoRecordResult();
        mkto.setSuccess(true);
        mkto.setRecords(Arrays.asList(record));
        when(client.bulkImport(any(TMarketoBulkExecProperties.class))).thenReturn(mkto);
        assertTrue(reader.start());
        assertFalse(reader.advance());
        int result = (int) reader.getReturnValues().get(RETURN_NB_CALL);
        assertEquals(1, result);
    }

    @Test
    public void testRetryOperationFailDieOnError() throws Exception {
        MarketoRecordResult mkto = new MarketoRecordResult();
        mkto.setErrors(Arrays.asList(new MarketoError("REST", "902", "Invalid operation")));
        when(client.bulkImport(any(TMarketoBulkExecProperties.class))).thenReturn(mkto);
        try {
            reader.start();
            fail("Should not be here");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("902"));
        }
    }

    @Test
    public void testRetryOperationFailNonRecoverableErrror() throws Exception {
        MarketoRecordResult mkto = new MarketoRecordResult();
        mkto.setErrors(Arrays.asList(new MarketoError("REST", "902", "Invalid operation")));
        when(client.bulkImport(any(TMarketoBulkExecProperties.class))).thenReturn(mkto);
        doReturn(false).when(client).isErrorRecoverable(any(List.class));
        reader.properties.dieOnError.setValue(false);
        assertFalse(reader.start());
        int result = (int) reader.getReturnValues().get(RETURN_NB_CALL);
        assertEquals(1, result);
    }

    @Test
    public void testRetryOperationFailRecoverableErrror() throws Exception {
        MarketoRecordResult mkto = new MarketoRecordResult();
        mkto.setErrors(Arrays.asList(new MarketoError("REST", "602", "expired header")));
        when(client.bulkImport(any(TMarketoBulkExecProperties.class))).thenReturn(mkto);
        doReturn(true).when(client).isErrorRecoverable(any(List.class));
        reader.properties.dieOnError.setValue(false);
        int minDelay = reader.getRetryAttemps() * reader.getRetryInterval();
        long start = System.currentTimeMillis();
        assertFalse(reader.start());
        long end = System.currentTimeMillis();
        int result = (int) reader.getReturnValues().get(RETURN_NB_CALL);
        assertEquals((long) reader.getRetryAttemps(), result);
        assertTrue(minDelay <= (end - start));
    }

}
