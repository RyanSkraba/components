// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import static org.junit.Assert.assertEquals;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.snowflake.SnowflakeDefinition;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Unit-tests for {@link TSnowflakeCloseDefinition} class
 */
public class TSnowflakeCloseDefinitionTest {

    private TSnowflakeCloseDefinition snowflakeCloseDefinition;

    @Before
    public void setup() {
        snowflakeCloseDefinition = new TSnowflakeCloseDefinition();
    }

    /**
     * Check {@link TSnowflakeCloseDefinition#getFamilies()} returns string array, which contains "Cloud/Snowflake"
     */
    @Test
    public void testGetFamilies() {
        String[] families = snowflakeCloseDefinition.getFamilies();
        assertThat(families, arrayContaining("Cloud/Snowflake"));
    }

    @Test
    public void testIsStartable() {
        // Since this value may be used by Studio we should provide such check.
        Assert.assertTrue(snowflakeCloseDefinition.isStartable());
    }

    @Test
    public void testGetReturnProperties(){
        Assert.assertEquals(snowflakeCloseDefinition.getReturnProperties()[0], ComponentDefinition.RETURN_ERROR_MESSAGE_PROP);
    }

    /**
     * Check {@link TSnowflakeCloseDefinition#getName()} returns "tSnowflakeClose"
     */
    @Test
    public void testGetName() {
        String componentName = snowflakeCloseDefinition.getName();
        assertEquals(componentName, "tSnowflakeClose");
    }

    /**
     * Check {@link TSnowflakeCloseDefinition#getPropertyClass()} returns class, which canonical name is
     * "org.talend.components.snowflake.tsnowflakeclose.TSnowflakeCloseProperties"
     */
    @Test
    public void testGetPropertyClass() {
        Class<?> propertyClass = snowflakeCloseDefinition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.snowflake.tsnowflakeclose.TSnowflakeCloseProperties"));
    }

    /**
     * Check {@link TSnowflakeCloseDefinition#getRuntimeInfo(ExecutionEngine, ComponentProperties, ConnectorTopology)} returns instance of {@link SnowflakeCloseSourceOrSink}
     */
    @Test(expected = TalendRuntimeException.class)
    public void testGetRuntime() {
        RuntimeInfo runtimeInfo = snowflakeCloseDefinition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.NONE);
        assertThat(runtimeInfo, CoreMatchers.instanceOf(JarRuntimeInfo.class));

        JarRuntimeInfo jarRuntimeInfo = (JarRuntimeInfo) runtimeInfo;
        Assert.assertNotNull(jarRuntimeInfo.getJarUrl());
        Assert.assertNotNull(jarRuntimeInfo.getDepTxtPath());
        assertEquals(SnowflakeDefinition.CLOSE_SOURCE_OR_SINK_CLASS, jarRuntimeInfo.getRuntimeClassName());

        runtimeInfo = snowflakeCloseDefinition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.OUTGOING);
    }

}
