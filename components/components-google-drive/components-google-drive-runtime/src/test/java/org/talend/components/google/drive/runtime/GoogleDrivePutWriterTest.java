package org.talend.components.google.drive.runtime;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.google.drive.put.GoogleDrivePutProperties;
import org.talend.components.google.drive.put.GoogleDrivePutProperties.UploadMode;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.services.drive.model.File;

public class GoogleDrivePutWriterTest extends GoogleDriveTestBaseRuntime {

    public static final String PUT_FILE_ID = "put-fileName-id";

    public static final String PUT_FILE_PARENT_ID = "put-fileName-parent-id";

    private GoogleDrivePutWriter writer;

    private GoogleDrivePutProperties properties;

    private GoogleDriveWriteOperation wop;

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
        properties.fileName.setValue("GoogleDrive Put test BR");
        properties.localFilePath.setValue("c:/Users/undx/brasil.jpg");
        properties.overwrite.setValue(true);
        properties.destinationFolder.setValue("root");

        sink.initialize(container, properties);
        wop = (GoogleDriveWriteOperation) sink.createWriteOperation();
        writer = new GoogleDrivePutWriter(wop, properties, container);

        when(drive.files().list().setQ(anyString()).execute()).thenReturn(emptyFileList);
        //
        File putFile = new File();
        putFile.setId(PUT_FILE_ID);
        putFile.setParents(Collections.singletonList(PUT_FILE_PARENT_ID));
        when(drive.files().create(any(File.class), any(AbstractInputStreamContent.class)).setFields(anyString()).execute())
                .thenReturn(putFile);
    }

    @Test
    public void testOpenException() throws Exception {
        when(sink.getDriveUtils()).thenThrow(new GeneralSecurityException("ERROR"));
        try {
            writer.open("test");
            fail("Should not be here");
        } catch (Exception e) {
        }
    }

    @Test
    public void testPut() throws Exception {
        writer.open("test");
        IndexedRecord data = new GenericData.Record(properties.getSchema());
        final String dataContent = "a content string";
        data.put(0, dataContent);
        writer.write(data);
        data.put(0, dataContent.getBytes());
        writer.write(data);
        writer.write(null);
        Result result = writer.close();
        assertNotNull(result);
        assertEquals(2, result.totalCount);
        assertEquals(2, result.successCount);
        List<IndexedRecord> writes = writer.getSuccessfulWrites();
        assertNotNull(writes);
        IndexedRecord record = writes.get(0);
        assertNotNull(record.get(0));
        assertEquals(PUT_FILE_PARENT_ID, record.get(1));
        assertEquals(PUT_FILE_ID, record.get(2));
        assertEquals(0, writer.getRejectedWrites().size());
    }

    @Test
    public void testGetWriteOperation() throws Exception {
        assertEquals(wop, writer.getWriteOperation());
    }
}
