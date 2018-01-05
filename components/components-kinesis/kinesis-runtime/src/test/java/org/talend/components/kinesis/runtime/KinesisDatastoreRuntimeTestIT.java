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

package org.talend.components.kinesis.runtime;

import static org.talend.components.kinesis.runtime.KinesisTestConstants.getDatastore;
import static org.talend.components.kinesis.runtime.KinesisTestConstants.getDatastoreForRoleAssume;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.kinesis.KinesisDatastoreProperties;
import org.talend.daikon.properties.ValidationResult;

public class KinesisDatastoreRuntimeTestIT {

    KinesisDatastoreRuntime runtime;

    @Before
    public void init() {
        Assume.assumeTrue(getDatastore().specifyCredentials.getValue());

        runtime = new KinesisDatastoreRuntime();
    }

    @Test
    public void doHealthChecks() {
        runtime.initialize(null, getDatastore());
        Iterable<ValidationResult> validationResults = runtime.doHealthChecks(null);
        Assert.assertEquals(ValidationResult.OK, validationResults.iterator().next());

        // Wrong access key
        {
            KinesisDatastoreProperties wrongAccess = getDatastore();
            wrongAccess.accessKey.setValue("wrong");
            runtime.initialize(null, wrongAccess);
            validationResults = runtime.doHealthChecks(null);
            Assert.assertEquals(ValidationResult.Result.ERROR, validationResults.iterator().next().getStatus());
        }

        // Wrong screct key
        {
            KinesisDatastoreProperties wrongSecret = getDatastore();
            wrongSecret.secretKey.setValue("wrong");
            runtime.initialize(null, wrongSecret);
            validationResults = runtime.doHealthChecks(null);
            Assert.assertEquals(ValidationResult.Result.ERROR, validationResults.iterator().next().getStatus());
        }
    }

    @Test
    public void doHealthChecksForRoleAssume() {
        runtime.initialize(null, getDatastoreForRoleAssume());
        Iterable<ValidationResult> validationResults = runtime.doHealthChecks(null);
        Assert.assertEquals(ValidationResult.OK, validationResults.iterator().next());

        // Wrong access key
        {
            KinesisDatastoreProperties wrongAccess = getDatastoreForRoleAssume();
            wrongAccess.accessKey.setValue("wrong");
            runtime.initialize(null, wrongAccess);
            validationResults = runtime.doHealthChecks(null);
            Assert.assertEquals(ValidationResult.Result.ERROR, validationResults.iterator().next().getStatus());
        }

        // Wrong screct key
        {
            KinesisDatastoreProperties wrongSecret = getDatastoreForRoleAssume();
            wrongSecret.secretKey.setValue("wrong");
            runtime.initialize(null, wrongSecret);
            validationResults = runtime.doHealthChecks(null);
            Assert.assertEquals(ValidationResult.Result.ERROR, validationResults.iterator().next().getStatus());
        }

        // Wrong arn
        {
            KinesisDatastoreProperties wrongSecret = getDatastoreForRoleAssume();
            wrongSecret.roleArn.setValue("wrong");
            runtime.initialize(null, wrongSecret);
            validationResults = runtime.doHealthChecks(null);
            Assert.assertEquals(ValidationResult.Result.ERROR, validationResults.iterator().next().getStatus());
        }

        // Wrong external id
        {
            KinesisDatastoreProperties wrongSecret = getDatastoreForRoleAssume();
            wrongSecret.roleExternalId.setValue("wrong");
            runtime.initialize(null, wrongSecret);
            validationResults = runtime.doHealthChecks(null);
            Assert.assertEquals(ValidationResult.Result.ERROR, validationResults.iterator().next().getStatus());
        }
    }

}
