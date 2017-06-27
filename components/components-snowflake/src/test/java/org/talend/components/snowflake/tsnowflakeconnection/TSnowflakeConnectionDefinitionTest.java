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
package org.talend.components.snowflake.tsnowflakeconnection;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.runtime.SnowflakeSourceOrSink;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Unit tests for {@link TSnowflakeConnectionDefinition} class
 */
public class TSnowflakeConnectionDefinitionTest {

    private TSnowflakeConnectionDefinition tSnowflakeConnectionDefinition;

    @Before
    public void setup() {
        tSnowflakeConnectionDefinition = new TSnowflakeConnectionDefinition();
    }

    @Test
    public void testCheckComponentName() {
        Assert.assertEquals(TSnowflakeConnectionDefinition.COMPONENT_NAME, tSnowflakeConnectionDefinition.getName());
    }

    @Test
    public void testGetPropertyClass() {
        Assert.assertEquals(SnowflakeConnectionProperties.class, tSnowflakeConnectionDefinition.getPropertyClass());
    }

    @Test
    public void testGetReturnProperties() {
        Assert.assertEquals(tSnowflakeConnectionDefinition.getReturnProperties()[0], ComponentDefinition.RETURN_ERROR_MESSAGE_PROP);
    }

    @Test
    public void testIsStartable() {
        Assert.assertTrue(tSnowflakeConnectionDefinition.isStartable());
    }

    @Test
    public void testGetRuntimeInfo() {
        RuntimeInfo runtime = tSnowflakeConnectionDefinition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.NONE);
        Assert.assertEquals(SnowflakeSourceOrSink.class.getCanonicalName(), runtime.getRuntimeClassName());
    }

    @Test
    public void testGetRuntimeInfoWithWrongComponentType() {
        Assert.assertNull(tSnowflakeConnectionDefinition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING_AND_OUTGOING));
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        Assert.assertThat(tSnowflakeConnectionDefinition.getSupportedConnectorTopologies(), Matchers.contains(ConnectorTopology.NONE));
    }
}
