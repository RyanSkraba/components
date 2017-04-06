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
package org.talend.components.marketo.tmarketobulkexec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.daikon.exception.TalendRuntimeException;

public class TMarketoBulkExecDefinitionTest {

    TMarketoBulkExecDefinition def;

    @Before
    public void setUp() throws Exception {
        def = new TMarketoBulkExecDefinition();
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.NONE));
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.OUTGOING));
    }

    @Test(expected = TalendRuntimeException.class)
    public void testGetRuntimeInfoFailIncoming() throws Exception {
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING));
    }

    @Test(expected = TalendRuntimeException.class)
    public void testGetRuntimeInfoFailIncomingOutgoing() throws Exception {
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING_AND_OUTGOING));
    }

    @Test
    public void testGetSupportedConnectorTopologies() throws Exception {
        assertEquals(EnumSet.of(ConnectorTopology.NONE, ConnectorTopology.OUTGOING), def.getSupportedConnectorTopologies());
    }

    @Test
    public void testGetPropertyClass() throws Exception {
        assertEquals(TMarketoBulkExecProperties.class, def.getPropertyClass());
    }

}
