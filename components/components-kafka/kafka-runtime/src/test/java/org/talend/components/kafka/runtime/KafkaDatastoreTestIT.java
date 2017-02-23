// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.kafka.runtime;

import static org.junit.Assert.assertEquals;
import static org.talend.components.kafka.runtime.KafkaTestConstants.BOOTSTRAP_HOST;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.kafka.datastore.KafkaDatastoreProperties;
import org.talend.daikon.properties.ValidationResult;

public class KafkaDatastoreTestIT {

    Logger LOG = LoggerFactory.getLogger(KafkaDatastoreTestIT.class);

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
    @Ignore("Temp fix to unlock others")
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

        datastoreProperties.brokers.setValue(BOOTSTRAP_HOST);
        Iterable<ValidationResult> correctValidationResultIter = runtime.doHealthChecks(null);
        List<ValidationResult> correctValidationResults = new ArrayList<>();
        for (ValidationResult validationResult : correctValidationResultIter) {
            correctValidationResults.add(validationResult);
        }
        assertEquals(1, correctValidationResults.size());
        assertEquals(ValidationResult.Result.OK, correctValidationResults.get(0).getStatus());
    }

}
