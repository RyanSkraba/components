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

package org.talend.components.salesforce.dataprep;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.salesforce.SalesforceDefinition.DATAPREP_SOURCE_CLASS;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.SupportedProduct;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 *
 */
public class SalesforceInputDefinitionTest extends SalesforceTestBase {

    private SalesforceInputDefinition definition;

    private SalesforceInputProperties properties;

    @Before
    public void setUp() {
        definition = new SalesforceInputDefinition();

        properties = new SalesforceInputProperties("root");
        properties.init();
    }

    @Test
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.OUTGOING);
        assertRuntimeInfo(runtimeInfo);
    }

    private void assertRuntimeInfo(RuntimeInfo runtimeInfo) {
        assertNotNull(runtimeInfo);
        assertThat(runtimeInfo, instanceOf(JarRuntimeInfo.class));

        JarRuntimeInfo jarRuntimeInfo = (JarRuntimeInfo) runtimeInfo;
        assertNotNull(jarRuntimeInfo.getJarUrl());
        assertNotNull(jarRuntimeInfo.getDepTxtPath());
        assertEquals(DATAPREP_SOURCE_CLASS, jarRuntimeInfo.getRuntimeClassName());
    }

    @Test
    public void testSupportedProducts() {
        assertThat(definition.getSupportedProducts(), contains(SupportedProduct.DATAPREP));
    }

    @Test
    public void testSupportedConnectorTopologies() {
        Set<ConnectorTopology> topologySet = definition.getSupportedConnectorTopologies();
        assertThat(topologySet, hasItems(ConnectorTopology.OUTGOING));
    }

    @Test
    public void testNestedCompatibleComponentPropertiesClass() {
        assertThat(definition.getNestedCompatibleComponentPropertiesClass().length, is(0));
    }

    @Test
    public void testStartable() {
        assertTrue(definition.isStartable());
    }

    @Test
    public void testReturnProperties() {
        assertEquals(0, definition.getReturnProperties().length);
    }
}
