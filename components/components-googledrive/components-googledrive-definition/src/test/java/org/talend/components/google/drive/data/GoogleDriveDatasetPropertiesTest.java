package org.talend.components.google.drive.data;

import static org.junit.Assert.*;
import static org.talend.components.google.drive.data.GoogleDriveDatasetProperties.ListMode.Both;
import static org.talend.components.google.drive.data.GoogleDriveDatasetProperties.ListMode.Directories;
import static org.talend.components.google.drive.data.GoogleDriveDatasetProperties.ListMode.Files;
import static org.talend.components.google.drive.data.GoogleDriveDatasetProperties.ListMode.valueOf;

import org.junit.Before;
import org.junit.Test;

public class GoogleDriveDatasetPropertiesTest extends GoogleDriveDatastoreDatasetBaseTest {

    private GoogleDriveDatasetProperties dataset;

    private GoogleDriveDatastoreProperties datastore;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        dataset = new GoogleDriveDatasetProperties("test");
        datastore = new GoogleDriveDatastoreProperties("test");
        dataset.setupProperties();
        dataset.setupLayout();
        dataset.setDatastoreProperties(datastore);
    }

    @Test
    public void testGetDatastoreProperties() throws Exception {
        assertNotNull(dataset.getDatastoreProperties());
    }

    @Test
    public void testSetupProperties() throws Exception {
        assertEquals("root", dataset.folder.getValue());
        assertEquals(Both, dataset.listMode.getValue());
        assertTrue(dataset.includeSubDirectories.getValue());
        assertFalse(dataset.includeTrashedFiles.getValue());
    }

    @Test
    public void testListMode() throws Exception {
        assertEquals("Files", Files.name());
        assertEquals(Files, valueOf("Files"));
        assertEquals("Directories", Directories.name());
        assertEquals(Directories, valueOf("Directories"));
        assertEquals("Both", Both.name());
        assertEquals(Both, valueOf("Both"));
    }

    @Test
    public void testGetSchema() throws Exception {
        assertEquals(datasetSchema, dataset.getSchema());
    }

}
