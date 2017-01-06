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

package ${package}.output;

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

public class ${componentNameClass}OutputDefinitionTest {
    private final ${componentNameClass}OutputDefinition outputDefinition = new ${componentNameClass}OutputDefinition();

    /**
     * Check {@link ${componentNameClass}OutputDefinition#getSupportedConnectorTopologies()} returns ConnectorTopology.INCOMING
     */
    @Test
    @Ignore("This can't work unless the runtime jar is already installed in maven!")
    public void testRuntimeInfo() {
        RuntimeInfo runtimeInfo = outputDefinition.getRuntimeInfo(ExecutionEngine.BEAM, null, null);
        assertEquals("org.talend.components.${componentNameLowerCase}.runtime${runtimeVersionConverted}.${componentNameClass}OutputRuntime", runtimeInfo.getRuntimeClassName());

    }

    /**
     * Check {@link ${componentNameClass}OutputDefinition#getPropertyClass()} returns class, which canonical name is
     * "${package}.output.${componentNameClass}OutputProperties"
     */
    @Test
    public void testGetPropertyClass() {
        Class<?> propertyClass = outputDefinition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("${package}.output.${componentNameClass}OutputProperties"));
    }

    /**
     * Check {@link ${componentNameClass}OutputDefinition#getName()} returns "t${componentNameClass}Output"
     */
    @Test
    public void testGetName() {
         String componentName = outputDefinition.getName();
         assertEquals(componentName, "${componentNameClass}Output");
    }

    /**
     * Check {@link ${componentNameClass}OutputDefinition#getSupportedConnectorTopologies()} returns ConnectorTopology.INCOMING
     */
    @Test
    public void testGetSupportedConnectorTopologies(){
         Set<ConnectorTopology> test = outputDefinition.getSupportedConnectorTopologies();
         assertTrue(test.contains(ConnectorTopology.INCOMING));
    }
}
