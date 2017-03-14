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
import static org.junit.Assert.assertTrue;
import static org.talend.components.marketo.MarketoConstants.FIELD_STATUS;
import static org.talend.components.marketo.MarketoConstants.FIELD_SUCCESS;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.REST;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.marketo.MarketoComponentDefinition;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties.ListOperation;

public class MarketoListOperationWriterTestIT extends MarketoBaseTestIT {

    MarketoListOperationWriter writer;

    TMarketoListOperationProperties props;

    private static final int TEST_NB_LEADS = 30;

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoListOperationWriterTestIT.class);

    @BeforeClass
    public static void setupDatasets() throws Exception {
        createDatasets(TEST_NB_LEADS);
    }

    @AfterClass
    public static void teardownDatasets() throws Exception {
        cleanupDatasets();
    }

    public TMarketoListOperationProperties getSOAPProperties() {
        TMarketoListOperationProperties props = new TMarketoListOperationProperties("test");
        props.connection.setupProperties();
        props.connection.endpoint.setValue(ENDPOINT_SOAP);
        props.connection.clientAccessId.setValue(USERID_SOAP);
        props.connection.secretKey.setValue(SECRETKEY_SOAP);
        props.connection.apiMode.setValue(APIMode.SOAP);
        props.connection.setupLayout();
        props.setupProperties();
        props.schemaInput.setupProperties();
        props.schemaInput.schema.setValue(MarketoConstants.getListOperationSOAPSchema());
        props.connection.setupLayout();
        props.schemaInput.setupLayout();
        props.setupLayout();
        props.updateOutputSchemas();

        return props;
    }

    public TMarketoListOperationProperties getRESTProperties() {
        TMarketoListOperationProperties props = new TMarketoListOperationProperties("test");
        props.connection.setupProperties();
        props.connection.endpoint.setValue(ENDPOINT_REST);
        props.connection.clientAccessId.setValue(USERID_REST);
        props.connection.secretKey.setValue(SECRETKEY_REST);
        props.connection.apiMode.setValue(REST);
        props.setupProperties();
        props.schemaInput.setupProperties();
        props.connection.setupLayout();
        props.schemaInput.setupLayout();
        props.setupLayout();
        props.schemaInput.schema.setValue(MarketoConstants.getListOperationRESTSchema());
        props.updateOutputSchemas();

        return props;
    }

    public MarketoListOperationWriter getWriter(TMarketoListOperationProperties properties) {
        MarketoSink sink = new MarketoSink();
        sink.initialize(null, properties);
        return (MarketoListOperationWriter) sink.createWriteOperation().createWriter(null);
    }

    @Test
    public void testIsMemberOfSOAP() throws Exception {
        props = getSOAPProperties();
        props.listOperation.setValue(ListOperation.isMemberOf);
        //
        Schema s = props.schemaInput.schema.getValue();
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, "MKTOLISTNAME");
        record.put(1, UNDX_TEST_LIST_SMALL);
        record.put(2, "IDNUM");
        // record.put(3, 2767254);
        record.put(3, createdLeads.get(0));
        //
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        assertEquals(1, writer.result.getTotalCount());
        assertEquals(1, writer.result.getSuccessCount());
        assertEquals(0, writer.result.getRejectCount());
        assertEquals(1, writer.result.getApiCalls());
    }

    @Test
    public void testIsMemberOfSOAPFail() throws Exception {
        props = getSOAPProperties();
        props.listOperation.setValue(ListOperation.isMemberOf);
        //
        Schema s = props.schemaInput.schema.getValue();
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, "MKTOLISTNAME");
        record.put(1, UNDX_TEST_LIST_SMALL);
        record.put(2, "IDNUM");
        // record.put(3, 2767254);
        record.put(3, "27672544444444");
        //
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        assertEquals(1, writer.result.getTotalCount());
        assertEquals(0, writer.result.getSuccessCount());
        assertEquals(1, writer.result.getRejectCount());
        assertEquals(1, writer.result.getApiCalls());
    }

    @Test
    public void testIsMemberOfREST() throws Exception {
        props = getRESTProperties();
        props.listOperation.setValue(ListOperation.isMemberOf);
        //
        Schema s = props.schemaInput.schema.getValue();
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, UNDX_TEST_LIST_SMALL_ID);
        record.put(1, createdLeads.get(0));
        //
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        assertEquals(1, writer.result.getTotalCount());
        assertEquals(1, writer.result.getSuccessCount());
        assertEquals(0, writer.result.getRejectCount());
        assertEquals(1, writer.result.getApiCalls());
    }

    @Test
    public void testAddToSOAP() throws Exception {
        props = getSOAPProperties();
        props.listOperation.setValue(ListOperation.addTo);
        //
        Schema s = props.schemaInput.schema.getValue();
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, "MKTOLISTNAME");
        record.put(1, UNDX_TEST_LIST_SMALL);
        record.put(2, "IDNUM");
        // record.put(3, 2767254);
        record.put(3, createdLeads.get(9));
        //
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        assertEquals(1, writer.result.getTotalCount());
        assertEquals(1, writer.result.getSuccessCount());
        assertEquals(0, writer.result.getRejectCount());
        //
        props.listOperation.setValue(ListOperation.isMemberOf);
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        for (IndexedRecord success : writer.getSuccessfulWrites()) {
            assertTrue((Boolean) success.get(props.schemaFlow.schema.getValue().getField(FIELD_SUCCESS).pos()));
        }
        assertEquals(1, writer.result.getTotalCount());
        assertEquals(1, writer.result.getSuccessCount());
        assertEquals(0, writer.result.getRejectCount());
        assertEquals(1, writer.result.getApiCalls());
    }

    @Test
    public void testAddToREST() throws Exception {
        props = getRESTProperties();
        props.listOperation.setValue(ListOperation.addTo);
        //
        Schema s = props.schemaInput.schema.getValue();
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, UNDX_TEST_LIST_SMALL_ID);
        record.put(1, createdLeads.get(9));
        //
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        assertEquals(1, writer.result.getTotalCount());
        assertEquals(1, writer.result.getSuccessCount());
        assertEquals(0, writer.result.getRejectCount());
        //
        props.listOperation.setValue(ListOperation.isMemberOf);
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        for (IndexedRecord success : writer.getSuccessfulWrites()) {
            assertEquals("memberof", success.get(props.schemaFlow.schema.getValue().getField(FIELD_STATUS).pos()));
        }
        assertEquals(1, writer.result.getTotalCount());
        assertEquals(1, writer.result.getSuccessCount());
        assertEquals(0, writer.result.getRejectCount());
        assertEquals(1, writer.result.getApiCalls());
    }

    @Test
    public void testRemoveFromSOAP() throws Exception {
        props = getSOAPProperties();
        props.listOperation.setValue(ListOperation.removeFrom);
        //
        Schema s = props.schemaInput.schema.getValue();
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, "MKTOLISTNAME");
        record.put(1, UNDX_TEST_LIST_SMALL);
        record.put(2, "IDNUM");
        // record.put(3, 2767254);
        record.put(3, createdLeads.get(4));
        //
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        assertEquals(1, writer.result.getTotalCount());
        assertEquals(1, writer.result.getSuccessCount());
        assertEquals(0, writer.result.getRejectCount());
        //
        props.listOperation.setValue(ListOperation.isMemberOf);
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        for (IndexedRecord success : writer.getSuccessfulWrites()) {
            assertFalse((Boolean) success.get(props.schemaFlow.schema.getValue().getField(FIELD_SUCCESS).pos()));
        }
        assertEquals(1, writer.result.getTotalCount());
        assertEquals(1, writer.result.getSuccessCount());
        assertEquals(0, writer.result.getRejectCount());
        assertEquals(1, writer.result.getApiCalls());
    }

    @Test
    public void testRemoveFromREST() throws Exception {
        props = getRESTProperties();
        props.listOperation.setValue(ListOperation.removeFrom);
        //
        Schema s = props.schemaInput.schema.getValue();
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, UNDX_TEST_LIST_SMALL_ID);
        record.put(1, createdLeads.get(4));
        //
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        writer.close();
        assertEquals(1, writer.result.getTotalCount());
        assertEquals(1, writer.result.getSuccessCount());
        assertEquals(0, writer.result.getRejectCount());
        //
        props.listOperation.setValue(ListOperation.isMemberOf);
        writer = getWriter(props);
        writer.open("test");
        writer.write(record);
        for (IndexedRecord success : writer.getSuccessfulWrites()) {
            assertEquals("notmemberof", success.get(props.schemaFlow.schema.getValue().getField(FIELD_STATUS).pos()));
        }
        Result result = writer.close();
        LOG.debug("resultMap = {}.", result);
        assertEquals(1, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getRejectCount());
        assertEquals(1, result.toMap().get(MarketoComponentDefinition.RETURN_NB_CALL));
    }

    @Test
    public void testMultipleOperationsSOAP() throws Exception {
        props = getSOAPProperties();
        props.listOperation.setValue(ListOperation.addTo);
        props.multipleOperation.setValue(true);
        //
        Schema s = props.schemaInput.schema.getValue();
        writer = getWriter(props);
        writer.open("test");

        List<IndexedRecord> testRecords = new ArrayList<>();
        for (Integer leadid : createdLeads) {

            IndexedRecord record = new GenericData.Record(s);
            record.put(0, "MKTOLISTNAME");
            record.put(1, UNDX_TEST_LIST_SMALL);
            record.put(2, "IDNUM");
            // record.put(3, 2767254);
            record.put(3, leadid);
            //
            testRecords.add(record);
            //
            writer.write(record);
        }
        writer.close();
        assertEquals(TEST_NB_LEADS, writer.result.getTotalCount());
        assertEquals(TEST_NB_LEADS, writer.result.getSuccessCount() + writer.result.getRejectCount());
        //
        props.listOperation.setValue(ListOperation.isMemberOf);
        writer = getWriter(props);
        writer.open("test");
        for (IndexedRecord r : testRecords) {
            writer.write(r);
            for (IndexedRecord success : writer.getSuccessfulWrites()) {
                assertTrue((Boolean) success.get(props.schemaFlow.schema.getValue().getField(FIELD_SUCCESS).pos()));
            }
        }
        writer.close();
        assertEquals(TEST_NB_LEADS, writer.result.getTotalCount());
        assertEquals(TEST_NB_LEADS, writer.result.getSuccessCount() + writer.result.getRejectCount());
    }

    @Test
    public void testMultipleOperationsREST() throws Exception {
        props = getRESTProperties();
        props.listOperation.setValue(ListOperation.addTo);
        props.multipleOperation.setValue(true);
        //
        Schema s = props.schemaInput.schema.getValue();
        writer = getWriter(props);
        writer.open("test");

        List<IndexedRecord> testRecords = new ArrayList<>();
        for (Integer leadid : createdLeads) {
            IndexedRecord record = new GenericData.Record(s);
            record.put(0, UNDX_TEST_LIST_SMALL_ID);
            record.put(1, leadid);
            //
            testRecords.add(record);
            //
            writer.write(record);
        }
        writer.close();
        assertEquals(TEST_NB_LEADS, writer.result.getTotalCount());
        assertEquals(TEST_NB_LEADS, writer.result.getSuccessCount() + writer.result.getRejectCount());
        //
        props.listOperation.setValue(ListOperation.isMemberOf);
        writer = getWriter(props);
        writer.open("test");
        for (IndexedRecord r : testRecords) {
            writer.write(r);
            for (IndexedRecord success : writer.getSuccessfulWrites()) {
                assertEquals("memberof", success.get(props.schemaFlow.schema.getValue().getField(FIELD_STATUS).pos()));
            }
        }
        writer.close();
        assertEquals(TEST_NB_LEADS, writer.result.getTotalCount());
        assertEquals(TEST_NB_LEADS, writer.result.getSuccessCount() + writer.result.getRejectCount());
    }
}
