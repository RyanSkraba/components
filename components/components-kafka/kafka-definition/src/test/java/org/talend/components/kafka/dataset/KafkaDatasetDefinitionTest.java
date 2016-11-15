package org.talend.components.kafka.dataset;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.runtime.RuntimeInfo;

public class KafkaDatasetDefinitionTest {

    KafkaDatasetDefinition definition;

    @Before
    public void reset() {
        definition = new KafkaDatasetDefinition();
    }

    @Test
    public void getPropertiesTest() throws Exception {
        assertEquals(KafkaDatasetProperties.class, definition.getPropertiesClass());
    }

    @Test
    public void getRuntimeInfo() throws Exception {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(null, null);
        assertNotNull(runtimeInfo);
    }

}
