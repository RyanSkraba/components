package org.talend.components.snowflake.tsnowflakeoutput;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Unit tests for {@link TSnowflakeOutputDefinition} class
 */
public class TSnowflakeOutputDefinitionTest {

    TSnowflakeOutputDefinition outputDefinition;

    @Before
    public void reset() {
        outputDefinition = new TSnowflakeOutputDefinition();
    }

    @Test
    public void testIsSchemaAutoPropagate() {
        assertFalse(outputDefinition.isSchemaAutoPropagate());
    }

    @Test
    public void testIsConditionalInputs() {
        assertTrue(outputDefinition.isConditionalInputs());
    }

    @Test
    public void testIsRejectAfterClose() {
        assertTrue(outputDefinition.isRejectAfterClose());
    }

    @Test
    public void testGetPartitioning() {
        assertEquals(TSnowflakeOutputDefinition.AUTO, outputDefinition.getPartitioning());
    }

    @Test
    public void testGetPropertyClass() {
        Class<? extends ComponentProperties> propertyClass;

        propertyClass = outputDefinition.getPropertyClass();

        assertEquals(propertyClass, TSnowflakeOutputProperties.class);

    }

    @Test
    public void testGetNestedCompatibleComponentPropertiesClass() {
        Class<? extends ComponentProperties>[] nestedCompatibleComponentPropertiesClass;

        nestedCompatibleComponentPropertiesClass = outputDefinition.getNestedCompatibleComponentPropertiesClass();
        assertEquals(2, nestedCompatibleComponentPropertiesClass.length);
        assertArrayEquals(nestedCompatibleComponentPropertiesClass,
                new Class[] { SnowflakeConnectionProperties.class, SnowflakeTableProperties.class });

    }

    @Test
    public void testGetReturnProperties() {

        assertArrayEquals(outputDefinition.getReturnProperties(),
                new Property<?>[] { TSnowflakeOutputDefinition.RETURN_ERROR_MESSAGE_PROP, TSnowflakeOutputDefinition.RETURN_TOTAL_RECORD_COUNT_PROP,
            TSnowflakeOutputDefinition.RETURN_SUCCESS_RECORD_COUNT_PROP, TSnowflakeOutputDefinition.RETURN_REJECT_RECORD_COUNT_PROP });

    }

    @Test
    public void testGetRuntimeInfo() {

        RuntimeInfo runtimeInfoForIncomingTopology;
        RuntimeInfo runtimeInfoForIncomingAndOutgoingTopology;

        runtimeInfoForIncomingTopology = outputDefinition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING);
        runtimeInfoForIncomingAndOutgoingTopology = outputDefinition.getRuntimeInfo(ExecutionEngine.DI, null,
                ConnectorTopology.INCOMING_AND_OUTGOING);

        assertNotNull(runtimeInfoForIncomingTopology);
        assertNotNull(runtimeInfoForIncomingAndOutgoingTopology);

    }

    @Test
    public void testGetRuntimeInfoWithIncorrectConnector() {
        Assert.assertNull(outputDefinition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.OUTGOING));
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        Set<ConnectorTopology> supportedConnectorTopologies;
        Set<ConnectorTopology> requiredConnectorTopologies;

        requiredConnectorTopologies = EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.INCOMING_AND_OUTGOING);
        supportedConnectorTopologies = outputDefinition.getSupportedConnectorTopologies();

        assertEquals(requiredConnectorTopologies, supportedConnectorTopologies);
    }

}
