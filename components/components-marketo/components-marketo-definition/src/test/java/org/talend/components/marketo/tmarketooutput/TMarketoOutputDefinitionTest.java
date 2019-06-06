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
package org.talend.components.marketo.tmarketooutput;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties;

public class TMarketoOutputDefinitionTest {

    TMarketoOutputDefinition def;

    @Before
    public void setup() {
        def = new TMarketoOutputDefinition();
    }

    @Test
    public void testGetPropertyClass() throws Exception {
        assertEquals(TMarketoOutputProperties.class, def.getPropertyClass());
    }

    @Test
    public void testGetSupportedConnectorTopologies() throws Exception {
        assertEquals(new HashSet<ConnectorTopology>(Arrays.asList(ConnectorTopology.INCOMING,
                ConnectorTopology.INCOMING_AND_OUTGOING, ConnectorTopology.NONE, ConnectorTopology.OUTGOING)),
                def.getSupportedConnectorTopologies());
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING));
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING_AND_OUTGOING));
    }

    @Test
    public void testNotStartable() {
        assertFalse(def.isStartable());
    }

    @Test
    public void testGetNestedCompatibleComponentPropertiesClass() {
        Class<? extends ComponentProperties>[] nestedCompatibleComponentPropertiesClass;
        nestedCompatibleComponentPropertiesClass = def.getNestedCompatibleComponentPropertiesClass();
        assertEquals(2, nestedCompatibleComponentPropertiesClass.length);
        assertArrayEquals(nestedCompatibleComponentPropertiesClass,
                new Class[] { TMarketoConnectionProperties.class, MarketoComponentWizardBaseProperties.class });
    }

    @Test
    public void testIsConditionalInputs() throws Exception {
        assertTrue(def.isConditionalInputs());
    }

}
