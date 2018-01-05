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

package org.talend.components.kinesis.integration;

import static org.talend.components.kinesis.integration.KinesisTestConstants.getDatastore;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.kinesis.KinesisDatastoreDefinition;
import org.talend.components.kinesis.KinesisDatastoreProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class KinesisDatastoreRuntimeTestIT {

    private final KinesisDatastoreDefinition def = new KinesisDatastoreDefinition();

    @Before
    public void init() {
        Assume.assumeTrue(getDatastore().specifyCredentials.getValue());
    }

    @Test
    public void doHealthChecks() {

        KinesisDatastoreProperties props = getDatastore();
        RuntimeInfo ri = def.getRuntimeInfo(props);
        try (SandboxedInstance si = RuntimeUtil.createRuntimeClass(ri, getClass().getClassLoader())) {
            DatastoreRuntime runtime = (DatastoreRuntime) si.getInstance();
            runtime.initialize(null, props);
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
    }

}
