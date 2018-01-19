package org.talend.components.google.drive.runtime;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.list.GoogleDriveListProperties;
import org.talend.daikon.properties.ValidationResult.Result;

import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDriveListReaderTest extends GoogleDriveTestBaseRuntime {

    GoogleDriveListProperties properties;

    List mockList;

    String qA = "name='A' and 'root' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false";

    String qGSA = "name='A' and mimeType='application/vnd.google-apps.folder'";

    String qB = "name='B' and 'A' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false";

    String qC = "name='C' and 'B' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        // stubbing
        mockList = mock(List.class, RETURNS_DEEP_STUBS);
        when(drive.files().list()).thenReturn(mockList);
        when(drive.files().list().setQ(eq(qA)).execute()).thenReturn(createFolderFileList("A", false));
        when(drive.files().list().setQ(eq(qGSA)).execute()).thenReturn(createFolderFileList("A", false));
        when(drive.files().list().setQ(eq(qB)).execute()).thenReturn(createFolderFileList("B", false));
        when(drive.files().list().setQ(eq(qC)).execute()).thenReturn(createFolderFileList("C", false));

        //
        properties = new GoogleDriveListProperties("test");
        properties.setupProperties();
        properties = (GoogleDriveListProperties) setupConnectionWithInstalledApplicationWithIdAndSecret(properties);
        properties.folder.setValue(FOLDER_ROOT);
    }

    @Test
    public void testStartOnly() throws Exception {
        FileList fileList = new FileList();
        File f = new File();
        f.setName("sd");
        f.setMimeType("text/text");
        f.setId("id-1");
        f.setModifiedTime(com.google.api.client.util.DateTime.parseRfc3339("2017-09-29T10:00:00"));
        f.setSize(100L);
        f.setKind("drive#fileName");
        f.setTrashed(false);
        f.setParents(Collections.singletonList(FOLDER_ROOT));
        f.setWebViewLink("https://toto.com");
        fileList.setFiles(Arrays.asList(f));

        when(mockList.execute()).thenReturn(fileList);
        //
        source.initialize(container, properties);
        GoogleDriveListReader reader = ((GoogleDriveListReader) source.createReader(container));
        assertTrue(reader.start());
        IndexedRecord record = (IndexedRecord) reader.getCurrent();
        assertNotNull(record);
        assertEquals(9, record.getSchema().getFields().size());
        assertEquals("id-1", record.get(0));
        assertEquals("sd", record.get(1));
        assertFalse(reader.advance());
        reader.close();
    }

    @Test
    public void testAdvance() throws Exception {
        FileList fileList = new FileList();
        for (int i = 0; i < 5; i++) {
            File f = new File();
            f.setName("sd" + i);
            f.setMimeType("text/text");
            f.setId("id-" + i);
            f.setModifiedTime(com.google.api.client.util.DateTime.parseRfc3339("2017-09-29T10:00:00"));
            f.setSize(100L);
            f.setKind("drive#fileName");
            f.setTrashed(false);
            f.setParents(Collections.singletonList(FOLDER_ROOT));
            f.setWebViewLink("https://toto.com");
            fileList.setFiles(Arrays.asList(f));
        }
        when(mockList.execute()).thenReturn(fileList);
        //
        properties.folder.setValue("A");
        source.initialize(container, properties);
        GoogleDriveListReader reader = ((GoogleDriveListReader) source.createReader(container));
        assertTrue(reader.start());
        while (reader.advance()) {
            assertNotNull(reader.getCurrent());
        }
        reader.close();
    }

    @Test
    public void testCheckPathWithEmptyPath() throws Exception {
        when(drive.files().list().setQ(eq(qA)).execute()).thenReturn(emptyFileList);
        when(drive.files().list().setQ(eq(qB)).execute()).thenReturn(emptyFileList);
        when(drive.files().list().setQ(eq(qC)).execute()).thenReturn(emptyFileList);

        properties.folder.setValue("/A/B/C");
        source.initialize(container, properties);
        GoogleDriveListReader reader = ((GoogleDriveListReader) source.createReader(container));
        assertFalse(reader.start());
    }

    @Test
    public void testCheckPathWithDuplicatedPath() throws Exception {
        when(drive.files().list().setQ(eq(qA)).execute()).thenReturn(createFolderFileList("A", false));
        when(drive.files().list().setQ(eq(qB)).execute()).thenReturn(createFolderFileList("B", true));
        when(drive.files().list().setQ(eq(qC)).execute()).thenReturn(createFolderFileList("C", false));
        when(mockList.execute()).thenReturn(emptyFileList);
        //
        properties.folder.setValue("/A/B/C");
        source.initialize(container, properties);
        GoogleDriveListReader reader = ((GoogleDriveListReader) source.createReader(container));
        assertFalse(reader.start());
    }

    @Test
    public void testCheckPathWithDuplicatedPathLastLevel() throws Exception {
        when(drive.files().list().setQ(eq(qA)).execute()).thenReturn(createFolderFileList("A", false));
        when(drive.files().list().setQ(eq(qB)).execute()).thenReturn(createFolderFileList("B", false));
        when(drive.files().list().setQ(eq(qC)).execute()).thenReturn(createFolderFileList("C", true));
        when(mockList.execute()).thenReturn(emptyFileList);
        //
        properties.folder.setValue("/A/B/C");
        source.initialize(container, properties);
        GoogleDriveListReader reader = ((GoogleDriveListReader) source.createReader(container));
        assertFalse(reader.start());
    }

    @Test
    public void testValidationOK() throws Exception {
        assertEquals(Result.OK, source.initialize(container, properties).getStatus());
        assertEquals(Result.OK, source.validate(container).getStatus());
    }

    @Test
    public void testValidationFolder() throws Exception {
        properties.folder.setValue("");
        source.initialize(container, properties);
        assertEquals(Result.ERROR, source.validate(container).getStatus());
    }

}
