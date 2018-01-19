package org.talend.components.google.drive.runtime.data;

import static org.junit.Assert.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.google.drive.runtime.GoogleDriveUtils;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDriveInputReaderTest extends GoogleDriveDataBaseTest {

    GoogleDriveInputReader reader;

    @Before
    public void setUp() throws Exception {
        super.setUp();

    }

    @Test(expected = ComponentException.class)
    public void testException() throws Exception {
        dataSource = spy(dataSource);
        Drive drive = mock(Drive.class, RETURNS_DEEP_STUBS);
        GoogleDriveUtils utils = mock(GoogleDriveUtils.class, RETURNS_DEEP_STUBS);
        doReturn(drive).when(dataSource).getDriveService();
        doThrow(new IOException()).when(dataSource).getDriveUtils();

        dataSource.initialize(container, inputProperties);
        reader = (GoogleDriveInputReader) dataSource.createReader(container);
        fail("Should not be here");
    }

    @Test
    public void testAdvance() throws Exception {
        dataSource = spy(dataSource);
        Drive drive = mock(Drive.class, RETURNS_DEEP_STUBS);
        GoogleDriveUtils utils = mock(GoogleDriveUtils.class, RETURNS_DEEP_STUBS);
        doReturn(drive).when(dataSource).getDriveService();
        doReturn(utils).when(dataSource).getDriveUtils();

        List mockList = mock(List.class, RETURNS_DEEP_STUBS);
        when(drive.files().list()).thenReturn(mockList);
        //
        // String qA = "name='A' and 'root' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false";
        //
        // when(drive.files().list().setQ(eq(qA)).execute()).thenReturn(createFolderFileList("A", false));
        //
        // GoogleDriveAbstractListReader alr = mock(GoogleDriveAbstractListReader.class);
        // doReturn(true).when(alr).start();

        inputProperties.getDatasetProperties().folder.setValue("A");

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
        fileList.setFiles(Arrays.asList(f, f, f, f, f));

        when(mockList.execute()).thenReturn(fileList);

        dataSource.initialize(container, inputProperties);
        reader = (GoogleDriveInputReader) dataSource.createReader(container);
        reader.setLimit(2);
        assertTrue(reader.start());
        reader.getCurrent();
        assertTrue(reader.advance());
        reader.getCurrent();
        assertFalse(reader.advance());
    }

    @Test
    public void testGetLimit() throws Exception {
        dataSource = spy(dataSource);
        Drive drive = mock(Drive.class, RETURNS_DEEP_STUBS);
        GoogleDriveUtils utils = mock(GoogleDriveUtils.class, RETURNS_DEEP_STUBS);
        doReturn(drive).when(dataSource).getDriveService();
        doReturn(utils).when(dataSource).getDriveUtils();

        dataSource.initialize(container, inputProperties);
        reader = (GoogleDriveInputReader) dataSource.createReader(container);
        reader.setLimit(30);
        assertEquals(30, reader.getLimit());
    }

}
