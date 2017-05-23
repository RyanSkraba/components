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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties.CampaignAction.get;
import static org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties.CampaignAction.getById;
import static org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties.CampaignAction.schedule;
import static org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties.CampaignAction.trigger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoComponentProperties;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.MarketoBaseTestIT;
import org.talend.components.marketo.runtime.MarketoSource;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties;

public class MarketoCampaignClientTestIT extends MarketoBaseTestIT {

    public static final int MY_LEAD_ID = 4137184;

    public static final Integer TRIGGER_CAMPAIGN = 23269;

    public static final Integer BATCH_CAMPAIGN = 23270;

    TMarketoCampaignProperties iprops;

    Schema s;

    @Before
    public void setUp() throws Exception {

        iprops = new TMarketoCampaignProperties("test");
        iprops.connection.setupProperties();
        iprops.connection.endpoint.setValue(ENDPOINT_REST);
        iprops.connection.clientAccessId.setValue(USERID_REST);
        iprops.connection.secretKey.setValue(SECRETKEY_REST);
        iprops.schemaInput.setupProperties();
        iprops.setupProperties();
        iprops.connection.setupLayout();
        iprops.schemaInput.setupLayout();
        iprops.setupLayout();
        iprops.batchSize.setValue(300);
        //
        s = MarketoConstants.getCampaignSchema();
    }

    public MarketoRESTClient getClient(MarketoComponentProperties props) throws IOException {
        MarketoSource source = new MarketoSource();
        source.initialize(null, props);
        return (MarketoRESTClient) source.getClientService(null);
    }

    public void testRecord(IndexedRecord record) {
        assertNotNull(record.get(s.getField("id").pos()));
        assertTrue(record.get(s.getField("id").pos()) instanceof Integer);
        assertNotNull(record.get(s.getField("name").pos()));
        assertTrue(record.get(s.getField("name").pos()) instanceof String);
        assertNotNull(record.get(s.getField("createdAt").pos()));
        assertTrue(record.get(s.getField("createdAt").pos()) instanceof Date);
        assertNotNull(record.get(s.getField("updatedAt").pos()));
        assertTrue(record.get(s.getField("updatedAt").pos()) instanceof Date);
        if (record.get(s.getField("active").pos()).toString().equals("true")) {
            LOG.debug("record = {}.", record);
        }
    }

    @Test
    public void testGetAllCampaigns() throws Exception {
        iprops.campaignAction.setValue(get);
        iprops.afterCampaignAction();
        int rCount = 0;
        MarketoRESTClient client = getClient(iprops);
        MarketoRecordResult cmp = client.getCampaigns(iprops, null);
        rCount = cmp.getRecordCount();
        for (IndexedRecord r : cmp.getRecords()) {
            testRecord(r);
        }
        while (cmp.getStreamPosition() != null) {
            cmp = client.getCampaigns(iprops, cmp.getStreamPosition());
            for (IndexedRecord r : cmp.getRecords()) {
                testRecord(r);
            }
            rCount += cmp.getRecordCount();
        }
        assertTrue(rCount > 300);
    }

    @Test
    public void testGetCampaignsByIds() throws Exception {
        iprops.campaignAction.setValue(get);
        iprops.campaignIds.setValue("1003,1004,1005");
        iprops.afterCampaignAction();
        MarketoRecordResult cmp = getClient(iprops).getCampaigns(iprops, null);
        assertEquals(3, cmp.getRecordCount());
        for (IndexedRecord r : cmp.getRecords()) {
            testRecord(r);
        }
    }

    @Test
    public void testGetCampaignsByNames() throws Exception {
        iprops.campaignAction.setValue(get);
        iprops.campaignNames.setValue("Registrant,No_show");
        iprops.afterCampaignAction();
        MarketoRecordResult cmp = getClient(iprops).getCampaigns(iprops, null);
        assertTrue(cmp.getRecordCount() >= 2);
        for (IndexedRecord r : cmp.getRecords()) {
            testRecord(r);
        }
    }

    @Test
    public void testGetCampaignsByProgramNames() throws Exception {
        iprops.campaignAction.setValue(get);
        iprops.programNames.setValue("DI_FR_Y12Q01_VirtualRoadshow2");
        iprops.afterCampaignAction();
        MarketoRecordResult cmp = getClient(iprops).getCampaigns(iprops, null);
        assertTrue(cmp.getRecordCount() >= 1);
        for (IndexedRecord r : cmp.getRecords()) {
            testRecord(r);
        }
    }

    @Test
    public void testGetCampaignsByWorkspaceNames() throws Exception {
        iprops.campaignAction.setValue(get);
        iprops.workspaceNames.setValue("DI_FR_Y12Q01_VirtualRoadshow2");
        iprops.afterCampaignAction();
        MarketoRecordResult cmp = getClient(iprops).getCampaigns(iprops, null);
        assertEquals(0, cmp.getRecordCount());
        //
        iprops.workspaceNames.setValue("Default");
        iprops.afterCampaignAction();
        cmp = getClient(iprops).getCampaigns(iprops, null);
        assertTrue(cmp.getRecordCount() >= 1);
        for (IndexedRecord r : cmp.getRecords()) {
            testRecord(r);
        }
        assertEquals(300, cmp.getRemainCount());
    }

    @Test
    public void testGetCampaignById() throws Exception {
        iprops.campaignAction.setValue(getById);
        iprops.campaignId.setValue(TRIGGER_CAMPAIGN);
        iprops.afterCampaignAction();
        MarketoRecordResult cmp = getClient(iprops).getCampaignById(iprops);
        assertEquals(1, cmp.getRecordCount());
        assertEquals(0, cmp.getRemainCount());
        for (IndexedRecord r : cmp.getRecords()) {
            testRecord(r);
        }
    }

    @Test
    public void testGetCampaignByIdNotFound() throws Exception {
        iprops.campaignAction.setValue(getById);
        iprops.campaignId.setValue(0);
        iprops.afterCampaignAction();
        MarketoRecordResult cmp = getClient(iprops).getCampaignById(iprops);
        assertEquals(0, cmp.getRecordCount());
        assertEquals(0, cmp.getRemainCount());
        assertEquals(1, cmp.getErrors().size());
        assertEquals("Campaign not found", cmp.getErrors().get(0).getMessage());
    }

    /*
     *
     * Schedule Campaigns
     *
     */
    @Test
    public void testScheduleCampaign() throws Exception {
        iprops.campaignAction.setValue(schedule);
        iprops.campaignId.setValue(BATCH_CAMPAIGN);
        iprops.afterCampaignAction();
        MarketoRecordResult rs = getClient(iprops).scheduleCampaign(iprops);
        LOG.debug("[testScheduleCampaign] {}", rs);
        assertTrue(rs.isSuccess());
        assertEquals(BATCH_CAMPAIGN, rs.getRecords().get(0).get(0));
    }

    @Test
    public void testScheduleCampaignFail() throws Exception {
        iprops.campaignAction.setValue(schedule);
        iprops.campaignId.setValue(TRIGGER_CAMPAIGN);
        iprops.afterCampaignAction();
        MarketoRecordResult rs = getClient(iprops).scheduleCampaign(iprops);
        LOG.debug("[testScheduleCampaign] {}", rs);
        assertFalse(rs.isSuccess());
        assertEquals("1003", rs.getErrors().get(0).getCode());
    }

    @Test
    public void testScheduleCampaignWithRunAt() throws Exception {
        iprops.campaignAction.setValue(schedule);
        iprops.campaignId.setValue(BATCH_CAMPAIGN);
        iprops.runAt.setValue(new Date(GregorianCalendar.getInstance().getTimeInMillis() + 1000000));
        iprops.afterCampaignAction();
        MarketoRecordResult rs = getClient(iprops).scheduleCampaign(iprops);
        LOG.debug("[testScheduleCampaign] {}", rs);
        assertTrue(rs.isSuccess());
        assertEquals(BATCH_CAMPAIGN, rs.getRecords().get(0).get(0));
    }

    @Test
    public void testScheduleCampaignWithTokens() throws Exception {
        iprops.campaignAction.setValue(schedule);
        iprops.campaignId.setValue(BATCH_CAMPAIGN);
        List<String> tnames = Arrays.asList("{{my.undx Token one}}", "{{my.undx Token two}}");
        List<String> tvalues = Arrays.asList("BATCH___TalendMktoTests", "197365");
        iprops.campaignTokens.tokenName.setValue(tnames);
        iprops.campaignTokens.tokenValue.setValue(tvalues);
        iprops.afterCampaignAction();
        MarketoRecordResult rs = getClient(iprops).scheduleCampaign(iprops);
        LOG.debug("[testScheduleCampaign] {}", rs);
        assertTrue(rs.isSuccess());
        assertEquals(BATCH_CAMPAIGN, rs.getRecords().get(0).get(0));
    }

    @Test
    public void testScheduleCampaignWithClone() throws Exception {
        iprops.campaignAction.setValue(schedule);
        iprops.campaignId.setValue(BATCH_CAMPAIGN);
        // TODO when program management will be implemented delete clone...
        iprops.cloneToProgramName.setValue("undx_clone_program" + GregorianCalendar.getInstance().getTimeInMillis());
        iprops.afterCampaignAction();
        MarketoRecordResult rs = getClient(iprops).scheduleCampaign(iprops);
        LOG.debug("[testScheduleCampaign] {}", rs);
        assertTrue(rs.isSuccess());
        // cloned program is greater than campaign...
        assertTrue(BATCH_CAMPAIGN < Integer.parseInt(rs.getRecords().get(0).get(0).toString()));
    }

    @Test
    public void testScheduleCampaignCloneFail() throws Exception {
        iprops.campaignAction.setValue(schedule);
        iprops.campaignId.setValue(BATCH_CAMPAIGN);
        iprops.cloneToProgramName.setValue("undx_test_program");
        iprops.afterCampaignAction();
        MarketoRecordResult rs = getClient(iprops).scheduleCampaign(iprops);
        LOG.debug("[testScheduleCampaign] {}", rs);
        assertFalse(rs.isSuccess());
        assertEquals("{[611] System error}", rs.getErrorsString());
    }

    /*
     *
     * Trigger Campaigns
     *
     */
    @Test
    public void testTriggerCampaign() throws Exception {
        iprops.campaignAction.setValue(trigger);
        iprops.campaignId.setValue(TRIGGER_CAMPAIGN);
        iprops.afterCampaignAction();
        // {{my.undx Token two}}
        // {{my.undx Token one:default=undx token 1}}
        Schema s = MarketoConstants.triggerCampaignSchema();
        IndexedRecord r0;
        r0 = new Record(s);
        r0.put(0, MY_LEAD_ID);
        MarketoSyncResult rs = getClient(iprops).triggerCampaign(iprops, Collections.singletonList(r0));
        LOG.debug("[testTriggerCampaign] {}", rs);
        assertTrue(rs.isSuccess());
        assertEquals(TRIGGER_CAMPAIGN, rs.getRecords().get(0).getId());
    }

    @Test
    public void testTriggerCampaignWithTokens() throws Exception {
        iprops.campaignAction.setValue(trigger);
        iprops.campaignId.setValue(TRIGGER_CAMPAIGN);
        List<String> tnames = Arrays.asList("{{my.undx Token one}}", "{{my.undx Token two}}");
        List<String> tvalues = Arrays.asList("TRIGGER__TalendMktoTests", "99998656565565");
        iprops.campaignTokens.tokenName.setValue(tnames);
        iprops.campaignTokens.tokenValue.setValue(tvalues);
        iprops.afterCampaignAction();
        Schema s = MarketoConstants.triggerCampaignSchema();
        IndexedRecord r0;
        r0 = new Record(s);
        r0.put(0, MY_LEAD_ID);
        MarketoSyncResult rs = getClient(iprops).triggerCampaign(iprops, Collections.singletonList(r0));
        LOG.debug("[testTriggerCampaign] {}", rs);
        assertTrue(rs.isSuccess());
        assertEquals(TRIGGER_CAMPAIGN, rs.getRecords().get(0).getId());
    }

    @Test
    public void testTriggerCampaignWithInvlidTokens() throws Exception {
        iprops.campaignAction.setValue(trigger);
        iprops.campaignId.setValue(TRIGGER_CAMPAIGN);
        List<String> tnames = Arrays.asList("{{my.undxTokenone}}", "{{my.undxTokentwo}}");
        List<String> tvalues = Arrays.asList("FAILED__TalendMktoTests", "99998656565565");
        iprops.campaignTokens.tokenName.setValue(tnames);
        iprops.campaignTokens.tokenValue.setValue(tvalues);
        iprops.afterCampaignAction();
        Schema s = MarketoConstants.triggerCampaignSchema();
        IndexedRecord r0;
        r0 = new Record(s);
        r0.put(0, MY_LEAD_ID);
        MarketoSyncResult rs = getClient(iprops).triggerCampaign(iprops, Collections.singletonList(r0));
        LOG.debug("[testTriggerCampaign] {}", rs);
        assertFalse(rs.isSuccess());
        assertEquals("{[1003] Invalid token value: [\"undxTokenone\",\"undxTokentwo\"] }", rs.getErrorsString());
    }

    @Test
    public void testTriggerCampaignFail() throws Exception {
        iprops.campaignAction.setValue(trigger);
        iprops.campaignId.setValue(TRIGGER_CAMPAIGN);
        iprops.afterCampaignAction();

        Schema s = MarketoConstants.triggerCampaignSchema();
        IndexedRecord r0, r1, r2;
        r0 = new Record(s);
        r0.put(0, MY_LEAD_ID);
        r1 = new Record(s);
        r1.put(0, 84);
        r2 = new Record(s);
        r2.put(0, 85);
        MarketoSyncResult rs = getClient(iprops).triggerCampaign(iprops, Arrays.asList(r0, r1, r2));
        LOG.debug("[testTriggerCampaign] {}", rs);
        assertFalse(rs.isSuccess());
        assertEquals("{[1004] Lead [84, 85] not found}", rs.getErrorsString());
    }

}
