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
package org.talend.components.marketo.tmarketoconnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;

public class TMarketoConnectionDefinitionTest {

    TMarketoConnectionDefinition def;

    private transient static final Logger LOG = LoggerFactory.getLogger(TMarketoConnectionDefinitionTest.class);

    @Before
    public void setup() {
        def = new TMarketoConnectionDefinition();
    }

    @Test
    public final void testGetPropertyClass() {
        assertEquals(def.getPropertyClass(), TMarketoConnectionProperties.class);
    }

    @Test
    public final void testTMarketoConnectionDefinition() {
        assertEquals("tMarketoConnection", TMarketoConnectionDefinition.COMPONENT_NAME);
    }

    @Test
    public final void testGetReturnProperties() {
        assertTrue(def.getReturnProperties().length == 2);
    }

    @Test
    public final void testGetRuntimeInfo() {
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.NONE));
        assertNull(def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING));
        assertNull(def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING_AND_OUTGOING));
        assertNull(def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.OUTGOING));
        //
    }

    @Test
    public final void testEnums() {
        assertEquals(APIMode.REST, APIMode.valueOf("REST"));
        assertEquals(APIMode.SOAP, APIMode.valueOf("SOAP"));
    }

    @Test
    public final void testGetSupportedConnectorTopologies() {
        assertEquals(ConnectorTopology.NONE, def.getSupportedConnectorTopologies().toArray()[0]);
    }

}
