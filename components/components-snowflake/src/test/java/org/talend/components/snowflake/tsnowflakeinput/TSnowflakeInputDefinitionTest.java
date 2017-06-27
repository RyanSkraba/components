package org.talend.components.snowflake.tsnowflakeinput;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.SnowflakeTableProperties;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Unit tests for {@link TSnowflakeInputDefinition} class
 *
 */
public class TSnowflakeInputDefinitionTest {

    TSnowflakeInputDefinition inputDefinition;

    @Before
    public void reset() {
        inputDefinition = new TSnowflakeInputDefinition();

    }

    @Test
    public void testIsStartable() {
        boolean isStartable;

        isStartable = inputDefinition.isStartable();

        assertTrue(isStartable);
    }

    @Test
    public void testGetPropertyClass() {
        Class<?> propertyClass;

        propertyClass = inputDefinition.getPropertiesClass();

        assertEquals(propertyClass, TSnowflakeInputProperties.class);
    }

    @Test
    public void testGetNestedCompatibleComponentPropertiesClass() {
        Class<? extends ComponentProperties>[] componentPropertiesClasses;

        componentPropertiesClasses = inputDefinition.getNestedCompatibleComponentPropertiesClass();

        assertArrayEquals(componentPropertiesClasses,
                new Class[] { SnowflakeConnectionProperties.class, SnowflakeTableProperties.class });

    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        RuntimeInfo runtimeInfo;

        runtimeInfo = inputDefinition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.OUTGOING);

        assertNotNull(runtimeInfo);
    }

    @Test
    public void testGetRuntimeInfoWithIncorrectConnector() {
        Assert.assertNull(inputDefinition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING));
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        Set<ConnectorTopology> supportedConnectorTopologies;
        Set<ConnectorTopology> expectedConnectorTopologies;

        expectedConnectorTopologies = EnumSet.of(ConnectorTopology.OUTGOING);
        supportedConnectorTopologies = inputDefinition.getSupportedConnectorTopologies();

        assertEquals(expectedConnectorTopologies, supportedConnectorTopologies);
    }
}
