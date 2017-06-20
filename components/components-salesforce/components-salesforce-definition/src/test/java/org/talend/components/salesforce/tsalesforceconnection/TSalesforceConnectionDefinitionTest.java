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

package org.talend.components.salesforce.tsalesforceconnection;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.salesforce.SalesforceDefinition.SOURCE_OR_SINK_CLASS;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 *
 */
public class TSalesforceConnectionDefinitionTest extends SalesforceTestBase {

    private TSalesforceConnectionDefinition definition;

    private SalesforceConnectionProperties properties;

    @Before
    public void setUp() {
        definition = new TSalesforceConnectionDefinition();

        properties = new SalesforceConnectionProperties("connection");
        properties.init();
    }

    @Test
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.NONE);
        assertThat(runtimeInfo, instanceOf(JarRuntimeInfo.class));

        JarRuntimeInfo jarRuntimeInfo = (JarRuntimeInfo) runtimeInfo;
        assertNotNull(jarRuntimeInfo.getJarUrl());
        assertNotNull(jarRuntimeInfo.getDepTxtPath());
        assertEquals(SOURCE_OR_SINK_CLASS, jarRuntimeInfo.getRuntimeClassName());

        runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.OUTGOING);
        assertThat(runtimeInfo, nullValue(RuntimeInfo.class));
    }

    @Test
    public void testSupportedConnectorTopologies() {
        Set<ConnectorTopology> topologySet = definition.getSupportedConnectorTopologies();
        assertThat(topologySet, hasItems(ConnectorTopology.NONE));
    }

    @Test
    public void testStartable() {
        assertTrue(definition.isStartable());
    }
}
