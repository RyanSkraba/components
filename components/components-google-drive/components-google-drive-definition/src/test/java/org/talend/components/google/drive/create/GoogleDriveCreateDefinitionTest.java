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
package org.talend.components.google.drive.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.google.drive.GoogleDriveComponentDefinition;

public class GoogleDriveCreateDefinitionTest {

    GoogleDriveCreateDefinition def;

    @Before
    public void setUp() throws Exception {
        def = new GoogleDriveCreateDefinition();
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.OUTGOING));
        assertNotNull(def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.NONE));
        assertEquals(GoogleDriveComponentDefinition.SOURCE_CLASS,
                def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.OUTGOING).getRuntimeClassName());
        assertEquals(GoogleDriveComponentDefinition.CREATE_RUNTIME_CLASS,
                def.getRuntimeInfo(ExecutionEngine.DI, null, ConnectorTopology.NONE).getRuntimeClassName());
    }

}
