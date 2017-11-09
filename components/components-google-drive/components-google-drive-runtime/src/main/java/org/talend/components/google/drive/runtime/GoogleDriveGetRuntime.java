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
import java.security.GeneralSecurityException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.google.drive.GoogleDriveComponentProperties.AccessMethod;
import org.talend.components.google.drive.GoogleDriveMimeTypes;
import org.talend.components.google.drive.GoogleDriveMimeTypes.MimeType;
import org.talend.components.google.drive.delete.GoogleDriveDeleteDefinition;
import org.talend.components.google.drive.get.GoogleDriveGetProperties;
import org.talend.components.google.drive.runtime.utils.GoogleDriveGetParameters;
import org.talend.components.google.drive.runtime.utils.GoogleDriveGetResult;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class GoogleDriveGetRuntime extends GoogleDriveRuntime implements ComponentDriverInitialization<ComponentProperties> {

    private GoogleDriveGetProperties properties;

    private String fileId;

    private transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveGetRuntime.class);

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        ValidationResult vr = super.initialize(container, properties);
        if (vr.getStatus().equals(Result.ERROR)) {
            return vr;
        }
        this.properties = (GoogleDriveGetProperties) properties;
        return validateProperties(this.properties);
    }

    @Override
    public void runAtDriver(RuntimeContainer container) {
        getFile(container);
        setReturnValues(container);
    }

    public void getFile(RuntimeContainer container) {
        try {
            String resourceId = properties.fileAccessMethod.getValue().equals(AccessMethod.Id) ? properties.file.getValue()
                    : getDriveUtils().getFileId(properties.file.getValue());
            Map<String, MimeType> mimes = GoogleDriveMimeTypes.newDefaultMimeTypesSupported();
            mimes.put(MIME_TYPE_GOOGLE_DOCUMENT, properties.exportDocument.getValue());
            mimes.put(MIME_TYPE_GOOGLE_DRAWING, properties.exportDrawing.getValue());
            mimes.put(MIME_TYPE_GOOGLE_PRESENTATION, properties.exportPresentation.getValue());
            mimes.put(MIME_TYPE_GOOGLE_SPREADSHEET, properties.exportSpreadsheet.getValue());
            GoogleDriveGetParameters p = new GoogleDriveGetParameters(resourceId, mimes, properties.storeToLocal.getValue(),
                    properties.outputFileName.getValue(), properties.setOutputExt.getValue());
            //
            GoogleDriveGetResult r = getDriveUtils().getResource(p);
            fileId = r.getId();
        } catch (IOException | GeneralSecurityException e) {
            LOG.error(e.getMessage());
            throw new ComponentException(e);
        }
    }

    public void setReturnValues(RuntimeContainer container) {
        String componentId = container.getCurrentComponentId();
        container.setComponentData(componentId, GoogleDriveDeleteDefinition.RETURN_FILE_ID, fileId);
    }
}
