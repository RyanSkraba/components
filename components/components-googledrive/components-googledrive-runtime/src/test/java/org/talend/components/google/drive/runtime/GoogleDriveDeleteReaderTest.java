package org.talend.components.google.drive.runtime;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.google.drive.GoogleDriveComponentProperties.AccessMethod;
import org.talend.components.google.drive.delete.GoogleDriveDeleteDefinition;
import org.talend.components.google.drive.delete.GoogleDriveDeleteProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDriveDeleteReaderTest extends GoogleDriveTestBaseRuntime {

    GoogleDriveDeleteProperties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        //
        properties = new GoogleDriveDeleteProperties("test");
        properties.setupProperties();
        properties = (GoogleDriveDeleteProperties) setupConnectionWithInstalledApplicationWithJson(properties);
        //
        properties.file.setValue(FOLDER_DELETE);

        when(drive.files().update(anyString(), any(File.class)).execute()).thenReturn(null);
        when(drive.files().delete(anyString()).execute()).thenReturn(null);

        FileList fileList = new FileList();
        List<File> files = new ArrayList<>();
        File f = new File();
        f.setId(FOLDER_DELETE_ID);
        files.add(f);
        fileList.setFiles(files);

        when(drive.files().list().setQ(anyString()).execute()).thenReturn(fileList);
    }

    private void delete() throws java.io.IOException {
        source.initialize(container, properties);
        BoundedReader reader = source.createReader(container);
        assertTrue(reader.start());
        IndexedRecord record = (IndexedRecord) reader.getCurrent();
        assertNotNull(record);
        assertEquals(1, record.getSchema().getFields().size());
        assertEquals(FOLDER_DELETE_ID, record.get(0));
        reader.close();
        Map<String, Object> returnValues = reader.getReturnValues();
        assertNotNull(returnValues);
        assertEquals(FOLDER_DELETE_ID, returnValues.get(GoogleDriveDeleteDefinition.RETURN_FILE_ID));
    }

    @Test
    public void testDeleteByName() throws Exception {
        delete();
    }

    @Test
    public void testDeleteByNameWithoutTrash() throws Exception {
        properties.useTrash.setValue(false);
        delete();
    }

    @Test
    public void testDeleteById() throws Exception {
        properties.deleteMode.setValue(AccessMethod.Id);
        properties.file.setValue(FOLDER_DELETE_ID);
        //
        delete();
    }

    @Test
    public void testDeleteByIdWithoutTrash() throws Exception {
        properties.useTrash.setValue(false);
        properties.deleteMode.setValue(AccessMethod.Id);
        properties.file.setValue(FOLDER_DELETE_ID);
        //
        delete();
    }

    @Test
    public void testValidationOK() throws Exception {
        ValidationResult vr = source.initialize(container, properties);
        assertNotNull(vr);
        assertEquals(ValidationResult.OK.getStatus(), vr.getStatus());
    }

    @Test
    public void testValidationByName() throws Exception {
        properties.file.setValue("");
        source.initialize(container, properties);
        assertEquals(Result.ERROR, source.validate(container).getStatus());
    }

    @Test
    public void testValidationNewId() throws Exception {
        properties.deleteMode.setValue(AccessMethod.Id);
        properties.file.setValue("");
        source.initialize(container, properties);
        assertEquals(Result.ERROR, source.validate(container).getStatus());
    }

}
