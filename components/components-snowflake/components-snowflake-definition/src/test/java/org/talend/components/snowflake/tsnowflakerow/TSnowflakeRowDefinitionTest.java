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
package org.talend.components.snowflake.tsnowflakerow;

import static org.talend.components.snowflake.SnowflakeDefinition.ROW_SINK_CLASS;
import static org.talend.components.snowflake.SnowflakeDefinition.ROW_STANDALONE_CLASS;
import static org.talend.components.snowflake.SnowflakeDefinition.ROW_SOURCE_CLASS;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.daikon.runtime.RuntimeInfo;

public class TSnowflakeRowDefinitionTest {

    private TSnowflakeRowDefinition definition;

    @Before
    public void setup() {
        definition = new TSnowflakeRowDefinition();
    }

    @Test
    public void testGetRuntimeInfo() {
        TSnowflakeRowProperties properties = new TSnowflakeRowProperties("rowProperties");
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.INCOMING);
        Assert.assertEquals(ROW_SINK_CLASS, runtimeInfo.getRuntimeClassName());

        runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.INCOMING_AND_OUTGOING);
        Assert.assertEquals(ROW_SINK_CLASS, runtimeInfo.getRuntimeClassName());

        runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.NONE);
        Assert.assertEquals(ROW_STANDALONE_CLASS, runtimeInfo.getRuntimeClassName());

        runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.OUTGOING);
        Assert.assertEquals(ROW_SOURCE_CLASS, runtimeInfo.getRuntimeClassName());
    }

    @Test
    public void testSupportedConnectorTopology() {
        Assert.assertThat(definition.getSupportedConnectorTopologies(), Matchers.containsInAnyOrder(ConnectorTopology.NONE, ConnectorTopology.INCOMING, ConnectorTopology.OUTGOING,
                ConnectorTopology.INCOMING_AND_OUTGOING));
    }

    @Test
    public void testPropertyClass() {
        Assert.assertEquals(TSnowflakeRowProperties.class, definition.getPropertiesClass());
    }

    @Test
    public void testIsConditionalInputs() {
        Assert.assertTrue(definition.isConditionalInputs());
    }

    @Test
    public void testGetPartitioning() {
        Assert.assertEquals(AbstractComponentDefinition.AUTO, definition.getPartitioning());
    }

    @Test
    public void testIsStartable() {
        Assert.assertTrue(definition.isStartable());
    }

}
