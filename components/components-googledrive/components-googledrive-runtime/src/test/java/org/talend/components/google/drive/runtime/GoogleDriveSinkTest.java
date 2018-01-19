package org.talend.components.google.drive.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;
import org.talend.daikon.properties.ValidationResult.Result;

public class GoogleDriveSinkTest extends GoogleDriveTestBaseRuntime {

    private GoogleDriveConnectionProperties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        properties = new GoogleDriveConnectionProperties("test");
        properties.setupProperties();
        properties = (GoogleDriveConnectionProperties) setupConnectionWithInstalledApplicationWithIdAndSecret(properties);
        properties.setupLayout();

        sink = new GoogleDriveSink();
        sink.initialize(container, properties);
    }

    @Test
    public void testValidate() throws Exception {
        assertEquals(Result.OK, sink.validate(container).getStatus());
    }

    @Test
    public void testCreateWriteOperation() throws Exception {
        assertNotNull(sink.createWriteOperation());
    }

}
