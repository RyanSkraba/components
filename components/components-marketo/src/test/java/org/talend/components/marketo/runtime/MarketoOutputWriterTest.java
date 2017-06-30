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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation;

public class MarketoOutputWriterTest extends MarketoRuntimeTestBase {

    MarketoOutputWriter writer;

    TMarketoOutputProperties props;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        props = new TMarketoOutputProperties("test");
        props.connection.setupProperties();
        props.setupProperties();
        props.outputOperation.setValue(OutputOperation.deleteLeads);
        props.batchSize.setValue(1);
        props.updateSchemaRelated();
        when(sink.getProperties()).thenReturn(props);
        writer = new MarketoOutputWriter(new MarketoWriteOperation(sink), null);
    }

    @Test
    public void testOpen() throws Exception {
        writer.open("test");
        assertNotNull(writer.close());
    }

    @Test
    public void testWriteDeleteLeads() throws Exception {
        writer.open("test");
        writer.write(null);
        // deleteLeads
        doReturn(getFailedSyncResult(true)).when(client).deleteLeads(any(ArrayList.class));
        IndexedRecord record = new Record(MarketoConstants.getDeleteLeadsSchema());
        record.put(0, 12345);
        try {
            writer.write(record);
            fail("Should not be here");
        } catch (Exception e) {
        }
        props.dieOnError.setValue(false);
        when(sink.getProperties()).thenReturn(props);
        writer.open("test");
        writer.write(record);
        assertNotNull(writer.close());
    }

    @Test
    public void testWriteSyncLead() throws Exception {
        props.outputOperation.setValue(OutputOperation.syncLead);
        props.dieOnError.setValue(false);
        props.batchSize.setValue(1);
        props.updateSchemaRelated();
        when(sink.getProperties()).thenReturn(props);
        doReturn(getFailedSyncResult(false)).when(client).syncLead(any(TMarketoOutputProperties.class), any(IndexedRecord.class));
        doReturn(getFailedSyncResult(false)).when(client).syncMultipleLeads(any(TMarketoOutputProperties.class), any(List.class));
        IndexedRecord record = new Record(MarketoConstants.getRESTOutputSchemaForSyncLead());
        record.put(0, 12345);
        writer.open("test");
        writer.write(record);
        assertNotNull(writer.close());
        doReturn(getSuccessSyncResult("created")).when(client).syncLead(any(TMarketoOutputProperties.class),
                any(IndexedRecord.class));
        writer.open("test");
        writer.write(record);
        assertNotNull(writer.close());
        //
        //
        props.outputOperation.setValue(OutputOperation.syncMultipleLeads);
        props.batchSize.setValue(2);
        when(sink.getProperties()).thenReturn(props);
        writer.open("test");
        writer.write(record);
        assertNotNull(writer.close());
        doReturn(getSuccessSyncResult("created")).when(client).syncMultipleLeads(any(TMarketoOutputProperties.class),
                any(List.class));
        writer.open("test");
        writer.write(record);
        assertNotNull(writer.close());
        //
        props.schemaInput.schema.setValue(getLeadDynamicSchema());
        props.updateOutputSchemas();
        when(sink.getProperties()).thenReturn(props);
        writer.open("test");
        writer.write(record);
        assertNotNull(writer.close());
        //
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.updateSchemaRelated();
        when(sink.getProperties()).thenReturn(props);
        writer.open("test");
        writer.write(record);
        assertNotNull(writer.close());
    }

    @Test
    public void testWriteCustomObject() throws Exception {
        props.outputOperation.setValue(OutputOperation.deleteCustomObjects);
        props.updateSchemaRelated();
        when(sink.getProperties()).thenReturn(props);
        doReturn(getSuccessSyncResult("deleted")).when(client).deleteCustomObjects(any(TMarketoOutputProperties.class),
                any(List.class));
        IndexedRecord record = new Record(MarketoConstants.getRESTOutputSchemaForSyncLead());
        record.put(0, 12345);
        writer.open("test");
        writer.write(record);
        assertNotNull(writer.close());
        //
        props.outputOperation.setValue(OutputOperation.syncCustomObjects);
        when(sink.getProperties()).thenReturn(props);
        doReturn(getSuccessSyncResult("updated")).when(client).syncCustomObjects(any(TMarketoOutputProperties.class),
                any(List.class));
        writer.open("test");
        writer.write(record);
        assertNotNull(writer.close());
    }

    @Test
    public void testGetWriteOperation() throws Exception {
        assertTrue(writer.getWriteOperation() instanceof MarketoWriteOperation);
    }

    @Test
    public void testClose() throws Exception {
        assertNotNull(writer.close());
        assertEquals(0, writer.result.totalCount);
    }

    @Test
    public void testGetSuccessfulWrites() throws Exception {
        assertEquals(Collections.emptyList(), writer.getSuccessfulWrites());
    }

    @Test
    public void testGetRejectedWrites() throws Exception {
        assertEquals(Collections.emptyList(), writer.getRejectedWrites());
    }

}
