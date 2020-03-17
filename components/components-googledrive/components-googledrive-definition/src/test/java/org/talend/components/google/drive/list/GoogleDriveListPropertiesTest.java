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
package org.talend.components.google.drive.list;

import static org.junit.Assert.assertEquals;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.list.GoogleDriveListProperties.ListMode;
import org.talend.daikon.properties.presentation.Form;

public class GoogleDriveListPropertiesTest {

    GoogleDriveListProperties properties;

    @Before
    public void setUp() throws Exception {
        properties = new GoogleDriveListProperties("test");
        properties.connection.setupProperties();
        properties.connection.setupLayout();
        properties.schemaMain.setupProperties();
        properties.schemaMain.setupLayout();
        properties.setupProperties();
        properties.setupLayout();
        properties.refreshLayout(properties.getForm(Form.MAIN));
    }

    @Test
    public void testListMode() throws Exception {
        assertEquals("Files", ListMode.Files.name());
        assertEquals(ListMode.Files, ListMode.valueOf("Files"));
        assertEquals("Directories", ListMode.Directories.name());
        assertEquals(ListMode.Directories, ListMode.valueOf("Directories"));
        assertEquals("Both", ListMode.Both.name());
        assertEquals(ListMode.Both, ListMode.valueOf("Both"));
    }

    @Test
    public void testGetSchema() throws Exception {
        Schema s = properties.schemaMain.schema.getValue();
        assertEquals(s, properties.getSchema());
        assertEquals(s, properties.getSchema(null, true));
    }

    @Test
    public void testPageSize() throws Exception {
        assertEquals(1000, properties.pageSize.getValue().longValue());
    }
}
