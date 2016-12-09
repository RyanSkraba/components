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

package ${package}.input;

import org.junit.Ignore;
import org.junit.Test;
import ${packageTalend}.api.component.ConnectorTopology;
import ${packageDaikon}.runtime.RuntimeInfo;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ${componentNameClass}InputDefinitionTest {
    private final ${componentNameClass}InputDefinition inputDefinition = new ${componentNameClass}InputDefinition();

    /**
     * Check {@link ${componentNameClass}InputDefinition#getRuntimeInfo(ComponentProperties, ConnectorTopology)} returns RuntimeInfo,
     * which runtime class name is "${package}.runtime_${runtimeVersion}.JmsSink"
     */
    @Test
    @Ignore("This can't work unless the runtime jar is already installed in maven!")
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = inputDefinition.getRuntimeInfo(null, null);
        assertEquals("org.talend.components.${componentLowerCase}.runtime.${componentNameClass}DatasetRuntime", runtimeInfo.getRuntimeClassName());

    }

    /**
     * Check {@link ${componentNameClass}InputDefinition#getPropertyClass()} returns class, which canonical name is
     * "${package}.${componentName}.input.${componentNameClass}InputProperties"
     */
    @Test
    public void testGetPropertyClass() {
        Class<?> propertyClass = inputDefinition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("${package}.input.${componentNameClass}InputProperties"));
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
