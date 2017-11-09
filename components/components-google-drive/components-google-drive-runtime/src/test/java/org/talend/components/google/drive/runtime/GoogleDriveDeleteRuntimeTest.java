package org.talend.components.google.drive.runtime;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.GoogleDriveComponentProperties.AccessMethod;
import org.talend.components.google.drive.delete.GoogleDriveDeleteDefinition;
import org.talend.components.google.drive.delete.GoogleDriveDeleteProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDriveDeleteRuntimeTest extends GoogleDriveTestBaseRuntime {

    GoogleDriveDeleteProperties properties;

    GoogleDriveDeleteRuntime testRuntime;

    FileList deleteFileList;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        properties = new GoogleDriveDeleteProperties("test");
        properties.setupProperties();
        properties = (GoogleDriveDeleteProperties) setupConnectionWithInstalledApplicationWithJson(properties);
        //
        properties.file.setValue(FOLDER_DELETE);

        testRuntime = spy(GoogleDriveDeleteRuntime.class);
        doReturn(drive).when(testRuntime).getDriveService();

        when(drive.files().update(anyString(), any(File.class)).execute()).thenReturn(null);
        when(drive.files().delete(anyString()).execute()).thenReturn(null);

        deleteFileList = new FileList();
        List<File> files = new ArrayList<>();
        File f = new File();
        f.setId(FOLDER_DELETE_ID);
        files.add(f);
        deleteFileList.setFiles(files);
    }

    @Test
    public void testDeleteByName() throws Exception {
        when(drive.files().list().setQ(anyString()).execute()).thenReturn(deleteFileList);
        testRuntime.initialize(container, properties);
        testRuntime.runAtDriver(container);
        assertEquals(FOLDER_DELETE_ID, container.getComponentData(TEST_CONTAINER, GoogleDriveDeleteDefinition.RETURN_FILE_ID));
    }

    @Test
    public void testDeleteByNamePath() throws Exception {
        String qA = "name='A' and 'root' in parents and mimeType='application/vnd.google-apps.folder'";
        String qB = "name='B' and 'A' in parents and mimeType='application/vnd.google-apps.folder'";
        String qC = "name='C' and 'B' in parents and mimeType='application/vnd.google-apps.folder'";
        String qD = "name='delete-id' and 'C' in parents and mimeType='application/vnd.google-apps.folder'";
        String qDn = "name='delete-id' and 'C' in parents and mimeType!='application/vnd.google-apps.folder'";
        when(drive.files().list().setQ(eq(qA)).execute()).thenReturn(createFolderFileList("A", false));
        when(drive.files().list().setQ(eq(qB)).execute()).thenReturn(createFolderFileList("B", false));
        when(drive.files().list().setQ(eq(qC)).execute()).thenReturn(deleteFileList);
        properties.file.setValue("/A/B/C");
        testRuntime.initialize(container, properties);
        testRuntime.runAtDriver(container);
        assertEquals(FOLDER_DELETE_ID, container.getComponentData(TEST_CONTAINER, GoogleDriveDeleteDefinition.RETURN_FILE_ID));
        //
        when(drive.files().list().setQ(eq(qC)).execute()).thenReturn(createFolderFileList("C", false));
        when(drive.files().list().setQ(eq(qD)).execute()).thenReturn(emptyFileList);
        when(drive.files().list().setQ(eq(qDn)).execute()).thenReturn(deleteFileList);
        properties.file.setValue("/A/B/C/delete-id");
        testRuntime.initialize(container, properties);
        testRuntime.runAtDriver(container);
        assertEquals(FOLDER_DELETE_ID, container.getComponentData(TEST_CONTAINER, GoogleDriveDeleteDefinition.RETURN_FILE_ID));
    }

    @Test
    public void testDeleteById() throws Exception {
        properties.deleteMode.setValue(AccessMethod.Id);
        properties.file.setValue(FOLDER_DELETE_ID);
        //
        testRuntime.initialize(container, properties);
        testRuntime.runAtDriver(container);
        assertEquals(FOLDER_DELETE_ID, container.getComponentData(TEST_CONTAINER, GoogleDriveDeleteDefinition.RETURN_FILE_ID));
    }

    @Test
    public void testFailedValidation() throws Exception {
        when(((GoogleDriveRuntime) testRuntime).initialize(container, properties))
                .thenReturn(new ValidationResult(Result.ERROR, "Invalid"));
        ValidationResult vr = testRuntime.initialize(container, properties);
        assertNotNull(vr);
        assertEquals(Result.ERROR, vr.getStatus());
    }

    @Test
    public void testExceptionThrown() throws Exception {
        when(drive.files().update(anyString(), any(File.class)).execute()).thenThrow(new IOException("error"));
        testRuntime.initialize(container, properties);
        try {
            testRuntime.runAtDriver(container);
            fail("Should not be here");
        } catch (Exception e) {
        }
    }

}
