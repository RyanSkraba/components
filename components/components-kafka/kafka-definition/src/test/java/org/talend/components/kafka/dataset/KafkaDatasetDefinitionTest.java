// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.kafka.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.runtime.RuntimeInfo;

public class KafkaDatasetDefinitionTest {

    KafkaDatasetDefinition definition;

    @Before
    public void reset() {
        definition = new KafkaDatasetDefinition();
    }

    @Test
    public void getPropertiesTest() throws Exception {
        assertEquals(KafkaDatasetProperties.class, definition.getPropertiesClass());
    }

    @Test
    public void getRuntimeInfo() throws Exception {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(null);
        assertNotNull(runtimeInfo);
    }

}
