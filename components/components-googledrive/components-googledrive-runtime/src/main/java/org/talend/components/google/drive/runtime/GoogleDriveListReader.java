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

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.google.drive.list.GoogleDriveListProperties;

public class GoogleDriveListReader extends GoogleDriveAbstractListReader {

    public GoogleDriveListReader(RuntimeContainer container, GoogleDriveSource source, GoogleDriveListProperties properties) {
        super(container, source);
        //
        try {
            drive = source.getDriveService();
            utils = source.getDriveUtils();
        } catch (GeneralSecurityException | IOException e) {
            LOG.error(e.getMessage());
            throw new ComponentException(e);
        }
        //
        schema = properties.getSchema();
        folderName = properties.folder.getValue().trim();
        folderAccessMethod = properties.folderAccessMethod.getValue();
        listModeStr = properties.listMode.getValue().name().toUpperCase();
        includeSubDirectories = properties.includeSubDirectories.getValue();
        includeTrashedFiles = properties.includeTrashedFiles.getValue();
        maxPageSize = properties.pageSize.getValue();
    }

}
