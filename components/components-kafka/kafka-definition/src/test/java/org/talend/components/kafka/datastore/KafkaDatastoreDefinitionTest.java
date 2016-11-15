package org.talend.components.kafka.datastore;

import static org.junit.Assert.*;

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
    public void getPropertiesTest() throws Exception {
        assertEquals(KafkaDatastoreProperties.class, definition.getPropertiesClass());
    }

    @Test
    public void getRuntimeInfo() throws Exception {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(null, null);
        assertNotNull(runtimeInfo);
    }

    @Test
    public void createDatasetProperties() throws Exception {
        KafkaDatastoreProperties properties = new KafkaDatastoreProperties("");
        DatasetProperties datasetProperties = definition.createDatasetProperties(properties);
        assertNotNull(datasetProperties);
        assertEquals(properties, datasetProperties.getDatastoreProperties());
    }

}