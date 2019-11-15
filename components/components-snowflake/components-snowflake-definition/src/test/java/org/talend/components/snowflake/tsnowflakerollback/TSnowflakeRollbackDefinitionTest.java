// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.tsnowflakerollback;

import org.hamcrest.CoreMatchers;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Unit-tests for {@link TSnowflakeRollbackDefinitionTest} class
 */
public class TSnowflakeRollbackDefinitionTest {

    private TSnowflakeRollbackDefinition snowflakeRollbackDefinition;

    @Before
    public void setup() {
        snowflakeRollbackDefinition = new TSnowflakeRollbackDefinition();
    }

    /**
     * Check {@link TSnowflakeRollbackDefinition#getFamilies()} returns string array, which contains "Cloud/Snowflake"
     */
    @Test
    public void testGetFamilies() {
        String[] families = snowflakeRollbackDefinition.getFamilies();
        assertThat(families, arrayContaining("Cloud/Snowflake"));
    }

    @Test
    public void testIsStartable() {
        // Since this value may be used by Studio we should provide such check.
        assertTrue(snowflakeRollbackDefinition.isStartable());
    }

    @Test
    public void testGetReturnProperties() {
        assertEquals(snowflakeRollbackDefinition.getReturnProperties()[0],
                ComponentDefinition.RETURN_ERROR_MESSAGE_PROP);
    }

    /**
     * Check {@link TSnowflakeRollbackDefinition#getName()} returns "tSnowflakeRollback"
     */
    @Test
    public void testGetName() {
        String componentName = snowflakeRollbackDefinition.getName();
        assertEquals(componentName, "tSnowflakeRollback");
    }

    /**
     * Check {@link TSnowflakeRollbackDefinition#getPropertyClass()} returns class, which canonical name is
     * "org.talend.components.snowflake.SnowflakeRollbackAndCommitProperties"
     */
    @Test
    public void testGetPropertyClass() {
        Class<?> propertyClass = snowflakeRollbackDefinition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.snowflake.SnowflakeRollbackAndCommitProperties"));
    }

    /**
     * Check
     * {@link TSnowflakeRollbackDefinition#getRuntimeInfo(ExecutionEngine, ComponentProperties, ConnectorTopology)}
     * returns instance of {@link SnowflakeRollbackSourceOrSink}
     */
    @Test(expected = TalendRuntimeException.class)
    public void testGetRuntime() {
        RuntimeInfo runtimeInfo =
                snowflakeRollbackDefinition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.NONE);
        assertThat(runtimeInfo, CoreMatchers.instanceOf(JarRuntimeInfo.class));

        JarRuntimeInfo jarRuntimeInfo = (JarRuntimeInfo) runtimeInfo;
        assertNotNull(jarRuntimeInfo.getJarUrl());
        assertNotNull(jarRuntimeInfo.getDepTxtPath());
        assertEquals(SnowflakeDefinition.ROLLBACK_SOURCE_OR_SINK_CLASS, jarRuntimeInfo.getRuntimeClassName());

        runtimeInfo = snowflakeRollbackDefinition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.OUTGOING);
    }

}
