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
package org.talend.components.kafka.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.daikon.runtime.RuntimeInfo;

public class KafkaOutputDefinitionTest {

    KafkaOutputDefinition definition;

    @Before
    public void reset() {
        definition = new KafkaOutputDefinition();
    }

    @Test
    public void getPropertyClass() throws Exception {
        assertTrue(KafkaOutputProperties.class.isAssignableFrom(definition.getPropertyClass()));
    }

    @Test
    public void getSupportedConnectorTopologies() throws Exception {
        assertEquals(1, definition.getSupportedConnectorTopologies().size());
        assertTrue(definition.getSupportedConnectorTopologies().contains(ConnectorTopology.INCOMING));
    }

    @Test
    public void getRuntimeInfo() throws Exception {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.BEAM, null, null);
        assertNotNull(runtimeInfo);
    }

}
