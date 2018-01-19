package org.talend.components.google.drive.data;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

public class GoogleDriveInputPropertiesTest extends GoogleDriveDatastoreDatasetBaseTest {

    GoogleDriveInputProperties props;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        props = new GoogleDriveInputProperties("test");
        props.setupProperties();
        props.setupLayout();
        props.setDatasetProperties(new GoogleDriveDatasetProperties("test"));
    }

    @Test
    public void testGetDatasetProperties() throws Exception {
        assertNotNull(props.getDatasetProperties());
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() throws Exception {
        assertEquals(Collections.EMPTY_SET, props.getAllSchemaPropertiesConnectors(false));
        assertEquals(1, props.getAllSchemaPropertiesConnectors(true).size());
    }
}
