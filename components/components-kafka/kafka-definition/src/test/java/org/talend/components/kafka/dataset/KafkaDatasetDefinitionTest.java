package org.talend.components.kafka.dataset;

import static org.junit.Assert.assertNotNull;

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
    public void createProperties() throws Exception {
        KafkaDatasetProperties properties = definition.createProperties();
        assertNotNull(properties);
    }

    @Test
    public void getRuntimeInfo() throws Exception {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(null, null);
        assertNotNull(runtimeInfo);
    }

}
