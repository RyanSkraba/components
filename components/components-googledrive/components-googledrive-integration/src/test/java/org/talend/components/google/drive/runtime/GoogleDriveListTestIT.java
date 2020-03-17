// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.google.drive.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.stream.IntStream;

import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.google.drive.list.GoogleDriveListProperties;
import org.talend.components.google.drive.list.GoogleDriveListProperties.ListMode;
import org.talend.components.google.drive.put.GoogleDrivePutDefinition;
import org.talend.components.google.drive.put.GoogleDrivePutProperties;
import org.talend.components.google.drive.put.GoogleDrivePutProperties.UploadMode;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class GoogleDriveListTestIT extends GoogleDriveBaseTestIT {

    private GoogleDriveListProperties properties;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        properties = new GoogleDriveListProperties(TEST_NAME);
        properties.init();
        properties.setupProperties();
        properties.connection = connectionProperties;
        properties.setupLayout();
        properties.folder.setValue(DRIVE_ROOT);
        // cleanup previous test folders
        properties.listMode.setValue(ListMode.Directories);
        properties.includeSubDirectories.setValue(false);
        ValidationResult vr = source.initialize(container, properties);
        GoogleDriveListReader reader = (GoogleDriveListReader) source.createReader(container);
        reader.start();
        if (reader.advance()) {
            IndexedRecord f = reader.getCurrent();
            deleteFolder(reader, f.get(f.getSchema().getField("name").pos()).toString());
            while (reader.advance()) {
                f = reader.getCurrent();
                deleteFolder(reader, f.get(f.getSchema().getField("name").pos()).toString());
            }
        }
    }

    private void deleteFolder(GoogleDriveListReader reader, String folderName) throws Exception {
        if (folderName.startsWith("TalendTest_")) {
            reader.utils.deleteResourceByName(folderName, false);
        }
    }

    @Test
    public void testListRoot() throws Exception {
        final String folder = "TalendTest_" + testTS;
        createFolderAtRoot(folder);
        properties.listMode.setValue(ListMode.Both);
        properties.includeSubDirectories.setValue(true);
        //
        ValidationResult vr = source.initialize(container, properties);
        assertEquals(Result.OK, vr.getStatus());
        GoogleDriveListReader reader = (GoogleDriveListReader) source.createReader(container);
        assertNotNull(reader);
        assertTrue(reader.start());
        assertNotNull(reader.getCurrent());
        while (reader.advance()) {
            assertNotNull(reader.getCurrent());
        }
        reader.utils.deleteResourceByName(folder, false);
    }

    @Test
    public void testListWithMoreFoldersThanPageSize() throws Exception {
        final String folder = "TalendTest_" + testTS;
        createFolderAtRoot(folder);
        // create resources over pageSize
        int MAX_FOLDERS = 16;
        IntStream.rangeClosed(1, MAX_FOLDERS).forEach(idx -> {
                    try {
                        createFolder(folder, "subfolder" + idx);
                    } catch (Exception e) {
                    }
                }
        );
        //
        properties.listMode.setValue(ListMode.Both);
        properties.includeSubDirectories.setValue(false);
        properties.folder.setValue(folder);
        properties.pageSize.setValue(5);
        ValidationResult vr = source.initialize(container, properties);
        assertEquals(Result.OK, vr.getStatus());
        GoogleDriveListReader reader = (GoogleDriveListReader) source.createReader(container);
        assertNotNull(reader);
        assertTrue(reader.start());
        assertNotNull(reader.getCurrent());
        int counter = 1;
        while (reader.advance()) {
            assertNotNull(reader.getCurrent());
            counter++;
        }
        assertEquals(MAX_FOLDERS, counter);
        // finally remove test folder
        reader.utils.deleteResourceByName(folder, false);
    }

    @Test
    public void testListWithMoreFilesThanPageSize() throws Exception {
        final String folder = "TalendTest_" + testTS;
        createFolderAtRoot(folder);
        // create resources over pageSize
        final GoogleDrivePutProperties putProperties = new GoogleDrivePutProperties(TEST_NAME);
        putProperties.setupProperties();
        putProperties.connection = connectionProperties;
        putProperties.overwrite.setValue(true);
        putProperties.uploadMode.setValue(UploadMode.READ_CONTENT_FROM_INPUT);
        putProperties.destinationFolder.setValue(folder);
        IndexedRecord record = new GenericData.Record(SchemaBuilder.builder().record("write").fields() //
                .name(GoogleDrivePutDefinition.RETURN_CONTENT).type(AvroUtils._bytes()).noDefault() //
                .endRecord());
        String content = "ABC\nDEF\nRH";
        record.put(0, content.getBytes());
        int MAX_FILES = 6;
        IntStream.rangeClosed(1, MAX_FILES).forEach(idx -> {
                    try {
                        putProperties.fileName.setValue("file-" + idx);
                        sink.initialize(container, putProperties);
                        WriterWithFeedback writer = (WriterWithFeedback) sink.createWriteOperation().createWriter(container);
                        writer.open(TEST_NAME);
                        writer.write(record);
                        writer.close();
                    } catch (Exception e) {
                    }
                }
        );
        //
        properties.listMode.setValue(ListMode.Both);
        properties.includeSubDirectories.setValue(false);
        properties.folder.setValue(folder);
        properties.pageSize.setValue(2);
        ValidationResult vr = source.initialize(container, properties);
        assertEquals(Result.OK, vr.getStatus());
        GoogleDriveListReader reader = (GoogleDriveListReader) source.createReader(container);
        assertNotNull(reader);
        assertTrue(reader.start());
        assertNotNull(reader.getCurrent());
        int counter = 1;
        while (reader.advance()) {
            assertNotNull(reader.getCurrent());
            counter++;
        }
        assertEquals(MAX_FILES, counter);
        // finally remove test folder
        reader.utils.deleteResourceByName(folder, false);
    }

}
