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

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.google.drive.GoogleDriveComponentProperties.AccessMethod;
import org.talend.components.google.drive.put.GoogleDrivePutDefinition;
import org.talend.components.google.drive.put.GoogleDrivePutProperties;
import org.talend.components.google.drive.runtime.utils.GoogleDrivePutParameters;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.google.api.services.drive.model.File;

public class GoogleDrivePutRuntime extends GoogleDriveRuntime implements ComponentDriverInitialization<ComponentProperties> {

    private GoogleDrivePutProperties properties;

    private File sentFile;

    private static final Logger LOG = LoggerFactory.getLogger(GoogleDrivePutRuntime.class);

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        ValidationResult vr = super.initialize(container, properties);
        if (vr.getStatus().equals(Result.ERROR)) {
            return vr;
        }
        this.properties = (GoogleDrivePutProperties) properties;
        return validateProperties(this.properties);
    }

    @Override
    public void runAtDriver(RuntimeContainer container) {
        putFile(container);
        setReturnValues(container);
    }

    private void putFile(RuntimeContainer container) {
        try {
            String destinationFolderId = properties.destinationFolderAccessMethod.getValue().equals(AccessMethod.Id)
                    ? properties.destinationFolder.getValue()
                    : getDriveUtils().getFolderId(properties.destinationFolder.getValue(), false);
            GoogleDrivePutParameters p = new GoogleDrivePutParameters(destinationFolderId, properties.fileName.getValue(),
                    properties.overwrite.getValue(), properties.localFilePath.getValue());
            sentFile = getDriveUtils().putResource(p);
        } catch (IOException | GeneralSecurityException e) {
            LOG.error(e.getLocalizedMessage());
            throw new ComponentException(e);
        }
    }

    private void setReturnValues(RuntimeContainer container) {
        String componentId = container.getCurrentComponentId();
        container.setComponentData(componentId, GoogleDrivePutDefinition.RETURN_PARENT_FOLDER_ID, sentFile.getParents().get(0));
        container.setComponentData(componentId, GoogleDrivePutDefinition.RETURN_FILE_ID, sentFile.getId());
    }
}
