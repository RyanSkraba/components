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
package org.talend.components.processing.replicate;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.processing.ProcessingFamilyDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReplicateDefinitionTest {

    private final ReplicateDefinition definition = new ReplicateDefinition();

    /**
     * Check {@link ReplicateDefinition#getFamilies()} returns string array,
     * which contains "Processing"
     */
    @Test
    public void testGetFamilies() {
        String[] families = definition.getFamilies();
        assertThat(families, arrayContaining(ProcessingFamilyDefinition.NAME));
    }

    /**
     * Check {@link ReplicateDefinition#getName()} returns "Replicate"
     */
    @Test
    public void testGetName() {
        String componentName = definition.getName();
        assertEquals(componentName, "Replicate");
    }

    /**
     * Check {@link ReplicateDefinition#getPropertyClass()} returns class, which
     * canonical name is
     * "org.talend.components.processing.replicate.ReplicateProperties"
     */
    @Test
    public void testGetPropertyClass() {
        Class<?> propertyClass = definition.getPropertyClass();
        String canonicalName = propertyClass.getCanonicalName();
        assertThat(canonicalName, equalTo("org.talend.components.processing.replicate.ReplicateProperties"));
    }

    @Test
    public void testGetPngImagePath() {
        assertEquals("Replicate_icon32.png", definition.getPngImagePath(ComponentImageType.PALLETE_ICON_32X32));
    }

    /**
     * Check ReplicateDefinition#getRuntimeInfo returns instance of
     * "org.talend.components.processing.runtime.replicate.ReplicateRuntime"
     */
    @Test
    @Ignore("This can't work unless the runtime jar is already installed in maven!")
    public void testGetRuntimeInfo() {
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.BEAM, null, ConnectorTopology.INCOMING_AND_OUTGOING);
        assertEquals("org.talend.components.processing.runtime.replicate.ReplicateRuntime", runtimeInfo.getRuntimeClassName());
    }

    @Test(expected = org.talend.daikon.exception.TalendRuntimeException.class)
    public void testGetRuntimeInfoNone() {
        assertNull(definition.getRuntimeInfo(ExecutionEngine.BEAM, null, ConnectorTopology.NONE));
    }

    @Test(expected = org.talend.daikon.exception.TalendRuntimeException.class)
    public void testGetRuntimeInfoIncoming() {
        assertNull(definition.getRuntimeInfo(ExecutionEngine.BEAM, null, ConnectorTopology.INCOMING));
    }

    @Test(expected = org.talend.daikon.exception.TalendRuntimeException.class)
    public void testGetRuntimeInfoOutgoing() {
        assertNull(definition.getRuntimeInfo(ExecutionEngine.BEAM, null, ConnectorTopology.OUTGOING));
    }

    @Test
    public void testGetSupportedConnectorTopologies() {
        Set<ConnectorTopology> connector = definition.getSupportedConnectorTopologies();
        assertEquals(1, connector.size());
        assertTrue(connector.contains(ConnectorTopology.INCOMING_AND_OUTGOING));
    }
}
