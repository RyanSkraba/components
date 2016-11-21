package org.talend.components.service.rest.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;

/**
 * Unit test for the org.talend.components.service.rest.dto.TopologyDTO class.
 *
 * @see TopologyDTO
 */
public class TopologyDTOTest {

    @Test
    public void shouldMapAllTopologies() throws Exception {
        // given
        final ConnectorTopology[] topologies = ConnectorTopology.values();

        // when
        for (ConnectorTopology expected : topologies) {
            // then
            final TopologyDTO dto = TopologyDTO.from(expected);
            assertNotNull(dto);
            assertEquals(expected, dto.getTopology());
        }

    }
}