package org.talend.components.google.drive.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.google.drive.GoogleDriveComponentDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.runtime.RuntimeInfo;

public class GoogleDriveDatastoreDefinitionTest {

    GoogleDriveDatastoreDefinition def;

    @Before
    public void setUp() throws Exception {
        def = new GoogleDriveDatastoreDefinition();
    }

    @Test
    public void testGetInputCompDefinitionName() throws Exception {
        assertEquals("GoogleDriveDataInput", def.getInputCompDefinitionName());
    }

    @Test
    public void testGetOutputCompDefinitionName() throws Exception {
        assertNull(def.getOutputCompDefinitionName());
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        RuntimeInfo rt = def.getRuntimeInfo(new GoogleDriveDatastoreProperties("test"));
        assertNotNull(rt);
        assertEquals(GoogleDriveComponentDefinition.DATASTORE_RUNTIME_CLASS, rt.getRuntimeClassName());
    }

    @Test
    public void testGetPropertiesClass() throws Exception {
        assertEquals(GoogleDriveDatastoreProperties.class, def.getPropertiesClass());
    }

    @Test
    public void testGetImagePath() throws Exception {
        assertEquals("GoogleDriveDatastore_icon32.png", def.getImagePath());
        assertEquals("GoogleDriveDatastore_icon32.png", def.getImagePath(DefinitionImageType.PALETTE_ICON_32X32));
        assertNull(def.getImagePath(DefinitionImageType.SVG_ICON));
        assertNull(def.getImagePath(DefinitionImageType.TREE_ICON_16X16));
        assertNull(def.getImagePath(DefinitionImageType.WIZARD_BANNER_75X66));
    }

    @Test
    public void testGetIconKey() throws Exception {
        assertNull(def.getIconKey());
    }

    @Test
    public void testCreateDatasetProperties() throws Exception {
        GoogleDriveDatastoreProperties dsp = new GoogleDriveDatastoreProperties("test");
        dsp.setupProperties();
        dsp.setupLayout();
        DatasetProperties ds = def.createDatasetProperties(dsp);
        assertNotNull(ds);
        assertEquals(dsp, ds.getDatastoreProperties());
    }
}
