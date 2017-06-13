package org.talend.components.pubsub.runtime;

import static org.junit.Assert.assertEquals;
import static org.talend.components.pubsub.runtime.PubSubTestConstants.createDatastore;

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.properties.ValidationResult;

public class PubSubDatastoreRuntimeTestIT {

    PubSubDatastoreRuntime runtime;

    @Before
    public void reset() {
        runtime = new PubSubDatastoreRuntime();
    }

    @Test
    public void doHealthChecksTest() {
        runtime.initialize(null, createDatastore());
        Iterable<ValidationResult> validationResults = runtime.doHealthChecks(null);
        assertEquals(ValidationResult.OK, validationResults.iterator().next());
    }

}
