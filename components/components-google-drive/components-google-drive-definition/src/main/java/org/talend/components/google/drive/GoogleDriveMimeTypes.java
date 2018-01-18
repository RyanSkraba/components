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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleDriveMimeTypes {

    public static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";

    public static final String MIME_TYPE_GOOGLE_DOCUMENT = "application/vnd.google-apps.document";

    public static final String MIME_TYPE_GOOGLE_DRAWING = "application/vnd.google-apps.drawing";

    public static final String MIME_TYPE_GOOGLE_PRESENTATION = "application/vnd.google-apps.presentation";

    public static final String MIME_TYPE_GOOGLE_SPREADSHEET = "application/vnd.google-apps.spreadsheet";

    public static final String MIME_TYPE_GOOGLE_FORMS = "application/vnd.google-apps.form";

    public static final String MIME_TYPE_GOOGLE_FUSION_TABLE = "application/vnd.google-apps.fusiontable";

    public static final String MIME_TYPE_GOOGLE_MY_MAPS = "application/vnd.google-apps.map";

    public static final String MIME_TYPE_GOOGLE_APPS_SCRIPT = "application/vnd.google-apps.script";

    public static final String MIME_TYPE_GOOGLE_SITE = "application/vnd.google-apps.sites";

    public static final List<String> GOOGLE_DRIVE_APPS = Collections.unmodifiableList(Arrays.asList(MIME_TYPE_GOOGLE_DOCUMENT,
            MIME_TYPE_GOOGLE_DRAWING, MIME_TYPE_GOOGLE_PRESENTATION, MIME_TYPE_GOOGLE_SPREADSHEET));

    public static final String MIME_TYPE_CSV = "text/csv";

    public static final String MIME_TYPE_CSV_EXT = ".csv";

    public static final String MIME_TYPE_CSV_TAB = "text/tab-separated-values";

    public static final String MIME_TYPE_CSV_TAB_EXT = ".csv";

    public static final String MIME_TYPE_EPUB = "application/epub+zip";

    public static final String MIME_TYPE_EPUB_EXT = ".zip";

    public static final String MIME_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static final String MIME_TYPE_EXCEL_EXT = ".xlsx";

    public static final String MIME_TYPE_HTML = "text/html";

    public static final String MIME_TYPE_HTML_EXT = ".html";

    public static final String MIME_TYPE_HTML_ZIPPED = "application/zip";

    public static final String MIME_TYPE_HTML_ZIPPED_EXT = ".zip";

    public static final String MIME_TYPE_JPG = "image/jpeg";

    public static final String MIME_TYPE_JPG_EXT = ".jpg";

    public static final String MIME_TYPE_OO_DOCUMENT = "application/vnd.oasis.opendocument.text";

    public static final String MIME_TYPE_OO_DOCUMENT_EXT = ".odt";

    public static final String MIME_TYPE_OO_PRESENTATION = "application/vnd.oasis.opendocument.presentation";

    public static final String MIME_TYPE_OO_PRESENTATION_EXT = ".odp";

    public static final String MIME_TYPE_OO_SPREADSHEET = "application/x-vnd.oasis.opendocument.spreadsheet";

    public static final String MIME_TYPE_OO_SPREADSHEET_EXT = ".ods";

    public static final String MIME_TYPE_OO_XSPREADSHEET = "application/x-vnd.oasis.opendocument.spreadsheet";

    public static final String MIME_TYPE_OO_XSPREADSHEET_EXT = ".ods";

    public static final String MIME_TYPE_PDF = "application/pdf";

    public static final String MIME_TYPE_PDF_EXT = ".pdf";

    public static final String MIME_TYPE_PNG = "image/png";

    public static final String MIME_TYPE_PNG_EXT = ".png";

    public static final String MIME_TYPE_POWERPOINT = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

    public static final String MIME_TYPE_POWERPOINT_EXT = ".pptx";

    public static final String MIME_TYPE_RTF = "application/rtf";

    public static final String MIME_TYPE_RTF_EXT = ".rtf";

    public static final String MIME_TYPE_SVG = "image/svg+xml";

    public static final String MIME_TYPE_SVG_EXT = ".svg";

    public static final String MIME_TYPE_TEXT = "text/plain";

    public static final String MIME_TYPE_TEXT_EXT = ".txt";

    public static final String MIME_TYPE_WORD = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    public static final String MIME_TYPE_WORD_EXT = ".docx";

    public static final String MIME_TYPE_ZIP = "application/zip";

    public static final String MIME_TYPE_ZIP_EXT = ".zip";

    public static final String MIME_TYPE_JSON = "application/json";

    public static final String MIME_TYPE_JSON_EXT = ".json";

    public enum MimeType {

        CSV(MIME_TYPE_CSV, MIME_TYPE_CSV_EXT),
        CSV_TAB(MIME_TYPE_CSV_TAB, MIME_TYPE_CSV_TAB_EXT),
        EPUB(MIME_TYPE_EPUB, MIME_TYPE_EPUB_EXT),
        EXCEL(MIME_TYPE_EXCEL, MIME_TYPE_EXCEL_EXT),
        HTML(MIME_TYPE_HTML, MIME_TYPE_HTML),
        HTML_ZIPPED(MIME_TYPE_HTML_ZIPPED, MIME_TYPE_HTML_ZIPPED_EXT),
        JPG(MIME_TYPE_JPG, MIME_TYPE_JPG_EXT),
        OO_DOCUMENT(MIME_TYPE_OO_DOCUMENT, MIME_TYPE_OO_DOCUMENT_EXT),
        OO_PRESENTATION(MIME_TYPE_OO_PRESENTATION, MIME_TYPE_OO_PRESENTATION_EXT),
        OO_SPREADSHEET(MIME_TYPE_OO_SPREADSHEET, MIME_TYPE_OO_SPREADSHEET_EXT),
        PDF(MIME_TYPE_PDF, MIME_TYPE_PDF_EXT),
        PNG(MIME_TYPE_PNG, MIME_TYPE_PNG_EXT),
        POWERPOINT(MIME_TYPE_POWERPOINT, MIME_TYPE_POWERPOINT_EXT),
        RTF(MIME_TYPE_RTF, MIME_TYPE_RTF_EXT),
        SVG(MIME_TYPE_SVG, MIME_TYPE_SVG_EXT),
        TEXT(MIME_TYPE_TEXT, MIME_TYPE_TEXT_EXT),
        WORD(MIME_TYPE_WORD, MIME_TYPE_WORD_EXT),
        // extra types non managed in UI
        JSON(MIME_TYPE_JSON, MIME_TYPE_JSON_EXT);

        private String mimeType;

        private String extension;

        MimeType(String mimeType, String extension) {
            this.mimeType = mimeType;
            this.extension = extension;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getExtension() {
            return extension;
        }

    }

    /**
     * Pre-fill a Map with some default values for supported GAPPS export
     * 
     * @return brand new Map
     */
    public static Map<String, MimeType> newDefaultMimeTypesSupported() {
        Map<String, MimeType> result = new HashMap<>();
        result.put(MIME_TYPE_GOOGLE_DOCUMENT, MimeType.WORD);
        result.put(MIME_TYPE_GOOGLE_DRAWING, MimeType.PNG);
        result.put(MIME_TYPE_GOOGLE_PRESENTATION, MimeType.POWERPOINT);
        result.put(MIME_TYPE_GOOGLE_SPREADSHEET, MimeType.EXCEL);
        result.put(MIME_TYPE_GOOGLE_APPS_SCRIPT, MimeType.JSON);

        return result;
    }
}
