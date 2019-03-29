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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;

public class MarketoInputReaderTest extends MarketoRuntimeTestBase {

    MarketoInputReader reader;

    MarketoSource source;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        TMarketoInputProperties props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.setupProperties();

        source = mock(MarketoSource.class);
        source.initialize(null, props);
        when(source.getClientService(any())).thenReturn(client);
        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        reader = (MarketoInputReader) source.createReader(null);
        assertTrue(reader instanceof MarketoInputReader);

        when(client.getLead(any(), any()))
                .thenReturn(getFailedRecordResult("REST", "", "error"));
        when(client.getMultipleLeads(any(), any()))
                .thenReturn(getFailedRecordResult("REST", "", "error"));
        when(client.getLeadActivity(any(), any()))
                .thenReturn(getFailedRecordResult("REST", "", "error"));
        when(client.getLeadChanges(any(), any()))
                .thenReturn(getFailedRecordResult("REST", "", "error"));
        when(client.describeCustomObject(any()))
                .thenReturn(getFailedRecordResult("REST", "", "error"));
        when(client.listCustomObjects(any())).thenReturn(getFailedRecordResult("REST", "", "error"));
        when(client.getCustomObjects(any(), any()))
                .thenReturn(getFailedRecordResult("REST", "", "error"));

    }

    @Test
    public void testStart() throws Exception {
        MarketoRecordResult mkto = new MarketoRecordResult();
        mkto.setSuccess(false);
        mkto.setErrors(Arrays.asList(new MarketoError("REST", "error")));

        when(client.bulkImport(any())).thenReturn(mkto);
        when(client.getLead(any(), any())).thenReturn(mkto);
        when(client.getMultipleLeads(any(), any())).thenReturn(mkto);
        when(client.getLeadActivity(any(), any())).thenReturn(mkto);
        when(client.getLeadChanges(any(), any())).thenReturn(mkto);
        when(client.describeCustomObject(any())).thenReturn(mkto);
        when(client.listCustomObjects(any())).thenReturn(mkto);
        when(client.getCustomObjects(any(), any())).thenReturn(mkto);

        try {
            assertFalse(reader.start());
            fail("Should not be here");
        } catch (Exception e) {
        }

        IndexedRecord record = new GenericData.Record(MarketoConstants.getEmptySchema());
        mkto.setSuccess(true);
        mkto.setRecords(Arrays.asList(record));

        when(client.bulkImport(any())).thenReturn(mkto);
        when(client.getLead(any(), any())).thenReturn(mkto);
        when(client.getMultipleLeads(any(), any())).thenReturn(mkto);
        when(client.getLeadActivity(any(), any())).thenReturn(mkto);
        when(client.getLeadChanges(any(), any())).thenReturn(mkto);
        when(client.describeCustomObject(any())).thenReturn(mkto);
        when(client.listCustomObjects(any())).thenReturn(mkto);
        when(client.getCustomObjects(any(), any())).thenReturn(mkto);

        assertFalse(reader.start());

    }

    @Test
    public void testGetCurrent() throws Exception {
        when(client.getLead(any(), any())).thenReturn(getLeadRecordResult(true));
        assertTrue(reader.start());
        assertNotNull(reader.getCurrent());
        assertTrue(reader.advance());
        assertNotNull(reader.getCurrent());
        when(client.getLead(any(), any())).thenReturn(getLeadRecordResult(false));
        assertTrue(reader.advance());
        assertNotNull(reader.getCurrent());
        assertTrue(reader.advance());
        assertNotNull(reader.getCurrent());
        assertFalse(reader.advance());
    }

    @Test
    public void testGetReturnValues() throws Exception {
        Map<String, Object> r = reader.getReturnValues();
        assertNotNull(r);
        assertEquals(0, r.get(RETURN_NB_CALL));
        assertNull(r.get(RETURN_ERROR_MESSAGE));
    }

    @Test
    public void testSplitList() throws Exception {
        List<String> strings = Arrays.asList("one", "two", "three", "four", "five");
        List<List<String>> results = MarketoInputReader.splitList(strings, 2);
        assertEquals(3, results.size());
        assertEquals(Arrays.asList("one", "two"), results.get(0));
        results = MarketoInputReader.splitList(strings, 5);
        assertEquals(1, results.size());
        assertEquals(strings, results.get(0));
    }

    @Test
    public void testExecuteOperation() throws Exception {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        props.connection.init();
        props.init();
        props.inputOperation.setValue(InputOperation.getLeadChanges);
        props.updateSchemaRelated();
        props.schemaInput.schema.setValue(getFullDynamicSchema());
        props.dieOnError.setValue(false);
        source.initialize(null, props);
        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        reader = (MarketoInputReader) source.createReader(null);
        assertFalse(reader.start());
        //
        props.inputOperation.setValue(InputOperation.getMultipleLeads);
        props.updateSchemaRelated();
        props.schemaInput.schema.setValue(getFullDynamicSchema());
        source.initialize(null, props);
        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        when(source.getDynamicSchema(any(), any(Schema.class)))
                .thenReturn(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        reader = (MarketoInputReader) source.createReader(null);
        assertFalse(reader.start());
        //
        props.inputOperation.setValue(InputOperation.CustomObject);
        props.customObjectAction.setValue(CustomObjectAction.describe);
        props.updateSchemaRelated();
        props.schemaInput.schema.setValue(getFullDynamicSchema());
        source.initialize(null, props);
        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        when(source.getDynamicSchema(any(), any(Schema.class)))
                .thenReturn(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        reader = (MarketoInputReader) source.createReader(null);
        assertFalse(reader.start());
        //
        props.customObjectAction.setValue(CustomObjectAction.get);
        props.updateSchemaRelated();
        props.schemaInput.schema.setValue(getFullDynamicSchema());
        source.initialize(null, props);
        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        when(source.getDynamicSchema(any(), any(Schema.class)))
                .thenReturn(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        reader = (MarketoInputReader) source.createReader(null);
        assertFalse(reader.start());
        //
        props.customObjectAction.setValue(CustomObjectAction.list);
        props.updateSchemaRelated();
        props.schemaInput.schema.setValue(getFullDynamicSchema());
        source.initialize(null, props);
        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        when(source.getDynamicSchema(any(), any(Schema.class)))
                .thenReturn(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        reader = (MarketoInputReader) source.createReader(null);
        assertFalse(reader.start());
        //
        props.customObjectAction.setValue(CustomObjectAction.get);
        props.updateSchemaRelated();
        props.schemaInput.schema.setValue(getFullDynamicSchema());
        source.initialize(null, props);
        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        when(source.getDynamicSchema(any(), any(Schema.class))).thenThrow(new IOException("error"));
        reader = (MarketoInputReader) source.createReader(null);
        try {
            assertFalse(reader.start());
            fail("Should not be here");
        } catch (IOException e) {
        }
        //
    }

    @Test
    public void testActivities() throws Exception {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.setupProperties();
        props.inputOperation.setValue(InputOperation.getLeadActivity);
        props.updateSchemaRelated();
        source.initialize(null, props);

        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        when(client.getLeadActivity(any(), any()))
                .thenReturn(getLeadRecordResult(false));

        reader = (MarketoInputReader) source.createReader(null);
        assertTrue(props.isApiREST());
        assertTrue(reader.start());
        assertNotNull(reader.getCurrent());
        assertTrue(reader.advance());
        assertNotNull(reader.getCurrent());
        //
        props.setIncludeTypes.setValue(true);
        props.includeTypes.type.setValue(Arrays.asList("activity1", "activity2"));
        source.initialize(null, props);
        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        reader = (MarketoInputReader) source.createReader(null);
        assertTrue(props.isApiREST());
        assertTrue(reader.start());
        assertNotNull(reader.getCurrent());
        assertTrue(reader.advance());
        assertNotNull(reader.getCurrent());
        assertFalse(reader.advance());
    }

    public MarketoInputReader getReaderForRetryOperation(boolean die) {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.setupProperties();
        props.inputOperation.setValue(InputOperation.getMultipleLeads);
        props.updateSchemaRelated();
        props.dieOnError.setValue(die);
        source.initialize(null, props);
        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        reader = (MarketoInputReader) source.createReader(null);

        return reader;
    }

    @Test
    public void testRetryOperationSuccess() throws Exception {
        doReturn(getLeadRecordResult(false)).when(client).getMultipleLeads(any(), any());
        reader = getReaderForRetryOperation(true);
        assertTrue(reader.start());
        assertTrue(reader.advance());
        assertFalse(reader.advance());
        int result = (int) reader.getReturnValues().get(RETURN_NB_CALL);
        assertEquals(1, result);
    }

    @Test
    public void testRetryOperationFailDieOnError() throws Exception {
        doReturn(getFailedRecordResult("REST", "902", "Invalid operation")).when(client)
                .getMultipleLeads(any(), any());
        reader = getReaderForRetryOperation(true);
        try {
            reader.start();
            fail("Should not be here");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("902"));
        }
    }

    @Test
    public void testRetryOperationFailNonRecoverableErrror() throws Exception {
        doReturn(false).when(client).isErrorRecoverable(any(List.class));
        doReturn(getFailedRecordResult("REST", "902", "Invalid operation")).when(client)
                .getMultipleLeads(any(), any());
        reader = getReaderForRetryOperation(false);
        assertFalse(reader.start());
        int result = (int) reader.getReturnValues().get(RETURN_NB_CALL);
        assertEquals(1, result);
    }

    @Test
    public void testRetryOperationFailRecoverableErrror() throws Exception {
        doReturn(true).when(client).isErrorRecoverable(any(List.class));
        doReturn(getFailedRecordResult("REST", "602", "expired header")).when(client)
                .getMultipleLeads(any(), any());
        reader = getReaderForRetryOperation(false);
        int minDelay = reader.getRetryAttemps() * reader.getRetryInterval();
        long start = System.currentTimeMillis();
        assertFalse(reader.start());
        long end = System.currentTimeMillis();
        int result = (int) reader.getReturnValues().get(RETURN_NB_CALL);
        assertEquals((long) reader.getRetryAttemps(), result);
        assertTrue(minDelay <= (end - start));
    }

}
