// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.marketo.MarketoComponentDefinition;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.daikon.definition.DefinitionImageType;

public class MarketoDatastoreDefinitionTest {

    private MarketoDatastoreDefinition definition;

    private MarketoDatastoreProperties connection;

    @Before
    public void setUp() throws Exception {
        definition = new MarketoDatastoreDefinition();
        connection = new MarketoDatastoreProperties("test");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetImagePath() throws Exception {
        assertEquals("MarketoDatastore_icon32.png", definition.getImagePath());
    }

    @Test
    public void testGetImagePathWithImageType() throws Exception {
        assertNull(definition.getImagePath(DefinitionImageType.SVG_ICON));
        assertNull(definition.getImagePath(DefinitionImageType.TREE_ICON_16X16));
        assertNull(definition.getImagePath(DefinitionImageType.WIZARD_BANNER_75X66));
        assertEquals("MarketoDatastore_icon32.png", definition.getImagePath(DefinitionImageType.PALETTE_ICON_32X32));
    }

    @Test
    public void testGetIconKey() throws Exception {
        assertNull(definition.getIconKey());
    }

    @Test
    public void testGetPropertiesClass() throws Exception {
        assertEquals(MarketoDatastoreProperties.class, definition.getPropertiesClass());
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        assertNotNull(definition.getRuntimeInfo(connection));
        assertEquals(MarketoComponentDefinition.RUNTIME_DATASTORE, definition.getRuntimeInfo(connection).getRuntimeClassName());
    }

    @Test
    public void testCreateDatasetProperties() throws Exception {
        DatasetProperties dataset = definition.createDatasetProperties(connection);
        assertNotNull(dataset);
        DatastoreProperties datastore = dataset.getDatastoreProperties();
        assertNotNull(datastore);
        assertNotNull(datastore.getDisplayName());
    }

    @Test
    public void testGetInputCompDefinitionName() throws Exception {
        assertNotNull(definition.getInputCompDefinitionName());
    }

    @Test
    public void testGetOutputCompDefinitionName() throws Exception {
        assertNull(definition.getOutputCompDefinitionName());
    }
}
