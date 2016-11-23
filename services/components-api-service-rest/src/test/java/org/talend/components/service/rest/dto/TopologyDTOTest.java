// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
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