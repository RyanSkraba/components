package org.talend.components.kafka.output;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
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
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(null, null);
        assertNotNull(runtimeInfo);
    }

}
