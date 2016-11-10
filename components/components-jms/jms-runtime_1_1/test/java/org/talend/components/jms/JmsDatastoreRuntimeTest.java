package org.talend.components.jms;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.jms.runtime_1_1.JmsDatastoreRuntime;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JmsDatastoreRuntimeTest {
    private final JmsDatastoreRuntime datastoreRuntime = new JmsDatastoreRuntime();

    /**
     * Check {@link JmsDatastoreRuntime#doHealthChecks(RuntimeContainer)}
     * returns //TODO
     */
    @Test
    public void testDoHealthChecks() {
        JmsDatastoreProperties props = new JmsDatastoreProperties("test");
        props.serverUrl.setValue("tcp://localhost:61616");
        datastoreRuntime.initialize(null,props);
        Iterable<ValidationResult> healthResult = datastoreRuntime.doHealthChecks(null);
        assertEquals(Arrays.asList(ValidationResult.OK), healthResult);
    }

    /**
     * Check {@link JmsDatastoreRuntime#initialize(RuntimeContainer, Properties)}
     * Returns OK
     */
    @Test
    public void testInitialize() {
        ValidationResult result = datastoreRuntime.initialize(null, null);
        assertEquals(ValidationResult.OK, result);
    }

    /**
     * Check {@link JmsDatastoreRuntime#getPossibleDatasetNames(RuntimeContainer, String)}
     * Returns // TODO
     */
    /*@Test
    public void testGetPossibleDatasetNames() {
        List<NamedThing> datasetListResult = new ArrayList();
        try {
            datasetListResult = datastoreRuntime.getPossibleDatasetNames(null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(null, datasetListResult);
    }*/
}
