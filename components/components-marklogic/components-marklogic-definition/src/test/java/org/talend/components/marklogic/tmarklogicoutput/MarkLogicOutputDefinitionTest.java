// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marklogic.tmarklogicoutput;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.talend.components.api.component.ComponentDefinition.RETURN_ERROR_MESSAGE_PROP;
import static org.talend.components.api.component.ComponentDefinition.RETURN_REJECT_RECORD_COUNT_PROP;
import static org.talend.components.api.component.ComponentDefinition.RETURN_SUCCESS_RECORD_COUNT_PROP;
import static org.talend.components.api.component.ComponentDefinition.RETURN_TOTAL_RECORD_COUNT_PROP;

public class MarkLogicOutputDefinitionTest {

    MarkLogicOutputDefinition definition;

    @Before
    public void init() {
        definition = new MarkLogicOutputDefinition();
    }

    @Test
    public void testGetFamilies() {
        String[] expectedFamilies = new String[] { "Databases/MarkLogic", "Big Data/MarkLogic" };

        String[] actualFamilies = definition.getFamilies();

        assertArrayEquals(expectedFamilies, actualFamilies);
    }

    @Test
    public void testGetPropertyClass() {
        Class expectedPropertyClass = MarkLogicOutputProperties.class;

        assertEquals(expectedPropertyClass, definition.getPropertyClass());
    }

    @Test
    public void testGetReturnProperties() {
        Property[] expectedReturnProperties = new Property[] { RETURN_TOTAL_RECORD_COUNT_PROP, RETURN_SUCCESS_RECORD_COUNT_PROP,
                RETURN_REJECT_RECORD_COUNT_PROP, RETURN_ERROR_MESSAGE_PROP };

        assertArrayEquals(expectedReturnProperties, definition.getReturnProperties());
    }

    @Test
    public void testGetRuntimeInfo() {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.INCOMING);

        assertEquals("org.talend.components.marklogic.runtime.MarkLogicSink", runtimeInfo.getRuntimeClassName());
    }

    @Test(expected = TalendRuntimeException.class)
    public void testGetRuntimeInfoForWrongTopology() {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.NONE);
        assertNull(runtimeInfo);
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        Set<ConnectorTopology> connectorTopologies = definition.getSupportedConnectorTopologies();

        assertThat(connectorTopologies, contains(ConnectorTopology.INCOMING, ConnectorTopology.INCOMING_AND_OUTGOING));
        assertThat(connectorTopologies, not((contains(ConnectorTopology.NONE, ConnectorTopology.OUTGOING))));
    }

}
