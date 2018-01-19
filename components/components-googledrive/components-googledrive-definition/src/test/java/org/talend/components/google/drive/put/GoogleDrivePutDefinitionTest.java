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
package org.talend.components.google.drive.put;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.talend.components.api.component.ConnectorTopology.INCOMING;
import static org.talend.components.api.component.ConnectorTopology.INCOMING_AND_OUTGOING;
import static org.talend.components.api.component.ConnectorTopology.NONE;
import static org.talend.components.api.component.ConnectorTopology.OUTGOING;
import static org.talend.components.google.drive.GoogleDriveComponentDefinition.PUT_RUNTIME_CLASS;
import static org.talend.components.google.drive.GoogleDriveComponentDefinition.SOURCE_CLASS;

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.ExecutionEngine;

public class GoogleDrivePutDefinitionTest {

    GoogleDrivePutDefinition def;

    @Before
    public void setUp() throws Exception {
        def = new GoogleDrivePutDefinition();
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, NONE));
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, OUTGOING));
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, INCOMING_AND_OUTGOING));
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, INCOMING));
        assertEquals(SOURCE_CLASS, def.getRuntimeInfo(ExecutionEngine.DI, null, OUTGOING).getRuntimeClassName());
        assertEquals(PUT_RUNTIME_CLASS, def.getRuntimeInfo(ExecutionEngine.DI, null, NONE).getRuntimeClassName());
    }

    @Test
    public void testGetSupportedConnectorTopologies() throws Exception {
        assertEquals(EnumSet.of(INCOMING, INCOMING_AND_OUTGOING, NONE, OUTGOING), def.getSupportedConnectorTopologies());
    }
}
