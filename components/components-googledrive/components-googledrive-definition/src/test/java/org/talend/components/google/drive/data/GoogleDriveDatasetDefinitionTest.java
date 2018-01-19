package org.talend.components.google.drive.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.GoogleDriveComponentDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.runtime.RuntimeInfo;

public class GoogleDriveDatasetDefinitionTest {

    GoogleDriveDatasetDefinition def;

    @Before
    public void setUp() throws Exception {
        def = new GoogleDriveDatasetDefinition();
    }

    @Test
    public void testGetRuntimeInfo() throws Exception {
        RuntimeInfo rt = def.getRuntimeInfo(new GoogleDriveDatasetProperties("test"));
        assertNotNull(rt);
        assertEquals(GoogleDriveComponentDefinition.DATASET_RUNTIME_CLASS, rt.getRuntimeClassName());
    }

    @Test
    public void testGetPropertiesClass() throws Exception {
        assertEquals(GoogleDriveDatasetProperties.class, def.getPropertiesClass());
    }

    @Test
    public void testGetImagePath() throws Exception {
        assertEquals("GoogleDriveDataset_icon32.png", def.getImagePath());
        assertEquals("GoogleDriveDataset_icon32.png", def.getImagePath(DefinitionImageType.PALETTE_ICON_32X32));
        assertNull(def.getImagePath(DefinitionImageType.SVG_ICON));
        assertNull(def.getImagePath(DefinitionImageType.TREE_ICON_16X16));
        assertNull(def.getImagePath(DefinitionImageType.WIZARD_BANNER_75X66));
    }

    @Test
    public void testGetIconKey() throws Exception {
        assertNull(def.getIconKey());
    }
}
