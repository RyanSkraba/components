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

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.list.GoogleDriveListProperties;
import org.talend.components.google.drive.list.GoogleDriveListProperties.ListMode;
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
    }

    @Test
    public void testListRoot() throws Exception {
        createFolderAtRoot("Talend" + testTS);
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
    }

}
