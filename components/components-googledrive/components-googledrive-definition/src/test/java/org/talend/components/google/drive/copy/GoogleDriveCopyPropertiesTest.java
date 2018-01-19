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
package org.talend.components.google.drive.copy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.copy.GoogleDriveCopyProperties.CopyMode;
import org.talend.daikon.properties.presentation.Form;

public class GoogleDriveCopyPropertiesTest {

    GoogleDriveCopyProperties properties;

    @Before
    public void setUp() throws Exception {
        properties = new GoogleDriveCopyProperties("test");
        properties.schemaMain.setupProperties();
        properties.schemaMain.setupLayout();
        properties.connection.setupProperties();
        properties.connection.setupLayout();
        properties.setupProperties();
        properties.setupLayout();
        properties.refreshLayout(properties.getForm(Form.MAIN));
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() throws Exception {
        assertThat(properties.getAllSchemaPropertiesConnectors(false), hasSize(0));
        assertThat(properties.getAllSchemaPropertiesConnectors(true), contains(properties.MAIN_CONNECTOR));
    }

    @Test
    public void testAfterCopyMode() throws Exception {
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.deleteSourceFile.getName()).isVisible());
        properties.copyMode.setValue(CopyMode.Folder);
        properties.afterCopyMode();
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.deleteSourceFile.getName()).isVisible());
    }

    @Test
    public void testAfterRename() throws Exception {
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.newName.getName()).isVisible());
        properties.rename.setValue(true);
        properties.afterRename();
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.newName.getName()).isVisible());
    }

    @Test
    public void testCopyMode() throws Exception {
        assertEquals("File", CopyMode.File.name());
        assertEquals(CopyMode.File, CopyMode.valueOf("File"));
        assertEquals("Folder", CopyMode.Folder.name());
        assertEquals(CopyMode.Folder, CopyMode.valueOf("Folder"));
    }
}
