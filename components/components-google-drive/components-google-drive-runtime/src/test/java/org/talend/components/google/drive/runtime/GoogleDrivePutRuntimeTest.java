package org.talend.components.google.drive.runtime;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.put.GoogleDrivePutDefinition;
import org.talend.components.google.drive.put.GoogleDrivePutProperties;
import org.talend.components.google.drive.put.GoogleDrivePutProperties.UploadMode;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDrivePutRuntimeTest extends GoogleDriveTestBaseRuntime {

    public static final String PUT_FILE_ID = "put-fileName-id";

    public static final String PUT_FILE_PARENT_ID = "put-fileName-parent-id";

    public static final String FILE_PUT_NAME = "fileName-put-name";

    private GoogleDrivePutProperties properties;

    private GoogleDrivePutRuntime testRuntime;

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
        properties.localFilePath.setValue("c:/Users/undx/brasil.jpg");
        properties.overwrite.setValue(true);
        properties.destinationFolder.setValue("root");

        testRuntime = spy(GoogleDrivePutRuntime.class);
        doReturn(drive).when(testRuntime).getDriveService();

        when(drive.files().list().setQ(anyString()).execute()).thenReturn(emptyFileList);
        //
        File putFile = new File();
        putFile.setId(PUT_FILE_ID);
        putFile.setParents(Collections.singletonList(PUT_FILE_PARENT_ID));
        when(drive.files().create(any(File.class), any(AbstractInputStreamContent.class)).setFields(anyString()).execute())
                .thenReturn(putFile);

    }

    @Test
    public void testInitialize() throws Exception {
        assertEquals(Result.OK, testRuntime.initialize(container, properties).getStatus());
    }

    @Test
    public void testValidate() throws Exception {
        assertEquals(Result.OK, testRuntime.validatePutProperties(properties).getStatus());
    }

    @Test
    public void testValidateUploadMode() throws Exception {
        properties.uploadMode.setValue(UploadMode.READ_CONTENT_FROM_INPUT);
        testRuntime.initialize(container, properties);
        assertEquals(Result.ERROR, testRuntime.validatePutProperties(properties).getStatus());
    }

    @Test
    public void testValidateDestinationFolder() throws Exception {
        properties.destinationFolder.setValue("");
        testRuntime.initialize(container, properties);
        assertEquals(Result.ERROR, testRuntime.validatePutProperties(properties).getStatus());
    }

    @Test
    public void testValidateLocalFilePath() throws Exception {
        properties.localFilePath.setValue("");
        testRuntime.initialize(container, properties);
        assertEquals(Result.ERROR, testRuntime.validatePutProperties(properties).getStatus());
    }

    @Test
    public void testFailedValidation() throws Exception {
        properties.fileName.setValue("");
        ValidationResult vr = testRuntime.initialize(container, properties);
        assertNotNull(vr);
        assertEquals(Result.ERROR, vr.getStatus());
    }

    @Test
    public void testExceptionThrown() throws Exception {
        when(drive.files().list().setQ(anyString()).execute()).thenThrow(new IOException("error"));
        testRuntime.initialize(container, properties);
        try {
            testRuntime.runAtDriver(container);
            fail("Should not be here");
        } catch (Exception e) {
        }
    }

    @Test
    public void testRunAtDriver() throws Exception {
        testRuntime.initialize(container, properties);
        testRuntime.runAtDriver(container);
        assertNull(container.getComponentData(TEST_CONTAINER, GoogleDrivePutDefinition.RETURN_CONTENT));
        assertEquals(PUT_FILE_ID, container.getComponentData(TEST_CONTAINER, GoogleDrivePutDefinition.RETURN_FILE_ID));
        assertEquals(PUT_FILE_PARENT_ID,
                container.getComponentData(TEST_CONTAINER, GoogleDrivePutDefinition.RETURN_PARENT_FOLDER_ID));
    }

    @Test
    public void testRunAtDriverTooManyFiles() throws Exception {
        FileList hasfilelist = new FileList();
        List<File> hfiles = new ArrayList<>();
        File hfile = new File();
        hfile.setId(FILE_PUT_NAME);
        hfiles.add(hfile);
        hfiles.add(new File());
        hasfilelist.setFiles(hfiles);
        when(drive.files().list().setQ(anyString()).execute()).thenReturn(hasfilelist);
        properties.overwrite.setValue(true);
        testRuntime.initialize(container, properties);
        try {
            testRuntime.runAtDriver(container);
            fail("Should not be here");
        } catch (Exception e) {
        }
    }

    @Test
    public void testRunAtDriverOverwrite() throws Exception {
        FileList hasfilelist = new FileList();
        List<File> hfiles = new ArrayList<>();
        File hfile = new File();
        hfile.setId(FILE_PUT_NAME);
        hfiles.add(hfile);
        hasfilelist.setFiles(hfiles);
        when(drive.files().list().setQ(anyString()).execute()).thenReturn(hasfilelist);
        properties.overwrite.setValue(true);
        testRuntime.initialize(container, properties);
        testRuntime.runAtDriver(container);
        assertNull(container.getComponentData(TEST_CONTAINER, GoogleDrivePutDefinition.RETURN_CONTENT));
        assertEquals(PUT_FILE_ID, container.getComponentData(TEST_CONTAINER, GoogleDrivePutDefinition.RETURN_FILE_ID));
        assertEquals(PUT_FILE_PARENT_ID,
                container.getComponentData(TEST_CONTAINER, GoogleDrivePutDefinition.RETURN_PARENT_FOLDER_ID));
    }

    @Test
    public void testRunAtDriverOverwriteError() throws Exception {
        FileList hasfilelist = new FileList();
        List<File> hfiles = new ArrayList<>();
        File hfile = new File();
        hfile.setId(FILE_PUT_NAME);
        hfiles.add(hfile);
        hasfilelist.setFiles(hfiles);
        when(drive.files().list().setQ(anyString()).execute()).thenReturn(hasfilelist);
        properties.overwrite.setValue(false);
        testRuntime.initialize(container, properties);
        try {
            testRuntime.runAtDriver(container);
            fail("Should not be here");
        } catch (Exception e) {
        }
    }

}
