package org.talend.components.google.drive.runtime;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.google.drive.GoogleDriveMimeTypes;
import org.talend.components.google.drive.copy.GoogleDriveCopyDefinition;
import org.talend.components.google.drive.copy.GoogleDriveCopyProperties;
import org.talend.components.google.drive.copy.GoogleDriveCopyProperties.CopyMode;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDriveCopyReaderTest extends GoogleDriveTestBaseRuntime {

    public static final String FILE_COPY_NAME = "fileName-copy-name";

    private GoogleDriveCopyProperties properties;

    private String SOURCE_ID = "source-id";

    private String DESTINATION_ID = "destination-id";

    @Before
    public void setUp() throws Exception {
        super.setUp();

        properties = new GoogleDriveCopyProperties("test");
        properties.setupProperties();
        properties = (GoogleDriveCopyProperties) setupConnectionWithInstalledApplicationWithIdAndSecret(properties);
        //
        properties.copyMode.setValue(CopyMode.File);
        properties.source.setValue(FILE_COPY_NAME);
        properties.destinationFolder.setValue("/A");
        properties.newName.setValue("newName");
        // source fileName/folder
        File dest = new File();
        dest.setId(SOURCE_ID);
        FileList list = new FileList();
        List<File> files = new ArrayList<>();
        files.add(dest);
        list.setFiles(files);
        final String q1 = "name='A' and 'root' in parents and mimeType='application/vnd.google-apps.folder'";
        final String q2 = "name='" + FILE_COPY_NAME + "' and mimeType!='application/vnd.google-apps.folder'";

        when(drive.files().list().setQ(eq(q1)).execute()).thenReturn(list);
        when(drive.files().list().setQ(eq(q2)).execute()).thenReturn(list);

        // destination/copied
        File copiedFile = new File();
        copiedFile.setId(DESTINATION_ID);
        copiedFile.setParents(Collections.singletonList(SOURCE_ID));
        when(drive.files().copy(anyString(), any(File.class)).setFields(anyString()).execute()).thenReturn(copiedFile);

        File destFolder = new File();
        destFolder.setId(DESTINATION_ID);
        destFolder.setParents(Collections.singletonList(SOURCE_ID));
        when(drive.files().create(any(File.class)).setFields(anyString()).execute()).thenReturn(destFolder);
    }

    @Test
    public void testInitialize() throws Exception {
        ValidationResult vr = source.initialize(container, properties);
        assertEquals(Result.OK, vr.getStatus());
    }

    @Test
    public void testValidate() throws Exception {
        source.initialize(container, properties);
        assertEquals(Result.OK, source.validate(container).getStatus());
    }

    @Test
    public void testValidateFileName() throws Exception {
        properties.source.setValue("");
        assertValidationIsFalse();
    }

    @Test
    public void testValidateDestinationFolder() throws Exception {
        properties.destinationFolder.setValue("");
        assertValidationIsFalse();
    }

    @Test
    public void testValidateFolderName() throws Exception {
        properties.copyMode.setValue(CopyMode.Folder);
        properties.source.setValue("");
        assertValidationIsFalse();
    }

    @Test
    public void testValidateRename() throws Exception {
        properties.rename.setValue(true);
        properties.newName.setValue("");
        assertValidationIsFalse();
    }

    private void assertValidationIsFalse() {
        source.initialize(container, properties);
        assertEquals(Result.ERROR, source.validate(container).getStatus());
    }

    @Test
    public void testStartCopyFile() throws Exception {
        source.initialize(container, properties);
        BoundedReader reader = source.createReader(container);
        assertTrue(reader.start());
        IndexedRecord record = (IndexedRecord) reader.getCurrent();
        assertNotNull(record);
        assertEquals(2, record.getSchema().getFields().size());
        assertEquals(SOURCE_ID, record.get(0));
        assertEquals(DESTINATION_ID, record.get(1));
        reader.close();
        Map<String, Object> returnValues = reader.getReturnValues();
        assertNotNull(returnValues);
        assertEquals(SOURCE_ID, returnValues.get(GoogleDriveCopyDefinition.RETURN_SOURCE_ID));
        assertEquals(DESTINATION_ID, returnValues.get(GoogleDriveCopyDefinition.RETURN_DESTINATION_ID));
    }

    @Test
    public void testStartCopyFileAndRemove() throws Exception {
        properties.deleteSourceFile.setValue(true);
        source.initialize(container, properties);
        BoundedReader reader = source.createReader(container);
        assertTrue(reader.start());
        IndexedRecord record = (IndexedRecord) reader.getCurrent();
        assertNotNull(record);
        assertEquals(2, record.getSchema().getFields().size());
        assertEquals(SOURCE_ID, record.get(0));
        assertEquals(DESTINATION_ID, record.get(1));
        reader.close();
        Map<String, Object> returnValues = reader.getReturnValues();
        assertNotNull(returnValues);
        assertEquals(SOURCE_ID, returnValues.get(GoogleDriveCopyDefinition.RETURN_SOURCE_ID));
        assertEquals(DESTINATION_ID, returnValues.get(GoogleDriveCopyDefinition.RETURN_DESTINATION_ID));
    }

    @Test
    public void testStartCopyFileAndRename() throws Exception {
        properties.rename.setValue(true);
        source.initialize(container, properties);
        BoundedReader reader = source.createReader(container);
        assertTrue(reader.start());
        IndexedRecord record = (IndexedRecord) reader.getCurrent();
        assertNotNull(record);
        assertEquals(2, record.getSchema().getFields().size());
        assertEquals(SOURCE_ID, record.get(0));
        assertEquals(DESTINATION_ID, record.get(1));
        reader.close();
        Map<String, Object> returnValues = reader.getReturnValues();
        assertNotNull(returnValues);
        assertEquals(SOURCE_ID, returnValues.get(GoogleDriveCopyDefinition.RETURN_SOURCE_ID));
        assertEquals(DESTINATION_ID, returnValues.get(GoogleDriveCopyDefinition.RETURN_DESTINATION_ID));
    }

    @Test
    public void testStartCopyFolder() throws Exception {
        final String q1 = "name='folder' and 'root' in parents and mimeType='application/vnd.google-apps.folder'";
        final String q2 = "'" + SOURCE_ID + "' in parents and trashed=false";
        final String q3 = "'folder-id2' in parents and trashed=false";
        //
        FileList fsource = new FileList();
        List<File> fsfiles = new ArrayList<>();
        File fsfolder = new File();
        fsfolder.setMimeType(GoogleDriveMimeTypes.MIME_TYPE_FOLDER);
        fsfolder.setName("folder");
        fsfolder.setId(SOURCE_ID);
        fsfiles.add(fsfolder);
        fsource.setFiles(fsfiles);
        when(drive.files().list().setQ(eq(q1)).execute()).thenReturn(fsource);

        FileList flist = new FileList();
        List<File> ffiles = new ArrayList<>();
        File ffile = new File();
        ffile.setMimeType(GoogleDriveMimeTypes.MIME_TYPE_CSV);
        ffile.setName("fileName");
        ffile.setId("fileName-id");
        ffiles.add(ffile);
        File ffolder = new File();
        ffolder.setMimeType(GoogleDriveMimeTypes.MIME_TYPE_FOLDER);
        ffolder.setName("folder");
        ffolder.setId("folder-id2");
        ffiles.add(ffolder);
        flist.setFiles(ffiles);
        when(drive.files().list().setQ(eq(q2)).execute()).thenReturn(flist);
        when(drive.files().list().setQ(eq(q3)).execute()).thenReturn(emptyFileList);

        properties.copyMode.setValue(CopyMode.Folder);
        properties.source.setValue("/folder");
        source.initialize(container, properties);
        BoundedReader reader = source.createReader(container);
        assertTrue(reader.start());
        IndexedRecord record = (IndexedRecord) reader.getCurrent();
        assertNotNull(record);
        assertEquals(2, record.getSchema().getFields().size());
        assertEquals(SOURCE_ID, record.get(0));
        assertEquals(DESTINATION_ID, record.get(1));
        reader.close();
        Map<String, Object> returnValues = reader.getReturnValues();
        assertNotNull(returnValues);
        assertEquals(SOURCE_ID, returnValues.get(GoogleDriveCopyDefinition.RETURN_SOURCE_ID));
        assertEquals(DESTINATION_ID, returnValues.get(GoogleDriveCopyDefinition.RETURN_DESTINATION_ID));
    }

}
