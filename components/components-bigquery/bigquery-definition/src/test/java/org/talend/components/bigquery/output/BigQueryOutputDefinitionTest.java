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

package org.talend.components.bigquery.output;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.daikon.runtime.RuntimeInfo;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BigQueryOutputDefinitionTest {
    private final BigQueryOutputDefinition outputDefinition = new BigQueryOutputDefinition();

    @Test
    @Ignore("This can't work unless the runtime jar is already installed in maven!")
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = outputDefinition.getRuntimeInfo(ExecutionEngine.BEAM, null, null);
        assertEquals("org.talend.components.bigquery.runtime.BigQueryOutputRuntime", runtimeInfo.getRuntimeClassName());

    }

    /**
     * Check {@link BigQueryOutputDefinition#getPropertyClass()} returns class, which canonical name is
     * "org.talend.components.bigquery.output.BigQueryOutputProperties"
     */
    @Test
    public void testGetPropertyClass() {
        Class<?> propertyClass = outputDefinition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.bigquery.output.BigQueryOutputProperties"));
    }

    /**
     * Check {@link BigQueryOutputDefinition#getName()} returns "tBigQueryOutput"
     */
    @Test
    public void testGetName() {
         String componentName = outputDefinition.getName();
         assertEquals(componentName, "BigQueryOutput");
    }

    /**
     * Check {@link BigQueryOutputDefinition#getSupportedConnectorTopologies()} returns ConnectorTopology.INCOMING
     */
    @Test
    public void testGetSupportedConnectorTopologies(){
         Set<ConnectorTopology> test = outputDefinition.getSupportedConnectorTopologies();
         assertTrue(test.contains(ConnectorTopology.INCOMING));
    }
}
