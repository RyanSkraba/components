package org.talend.components.kafka.input;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
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
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(null, null);
        assertNotNull(runtimeInfo);
    }

}
