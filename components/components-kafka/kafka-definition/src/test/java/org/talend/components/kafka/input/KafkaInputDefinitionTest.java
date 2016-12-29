// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.kafka.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.daikon.runtime.RuntimeInfo;

public class KafkaInputDefinitionTest {

    KafkaInputDefinition definition;

    @Before
    public void reset() {
        definition = new KafkaInputDefinition();
    }

    @Test
    public void getPropertyClass() throws Exception {
        assertTrue(KafkaInputProperties.class.isAssignableFrom(definition.getPropertyClass()));
    }

    @Test
    public void getSupportedConnectorTopologies() throws Exception {
        Set<ConnectorTopology> supportedConnectorTopologies = definition.getSupportedConnectorTopologies();
        assertEquals(1, supportedConnectorTopologies.size());
        assertTrue(supportedConnectorTopologies.contains(ConnectorTopology.OUTGOING));
    }

    @Test
    public void getRuntimeInfo() throws Exception {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.BEAM, null, null);
        assertNotNull(runtimeInfo);
    }

}
