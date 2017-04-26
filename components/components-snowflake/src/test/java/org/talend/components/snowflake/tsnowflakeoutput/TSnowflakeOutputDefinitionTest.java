package org.talend.components.snowflake.tsnowflakeoutput;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.SnowflakeTableProperties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class TSnowflakeOutputDefinitionTest {

    TSnowflakeOutputDefinition outputDefinition;

    @Before
    public void reset() {
        outputDefinition = new TSnowflakeOutputDefinition();
    }

    @Test
    public void testIsSchemaAutoPropagate() {
        boolean isSchemaAutoPropagate;

        isSchemaAutoPropagate = outputDefinition.isSchemaAutoPropagate();

        assertFalse(isSchemaAutoPropagate);
    }

    @Test
    public void testIsConditionalInputs() {
        boolean isConditionalInputs;

        isConditionalInputs = outputDefinition.isConditionalInputs();

        assertTrue(isConditionalInputs);
    }

    @Test
    public void testIsRejectAfterClose() {
        boolean isRejectAfterClose;

        isRejectAfterClose = outputDefinition.isRejectAfterClose();

        assertTrue(isRejectAfterClose);
    }

    @Test
    public void testGetPartitioning() {
        String partitioning;

        partitioning = outputDefinition.getPartitioning();

        assertEquals(partitioning, outputDefinition.AUTO);
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
        Property<?>[] properties;

        properties = outputDefinition.getReturnProperties();

        assertArrayEquals(properties,
                new Property<?>[] { outputDefinition.RETURN_ERROR_MESSAGE_PROP, outputDefinition.RETURN_TOTAL_RECORD_COUNT_PROP,
                        outputDefinition.RETURN_SUCCESS_RECORD_COUNT_PROP, outputDefinition.RETURN_REJECT_RECORD_COUNT_PROP });

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
    public void testGetSupportedConnectorTopologies() {
        Set<ConnectorTopology> supportedConnectorTopologies;
        Set<ConnectorTopology> requiredConnectorTopologies;

        requiredConnectorTopologies = EnumSet.of(ConnectorTopology.INCOMING, ConnectorTopology.INCOMING_AND_OUTGOING);
        supportedConnectorTopologies = outputDefinition.getSupportedConnectorTopologies();

        assertEquals(requiredConnectorTopologies, supportedConnectorTopologies);
    }
    //

}
