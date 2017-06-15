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
package org.talend.components.marketo.tmarketolistoperation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;

import java.util.HashSet;
import java.util.Set;

public class TMarketoListOperationDefinitionTest {

    TMarketoListOperationDefinition def;
    @Before
    public void setup() {
        def = new TMarketoListOperationDefinition();
    }

    @Test
    public void testGetPropertyClass() throws Exception {
        assertEquals(TMarketoListOperationProperties.class, def.getPropertyClass());
    }

    @Test
    public void testGetSupportedConnectorTopologies() throws Exception {
        Set<ConnectorTopology> set = new HashSet<>();
        set.add(ConnectorTopology.INCOMING);
        set.add(ConnectorTopology.INCOMING_AND_OUTGOING);
        assertEquals(set, def.getSupportedConnectorTopologies());
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING_AND_OUTGOING));
    }
    
    @Test
    public void testNotStartable() {
        assertFalse(def.isStartable());
    }

}