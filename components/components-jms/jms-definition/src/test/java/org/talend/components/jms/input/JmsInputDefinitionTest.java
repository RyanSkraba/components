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

package org.talend.components.jms.input;

import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.properties.Properties;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JmsInputDefinitionTest {

    private final JmsInputDefinition inputDefinition = new JmsInputDefinition();

    /**
     * Check {@link JmsInputDefinition#getRuntimeInfo(ComponentProperties, ConnectorTopology)} returns RuntimeInfo,
     * which runtime class name is "org.talend.components.jms.runtime_1_1.JmsSink"
     */
    @Test
    public void testGetRuntimeInfo() {
        RuntimeInfo runtimeInfo = inputDefinition.getRuntimeInfo(null, null);
        assertEquals("org.talend.components.jms.runtime_1_1.JmsSink", runtimeInfo.getRuntimeClassName());
    }

    /**
     * Check {@link JmsInputDefinition#getPropertyClass()} returns class, which canonical name is
     * "org.talend.components.jms.input.JmsInputProperties"
     */
    @Test
    public void testGetPropertyClass() {
        Class<?> propertyClass = inputDefinition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.jms.input.JmsInputProperties"));
    }

    /**
     * Check {@link JmsInputDefinition#getName()} returns "JmsInput"
     */
    @Test
    public void testGetName() {
        String componentName = inputDefinition.getName();
        assertEquals(componentName, "JmsInput");
    }

    /**
     * Check {@link JmsInputDefinition#getSupportedConnectorTopologies()} returns ConnectorTopology.OUTGOING
     */
    @Test
    public void testGetSupportedConnectorTopologies() {
        Set<ConnectorTopology> test = inputDefinition.getSupportedConnectorTopologies();
        assertTrue(test.contains(ConnectorTopology.OUTGOING));
    }
}
