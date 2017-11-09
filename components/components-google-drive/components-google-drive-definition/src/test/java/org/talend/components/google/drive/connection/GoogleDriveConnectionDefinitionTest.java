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
package org.talend.components.google.drive.connection;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.components.api.component.ComponentDefinition.RETURN_ERROR_MESSAGE_PROP;

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public class GoogleDriveConnectionDefinitionTest {

    GoogleDriveConnectionDefinition def;

    @Before
    public void setup() {
        def = new GoogleDriveConnectionDefinition();
    }

    @Test
    public void testGetSupportedConnectorTopologies() throws Exception {
        assertThat(EnumSet.of(ConnectorTopology.NONE), equalTo(def.getSupportedConnectorTopologies()));
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        RuntimeInfo rti = def.getRuntimeInfo(ExecutionEngine.DI, new GoogleDriveConnectionProperties("test"),
                ConnectorTopology.NONE);
        assertNotNull(rti);
    }

    @Test
    public void testIsStartable() throws Exception {
        assertTrue(def.isStartable());
    }

    @Test
    public void testGetFamilies() {
        assertThat(new String[] { "Cloud/Google Drive" }, equalTo(def.getFamilies()));
    }

    @Test
    public void testGetPropertyClass() throws Exception {
        assertEquals(GoogleDriveConnectionProperties.class, def.getPropertyClass());
    }

    @Test
    public void testGetNestedCompatibleComponentPropertiesClass() throws Exception {
        assertEquals(new Object[] { GoogleDriveConnectionProperties.class }, def.getNestedCompatibleComponentPropertiesClass());
    }

    @Test
    public void testGetReturnProperties() throws Exception {
        assertEquals(new Property[] { RETURN_ERROR_MESSAGE_PROP }, def.getReturnProperties());
    }

}
