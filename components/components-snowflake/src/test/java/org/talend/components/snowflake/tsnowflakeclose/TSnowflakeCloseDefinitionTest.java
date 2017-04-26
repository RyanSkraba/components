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
package org.talend.components.snowflake.tsnowflakeclose;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.snowflake.SnowflakeDefinition;
import org.talend.components.snowflake.runtime.SnowflakeCloseSourceOrSink;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * Unit-tests for {@link TSnowflakeCloseDefinition} class
 */
public class TSnowflakeCloseDefinitionTest {

    /**
     * Check {@link TSnowflakeCloseDefinition#getFamilies()} returns string array, which contains "Cloud/Snowflake"
     */
    @Test
    public void testGetFamilies() {
        SnowflakeDefinition definition = new TSnowflakeCloseDefinition();
        String[] families = definition.getFamilies();
        assertThat(families, arrayContaining("Cloud/Snowflake"));
    }

    /**
     * Check {@link TSnowflakeCloseDefinition#getName()} returns "tSnowflakeClose"
     */
    @Test
    public void testGetName() {
        SnowflakeDefinition definition = new TSnowflakeCloseDefinition();
        String componentName = definition.getName();
        assertEquals(componentName, "tSnowflakeClose");
    }

    /**
     * Check {@link TSnowflakeCloseDefinition#getPropertyClass()} returns class, which canonical name is
     * "org.talend.components.snowflake.tsnowflakeclose.TSnowflakeCloseProperties"
     */
    @Test
    public void testGetPropertyClass() {
        TSnowflakeCloseDefinition definition = new TSnowflakeCloseDefinition();
        Class<?> propertyClass = definition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.snowflake.tsnowflakeclose.TSnowflakeCloseProperties"));
    }

    /**
     * Check {@link TSnowflakeCloseDefinition#getRuntimeInfo(ExecutionEngine, ComponentProperties, ConnectorTopology)} returns instance of {@link SnowflakeCloseSourceOrSink}
     */
    @Test
    public void testGetRuntime() {
        TSnowflakeCloseDefinition definition = new TSnowflakeCloseDefinition();
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.NONE);
        SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, definition.getClass().getClassLoader());
        SourceOrSink source = (SourceOrSink) sandboxedInstance.getInstance();
        assertThat(source, is(instanceOf(SnowflakeCloseSourceOrSink.class)));
    }

}
