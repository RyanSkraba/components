package org.talend.components.kafka.runtime;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.kafka.datastore.KafkaDatastoreProperties;
import org.talend.daikon.properties.ValidationResult;

public class KafkaDatastoreTest extends KafkaTestBase {

    KafkaDatastoreProperties datastoreProperties;

    KafkaDatastoreRuntime runtime;

    @Before
    public void init() throws TimeoutException {
        datastoreProperties = new KafkaDatastoreProperties("datastoreProperties");
        datastoreProperties.init();
        runtime = new KafkaDatastoreRuntime();
        runtime.initialize(null, datastoreProperties);
    }

    @Test
    public void doHealthChecksForRuntimeEmpty() throws Exception {
        datastoreProperties.brokers.setValue("");
        Iterable<ValidationResult> emptyValidationResultIter = runtime.doHealthChecks(null);
        List<ValidationResult> emptyValidationResults = new ArrayList<>();
        for (ValidationResult validationResult : emptyValidationResultIter) {
            emptyValidationResults.add(validationResult);
        }
        assertEquals(1, emptyValidationResults.size());
        assertEquals(ValidationResult.Result.ERROR, emptyValidationResults.get(0).getStatus());
        assertEquals("Bootstrap server urls should not be empty", emptyValidationResults.get(0).getMessage());

        datastoreProperties.brokers.setValue("wronghost:1");
        Iterable<ValidationResult> wrongValidationResultIter = runtime.doHealthChecks(null);
        List<ValidationResult> wrongValidationResults = new ArrayList<>();
        for (ValidationResult validationResult : wrongValidationResultIter) {
            wrongValidationResults.add(validationResult);
        }
        assertEquals(1, wrongValidationResults.size());
        assertEquals(ValidationResult.Result.ERROR, wrongValidationResults.get(0).getStatus());

        datastoreProperties.brokers.setValue("localhost:5001");
        Iterable<ValidationResult> correctValidationResultIter = runtime.doHealthChecks(null);
        List<ValidationResult> correctValidationResults = new ArrayList<>();
        for (ValidationResult validationResult : correctValidationResultIter) {
            correctValidationResults.add(validationResult);
        }
        assertEquals(1, correctValidationResults.size());
        assertEquals(ValidationResult.Result.OK, correctValidationResults.get(0).getStatus());
    }

    @Test
    public void doHealthChecksForProperties() throws Exception {
        datastoreProperties.brokers.setValue("");
        ValidationResult emptyValidationResult = datastoreProperties.validateTestConnection();
        assertEquals(ValidationResult.Result.ERROR, emptyValidationResult.getStatus());
        assertEquals("Bootstrap server urls should not be empty", emptyValidationResult.getMessage());

        datastoreProperties.brokers.setValue("wronghost:1");
        ValidationResult wrongValidationResult = datastoreProperties.validateTestConnection();
        assertEquals(ValidationResult.Result.ERROR, wrongValidationResult.getStatus());

        datastoreProperties.brokers.setValue("localhost:5001");
        ValidationResult correctValidationResult = datastoreProperties.validateTestConnection();
        assertEquals(ValidationResult.Result.OK, correctValidationResult.getStatus());
    }

}
