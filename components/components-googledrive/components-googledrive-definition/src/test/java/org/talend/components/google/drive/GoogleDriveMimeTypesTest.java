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
package org.talend.components.google.drive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MIME_TYPE_GOOGLE_APPS_SCRIPT;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MIME_TYPE_GOOGLE_DOCUMENT;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MIME_TYPE_GOOGLE_DRAWING;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MIME_TYPE_GOOGLE_PRESENTATION;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MIME_TYPE_GOOGLE_SPREADSHEET;

import java.util.Map;

import org.junit.Test;
import org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType;

public class GoogleDriveMimeTypesTest {

    @Test
    public void testMimeTypesGetMimeType() throws Exception {
        assertEquals(GoogleDriveMimeTypes.MIME_TYPE_CSV, MimeType.CSV.getMimeType());
    }

    @Test
    public void testMimeTypesGetExtension() throws Exception {
        assertEquals(GoogleDriveMimeTypes.MIME_TYPE_CSV, MimeType.CSV.getMimeType());
        assertEquals(GoogleDriveMimeTypes.MIME_TYPE_CSV_EXT, MimeType.CSV.getExtension());
    }

    @Test
    public void testNewDefaultMimeTypesSupported() throws Exception {
        Map<String, MimeType> mimes = GoogleDriveMimeTypes.newDefaultMimeTypesSupported();
        assertNotNull(mimes);
        assertEquals(MimeType.WORD, mimes.get(MIME_TYPE_GOOGLE_DOCUMENT));
        assertEquals(MimeType.PNG, mimes.get(MIME_TYPE_GOOGLE_DRAWING));
        assertEquals(MimeType.POWERPOINT, mimes.get(MIME_TYPE_GOOGLE_PRESENTATION));
        assertEquals(MimeType.EXCEL, mimes.get(MIME_TYPE_GOOGLE_SPREADSHEET));
        assertEquals(MimeType.JSON, mimes.get(MIME_TYPE_GOOGLE_APPS_SCRIPT));
    }
}
