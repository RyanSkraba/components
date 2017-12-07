package org.talend.components.couchbase.input;

import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.couchbase.runtime.CouchbaseSource;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class CouchbaseInputDefinitionTest {

    private CouchbaseInputDefinition definition;

    @Before
    public void setup() {
        definition = new CouchbaseInputDefinition();
    }

    @Test
    public void testGetPropertyClass() {
        Assert.assertEquals(CouchbaseInputProperties.class, definition.getPropertiesClass());
    }

    @Test
    public void testGetReturnProperties() {
        Property<?>[] actual = definition.getReturnProperties();
        Assert.assertEquals(2, actual.length);
        Assert.assertThat(actual,
                Matchers.<Property<?>>hasItemInArray(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT_PROP));
        Assert.assertThat(actual,
                Matchers.<Property<?>>hasItemInArray(ComponentDefinition.RETURN_ERROR_MESSAGE_PROP));
    }

    @Test
    public void testGetRuntimeInfo() {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.OUTGOING);
        Assert.assertEquals(CouchbaseSource.class.getName(), runtimeInfo.getRuntimeClassName());
    }

    @Test
    public void testGetRuntimeInfoWithUnsupportedConnectorTopology() {
        Assert.assertNull(definition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING));
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        Set<ConnectorTopology> connectorTopologies = definition.getSupportedConnectorTopologies();

        Assert.assertEquals(1, connectorTopologies.size());
        Assert.assertThat(connectorTopologies, Matchers.hasItem(ConnectorTopology.OUTGOING));
    }

    @Test
    public void testGetSupportedEngines() {
        Assert.assertThat(definition.getSupportedProducts(), Matchers.contains(ExecutionEngine.DI.name()));
    }

    @Test
    public void testGetFamilies() {
        String[] families = definition.getFamilies();
        Assert.assertEquals(1, families.length);
    }
}
