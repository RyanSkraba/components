//==============================================================================
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
//==============================================================================
package org.talend.components.service.rest.dto;

import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test for the org.talend.components.service.rest.dto.ConnectorTypology class.
 *
 * @see ConnectorTypology
 */
public class ConnectorTypologyTest {

    @Test
    public void shouldMapAllTypologies() throws Exception {
        // given
        final ConnectorTopology[] topologies = ConnectorTopology.values();

        // when
        for (ConnectorTopology expected : topologies) {
            // then
            final ConnectorTypology dto = ConnectorTypology.from(expected);
            assertNotNull(dto);
            assertEquals(expected, dto.getTopology());
        }

    }
}