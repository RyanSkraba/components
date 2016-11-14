package org.talend.components.kafka.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.daikon.runtime.RuntimeInfo;

public class KafkaDatastoreDefinitionTest {

    KafkaDatastoreDefinition definition;

    @Before
    public void reset() {
        definition = new KafkaDatastoreDefinition();
    }

    @Test
    public void testBasic() {
        assertEquals("KafkaDatastore", definition.getName());
    }

    @Test
    public void createProperties() throws Exception {
        KafkaDatastoreProperties properties = definition.createProperties();
        assertNotNull(properties);
    }

    @Test
    public void getRuntimeInfo() throws Exception {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(null, null);
        assertNotNull(runtimeInfo);
    }

    @Test
    public void createDatasetProperties() throws Exception {
        KafkaDatastoreProperties properties = definition.createProperties();
        DatasetProperties datasetProperties = definition.createDatasetProperties(properties);
        assertNotNull(datasetProperties);
        assertEquals(properties, datasetProperties.getDatastoreProperties());
    }

}