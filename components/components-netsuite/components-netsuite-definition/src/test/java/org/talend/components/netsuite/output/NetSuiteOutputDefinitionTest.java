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

package org.talend.components.netsuite.output;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.components.api.component.ComponentDefinition.RETURN_ERROR_MESSAGE_PROP;
import static org.talend.components.api.component.ComponentDefinition.RETURN_REJECT_RECORD_COUNT_PROP;
import static org.talend.components.api.component.ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT_PROP;
import static org.talend.components.api.component.ComponentDefinition.RETURN_TOTAL_RECORD_COUNT_PROP;
import static org.talend.components.netsuite.output.NetSuiteOutputDefinition.RETURN_CURRENT_INTERNAL_ID_PROP;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 *
 */
public class NetSuiteOutputDefinitionTest {

    private NetSuiteOutputDefinition definition = new NetSuiteOutputDefinition();

    @Test
    public void testGetName() {
        assertEquals("tNetsuiteOutput", definition.getName());
    }

    @Test
    public void testGetPropertyClass() {
        assertThat(definition.getPropertyClass(), is(equalTo((Class) NetSuiteOutputProperties.class)));
    }

    @Test
    public void testGetReturnProperties() {
        assertThat(definition.getReturnProperties().length, is(5));
        assertThat(definition.getReturnProperties(), arrayContainingInAnyOrder(
                (Property) RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP,
                RETURN_SUCCESS_RECORD_COUNT_PROP, RETURN_REJECT_RECORD_COUNT_PROP, RETURN_CURRENT_INTERNAL_ID_PROP));
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        assertThat(definition.getSupportedConnectorTopologies().size(), is(2));
        assertThat(definition.getSupportedConnectorTopologies(), contains(
                ConnectorTopology.INCOMING, ConnectorTopology.INCOMING_AND_OUTGOING));
    }

    @Test
    public void testGetNestedCompatibleComponentPropertiesClass() {
        assertThat(definition.getNestedCompatibleComponentPropertiesClass().length, is(2));
        Collection<Class<? extends ComponentProperties>> properties = Arrays.asList(
                definition.getNestedCompatibleComponentPropertiesClass());
        assertTrue(properties.contains(NetSuiteConnectionProperties.class));
        assertTrue(properties.contains(NetSuiteOutputModuleProperties.class));
    }

    @Test
    public void testRuntimeInfoForIncoming() {
        NetSuiteOutputProperties properties = new NetSuiteOutputProperties("test");
        properties.initForRuntime();

        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.INCOMING);
        assertNotNull(runtimeInfo);
        assertThat(runtimeInfo, instanceOf(JarRuntimeInfo.class));

        JarRuntimeInfo jarRuntimeInfo = (JarRuntimeInfo) runtimeInfo;
        assertThat(jarRuntimeInfo.getRuntimeClassName(), endsWith(".NetSuiteSinkImpl"));
    }

    @Test
    public void testRuntimeInfoForIncomingAndOutgoing() {
        NetSuiteOutputProperties properties = new NetSuiteOutputProperties("test");
        properties.initForRuntime();

        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.INCOMING_AND_OUTGOING);
        assertNotNull(runtimeInfo);
        assertThat(runtimeInfo, instanceOf(JarRuntimeInfo.class));

        JarRuntimeInfo jarRuntimeInfo = (JarRuntimeInfo) runtimeInfo;
        assertThat(jarRuntimeInfo.getRuntimeClassName(), endsWith(".NetSuiteSinkImpl"));
    }

    @Test
    public void testConditionalInputsFlag() {
        assertTrue(definition.isConditionalInputs());
    }

    @Test
    public void testRejectAfterCloseFlag() {
        assertFalse(definition.isRejectAfterClose());
    }

    @Test
    public void testNotStartable() {
        assertFalse(definition.isStartable());
    }
}
