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

import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.GoogleDriveComponentProperties.AccessMethod;
import org.talend.components.google.drive.delete.GoogleDriveDeleteDefinition;
import org.talend.components.google.drive.delete.GoogleDriveDeleteProperties;
import org.talend.components.google.drive.list.GoogleDriveListProperties;
import org.talend.components.google.drive.list.GoogleDriveListProperties.ListMode;

public class GoogleDriveCreateDeleteTestIT extends GoogleDriveBaseTestIT {

    private GoogleDriveDeleteProperties properties;

    private GoogleDriveListReader listReader;

    private GoogleDriveDeleteRuntime runtime;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        runtime = new GoogleDriveDeleteRuntime();

        properties = new GoogleDriveDeleteProperties(TEST_NAME);
        properties.setupProperties();
        properties.connection = connectionProperties;
        properties.deleteMode.setValue(AccessMethod.Id);
        properties.useTrash.setValue(false);

        GoogleDriveListProperties listProperties = new GoogleDriveListProperties(TEST_NAME);
        listProperties.init();
        listProperties.setupProperties();
        listProperties.connection = connectionProperties;
        listProperties.setupLayout();
        listProperties.folder.setValue(DRIVE_ROOT);
        listProperties.listMode.setValue(ListMode.Both);
        listProperties.includeSubDirectories.setValue(true);
        source.initialize(container, listProperties);
        listReader = (GoogleDriveListReader) source.createReader(container);
    }

    @Test
    public void testDeleteAllResources() throws Exception {
        createFolderAtRoot("Talend-Delete" + testTS); // at least one item
        listReader.start();
        IndexedRecord record = listReader.getCurrent();
        deleteResource(record);
        while (listReader.advance()) {
            record = listReader.getCurrent();
            deleteResource(record);
        }
    }

    private void deleteResource(IndexedRecord record) {
        assertNotNull(record);
        String id = (String) record.get(0);
        assertNotNull(id);
        properties.file.setValue(id);
        runtime.initialize(container, properties);
        runtime.runAtDriver(container);
        String fileId = (String) container.getComponentData(container.getCurrentComponentId(),
                getStudioName(GoogleDriveDeleteDefinition.RETURN_FILE_ID));
        LOG.debug("[deleteResource] (record = [{}]). deletedId: {}", new Object[] { record, fileId });
        assertNotNull(fileId);
    }
}
