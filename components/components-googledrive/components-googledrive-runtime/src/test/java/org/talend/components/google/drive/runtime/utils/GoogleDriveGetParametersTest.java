package org.talend.components.google.drive.runtime.utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.GoogleDriveMimeTypes;

public class GoogleDriveGetParametersTest {

    GoogleDriveGetParameters parameters;

    @Before
    public void setUp() throws Exception {
        parameters = new GoogleDriveGetParameters("resource", GoogleDriveMimeTypes.newDefaultMimeTypesSupported(), true,
                "outfile", true);
    }

    @Test
    public void testGetResourceName() throws Exception {
        assertEquals("resource", parameters.getResourceId());
    }

    @Test
    public void testGetMimeType() throws Exception {
        assertEquals(GoogleDriveMimeTypes.newDefaultMimeTypesSupported(), parameters.getMimeType());
    }

    @Test
    public void testIsStoreToLocal() throws Exception {
        assertTrue(parameters.isStoreToLocal());
    }

    @Test
    public void testGetOutputFileName() throws Exception {
        assertEquals("outfile", parameters.getOutputFileName());
    }

    @Test
    public void testIsAddExt() throws Exception {
        assertTrue(parameters.isAddExt());
    }

    @Test
    public void testSetOutputFileName() throws Exception {
        parameters.setOutputFileName("outputFile");
        assertEquals("outputFile", parameters.getOutputFileName());
    }

}
