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
package org.talend.components.azurestorage.tazurestorageconnection;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.daikon.properties.property.Property;

public class TAzureStorageConnectionDefinitionTest {

    private TAzureStorageConnectionDefinition def;

    @Before
    public void setUp() throws Exception {
        def = new TAzureStorageConnectionDefinition();
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionDefinition#getPropertyClass()}.
     */
    @Test
    public final void testGetPropertyClass() {
        assertEquals(TAzureStorageConnectionProperties.class, def.getPropertyClass());
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionDefinition#getReturnProperties()}.
     */
    @SuppressWarnings("deprecation")
    @Test
    public final void testGetReturnProperties() {
        assertEquals(new Property[]{}, def.getReturnProperties());
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionDefinition#getPropertiesClass()}.
     */
    @Test
    public final void testGetPropertiesClass() {
        assertEquals(TAzureStorageConnectionProperties.class, def.getPropertiesClass());
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionDefinition#getRuntimeInfo(org.talend.components.api.component.runtime.ExecutionEngine, org.talend.components.api.properties.ComponentProperties, org.talend.components.api.component.ConnectorTopology)}.
     */
    @Test
    public final void testGetRuntimeInfo() {
        assertNotNull(def.getRuntimeInfo(null, null, ConnectorTopology.NONE));
        assertNull(def.getRuntimeInfo(null, null, ConnectorTopology.INCOMING));
    }

}
