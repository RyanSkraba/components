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
package org.talend.components.marketo.runtime.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.rest.response.SyncResult;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties.CustomObjectDeleteBy;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectSyncAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;

import com.google.gson.JsonObject;

public class MarketoCustomObjectClientTest extends MarketoLeadClientTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        iprops = new TMarketoInputProperties("test");
        iprops.schemaInput.setupProperties();
        iprops.schemaInput.setupLayout();
        iprops.connection.setupProperties();
        iprops.connection.setupLayout();
        iprops.connection.setupProperties();
        iprops.connection.endpoint.setValue("https://fake.io/rest");
        iprops.connection.clientAccessId.setValue("clientaccess");
        iprops.connection.secretKey.setValue("sekret");
        iprops.connection.attemptsIntervalTime.setValue(200); // shorten interval for tests
        iprops.setupProperties();
        iprops.setupLayout();
        iprops.customObjectName.setValue("car_c");
        iprops.inputOperation.setValue(InputOperation.CustomObject);

        oprops = new TMarketoOutputProperties("test");
        oprops.schemaInput.setupProperties();
        oprops.schemaInput.setupLayout();
        oprops.connection.setupProperties();
        oprops.connection.setupLayout();
        oprops.connection.setupProperties();
        oprops.connection.endpoint.setValue("https://fake.io/rest");
        oprops.connection.clientAccessId.setValue("clientaccess");
        oprops.connection.secretKey.setValue("sekret");
        oprops.connection.attemptsIntervalTime.setValue(200); // shorten interval for tests
        oprops.setupProperties();
        oprops.setupLayout();
        oprops.customObjectName.setValue("car_c");
    }

    public MarketoRecordResult getCustomObjectResult() {
        MarketoRecordResult cor = new MarketoRecordResult();
        cor.setSuccess(true);
        List<IndexedRecord> cos = new ArrayList<>();
        IndexedRecord co = new Record(MarketoConstants.getCustomObjectDescribeSchema());
        co.put(0, "car_c");
        co.put(1, "marketoGUID");
        co.put(2, "Car");
        co.put(3, "Car system");
        co.put(4, new Date());
        co.put(5, new Date());
        co.put(6, "");
        co.put(7, "{ \"brand\", \"model\" }");
        co.put(8, "{}");
        co.put(9, "{}");
        cos.add(co);
        cor.setRecords(cos);
        return cor;
    }

    @Test
    public void testDescribeCustomObject() throws Exception {
        iprops.customObjectAction.setValue(CustomObjectAction.describe);
        //
        doThrow(new MarketoException("REST", "error")).when(client).executeGetRequest(any(Schema.class));
        mktoRR = client.describeCustomObject(iprops);
        assertFalse(mktoRR.isSuccess());
        assertFalse(mktoRR.getErrorsString().isEmpty());
        //
        doReturn(new MarketoRecordResult()).when(client).executeGetRequest(any(Schema.class));
        mktoRR = client.describeCustomObject(iprops);
        assertFalse(mktoRR.isSuccess());
        assertTrue(mktoRR.getErrorsString().isEmpty());
        //
        doReturn(getCustomObjectResult()).when(client).executeGetRequest(any(Schema.class));
        mktoRR = client.describeCustomObject(iprops);
        assertTrue(mktoRR.isSuccess());
        assertTrue(mktoRR.getErrorsString().isEmpty());
    }

    @Test
    public void testListCustomObjects() throws Exception {
        iprops.customObjectAction.setValue(CustomObjectAction.list);
        doThrow(new MarketoException("REST", "error")).when(client).executeGetRequest(any(Schema.class));
        mktoRR = client.listCustomObjects(iprops);
        assertFalse(mktoRR.isSuccess());
        assertFalse(mktoRR.getErrorsString().isEmpty());
        //
        doReturn(new MarketoRecordResult()).when(client).executeGetRequest(any(Schema.class));
        mktoRR = client.listCustomObjects(iprops);
        assertFalse(mktoRR.isSuccess());
        assertTrue(mktoRR.getErrorsString().isEmpty());
        //
        doReturn(getCustomObjectResult()).when(client).executeGetRequest(any(Schema.class));
        mktoRR = client.listCustomObjects(iprops);
        assertTrue(mktoRR.isSuccess());
        assertTrue(mktoRR.getErrorsString().isEmpty());
    }

    @Test
    public void testGetCustomObjects() throws Exception {
        iprops.customObjectAction.setValue(CustomObjectAction.get);
        doThrow(new MarketoException("REST", "error")).when(client).executeGetRequest(any(Schema.class));
        mktoRR = client.getCustomObjects(iprops, null);
        assertFalse(mktoRR.isSuccess());
        assertFalse(mktoRR.getErrorsString().isEmpty());
        //
        doReturn(new MarketoRecordResult()).when(client).executeGetRequest(any(Schema.class));
        mktoRR = client.getCustomObjects(iprops, null);
        assertFalse(mktoRR.isSuccess());
        assertTrue(mktoRR.getErrorsString().isEmpty());
        //
        MarketoRecordResult mrr = new MarketoRecordResult();
        mrr.setSuccess(true);
        mrr.setRemainCount(0);
        mrr.setRecordCount(1);
        List<IndexedRecord> records = new ArrayList<>();
        IndexedRecord record = new Record(MarketoConstants.getCustomObjectRecordSchema());
        record.put(0, "mkto-123456");
        record.put(1, 0);
        record.put(2, new Date());
        record.put(3, new Date());
        records.add(record);
        mrr.setRecords(records);
        doReturn(mrr).when(client).executeGetRequest(any(Schema.class));
        mktoRR = client.getCustomObjects(iprops, null);
        assertTrue(mktoRR.isSuccess());
        assertTrue(mktoRR.getErrorsString().isEmpty());
    }

    @Test
    public void testSyncCustomObjects() throws Exception {
        oprops.customObjectSyncAction.setValue(CustomObjectSyncAction.createOrUpdate);
        oprops.customObjectDedupeBy.setValue("marketoGUID");
        //
        doThrow(new MarketoException("REST", "error")).when(client).executePostRequest(eq(SyncResult.class),
                any(JsonObject.class));
        List<IndexedRecord> records = new ArrayList<>();
        IndexedRecord record = new Record(MarketoConstants.getCustomObjectRecordSchema());
        record.put(0, "mkto-123456");
        records.add(record);
        mktoSR = client.syncCustomObjects(oprops, records);
        assertFalse(mktoSR.isSuccess());
        assertFalse(mktoSR.getErrorsString().isEmpty());
        //
        doReturn(new SyncResult()).when(client).executePostRequest(eq(SyncResult.class), any(JsonObject.class));
        mktoSR = client.syncCustomObjects(oprops, records);
        assertFalse(mktoSR.isSuccess());
        //
        doReturn(getListOperationResult(true, "deleted")).when(client).executePostRequest(eq(SyncResult.class),
                any(JsonObject.class));
        mktoSR = client.syncCustomObjects(oprops, records);
        assertTrue(mktoSR.isSuccess());
        assertTrue(mktoSR.getErrorsString().isEmpty());
    }

    @Test
    public void testDeleteCustomObjects() throws Exception {
        oprops.customObjectDeleteBy.setValue(CustomObjectDeleteBy.idField);
        //
        doThrow(new MarketoException("REST", "error")).when(client).executePostRequest(eq(SyncResult.class),
                any(JsonObject.class));
        List<IndexedRecord> records = new ArrayList<>();
        IndexedRecord record = new Record(MarketoConstants.getCustomObjectRecordSchema());
        record.put(0, "mkto-123456");
        records.add(record);
        mktoSR = client.deleteCustomObjects(oprops, records);
        assertFalse(mktoSR.isSuccess());
        assertFalse(mktoSR.getErrorsString().isEmpty());
        //
        doReturn(new SyncResult()).when(client).executePostRequest(eq(SyncResult.class), any(JsonObject.class));
        mktoSR = client.deleteCustomObjects(oprops, records);
        assertFalse(mktoSR.isSuccess());
        //
        doReturn(getListOperationResult(true, "deleted")).when(client).executePostRequest(eq(SyncResult.class),
                any(JsonObject.class));
        mktoSR = client.deleteCustomObjects(oprops, records);
        assertTrue(mktoSR.isSuccess());
        assertTrue(mktoSR.getErrorsString().isEmpty());
    }

}
