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

package ${package}.definition.input;

import org.junit.Ignore;
import org.junit.Test;
import ${packageTalend}.api.component.ConnectorTopology;
import ${packageTalend}.api.component.runtime.ExecutionEngine;
import ${packageDaikon}.runtime.RuntimeInfo;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ${componentNameClass}InputDefinitionTest {
    private final ${componentNameClass}InputDefinition inputDefinition = new ${componentNameClass}InputDefinition();

    /**
     * Check {@link ${componentNameClass}InputDefinition#getRuntimeInfo(ExecutionEngine, ComponentProperties, ConnectorTopology)} returns RuntimeInfo,
     * which runtime class name is "${package}.runtime.${componentNameClass}InputRuntime"
     */
    @Test
    @Ignore("This can't work unless the runtime jar is already installed in maven!")
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = inputDefinition.getRuntimeInfo(ExecutionEngine.BEAM, null, null);
        assertEquals("org.talend.components.${componentNameLowerCase}.runtime.${componentNameClass}InputRuntime", runtimeInfo.getRuntimeClassName());

    }

    /**
     * Check {@link ${componentNameClass}InputDefinition#getPropertyClass()} returns class, which canonical name is
     * "${package}.${componentName}.input.${componentNameClass}InputProperties"
     */
    @Test
    public void testGetPropertyClass() {
        Class<?> propertyClass = inputDefinition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("${package}.definition.input.${componentNameClass}InputProperties"));
    }

    /**
     * Check {@link ${componentNameClass}InputDefinition} returns "${componentNameClass}Input"
     */
    @Test
    public void testGetName(){
        String componentName = inputDefinition.getName();
        assertEquals(componentName, "${componentNameClass}Input");
    }

    /**
     * Check {@link ${componentNameClass}InputDefinition#getSupportedConnectorTopologies()} returns ConnectorTopology.OUTGOING
     */
    @Test
    public void testGetSupportedConnectorTopologies(){
        Set<ConnectorTopology> test = inputDefinition.getSupportedConnectorTopologies();
        assertTrue(test.contains(ConnectorTopology.OUTGOING));
    }
}
