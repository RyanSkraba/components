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

package org.talend.components.kinesis;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.daikon.runtime.RuntimeInfo;

public class KinesisDatasetDefinitionTest {

    private final KinesisDatasetDefinition datasetDefinition = new KinesisDatasetDefinition();

    /**
     * Check {@link KinesisDatasetDefinition#getRuntimeInfo(KinesisDatasetProperties)} returns RuntimeInfo,
     * which runtime class name is "org.talend.components.kinesis.runtime.KinesisDatasetRuntime"
     */
    @Test
    @Ignore("This can't work unless the runtime jar is already installed in maven!")
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = datasetDefinition.getRuntimeInfo(null);
        assertEquals("org.talend.components.kinesis.runtime.KinesisDatasetRuntime", runtimeInfo.getRuntimeClassName());

    }
}
