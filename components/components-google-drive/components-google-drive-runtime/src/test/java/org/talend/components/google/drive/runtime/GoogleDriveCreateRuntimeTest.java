package org.talend.components.google.drive.runtime;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.talend.components.google.drive.runtime.GoogleDriveRuntime.getStudioName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.google.drive.create.GoogleDriveCreateDefinition;
import org.talend.components.google.drive.create.GoogleDriveCreateProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDriveCreateRuntimeTest extends GoogleDriveTestBaseRuntime {

    protected GoogleDriveCreateRuntime testRuntime;

    protected GoogleDriveCreateProperties properties;

    String qA = "name='A' and 'root' in parents and mimeType='application/vnd.google-apps.folder'";

    String qB = "name='B' and 'A' in parents and mimeType='application/vnd.google-apps.folder'";

    String qC = "name='C' and 'B' in parents and mimeType='application/vnd.google-apps.folder'";

    @Before
    public void setUp() throws Exception {
        super.setUp();

        properties = new GoogleDriveCreateProperties("test");
        properties.setupProperties();
        properties = (GoogleDriveCreateProperties) setupConnectionWithInstalledApplicationWithIdAndSecret(properties);
        //
        properties.parentFolder.setValue(FOLDER_ROOT);
        properties.newFolder.setValue(FOLDER_CREATE);

        testRuntime = spy(GoogleDriveCreateRuntime.class);
        doReturn(drive).when(testRuntime).getDriveService();
        File fc = new File();
        fc.setId(FOLDER_CREATE_ID);
        when(drive.files().create(any(File.class)).setFields(eq("id")).execute()).thenReturn(fc);
        //

        FileList flA = new FileList();
        List<File> fs = new ArrayList<>();
        File fA = new File();
        fA.setId("A");
        fs.add(fA);
        flA.setFiles(fs);
        when(drive.files().list().setQ(eq(qA)).execute()).thenReturn(flA);
        FileList flB = new FileList();
        List<File> fsA = new ArrayList<>();
        File fB = new File();
        fB.setId("B");
        fsA.add(fB);
        flB.setFiles(fsA);
        when(drive.files().list().setQ(eq(qB)).execute()).thenReturn(flB);
        FileList flC = new FileList();
        List<File> fsC = new ArrayList<>();
        File fC = new File();
        fC.setId("C");
        fsC.add(fC);
        flC.setFiles(fsC);
        when(drive.files().list().setQ(eq(qC)).execute()).thenReturn(flC);
    }

    @Test
    public void testRunAtDriverOnRoot() throws Exception {
        testRuntime.initialize(container, properties);
        testRuntime.runAtDriver(container);
        assertEquals(FOLDER_ROOT,
                container.getComponentData(TEST_CONTAINER, getStudioName(GoogleDriveCreateDefinition.RETURN_PARENT_FOLDER_ID)));
        assertEquals(FOLDER_CREATE_ID,
                container.getComponentData(TEST_CONTAINER, getStudioName(GoogleDriveCreateDefinition.RETURN_NEW_FOLDER_ID)));
    }

    @Test
    public void testRunAtDriverNestedPath() throws Exception {
        properties.parentFolder.setValue("/A/B/C/");
        testRuntime.initialize(container, properties);
        testRuntime.runAtDriver(container);
        assertEquals("C",
                container.getComponentData(TEST_CONTAINER, getStudioName(GoogleDriveCreateDefinition.RETURN_PARENT_FOLDER_ID)));
        assertEquals(FOLDER_CREATE_ID,
                container.getComponentData(TEST_CONTAINER, getStudioName(GoogleDriveCreateDefinition.RETURN_NEW_FOLDER_ID)));
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
    public void testExceptionThrownInRuntime() throws Exception {
        when(drive.files().create(any(File.class)).setFields("id").execute().getId()).thenThrow(new IOException("error"));
        testRuntime.initialize(container, properties);
        try {
            testRuntime.runAtDriver(container);
            fail("Should not be here");
        } catch (Exception e) {
        }
    }

    @Test(expected = ComponentException.class)
    public void testManyResourcesMatching() throws Exception {
        FileList flA = new FileList();
        List<File> fs = new ArrayList<>();
        File fA = new File();
        fA.setId("A");
        fs.add(fA);
        File fAp = new File();
        fAp.setId("A");
        fs.add(fAp);
        flA.setFiles(fs);
        when(drive.files().list().setQ(eq(qA)).execute()).thenReturn(flA);

        properties.parentFolder.setValue("/A");
        testRuntime.initialize(container, properties);
        testRuntime.runAtDriver(container);
        fail("Should not be here");
    }

    @Test(expected = ComponentException.class)
    public void testNoResourcesMatching() throws Exception {
        when(drive.files().list().setQ(eq(qA)).execute()).thenReturn(emptyFileList);

        properties.parentFolder.setValue("/A");
        testRuntime.initialize(container, properties);
        testRuntime.runAtDriver(container);
        fail("Should not be here");
    }
}
