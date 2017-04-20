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

package org.talend.components.netsuite.connection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.talend.components.api.component.ComponentDefinition.RETURN_ERROR_MESSAGE_PROP;

import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 *
 */
public class NetSuiteConnectionDefinitionTest {

    private NetSuiteConnectionDefinition definition = new NetSuiteConnectionDefinition();

    @Test
    public void testGetName() {
        assertEquals("tNetsuiteConnection", definition.getName());
    }

    @Test
    public void testGetPropertyClass() {
        assertThat(definition.getPropertyClass(), is(equalTo((Class) NetSuiteConnectionProperties.class)));
    }

    @Test
    public void testGetReturnProperties() {
        assertThat(definition.getReturnProperties().length, is(1));
        assertThat(definition.getReturnProperties(), arrayContainingInAnyOrder(
                (Property) RETURN_ERROR_MESSAGE_PROP));
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        assertThat(definition.getSupportedConnectorTopologies().size(), is(1));
        assertThat(definition.getSupportedConnectorTopologies(), contains(ConnectorTopology.NONE));
    }

    @Test
    public void testGetRuntimeInfo() {
        NetSuiteConnectionProperties properties = new NetSuiteConnectionProperties("test");
        properties.initForRuntime();

        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.NONE);
        assertNotNull(runtimeInfo);
        assertThat(runtimeInfo, instanceOf(JarRuntimeInfo.class));

        JarRuntimeInfo jarRuntimeInfo = (JarRuntimeInfo) runtimeInfo;
        assertThat(jarRuntimeInfo.getRuntimeClassName(), endsWith(".NetSuiteSourceOrSinkImpl"));
    }
}
