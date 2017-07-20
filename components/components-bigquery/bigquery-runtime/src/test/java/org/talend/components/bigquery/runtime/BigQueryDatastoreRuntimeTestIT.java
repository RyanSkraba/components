package org.talend.components.bigquery.runtime;

import static org.junit.Assert.assertEquals;
import static org.talend.components.bigquery.runtime.BigQueryTestConstants.createDatastore;

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.properties.ValidationResult;

public class BigQueryDatastoreRuntimeTestIT {

    BigQueryDatastoreRuntime runtime;

    @Before
    public void reset() {
        runtime = new BigQueryDatastoreRuntime();
    }

    @Test
    public void doHealthChecksTest() {
        runtime.initialize(null, createDatastore());
        Iterable<ValidationResult> validationResults = runtime.doHealthChecks(null);
        assertEquals(ValidationResult.OK, validationResults.iterator().next());
    }

}
