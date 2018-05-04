// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.MarketoComponentDefinition;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.daikon.definition.DefinitionImageType;

public class MarketoDatasetDefinitionTest {

    private MarketoDatasetDefinition definition;

    private MarketoDatasetProperties properties;

    private TMarketoConnectionProperties connection;

    // private MarketoDatastore

    @Before
    public void setUp() throws Exception {
        definition = new MarketoDatasetDefinition();
        properties = new MarketoDatasetProperties("test");
        connection = new TMarketoConnectionProperties("test");
        connection.init();
        properties.setDatastoreProperties(connection);
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        assertEquals(MarketoComponentDefinition.RUNTIME_DATASET, definition.getRuntimeInfo(properties).getRuntimeClassName());
    }

    @Test
    public void testGetPropertiesClass() throws Exception {
        assertEquals(MarketoDatasetProperties.class, definition.getPropertiesClass());
    }

    @Test
    public void testGetImagePath() throws Exception {
        assertEquals("MarketoDataset_icon32.png", definition.getImagePath());
    }

    @Test
    public void testGetImagePathWithImageType() throws Exception {
        assertNull(definition.getImagePath(DefinitionImageType.SVG_ICON));
        assertNull(definition.getImagePath(DefinitionImageType.TREE_ICON_16X16));
        assertNull(definition.getImagePath(DefinitionImageType.WIZARD_BANNER_75X66));
        assertEquals("MarketoDataset_icon32.png", definition.getImagePath(DefinitionImageType.PALETTE_ICON_32X32));
    }

    @Test
    public void testGetIconKey() throws Exception {
        assertNull(definition.getIconKey());
    }

}
