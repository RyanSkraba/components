package org.talend.components.google.drive.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.put.GoogleDrivePutProperties;

public class GoogleDriveWriteOperationTest extends GoogleDriveTestBaseRuntime {

    private GoogleDrivePutProperties properties;

    private GoogleDriveWriteOperation wop;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        properties = new GoogleDrivePutProperties("test");
        properties.connection.setupProperties();
        properties.connection.setupLayout();
        properties.schemaMain.setupProperties();
        properties.schemaMain.setupLayout();
        properties = (GoogleDrivePutProperties) setupConnectionWithInstalledApplicationWithIdAndSecret(properties);
        properties.setupLayout();

        sink = new GoogleDriveSink();
        sink.initialize(container, properties);
        wop = new GoogleDriveWriteOperation(sink);
    }

    @Test
    public void testFinalize() throws Exception {
        assertNull(wop.finalize(null, container));
    }

    @Test
    public void testCreateWriter() throws Exception {
        assertEquals(GoogleDrivePutWriter.class, wop.createWriter(container).getClass());
    }

    @Test()
    public void testGetSink() throws Exception {
        assertEquals(sink, wop.getSink());
    }

}
