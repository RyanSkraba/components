package org.talend.components.couchbase.output;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.couchbase.runtime.CouchbaseSink;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class CouchbaseOutputDefinitionTest {

    private CouchbaseOutputDefinition definition;

    @Before
    public void setup() {
        definition = new CouchbaseOutputDefinition();
    }

    @Test
    public void testGetPropertyClass() {
        Assert.assertEquals(CouchbaseOutputProperties.class, definition.getPropertyClass());
    }

    @Test
    public void testGetReturnProperties() {
        Assert.assertThat(definition.getReturnProperties(),
                Matchers.arrayContaining(new Property[] { ComponentDefinition.RETURN_TOTAL_RECORD_COUNT_PROP,
                        ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT_PROP, ComponentDefinition.RETURN_REJECT_RECORD_COUNT_PROP,
                        ComponentDefinition.RETURN_ERROR_MESSAGE_PROP }));
    }

    @Test
    public void testGetRuntimeInfo() {
        RuntimeInfo runtime = definition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING);
        Assert.assertEquals(CouchbaseSink.class.getCanonicalName(), runtime.getRuntimeClassName());
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        Assert.assertThat(definition.getSupportedConnectorTopologies(), Matchers.hasItems(ConnectorTopology.INCOMING));
    }
}
