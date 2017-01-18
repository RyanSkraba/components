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

package org.talend.components.jms;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.daikon.runtime.RuntimeInfo;

public class JmsDatasetDefinitionTest {

    private final JmsDatasetDefinition datasetDefinition = new JmsDatasetDefinition();

    /**
     * Check {@link JmsDatasetDefinition#getRuntimeInfo(JmsDatasetProperties properties, Object ctx)} returns
     * RuntimeInfo, which runtime class name is "org.talend.components.jms.runtime_1_1.DatasetRuntime"
     */
    @Test
    public void testGetRuntimeInfo() {
        RuntimeInfo runtimeInfo = datasetDefinition.getRuntimeInfo(null);
        assertEquals("org.talend.components.jms.runtime_1_1.DatasetRuntime", runtimeInfo.getRuntimeClassName());
    }

    /**
     * Check {@link JmsDatasetDefinition#getImagePath()} ()} returns JmsDatasetProperties, which canonical name is "jms"
     */
    @Test
    public void testGetImagePath() {
        assertNotNull(datasetDefinition.getImagePath());
    }

    /**
     * Check {@link JmsDatasetDefinition#getImagePath()} ()} returns JmsDatasetProperties, which canonical name is "jms"
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Java Message Service", datasetDefinition.getDisplayName());
    }
}
