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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.google.drive.GoogleDriveComponentProperties.AccessMethod;
import org.talend.components.google.drive.copy.GoogleDriveCopyDefinition;
import org.talend.components.google.drive.copy.GoogleDriveCopyProperties;
import org.talend.components.google.drive.copy.GoogleDriveCopyProperties.CopyMode;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

import com.google.api.services.drive.Drive;

public class GoogleDriveCopyRuntime extends GoogleDriveRuntime implements ComponentDriverInitialization<ComponentProperties> {

    private GoogleDriveCopyProperties properties;

    private String sourceId = "";

    private String destinationId = "";

    private transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveCopyRuntime.class);

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        ValidationResult vr = super.initialize(container, properties);
        if (vr.getStatus().equals(Result.ERROR)) {
            return vr;
        }
        this.properties = (GoogleDriveCopyProperties) properties;

        return validateProperties(this.properties);
    }

    @Override
    public void runAtDriver(RuntimeContainer container) {
        copyProcess(container);
        setReturnValues(container);
    }

    private void copyProcess(RuntimeContainer container) {
        CopyMode copyMode = properties.copyMode.getValue();
        String source = properties.source.getValue();
        String destinationFolder = properties.destinationFolder.getValue();
        String newName = properties.rename.getValue() ? properties.newName.getValue() : "";
        boolean deleteSourceFile = properties.deleteSourceFile.getValue();
        try {
            Drive drive = getDriveService();
            final GoogleDriveUtils utils = getDriveUtils();

            /* check for destination folder */
            String destinationFolderId = properties.destinationFolderAccessMethod.getValue().equals(AccessMethod.Id)
                    ? destinationFolder
                    : utils.getFolderId(destinationFolder, false);
            /* work on a fileName */
            if (CopyMode.File.equals(copyMode)) {
                /* check for managed resource */
                sourceId = properties.sourceAccessMethod.getValue().equals(AccessMethod.Id) ? source : utils.getFileId(source);
                destinationId = utils.copyFile(sourceId, destinationFolderId, newName, deleteSourceFile);
            } else {/* work on a folder */
                /* check for managed resource */
                sourceId = properties.sourceAccessMethod.getValue().equals(AccessMethod.Id) ? source
                        : utils.getFolderId(source, false);
                if (newName.isEmpty()) {
                    List<String> paths = utils.getExplodedPath(source);
                    newName = paths.get(paths.size() - 1);
                }
                destinationId = utils.copyFolder(sourceId, destinationFolderId, newName);
            }
        } catch (IOException | GeneralSecurityException e) {
            LOG.error(e.getLocalizedMessage());
            throw new ComponentException(e);
        }
    }

    public void setReturnValues(RuntimeContainer container) {
        String componentId = container.getCurrentComponentId();
        container.setComponentData(componentId, GoogleDriveCopyDefinition.RETURN_SOURCE_ID, sourceId);
        container.setComponentData(componentId, GoogleDriveCopyDefinition.RETURN_DESTINATION_ID, destinationId);
    }

}
