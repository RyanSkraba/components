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

package org.talend.components.jms.output;

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

public class JmsOutputDefinitionTest {

    private final JmsOutputDefinition outputDefinition = new JmsOutputDefinition();

    /**
     * Check {@link JmsOutputDefinition#getRuntimeInfo(ComponentProperties properties, ConnectorTopology connectorTopology)
     * returns RuntimeInfo,
     * which runtime class name is "org.talend.components.jms.runtime_1_1.JmsSource"
     */
    @Test
    public void testGetRuntimeInfo() {
        RuntimeInfo runtimeInfo = outputDefinition.getRuntimeInfo(null, null);
        assertEquals("org.talend.components.jms.runtime_1_1.JmsSource", runtimeInfo.getRuntimeClassName());
    }

    /**
     * Check {@link JmsOutputDefinition#getPropertyClass()} returns class, which canonical name is
     * "org.talend.components.jms.input.JmsInputProperties"
     */
    @Test
    public void testGetPropertyClass() {
        Class<?> propertyClass = outputDefinition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.jms.output.JmsOutputProperties"));
    }

    /**
     * Check {@link JmsOutputDefinition#getName()} returns "JmsOutput"
     */
    @Test
    public void testGetName() {
        String componentName = outputDefinition.getName();
        assertEquals(componentName, "JmsOutput");
    }

    /**
     * Check {@link JmsOutputDefinition#getSupportedConnectorTopologies()} returns ConnectorTopology.INCOMING
     */
    @Test
    public void testGetSupportedConnectorTopologies() {
        Set<ConnectorTopology> test = outputDefinition.getSupportedConnectorTopologies();
        assertTrue(test.contains(ConnectorTopology.INCOMING));
    }
}
