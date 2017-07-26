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

package org.talend.components.jdbc.datastream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.runtime.RuntimeInfo;

public class JDBCOutputDefinitionTest {

    JDBCOutputDefinition definition;

    @Before
    public void reset() {
        definition = new JDBCOutputDefinition();
    }

    @Test
    public void getRuntimeInfo() throws Exception {
        AllSetting allSetting = Mockito.mock(AllSetting.class);
        Mockito.when(allSetting.getDriverClass()).thenReturn("anyDriverClass");
        JDBCOutputProperties anyProperties = Mockito.mock(JDBCOutputProperties.class);
        Mockito.when(anyProperties.getRuntimeSetting()).thenReturn(allSetting);
        
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.BEAM, anyProperties, null);
        assertEquals(JDBCOutputDefinition.BEAM_RUNTIME, runtimeInfo.getRuntimeClassName());
    }

    @Test
    public void getSupportedConnectorTopologies() throws Exception {
        assertEquals(1, definition.getSupportedConnectorTopologies().size());
        assertTrue(definition.getSupportedConnectorTopologies().contains(ConnectorTopology.INCOMING));
    }

    @Test
    public void getPropertyClass() throws Exception {
        assertEquals(JDBCOutputProperties.class, definition.getPropertyClass());
    }

}
