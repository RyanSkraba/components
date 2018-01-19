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
package org.talend.components.google.drive.put;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.put.GoogleDrivePutProperties.UploadMode;
import org.talend.daikon.properties.presentation.Form;

public class GoogleDrivePutPropertiesTest {

    GoogleDrivePutProperties properties;

    @Before
    public void setUp() throws Exception {
        properties = new GoogleDrivePutProperties("test");
        properties.connection.setupProperties();
        properties.connection.setupLayout();
        properties.schemaMain.setupProperties();
        properties.schemaMain.setupLayout();
        properties.setupProperties();
        properties.setupLayout();
    }

    @Test
    public void testUploadMode() throws Exception {
        assertEquals("UPLOAD_LOCAL_FILE", UploadMode.UPLOAD_LOCAL_FILE.name());
        assertEquals(UploadMode.UPLOAD_LOCAL_FILE, UploadMode.valueOf("UPLOAD_LOCAL_FILE"));
        assertEquals("READ_CONTENT_FROM_INPUT", UploadMode.READ_CONTENT_FROM_INPUT.name());
        assertEquals(UploadMode.READ_CONTENT_FROM_INPUT, UploadMode.valueOf("READ_CONTENT_FROM_INPUT"));
        assertEquals("EXPOSE_OUTPUT_STREAM", UploadMode.EXPOSE_OUTPUT_STREAM.name());
        assertEquals(UploadMode.EXPOSE_OUTPUT_STREAM, UploadMode.valueOf("EXPOSE_OUTPUT_STREAM"));
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() throws Exception {
        assertThat(properties.getAllSchemaPropertiesConnectors(false), hasSize(1));
        assertThat(properties.getAllSchemaPropertiesConnectors(false), contains(properties.MAIN_CONNECTOR));
        assertThat(properties.getAllSchemaPropertiesConnectors(true), hasSize(1));
        assertThat(properties.getAllSchemaPropertiesConnectors(true), contains(properties.MAIN_CONNECTOR));
    }

    @Test
    public void testAfterUploadMode() throws Exception {
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.localFilePath.getName()).isVisible());
        properties.uploadMode.setValue(UploadMode.READ_CONTENT_FROM_INPUT);
        properties.afterUploadMode();
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.localFilePath.getName()).isVisible());
    }

    @Test
    public void testGetEffectiveConnection() throws Exception {
        assertNotNull(properties.getConnectionProperties().getEffectiveConnectionProperties());
    }

    @Test
    public void testGetSchema() throws Exception {
        assertEquals(properties.schemaMain.schema.getValue(), properties.getSchema());
        assertEquals(properties.schemaMain.schema.getValue(), properties.getSchema(null, true));
    }

}
