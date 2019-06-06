// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties;

import com.google.gson.JsonObject;

public class MarketoCampaignClientTest extends MarketoLeadClientTest {

    TMarketoCampaignProperties props;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        props = new TMarketoCampaignProperties("test");
        props.schemaInput.setupProperties();
        props.schemaInput.setupLayout();
        props.connection.setupProperties();
        props.connection.setupLayout();
        props.connection.setupProperties();
        props.connection.endpoint.setValue("https://fake.io/rest");
        props.connection.clientAccessId.setValue("clientaccess");
        props.connection.secretKey.setValue("sekret");
        props.connection.attemptsIntervalTime.setValue(200); // shorten interval for tests
        props.setupProperties();
        props.setupLayout();
        props.campaignId.setValue(12345);
        props.cloneToProgramName.setValue("cloned");
        props.campaignIds.setValue("1,32");
        props.campaignNames.setValue("c1,c2");
        props.programNames.setValue("one,two");
        props.workspaceNames.setValue("wrk1");
        props.runAt.setValue(new Date());
        List<String> tnames = Arrays.asList("{{my.undx Token one}}", "{{my.undx Token two}}");
        List<String> tvalues = Arrays.asList("BATCH___TalendMktoTests", "197365");
        props.campaignTokens.tokenName.setValue(tnames);
        props.campaignTokens.tokenValue.setValue(tvalues);

    }

    @Test
    public void testGetCampaigns() throws Exception {
        doThrow(new MarketoException("REST", "error")).when(client).executeGetRequest(any(Schema.class));
        mktoRR = client.getCampaigns(props, "testoff");
        assertFalse(mktoRR.isSuccess());
        assertFalse(mktoRR.getErrorsString().isEmpty());
    }

    @Test
    public void testGetCampaignById() throws Exception {
        MarketoRecordResult mrr = new MarketoRecordResult();
        mrr.setSuccess(true);
        mrr.setRemainCount(0);
        mrr.setRecordCount(1);
        doReturn(mrr).when(client).executeGetRequest(any(Schema.class));
        mktoRR = client.getCampaignById(props);
        assertTrue(mktoRR.isSuccess());
        assertTrue(mktoRR.getErrorsString().isEmpty());
    }

    @Test
    public void testScheduleCampaign() throws Exception {
        doThrow(new MarketoException("REST", "error")).when(client).executePostRequest(eq(SyncResult.class),
                any(JsonObject.class));
        mktoRR = client.scheduleCampaign(props);
        assertFalse(mktoRR.isSuccess());
        assertFalse(mktoRR.getErrorsString().isEmpty());

        doReturn(new SyncResult()).when(client).executePostRequest(eq(SyncResult.class), any(JsonObject.class));
        mktoRR = client.scheduleCampaign(props);
        assertFalse(mktoRR.isSuccess());

        doReturn(getListOperationResult(true, "deleted")).when(client).executePostRequest(eq(SyncResult.class),
                any(JsonObject.class));
        mktoRR = client.scheduleCampaign(props);
        assertTrue(mktoRR.isSuccess());
        assertTrue(mktoRR.getErrorsString().isEmpty());
    }

    @Test
    public void testTriggerCampaign() throws Exception {
        //
        doThrow(new MarketoException("REST", "error")).when(client).executePostRequest(eq(SyncResult.class),
                any(JsonObject.class));
        List<IndexedRecord> records = new ArrayList<>();
        Schema s = MarketoConstants.triggerCampaignSchema();
        IndexedRecord record;
        record = new Record(s);
        record.put(0, "123456");
        records.add(record);
        mktoSR = client.requestCampaign(props, records);
        assertFalse(mktoSR.isSuccess());
        assertFalse(mktoSR.getErrorsString().isEmpty());
        //
        doReturn(new SyncResult()).when(client).executePostRequest(eq(SyncResult.class), any(JsonObject.class));
        mktoSR = client.requestCampaign(props, records);
        assertFalse(mktoSR.isSuccess());
        //
        doReturn(getListOperationResult(true, "deleted")).when(client).executePostRequest(eq(SyncResult.class),
                any(JsonObject.class));
        mktoSR = client.requestCampaign(props, records);
        assertTrue(mktoSR.isSuccess());
        assertTrue(mktoSR.getErrorsString().isEmpty());
    }
}
