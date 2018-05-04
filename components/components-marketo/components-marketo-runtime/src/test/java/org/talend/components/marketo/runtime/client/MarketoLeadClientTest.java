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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode.REST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.MarketoUtils;
import org.talend.components.marketo.runtime.MarketoRuntimeTestBase;
import org.talend.components.marketo.runtime.client.rest.response.DescribeFieldsResult;
import org.talend.components.marketo.runtime.client.rest.response.LeadActivitiesResult;
import org.talend.components.marketo.runtime.client.rest.response.LeadChangesResult;
import org.talend.components.marketo.runtime.client.rest.response.LeadResult;
import org.talend.components.marketo.runtime.client.rest.response.StaticListResult;
import org.talend.components.marketo.runtime.client.rest.response.SyncResult;
import org.talend.components.marketo.runtime.client.rest.type.FieldDescription;
import org.talend.components.marketo.runtime.client.rest.type.LeadActivityRecord;
import org.talend.components.marketo.runtime.client.rest.type.LeadChangeRecord;
import org.talend.components.marketo.runtime.client.rest.type.ListRecord;
import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;
import org.talend.components.marketo.runtime.client.type.ListOperationParameters;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.IncludeExcludeFieldsREST;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadKeyTypeREST;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.ListParam;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.daikon.avro.AvroUtils;

import com.google.gson.JsonObject;

public class MarketoLeadClientTest extends MarketoRuntimeTestBase {

    TMarketoInputProperties iprops;

    TMarketoOutputProperties oprops;

    TMarketoListOperationProperties lprops;

    MarketoRecordResult mktoRR;

    MarketoSyncResult mktoSR;

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

        lprops = new TMarketoListOperationProperties("test");
        lprops.schemaInput.setupProperties();
        lprops.schemaInput.setupLayout();
        lprops.connection.setupProperties();
        lprops.connection.setupLayout();
        lprops.connection.setupProperties();
        lprops.connection.endpoint.setValue("https://fake.io/rest");
        lprops.connection.clientAccessId.setValue("clientaccess");
        lprops.connection.secretKey.setValue("sekret");
        lprops.connection.attemptsIntervalTime.setValue(200); // shorten interval for tests
        lprops.setupProperties();
        lprops.setupLayout();

        client = spy(new MarketoRESTClient(iprops.connection));
        doNothing().when(client).getToken();
        doReturn("000123456").when(client).getPageToken(anyString());
        client.connect();
    }

    @Test
    public void testGetApi() throws Exception {
        assertEquals("REST", client.getApi());
    }

    @Test
    public void testToString() throws Exception {
        assertTrue(client.toString().contains("Marketo REST API Client"));
    }

    @Test
    public void testGetActivityTypeNameById() throws Exception {
        assertEquals("Unsubscribe Email", client.getActivityTypeNameById(9));
    }

    @Test
    public void testGetLocalActivityTypes() throws Exception {
        assertEquals(52, client.getLocalActivityTypes().size());
    }

    @Test
    public void testGetListIdByName() throws Exception {
        doReturn(null).when(client).executeGetRequest(StaticListResult.class);
        try {
            client.getListIdByName("test_list");
            fail("Should not be here");
        } catch (MarketoException e) {
        }
        StaticListResult rr = new StaticListResult();
        rr.setSuccess(true);
        ListRecord lr = new ListRecord();
        lr.setId(666);
        rr.setResult(Arrays.asList(lr));
        doReturn(rr).when(client).executeGetRequest(StaticListResult.class);
        assertEquals(Integer.valueOf(666), client.getListIdByName("test_list"));
    }

    public LeadResult getLeadResult() {
        LeadResult lr = new LeadResult();
        lr.setSuccess(true);
        List<Map<String, String>> kv = new ArrayList<>();
        Map<String, String> lv = new HashMap<>();
        lv.put("id", "12345");
        lv.put("firstName", "testFN");
        lv.put("lastName", "testLN");
        lv.put("attrName", "attrValue");
        kv.add(lv);
        lr.setResult(kv);
        return lr;
    }

    @Test
    public void testGetLead() throws Exception {
        iprops.inputOperation.setValue(InputOperation.getLead);
        iprops.leadKeyTypeREST.setValue(LeadKeyTypeREST.id);
        iprops.leadKeyValue.setValue("12345");
        iprops.afterInputOperation();
        Field attr = new Field("attrName", AvroUtils._string(), "", null);
        iprops.schemaInput.schema
                .setValue(MarketoUtils.newSchema(iprops.schemaInput.schema.getValue(), "test", Collections.singletonList(attr)));
        iprops.beforeMappingInput();
        //
        doThrow(new MarketoException("REST", "error")).when(client).executeFakeGetRequestForLead(anyString());
        mktoRR = client.getLead(iprops, null);
        assertFalse(mktoRR.isSuccess());
        assertFalse(mktoRR.getErrorsString().isEmpty());
        //
        doReturn(new LeadResult()).when(client).executeFakeGetRequestForLead(anyString());
        mktoRR = client.getLead(iprops, null);
        assertFalse(mktoRR.isSuccess());
        //
        doReturn(getLeadResult()).when(client).executeFakeGetRequestForLead(anyString());
        mktoRR = client.getLead(iprops, null);
        assertTrue(mktoRR.isSuccess());
        List<IndexedRecord> records = mktoRR.getRecords();
        assertNotNull(records);
        IndexedRecord record = records.get(0);
        assertNotNull(record);
        Schema refSchema = iprops.schemaInput.schema.getValue();
        assertEquals(refSchema, record.getSchema());
        assertEquals(12345, record.get(refSchema.getField("id").pos()));
        assertEquals("testFN", record.get(refSchema.getField("firstName").pos()));
        assertEquals("testLN", record.get(refSchema.getField("lastName").pos()));
        assertEquals("attrValue", record.get(refSchema.getField("attrName").pos()));
    }

    @Test
    public void testGetMultipleLeads() throws Exception {
        iprops.inputOperation.setValue(InputOperation.getMultipleLeads);
        iprops.leadKeyTypeREST.setValue(LeadKeyTypeREST.id);
        iprops.leadKeyValues.setValue("12345");
        iprops.afterInputOperation();
        //
        doThrow(new MarketoException("REST", "error")).when(client).executeFakeGetRequestForLead(anyString());
        mktoRR = client.getMultipleLeads(iprops, null);
        assertFalse(mktoRR.isSuccess());
        assertFalse(mktoRR.getErrorsString().isEmpty());
        //
        doReturn(new LeadResult()).when(client).executeFakeGetRequestForLead(anyString());
        mktoRR = client.getMultipleLeads(iprops, null);
        assertFalse(mktoRR.isSuccess());
        //
        doReturn(getLeadResult()).when(client).executeFakeGetRequestForLead(anyString());
        mktoRR = client.getMultipleLeads(iprops, null);
        assertTrue(mktoRR.isSuccess());
        //
        iprops.leadSelectorREST.setValue(LeadSelector.StaticListSelector);
        iprops.listParam.setValue(ListParam.STATIC_LIST_ID);
        iprops.listParamListId.setValue(666123);
        mktoRR = client.getMultipleLeads(iprops, null);
        assertTrue(mktoRR.isSuccess());
    }

    @Test
    public void testGetLeadActivity() throws Exception {
        iprops.inputOperation.setValue(InputOperation.getLeadActivity);
        iprops.leadKeyTypeREST.setValue(LeadKeyTypeREST.id);
        iprops.leadKeyValues.setValue("12345");
        iprops.includeTypes.type.setValue(Arrays.asList(IncludeExcludeFieldsREST.AddToList.name()));
        iprops.afterInputOperation();
        List<Field> moreFields = new ArrayList<>();
        moreFields.add(new Field("attrName", AvroUtils._string(), "", null));
        moreFields.add(new Field("campaignId", AvroUtils._int(), "", null));
        iprops.schemaInput.schema.setValue(MarketoUtils.newSchema(iprops.schemaInput.schema.getValue(), "test", moreFields));
        iprops.beforeMappingInput();
        //
        doThrow(new MarketoException("REST", "error")).when(client).executeGetRequest(LeadActivitiesResult.class);
        mktoRR = client.getLeadActivity(iprops, null);
        assertFalse(mktoRR.isSuccess());
        assertFalse(mktoRR.getErrorsString().isEmpty());
        //
        doReturn(new LeadActivitiesResult()).when(client).executeGetRequest(LeadActivitiesResult.class);
        mktoRR = client.getLeadActivity(iprops, null);
        assertFalse(mktoRR.isSuccess());
        //
        LeadActivitiesResult lar = new LeadActivitiesResult();
        lar.setSuccess(true);
        List<LeadActivityRecord> lars = new ArrayList<>();
        LeadActivityRecord larecord = new LeadActivityRecord();
        larecord.setId(123456);
        larecord.setMarketoGUID("ABC-123-DEF");
        larecord.setLeadId(12345);
        larecord.setActivityTypeId(1);
        larecord.setActivityTypeValue("Visit Webpage");
        larecord.setPrimaryAttributeValue("changed");
        larecord.setPrimaryAttributeValueId(123);
        larecord.setActivityDate(new Date());
        larecord.setCampaignId(456);
        List<Map<String, Object>> attributes = new ArrayList<>();
        Map<String, Object> attribute = new HashMap<>();
        attribute.put("name", "attrName");
        attribute.put("value", "attrValue");
        attributes.add(attribute);
        larecord.setAttributes(attributes);
        lars.add(larecord);
        lar.setResult(lars);
        doReturn(lar).when(client).executeGetRequest(LeadActivitiesResult.class);
        mktoRR = client.getLeadActivity(iprops, null);
        assertTrue(mktoRR.isSuccess());
        List<IndexedRecord> records = mktoRR.getRecords();
        assertNotNull(records);
        IndexedRecord record = records.get(0);
        assertNotNull(record);
        Schema refSchema = iprops.schemaInput.schema.getValue();
        assertEquals(refSchema, record.getSchema());
        assertEquals(123456, record.get(refSchema.getField("id").pos()));
        assertEquals("ABC-123-DEF", record.get(refSchema.getField("marketoGUID").pos()));
        assertEquals(12345, record.get(refSchema.getField("leadId").pos()));
        assertEquals(1, record.get(refSchema.getField("activityTypeId").pos()));
        assertEquals("Visit Webpage", record.get(refSchema.getField("activityTypeValue").pos()));
        assertEquals(123, record.get(refSchema.getField("primaryAttributeValueId").pos()));
        assertEquals(456, record.get(refSchema.getField("campaignId").pos()));
        assertEquals("changed", record.get(refSchema.getField("primaryAttributeValue").pos()));
        assertTrue(record.get(refSchema.getField("activityDate").pos()) instanceof Long);
        assertEquals("attrValue", record.get(refSchema.getField("attrName").pos()));
    }

    @Test
    public void testGetLeadChanges() throws Exception {
        iprops.inputOperation.setValue(InputOperation.getLeadChanges);
        iprops.sinceDateTime.setValue("2017-01-30 10:00:00 +0100");
        iprops.afterInputOperation();
        List<Field> moreFields = new ArrayList<>();
        moreFields.add(new Field("attrName", AvroUtils._string(), "", null));
        moreFields.add(new Field("campaignId", AvroUtils._int(), "", null));
        iprops.schemaInput.schema.setValue(MarketoUtils.newSchema(iprops.schemaInput.schema.getValue(), "test", moreFields));
        iprops.beforeMappingInput();
        //
        doThrow(new MarketoException("REST", "error")).when(client).executeGetRequest(LeadChangesResult.class);
        mktoRR = client.getLeadChanges(iprops, null);
        assertFalse(mktoRR.isSuccess());
        assertFalse(mktoRR.getErrorsString().isEmpty());
        //
        doReturn(new LeadChangesResult()).when(client).executeGetRequest(LeadChangesResult.class);
        mktoRR = client.getLeadChanges(iprops, null);
        assertFalse(mktoRR.isSuccess());
        //
        LeadChangesResult lcr = new LeadChangesResult();
        lcr.setSuccess(true);
        List<LeadChangeRecord> lcrs = new ArrayList<>();
        LeadChangeRecord lc = new LeadChangeRecord();
        lc.setId(123456);
        lc.setMarketoGUID("ABC-123-DEF");
        lc.setLeadId(12345);
        lc.setActivityTypeId(1);
        lc.setActivityTypeValue("Visit Webpage");
        lc.setActivityDate(new Date());
        lc.setCampaignId(456);
        List<Map<String, String>> fields = new ArrayList<>();
        Map<String, String> field = new HashMap<>();
        field.put("field1", "value1");
        fields.add(field);
        lc.setFields(fields);
        List<Map<String, Object>> attributes = new ArrayList<>();
        Map<String, Object> attribute = new HashMap<>();
        attribute.put("name", "attrName");
        attribute.put("value", "attrValue");
        attributes.add(attribute);
        lc.setAttributes(attributes);
        lcrs.add(lc);
        lcr.setResult(lcrs);
        //
        doReturn(lcr).when(client).executeGetRequest(LeadChangesResult.class);
        mktoRR = client.getLeadChanges(iprops, null);
        assertTrue(mktoRR.isSuccess());
        List<IndexedRecord> records = mktoRR.getRecords();
        assertNotNull(records);
        IndexedRecord record = records.get(0);
        assertNotNull(record);
        Schema refSchema = iprops.schemaInput.schema.getValue();
        assertEquals(refSchema, record.getSchema());
        assertEquals(123456, record.get(refSchema.getField("id").pos()));
        assertEquals("ABC-123-DEF", record.get(refSchema.getField("marketoGUID").pos()));
        assertEquals(12345, record.get(refSchema.getField("leadId").pos()));
        assertEquals(1, record.get(refSchema.getField("activityTypeId").pos()));
        assertEquals("Visit Webpage", record.get(refSchema.getField("activityTypeValue").pos()));
        assertEquals(456, record.get(refSchema.getField("campaignId").pos()));
        assertEquals("[{\"field1\":\"value1\"}]", record.get(refSchema.getField("fields").pos()));
        assertTrue(record.get(refSchema.getField("activityDate").pos()) instanceof Long);
        assertEquals("attrValue", record.get(refSchema.getField("attrName").pos()));
    }

    public SyncResult getListOperationResult(boolean isSuccess, String status) {
        SyncResult result = new SyncResult();
        List<SyncStatus> records = new ArrayList<>();
        if (isSuccess) {
            result.setSuccess(true);
            SyncStatus ss = new SyncStatus();
            ss.setSeq(0);
            ss.setMarketoGUID("mkto-123456");
            ss.setStatus(status);
            ss.setErrorMessage("");
            records.add(ss);
            result.setResult(records);
        } else {
            result.setSuccess(false);
            result.setErrors(Arrays.asList(new MarketoError("REST", "errorlist")));
        }
        return result;
    }

    @Test
    public void testListOperations() throws Exception {
        lprops.afterListOperation();
        ListOperationParameters parms = new ListOperationParameters();
        parms.setApiMode(REST.name());
        parms.setListId(666123);
        parms.setLeadIds(new Integer[] { 12345 });
        //
        doThrow(new MarketoException("REST", "error")).when(client).executePostRequest(eq(SyncResult.class),
                any(JsonObject.class));
        mktoSR = client.addToList(parms);
        assertFalse(mktoSR.isSuccess());
        assertFalse(mktoSR.getErrorsString().isEmpty());
        //
        doReturn(getListOperationResult(false, "skipped")).when(client).executePostRequest(eq(SyncResult.class),
                any(JsonObject.class));
        mktoSR = client.removeFromList(parms);
        assertFalse(mktoSR.isSuccess());
        assertNotNull(mktoSR.getErrorsString());
        //
        doReturn(getListOperationResult(true, "removed")).when(client).executePostRequest(eq(SyncResult.class),
                any(JsonObject.class));
        mktoSR = client.removeFromList(parms);
        assertTrue(mktoSR.isSuccess());
        //
        doReturn(getListOperationResult(true, "memberof")).when(client).executeGetRequest(eq(SyncResult.class));
        mktoSR = client.isMemberOfList(parms);
        assertTrue(mktoSR.isSuccess());
        assertNotNull(mktoSR.getRecords().get(0));
    }

    @Test
    public void testSyncLead() throws Exception {
        oprops.afterOutputOperation();
        oprops.beforeMappingInput();
        IndexedRecord record = new Record(MarketoConstants.getRESTOutputSchemaForSyncLead());
        record.put(0, 12345);
        record.put(1, "t@t.com");
        //
        doThrow(new MarketoException("REST", "error")).when(client).executePostRequest(eq(SyncResult.class),
                any(JsonObject.class));
        mktoSR = client.syncLead(oprops, record);
        assertFalse(mktoSR.isSuccess());
        assertFalse(mktoSR.getErrorsString().isEmpty());
        //
        doReturn(new SyncResult()).when(client).executePostRequest(eq(SyncResult.class), any(JsonObject.class));
        mktoSR = client.syncLead(oprops, record);
        assertFalse(mktoSR.isSuccess());
        //
        SyncResult sr = new SyncResult();
        sr.setSuccess(true);
        List<SyncStatus> ssr = new ArrayList<>();
        SyncStatus ss = new SyncStatus();
        ss.setStatus("created");
        ss.setMarketoGUID("mkto-123456");
        ss.setSeq(0);
        ss.setId(12345);
        ss.setErrorMessage("");
        ssr.add(ss);
        sr.setResult(ssr);
        doReturn(sr).when(client).executePostRequest(eq(SyncResult.class), any(JsonObject.class));
        mktoSR = client.syncLead(oprops, record);
        assertTrue(mktoSR.isSuccess());
        assertTrue(mktoSR.getErrorsString().isEmpty());
    }

    @Test
    public void testGetAllLeadFields() throws Exception {
        doThrow(new MarketoException("REST", "error")).when(client).executeGetRequest(DescribeFieldsResult.class);
        List<Field> result = client.getAllLeadFields();
        assertTrue(result.isEmpty());
        //
        doReturn(new DescribeFieldsResult()).when(client).executeGetRequest(DescribeFieldsResult.class);
        result = client.getAllLeadFields();
        assertTrue(result.isEmpty());
        //
        DescribeFieldsResult dfr = new DescribeFieldsResult();
        dfr.setSuccess(true);
        List<FieldDescription> fields = new ArrayList<>();
        FieldDescription fd = new FieldDescription();
        fd.setName("test");
        fd.setDataType("string");
        fd.setDisplayName("test");
        fd.setId(124566);
        fd.setLength(10);
        fd.setUpdateable(true);
        fields.add(fd);
        dfr.setResult(fields);
        doReturn(dfr).when(client).executeGetRequest(DescribeFieldsResult.class);
        result = client.getAllLeadFields();
        assertFalse(result.isEmpty());
    }

    @Test
    public void testDeleteLeads() throws Exception {
        IndexedRecord record = new Record(MarketoConstants.getDeleteLeadsSchema());
        record.put(0, 12345);
        //
        doThrow(new MarketoException("REST", "error")).when(client).executePostRequest(eq(SyncResult.class),
                any(JsonObject.class));
        mktoSR = client.deleteLeads(new Integer[] { 12345 });
        mktoSR = client.deleteLeads(Arrays.asList(record));
        assertFalse(mktoSR.isSuccess());
        assertFalse(mktoSR.getErrorsString().isEmpty());
        //
        doReturn(new SyncResult()).when(client).executePostRequest(eq(SyncResult.class), any(JsonObject.class));
        mktoSR = client.deleteLeads(new Integer[] { 12345 });
        assertFalse(mktoSR.isSuccess());
        //
        SyncResult sr = new SyncResult();
        sr.setSuccess(true);
        List<SyncStatus> ssr = new ArrayList<>();
        SyncStatus ss = new SyncStatus();
        ss.setStatus("created");
        ss.setMarketoGUID("mkto-123456");
        ss.setSeq(0);
        ss.setId(12345);
        ss.setErrorMessage("");
        ssr.add(ss);
        sr.setResult(ssr);
        doReturn(sr).when(client).executePostRequest(eq(SyncResult.class), any(JsonObject.class));
        mktoSR = client.deleteLeads(new Integer[] { 12345 });
        assertTrue(mktoSR.isSuccess());
        assertTrue(mktoSR.getErrorsString().isEmpty());
    }

    @Test
    public void testIsAvailable() throws Exception {
        assertFalse(client.isAvailable());
    }

}
