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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties;

public class MarketoCampaignReaderTest extends MarketoRuntimeTestBase {

    MarketoCampaignReader reader;

    TMarketoCampaignProperties props;

    MarketoSource source;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        props = new TMarketoCampaignProperties("test");
        props.connection.setupProperties();
        props.setupProperties();

        source = mock(MarketoSource.class);
        source.initialize(null, props);
        when(source.getClientService(any())).thenReturn(client);
        when(source.createReader(null)).thenReturn(new MarketoCampaignReader(null, source, props));
        reader = (MarketoCampaignReader) source.createReader(null);
        assertTrue(reader instanceof MarketoCampaignReader);

        when(client.getCampaigns(any(TMarketoCampaignProperties.class), any(String.class)))
                .thenReturn(getFailedRecordResult("REST", "", "error"));
    }

    @Test
    public void testGetCurrent() throws Exception {
        when(client.getCampaigns(any(), any())).thenReturn(getLeadRecordResult(true));
        assertTrue(reader.start());
        assertNotNull(reader.getCurrent());
        assertTrue(reader.advance());
        assertNotNull(reader.getCurrent());
        when(client.getCampaigns(any(), any()))
                .thenReturn(getLeadRecordResult(false));
        assertTrue(reader.advance());
        assertNotNull(reader.getCurrent());
        assertTrue(reader.advance());
        assertNotNull(reader.getCurrent());
        assertFalse(reader.advance());
        assertNotNull(reader.getReturnValues());
    }

}
