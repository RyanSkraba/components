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

import static org.junit.Assert.*;
import static org.talend.components.google.drive.runtime.GoogleDriveRuntime.getStudioName;

import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.google.drive.copy.GoogleDriveCopyDefinition;
import org.talend.components.google.drive.copy.GoogleDriveCopyProperties;
import org.talend.components.google.drive.copy.GoogleDriveCopyProperties.CopyMode;
import org.talend.components.google.drive.put.GoogleDrivePutDefinition;
import org.talend.components.google.drive.put.GoogleDrivePutProperties;
import org.talend.components.google.drive.put.GoogleDrivePutProperties.UploadMode;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class GoogleDriveCopyTestIT extends GoogleDriveBaseTestIT {

    private String folderTalendCopy;

    private String folderTalendCopyDest;

    private String fileTestSource;

    private String fileTestCopy;

    private String fileTestMove;

    private GoogleDriveCopyProperties properties;

    private GoogleDriveCopyRuntime runtime;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderTalendCopy = "talend-copy-dir" + testTS;
        folderTalendCopyDest = "talend-copy-dest-dir" + testTS;
        fileTestSource = "source-test-file" + testTS;
        fileTestCopy = "copy-test-file" + testTS;
        fileTestMove = "move-test-file" + testTS;
        //
        runtime = new GoogleDriveCopyRuntime();
        properties = new GoogleDriveCopyProperties(TEST_NAME);
        properties.setupProperties();
        properties.connection = connectionProperties;
        properties.destinationFolder.setValue(DRIVE_ROOT);
        // create source file
        GoogleDrivePutProperties putProperties = new GoogleDrivePutProperties(TEST_NAME);
        putProperties.setupProperties();
        putProperties.connection = connectionProperties;
        putProperties.overwrite.setValue(true);
        putProperties.uploadMode.setValue(UploadMode.READ_CONTENT_FROM_INPUT);
        putProperties.destinationFolder.setValue(DRIVE_ROOT);
        putProperties.fileName.setValue(fileTestSource);
        sink.initialize(container, putProperties);
        WriterWithFeedback writer = (WriterWithFeedback) sink.createWriteOperation().createWriter(container);
        writer.open(TEST_NAME);
        IndexedRecord record = new GenericData.Record(SchemaBuilder.builder().record("write").fields() //
                .name(GoogleDrivePutDefinition.RETURN_CONTENT).type(AvroUtils._bytes()).noDefault() //
                .endRecord());
        String content = "ABC\nDEF\nRH";
        record.put(0, content.getBytes());
        writer.write(record);
        writer.close();
        LOG.debug("created source file {}.", fileTestSource);
    }

    @Test
    public void testCopyFile() throws Exception {
        properties.copyMode.setValue(CopyMode.File);
        properties.source.setValue(fileTestSource);
        properties.rename.setValue(true);
        properties.newName.setValue(fileTestCopy);
        ValidationResult vr = runtime.initialize(container, properties);
        assertEquals(Result.OK, vr.getStatus());
        runtime.runAtDriver(container);
        assertNotNull(container.getComponentData(container.getCurrentComponentId(),
                getStudioName(GoogleDriveCopyDefinition.RETURN_DESTINATION_ID)));
        assertNotNull(container.getComponentData(container.getCurrentComponentId(),
                getStudioName(GoogleDriveCopyDefinition.RETURN_SOURCE_ID)));
        assertTrue(resourceExists(DRIVE_ROOT, fileTestCopy));
        assertTrue(resourceExists(DRIVE_ROOT, fileTestSource));
    }

    @Test
    public void testMoveFile() throws Exception {
        properties.copyMode.setValue(CopyMode.File);
        properties.source.setValue(fileTestSource);
        properties.deleteSourceFile.setValue(true);
        properties.rename.setValue(true);
        properties.newName.setValue(fileTestMove);
        ValidationResult vr = runtime.initialize(container, properties);
        assertEquals(Result.OK, vr.getStatus());
        runtime.runAtDriver(container);
        assertNotNull(container.getComponentData(container.getCurrentComponentId(),
                getStudioName(GoogleDriveCopyDefinition.RETURN_DESTINATION_ID)));
        assertNotNull(container.getComponentData(container.getCurrentComponentId(),
                getStudioName(GoogleDriveCopyDefinition.RETURN_SOURCE_ID)));
        assertTrue(resourceExists(DRIVE_ROOT, fileTestMove));
        assertFalse(resourceExists(DRIVE_ROOT, fileTestSource));
    }

    @Test
    public void testCopyDirectories() throws Exception {
        createFolderAtRoot(folderTalendCopy);
        createFolder(folderTalendCopy, "subfolderA");
        createFolder(folderTalendCopy, "subfolderB");
        //
        properties.copyMode.setValue(CopyMode.Folder);
        properties.source.setValue(folderTalendCopy);
        properties.destinationFolder.setValue(DRIVE_ROOT);
        properties.rename.setValue(true);
        properties.newName.setValue(folderTalendCopyDest);
        ValidationResult vr = runtime.initialize(container, properties);
        assertEquals(Result.OK, vr.getStatus());
        runtime.runAtDriver(container);
        assertNotNull(container.getComponentData(container.getCurrentComponentId(),
                getStudioName(GoogleDriveCopyDefinition.RETURN_DESTINATION_ID)));
        assertNotNull(container.getComponentData(container.getCurrentComponentId(),
                getStudioName(GoogleDriveCopyDefinition.RETURN_SOURCE_ID)));
        assertTrue(resourceExists(DRIVE_ROOT, folderTalendCopyDest));
        assertTrue(resourceExists(folderTalendCopyDest, "subfolderA"));
        assertTrue(resourceExists(folderTalendCopyDest, "subfolderB"));
    }
}
