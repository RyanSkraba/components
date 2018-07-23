// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.runtime.client.rest.type.SyncStatus;
import org.talend.components.marketo.runtime.client.type.MarketoSyncResult;
import org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties;
import org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties.CampaignAction;

public class MarketoCampaignWriterTest extends MarketoRuntimeTestBase {

    MarketoCampaignWriter writer;

    MarketoWriteOperation wop;

    TMarketoCampaignProperties props;

    IndexedRecord record;

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
        props.campaignAction.setValue(CampaignAction.trigger);
        props.afterCampaignAction();

        when(sink.getProperties()).thenReturn(props);

        wop = new MarketoWriteOperation(sink);
        writer = new MarketoCampaignWriter(wop, null);
        writer.properties = props;
        assertTrue(writer instanceof MarketoCampaignWriter);

        record = new Record(MarketoConstants.triggerCampaignSchema());
        record.put(0, "12345");

    }

    @Test
    public void testOpen() throws Exception {
        writer.open("test");
        assertNotNull(writer.close());
    }

    @Test
    public void testWrite() throws Exception {
        MarketoSyncResult msr = new MarketoSyncResult();
        msr.setSuccess(true);
        List<SyncStatus> rr = new ArrayList<>();
        SyncStatus ss = new SyncStatus();
        ss.setErrorMessage("");
        ss.setStatus("success");
        ss.setSeq(0);
        ss.setId(123);
        rr.add(ss);
        msr.setRecords(rr);
        doReturn(msr).when(client).requestCampaign(any(TMarketoCampaignProperties.class), any(List.class));
        writer.open("test");
        writer.write(null);
        writer.write(record);
    }

    @Test
    public void testProcessResult() throws Exception {
    }

    @Test
    public void testFillRecord() throws Exception {
    }
}
