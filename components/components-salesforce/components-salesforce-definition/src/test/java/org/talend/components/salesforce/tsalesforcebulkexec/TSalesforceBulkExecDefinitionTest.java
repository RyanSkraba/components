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

package org.talend.components.salesforce.tsalesforcebulkexec;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.talend.components.salesforce.SalesforceDefinition.BULK_EXEC_RUNTIME_CLASS;
import static org.talend.components.salesforce.SalesforceDefinition.SOURCE_CLASS;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 *
 */
public class TSalesforceBulkExecDefinitionTest extends SalesforceTestBase {

    private TSalesforceBulkExecDefinition definition;

    private TSalesforceBulkExecProperties properties;

    @Before
    public void setUp() {
        definition = new TSalesforceBulkExecDefinition();

        properties = new TSalesforceBulkExecProperties("root");
        properties.init();
    }

    @Test
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.OUTGOING);
        assertRuntimeInfo(runtimeInfo, SOURCE_CLASS);

        runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.NONE);
        assertRuntimeInfo(runtimeInfo, BULK_EXEC_RUNTIME_CLASS);

        runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.INCOMING);
        assertThat(runtimeInfo, nullValue(RuntimeInfo.class));
    }

    private void assertRuntimeInfo(RuntimeInfo runtimeInfo, String clazz) {
        assertNotNull(runtimeInfo);
        assertThat(runtimeInfo, instanceOf(JarRuntimeInfo.class));

        JarRuntimeInfo jarRuntimeInfo = (JarRuntimeInfo) runtimeInfo;
        assertNotNull(jarRuntimeInfo.getJarUrl());
        assertNotNull(jarRuntimeInfo.getDepTxtPath());
        assertEquals(clazz, jarRuntimeInfo.getRuntimeClassName());
    }

    @Test
    public void testSupportedConnectorTopologies() {
        Set<ConnectorTopology> topologySet = definition.getSupportedConnectorTopologies();
        assertThat(topologySet, containsInAnyOrder(ConnectorTopology.OUTGOING));
    }

    @Test
    public void testNestedCompatibleComponentPropertiesClass() {
        Assert.assertThat(definition.getNestedCompatibleComponentPropertiesClass().length, is(2));
        Collection<Class<? extends ComponentProperties>> properties = Arrays
                .asList(definition.getNestedCompatibleComponentPropertiesClass());
        assertTrue(properties.contains(SalesforceModuleProperties.class));
        assertTrue(properties.contains(SalesforceConnectionProperties.class));
    }

    @Test
    public void testStartable() {
        assertTrue(definition.isStartable());
    }

    @Test
    public void testSchemaAutoPropagate() {
        assertFalse(definition.isSchemaAutoPropagate());
    }

    @Test
    public void testConditionalInputs() {
        assertTrue(definition.isConditionalInputs());
    }

    @Test
    public void testPartitioning() {
        assertNull(definition.getPartitioning());
    }
}
