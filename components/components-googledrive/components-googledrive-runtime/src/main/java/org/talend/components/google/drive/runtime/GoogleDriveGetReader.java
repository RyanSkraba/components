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

import static org.talend.components.google.drive.GoogleDriveMimeTypes.MIME_TYPE_GOOGLE_DOCUMENT;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MIME_TYPE_GOOGLE_DRAWING;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MIME_TYPE_GOOGLE_PRESENTATION;
import static org.talend.components.google.drive.GoogleDriveMimeTypes.MIME_TYPE_GOOGLE_SPREADSHEET;

import java.io.IOException;
import java.util.Map;

import org.apache.avro.generic.GenericData.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.google.drive.GoogleDriveComponentProperties.AccessMethod;
import org.talend.components.google.drive.GoogleDriveMimeTypes;
import org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType;
import org.talend.components.google.drive.get.GoogleDriveGetDefinition;
import org.talend.components.google.drive.get.GoogleDriveGetProperties;
import org.talend.components.google.drive.runtime.utils.GoogleDriveGetParameters;
import org.talend.components.google.drive.runtime.utils.GoogleDriveGetResult;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public class GoogleDriveGetReader extends GoogleDriveReader {

    private GoogleDriveGetProperties properties;

    private String fileId;

    private transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveDeleteReader.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(GoogleDriveDeleteReader.class);

    protected GoogleDriveGetReader(RuntimeContainer container, GoogleDriveSource source, GoogleDriveGetProperties properties) {
        super(container, source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        super.start();
        //
        String resourceId = properties.fileAccessMethod.getValue().equals(AccessMethod.Id) ? properties.file.getValue()
                : utils.getFileId(properties.file.getValue());
        Map<String, MimeType> mimes = GoogleDriveMimeTypes.newDefaultMimeTypesSupported();
        mimes.put(MIME_TYPE_GOOGLE_DOCUMENT, properties.exportDocument.getValue());
        mimes.put(MIME_TYPE_GOOGLE_DRAWING, properties.exportDrawing.getValue());
        mimes.put(MIME_TYPE_GOOGLE_PRESENTATION, properties.exportPresentation.getValue());
        mimes.put(MIME_TYPE_GOOGLE_SPREADSHEET, properties.exportSpreadsheet.getValue());
        GoogleDriveGetParameters p = new GoogleDriveGetParameters(resourceId, mimes, properties.storeToLocal.getValue(),
                properties.outputFileName.getValue(), properties.setOutputExt.getValue());
        //
        GoogleDriveGetResult r = utils.getResource(p);
        fileId = r.getId();
        byte[] content = r.getContent();
        record = new Record(properties.schemaMain.schema.getValue());
        record.put(0, content);
        result.totalCount++;
        result.successCount++;

        return true;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> r = result.toMap();
        r.put(GoogleDriveGetDefinition.RETURN_FILE_ID, fileId);

        return r;
    }

}
