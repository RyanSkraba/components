package org.talend.components.google.drive.runtime;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.google.drive.put.GoogleDrivePutDefinition;
import org.talend.components.google.drive.put.GoogleDrivePutProperties;
import org.talend.components.google.drive.put.GoogleDrivePutProperties.UploadMode;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.services.drive.model.File;

public class GoogleDrivePutReaderTest extends GoogleDriveTestBaseRuntime {

    public static final String PUT_FILE_ID = "put-fileName-id";

    public static final String PUT_FILE_PARENT_ID = "put-fileName-parent-id";

    public static final String FILE_PUT_NAME = "fileName-put-name";

    private GoogleDrivePutProperties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        properties = new GoogleDrivePutProperties("test");
        properties.connection.setupProperties();
        properties.connection.setupLayout();
        properties.schemaMain.setupProperties();
        properties.schemaMain.setupLayout();
        properties.setupProperties();
        properties.setupLayout();
        properties = (GoogleDrivePutProperties) setupConnectionWithAccessToken(properties);
        properties.uploadMode.setValue(UploadMode.UPLOAD_LOCAL_FILE);
        properties.fileName.setValue(FILE_PUT_NAME);
        properties.localFilePath
                .setValue(Paths.get(getClass().getClassLoader().getResource("service_account.json").toURI()).toString());
        properties.overwrite.setValue(true);
        properties.destinationFolder.setValue("root");

        when(drive.files().list().setQ(anyString()).execute()).thenReturn(emptyFileList);
        //
        File putFile = new File();
        putFile.setId(PUT_FILE_ID);
        putFile.setParents(Collections.singletonList(PUT_FILE_PARENT_ID));
        when(drive.files().create(any(File.class), any(AbstractInputStreamContent.class)).setFields(anyString()).execute())
                .thenReturn(putFile);

    }

    @Test
    public void testStart() throws Exception {
        source.initialize(container, properties);
        BoundedReader reader = source.createReader(container);
        assertTrue(reader.start());
        reader.close();
        IndexedRecord record = (IndexedRecord) reader.getCurrent();
        assertNotNull(record);
        assertNotNull(record.get(0));
        assertEquals(PUT_FILE_PARENT_ID, record.get(1));
        assertEquals(PUT_FILE_ID, record.get(2));
        Map result = reader.getReturnValues();
        assertEquals(PUT_FILE_ID, result.get(GoogleDrivePutDefinition.RETURN_FILE_ID));
        assertEquals(PUT_FILE_PARENT_ID, result.get(GoogleDrivePutDefinition.RETURN_PARENT_FOLDER_ID));
    }
}
