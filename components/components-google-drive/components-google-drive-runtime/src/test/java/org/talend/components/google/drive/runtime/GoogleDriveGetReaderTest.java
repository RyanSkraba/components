package org.talend.components.google.drive.runtime;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.google.drive.GoogleDriveMimeTypes;
import org.talend.components.google.drive.get.GoogleDriveGetDefinition;
import org.talend.components.google.drive.get.GoogleDriveGetProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDriveGetReaderTest extends GoogleDriveTestBaseRuntime {

    public static final String FILE_GET_ID = "fileName-get-id";

    private GoogleDriveGetProperties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        //
        properties = new GoogleDriveGetProperties("test");
        properties.setupProperties();
        properties = (GoogleDriveGetProperties) setupConnectionWithInstalledApplicationWithJson(properties);
        //
        properties.file.setValue("google-drive-get");

        FileList fileList = new FileList();
        List<File> files = new ArrayList<>();
        File f = new File();
        f.setId(FILE_GET_ID);
        files.add(f);
        fileList.setFiles(files);
        when(drive.files().list().setQ(anyString()).execute()).thenReturn(fileList);

        File file = new File();
        file.setId(FILE_GET_ID);
        file.setMimeType(GoogleDriveMimeTypes.MIME_TYPE_JSON);
        file.setFileExtension("json");
        when(drive.files().get(anyString()).setFields(anyString()).execute()).thenReturn(file);

    }

    @Test
    public void testStart() throws Exception {
        ValidationResult vr = source.initialize(container, properties);
        assertNotNull(vr);
        assertEquals(Result.OK, vr.getStatus());
        BoundedReader reader = source.createReader(container);
        assertTrue(reader.start());
        IndexedRecord record = (IndexedRecord) reader.getCurrent();
        assertNotNull(record);
        assertEquals(1, record.getSchema().getFields().size());
        assertFalse(reader.advance());
        reader.close();
        Map<String, Object> returnValues = reader.getReturnValues();
        assertEquals(FILE_GET_ID, returnValues.get(GoogleDriveGetDefinition.RETURN_FILE_ID));
        assertNull(returnValues.get(GoogleDriveGetDefinition.RETURN_CONTENT));
    }

}
