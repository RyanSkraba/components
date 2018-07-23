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
package org.talend.components.marketo.tmarketocampaign;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.marketo.MarketoComponentDefinition;
import org.talend.daikon.exception.TalendRuntimeException;

public class TMarketoCampaignDefinitionTest {

    TMarketoCampaignDefinition def;

    TMarketoCampaignProperties prop;

    @Before
    public void setUp() throws Exception {
        def = new TMarketoCampaignDefinition();
        prop = new TMarketoCampaignProperties("test");
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        assertThat(def.getRuntimeInfo(ExecutionEngine.DI, prop, ConnectorTopology.OUTGOING).getRuntimeClassName(),
                is(MarketoComponentDefinition.RUNTIME_SOURCE_CLASS));
        assertThat(def.getRuntimeInfo(ExecutionEngine.DI, prop, ConnectorTopology.INCOMING).getRuntimeClassName(),
                is(MarketoComponentDefinition.RUNTIME_SINK_CLASS));
        assertThat(def.getRuntimeInfo(ExecutionEngine.DI, prop, ConnectorTopology.INCOMING_AND_OUTGOING).getRuntimeClassName(),
                is(MarketoComponentDefinition.RUNTIME_SINK_CLASS));
        try {
            assertThat(def.getRuntimeInfo(ExecutionEngine.DI, prop, ConnectorTopology.NONE).getRuntimeClassName(),
                    is(MarketoComponentDefinition.RUNTIME_SINK_CLASS));
            fail("ConnectorTopology.NONE not valid - Must have a connector.");
        } catch (TalendRuntimeException e) {
        }
    }

    @Test
    public void testGetSupportedConnectorTopologies() throws Exception {
        assertThat(def.getSupportedConnectorTopologies(), containsInAnyOrder(ConnectorTopology.INCOMING,
                ConnectorTopology.OUTGOING, ConnectorTopology.INCOMING_AND_OUTGOING));
    }

    @Test
    public void testGetPropertyClass() throws Exception {
        assertThat(def.getPropertyClass().getCanonicalName(), is(TMarketoCampaignProperties.class.getCanonicalName()));
    }
}
