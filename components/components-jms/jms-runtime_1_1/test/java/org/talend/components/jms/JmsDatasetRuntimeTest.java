package org.talend.components.jms;

import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jms.runtime_1_1.JmsDatasetRuntime;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.runtime.RuntimeInfo;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class JmsDatasetRuntimeTest {

    private final JmsDatasetRuntime datasetRuntime = new JmsDatasetRuntime();
    /*

     * Check {@link JmsDatasetRuntime#getEndpointSchema(RuntimeContainer)}}
     * returns null in the jms case


    @Test
    public void testGetEndpointSchema() {
        try {
            Schema schema = datasetRuntime.getEndpointSchema(null);
            assertEquals(null, schema);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     * Check {@link JmsDatasetRuntime#initialize(RuntimeContainer, ComponentProperties)}
     * Returns OK


    @Test
    public void initialize() {
        ValidationResult result = datasetRuntime.initialize(null, null);
        assertEquals(ValidationResult.OK, result);
    }
    */
}
