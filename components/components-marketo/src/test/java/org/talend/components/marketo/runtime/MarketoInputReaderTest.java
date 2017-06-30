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
        when(source.getClientService(any(RuntimeContainer.class))).thenReturn(client);
        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        reader = (MarketoInputReader) source.createReader(null);
        assertTrue(reader instanceof MarketoInputReader);

        when(client.getLead(any(TMarketoInputProperties.class), any(String.class))).thenReturn(getFailedRecordResult());
        when(client.getMultipleLeads(any(TMarketoInputProperties.class), any(String.class))).thenReturn(getFailedRecordResult());
        when(client.getLeadActivity(any(TMarketoInputProperties.class), any(String.class))).thenReturn(getFailedRecordResult());
        when(client.getLeadChanges(any(TMarketoInputProperties.class), any(String.class))).thenReturn(getFailedRecordResult());
        when(client.describeCustomObject(any(TMarketoInputProperties.class))).thenReturn(getFailedRecordResult());
        when(client.listCustomObjects(any(TMarketoInputProperties.class))).thenReturn(getFailedRecordResult());
        when(client.getCustomObjects(any(TMarketoInputProperties.class), any(String.class))).thenReturn(getFailedRecordResult());

    }

    @Test
    public void testStart() throws Exception {
        MarketoRecordResult mkto = new MarketoRecordResult();
        mkto.setSuccess(false);
        mkto.setErrors(Arrays.asList(new MarketoError("REST", "error")));

        when(client.bulkImport(any(TMarketoBulkExecProperties.class))).thenReturn(mkto);
        when(client.getLead(any(TMarketoInputProperties.class), any(String.class))).thenReturn(mkto);
        when(client.getMultipleLeads(any(TMarketoInputProperties.class), any(String.class))).thenReturn(mkto);
        when(client.getLeadActivity(any(TMarketoInputProperties.class), any(String.class))).thenReturn(mkto);
        when(client.getLeadChanges(any(TMarketoInputProperties.class), any(String.class))).thenReturn(mkto);
        when(client.describeCustomObject(any(TMarketoInputProperties.class))).thenReturn(mkto);
        when(client.listCustomObjects(any(TMarketoInputProperties.class))).thenReturn(mkto);
        when(client.getCustomObjects(any(TMarketoInputProperties.class), any(String.class))).thenReturn(mkto);

        try {
            assertFalse(reader.start());
            fail("Should not be here");
        } catch (IOException e) {
        }

        IndexedRecord record = new GenericData.Record(MarketoConstants.getEmptySchema());
        mkto.setSuccess(true);
        mkto.setRecords(Arrays.asList(record));

        when(client.bulkImport(any(TMarketoBulkExecProperties.class))).thenReturn(mkto);
        when(client.getLead(any(TMarketoInputProperties.class), any(String.class))).thenReturn(mkto);
        when(client.getMultipleLeads(any(TMarketoInputProperties.class), any(String.class))).thenReturn(mkto);
        when(client.getLeadActivity(any(TMarketoInputProperties.class), any(String.class))).thenReturn(mkto);
        when(client.getLeadChanges(any(TMarketoInputProperties.class), any(String.class))).thenReturn(mkto);
        when(client.describeCustomObject(any(TMarketoInputProperties.class))).thenReturn(mkto);
        when(client.listCustomObjects(any(TMarketoInputProperties.class))).thenReturn(mkto);
        when(client.getCustomObjects(any(TMarketoInputProperties.class), any(String.class))).thenReturn(mkto);

        assertFalse(reader.start());

    }

    @Test
    public void testGetCurrent() throws Exception {
        when(client.getLead(any(TMarketoInputProperties.class), any(String.class))).thenReturn(getLeadRecordResult(true));
        assertTrue(reader.start());
        assertNotNull(reader.getCurrent());
        assertTrue(reader.advance());
        assertNotNull(reader.getCurrent());
        when(client.getLead(any(TMarketoInputProperties.class), any(String.class))).thenReturn(getLeadRecordResult(false));
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
        props.inputOperation.setValue(InputOperation.getLeadChanges);
        props.updateSchemaRelated();
        props.schemaInput.schema.setValue(getFullDynamicSchema());
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
        when(source.getDynamicSchema(any(String.class), any(Schema.class)))
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
        when(source.getDynamicSchema(any(String.class), any(Schema.class)))
                .thenReturn(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        reader = (MarketoInputReader) source.createReader(null);
        assertFalse(reader.start());
        //
        props.customObjectAction.setValue(CustomObjectAction.get);
        props.updateSchemaRelated();
        props.schemaInput.schema.setValue(getFullDynamicSchema());
        source.initialize(null, props);
        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        when(source.getDynamicSchema(any(String.class), any(Schema.class)))
                .thenReturn(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        reader = (MarketoInputReader) source.createReader(null);
        assertFalse(reader.start());
        //
        props.customObjectAction.setValue(CustomObjectAction.list);
        props.updateSchemaRelated();
        props.schemaInput.schema.setValue(getFullDynamicSchema());
        source.initialize(null, props);
        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        when(source.getDynamicSchema(any(String.class), any(Schema.class)))
                .thenReturn(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        reader = (MarketoInputReader) source.createReader(null);
        assertFalse(reader.start());
        //
        props.customObjectAction.setValue(CustomObjectAction.get);
        props.updateSchemaRelated();
        props.schemaInput.schema.setValue(getFullDynamicSchema());
        source.initialize(null, props);
        when(source.createReader(null)).thenReturn(new MarketoInputReader(null, source, props));
        when(source.getDynamicSchema(any(String.class), any(Schema.class))).thenThrow(new IOException("error"));
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
        when(client.getLeadActivity(any(TMarketoInputProperties.class), any(String.class)))
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
}
