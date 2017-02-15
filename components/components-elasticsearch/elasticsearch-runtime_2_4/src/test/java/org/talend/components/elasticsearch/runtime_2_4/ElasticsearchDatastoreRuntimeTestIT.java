package org.talend.components.elasticsearch.runtime_2_4;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.elasticsearch.ElasticsearchDatastoreProperties;
import org.talend.daikon.properties.ValidationResult;

import static org.junit.Assert.assertEquals;

public class ElasticsearchDatastoreRuntimeTestIT {

    ElasticsearchDatastoreRuntime runtime;

    @Before
    public void reset() {
        runtime = new ElasticsearchDatastoreRuntime();
    }

    @Test
    public void doHealthChecksTest() {
        runtime.initialize(null, createDatastoreProp());
        Iterable<ValidationResult> validationResults = runtime.doHealthChecks(null);
        assertEquals(ValidationResult.OK, validationResults.iterator().next());
    }

    public static ElasticsearchDatastoreProperties createDatastoreProp() {
        ElasticsearchDatastoreProperties datastore = new ElasticsearchDatastoreProperties("datastore");
        datastore.nodes.setValue(ElasticsearchTestConstants.HOSTS);
        return datastore;
    }
}
