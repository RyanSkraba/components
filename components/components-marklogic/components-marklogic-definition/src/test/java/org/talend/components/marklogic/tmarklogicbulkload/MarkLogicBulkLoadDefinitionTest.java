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
package org.talend.components.marklogic.tmarklogicbulkload;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.api.component.ComponentDefinition.RETURN_ERROR_MESSAGE_PROP;

public class MarkLogicBulkLoadDefinitionTest {

    MarkLogicBulkLoadDefinition bulkLoadDefinition;

    @Before
    public void init() {
        bulkLoadDefinition = new MarkLogicBulkLoadDefinition();
    }

    @Test
    public void testGetFamilies() {
        String[] expectedFamilies = new String[] { "Databases/DB Specifics/MarkLogic", "Big Data/MarkLogic" };

        String[] actualFamilies = bulkLoadDefinition.getFamilies();

        assertArrayEquals(expectedFamilies, actualFamilies);
    }

    @Test
    public void testGetPropertyClass() {
        Class expectedPropertyClass = MarkLogicBulkLoadProperties.class;

        assertEquals(expectedPropertyClass, bulkLoadDefinition.getPropertyClass());
    }

    @Test
    public void testGetReturnProperties() {
        Property[] expectedReturnProperties = new Property[] { RETURN_ERROR_MESSAGE_PROP };

        assertArrayEquals(expectedReturnProperties, bulkLoadDefinition.getReturnProperties());
    }

    @Test
    public void testGetRuntimeInfo() {
        RuntimeInfo runtimeInfo = bulkLoadDefinition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.NONE);

        assertEquals("org.talend.components.marklogic.runtime.MarkLogicBulkLoad", runtimeInfo.getRuntimeClassName());
    }

    @Test
    public void testGetRuntimeInfoForWrongTopology() {
        RuntimeInfo runtimeInfo = bulkLoadDefinition.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.OUTGOING);
        assertNull(runtimeInfo);
    }

    @Test
    public void testIsStartable() {
        assertTrue(bulkLoadDefinition.isStartable());
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        Set<ConnectorTopology> connectorTopologies = bulkLoadDefinition.getSupportedConnectorTopologies();

        assertThat(connectorTopologies, contains(ConnectorTopology.NONE));
        assertThat(connectorTopologies,
                not((contains(ConnectorTopology.INCOMING, ConnectorTopology.OUTGOING, ConnectorTopology.INCOMING_AND_OUTGOING))));
    }

}
