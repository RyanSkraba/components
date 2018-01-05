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

import static org.junit.Assert.assertTrue;
import static org.talend.components.kinesis.integration.KinesisTestConstants.getDatasetForListStreams;
import static org.talend.components.kinesis.integration.KinesisTestConstants.getDatastore;

import java.util.Set;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.kinesis.KinesisDatasetDefinition;
import org.talend.components.kinesis.KinesisDatasetProperties;
import org.talend.components.kinesis.KinesisRegion;
import org.talend.components.kinesis.runtime.IKinesisDatasetRuntime;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class KinesisDatasetRuntimeTestIT {

    private final KinesisDatasetDefinition def = new KinesisDatasetDefinition();

    @Before
    public void init() {
        Assume.assumeTrue(getDatastore().specifyCredentials.getValue());
    }

    // Can't use localstack to list streams by region
    @Test
    public void listStreams() {
        KinesisDatasetProperties props = getDatasetForListStreams(getDatastore(), KinesisRegion.DEFAULT, null);
        RuntimeInfo ri = def.getRuntimeInfo(props);
        try (SandboxedInstance si = RuntimeUtil.createRuntimeClass(ri, getClass().getClassLoader())) {
            IKinesisDatasetRuntime runtime = (IKinesisDatasetRuntime) si.getInstance();
            runtime.initialize(null, props);
            Set<String> streams = runtime.listStreams();
            assertTrue(true);// no exception is ok
        }
    }

}
