package org.talend.components.service.rest.dto;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.service.rest.mock.MockComponentDefinition;
import org.talend.components.service.rest.mock.MockComponentSinkProperties;
import org.talend.components.service.rest.mock.MockComponentSourceProperties;
import org.talend.components.service.rest.mock.MockComponentTransformerProperties;

/**
 * Unit tests for {@link ConnectorDto}.
 */
public class ConnectorDtoTest {

    @Test
    public void createConnectorList_empty() throws Exception {
        // The default components don't have connectors specified.
        List<ConnectorDto> actual = ConnectorDto.createConnectorList(new MockComponentDefinition("components"));
        assertThat(actual, empty());
    }

    @Test
    public void createConnectorList_source() throws Exception {
        // Partial mock to override the connectors available for the definition.
        MockComponentDefinition def = new MockComponentDefinition("components", ConnectorTopology.OUTGOING);
        def.setPropertyClass(MockComponentSourceProperties.class);

        // The default components don't have connectors specified.
        List<ConnectorDto> actual = ConnectorDto.createConnectorList(def);
        assertThat(actual, hasSize(1));
        assertThat(actual.get(0).getConnectorType(), is(ConnectorTypology.SOURCE));
        assertThat(actual.get(0).getFlowType(), is("MAIN"));
    }

    @Test
    public void createConnectorList_sink() throws Exception {
        // Partial mock to override the connectors available for the definition.
        MockComponentDefinition def = new MockComponentDefinition("components", ConnectorTopology.OUTGOING);
        def.setPropertyClass(MockComponentSinkProperties.class);

        // The default components don't have connectors specified.
        List<ConnectorDto> actual = ConnectorDto.createConnectorList(def);
        assertThat(actual, hasSize(1));
        assertThat(actual.get(0).getConnectorType(), is(ConnectorTypology.SINK));
        assertThat(actual.get(0).getFlowType(), is("REJECT"));
    }

    @Test
    public void createConnectorList_transformer() throws Exception {
        // Partial mock to override the connectors available for the definition.
        MockComponentDefinition def = new MockComponentDefinition("components", ConnectorTopology.OUTGOING);
        def.setPropertyClass(MockComponentTransformerProperties.class);

        // The default components don't have connectors specified.
        List<ConnectorDto> actual = ConnectorDto.createConnectorList(def);
        assertThat(actual, hasSize(2));
        assertThat(actual.get(0).getConnectorType(), is(ConnectorTypology.SINK));
        assertThat(actual.get(0).getFlowType(), is("REJECT"));
        assertThat(actual.get(1).getConnectorType(), is(ConnectorTypology.SOURCE));
        assertThat(actual.get(1).getFlowType(), is("MAIN"));
    }
}
