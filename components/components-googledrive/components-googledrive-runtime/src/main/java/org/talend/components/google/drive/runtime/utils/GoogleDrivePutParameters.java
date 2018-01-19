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
package org.talend.components.google.drive.runtime.utils;

public class GoogleDrivePutParameters {

    private final String destinationFolderId;

    private final String resourceName;

    private final boolean overwriteIfExist;

    private final String fromLocalFilePath;

    private final byte[] fromBytes;

    public GoogleDrivePutParameters(String destinationFolderName, String resourceName, boolean overwriteIfExist,
            String fromLocalFilePath) {
        this.destinationFolderId = destinationFolderName;
        this.resourceName = resourceName;
        this.overwriteIfExist = overwriteIfExist;
        this.fromLocalFilePath = fromLocalFilePath;
        this.fromBytes = null;
    }

    public GoogleDrivePutParameters(String destinationFolderName, String resourceName, boolean overwriteIfExist,
            byte[] fromBytes) {
        this.destinationFolderId = destinationFolderName;
        this.resourceName = resourceName;
        this.overwriteIfExist = overwriteIfExist;
        this.fromBytes = fromBytes;
        this.fromLocalFilePath = null;
    }

    public String getDestinationFolderId() {
        return destinationFolderId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public boolean isOverwriteIfExist() {
        return overwriteIfExist;
    }

    public String getFromLocalFilePath() {
        return fromLocalFilePath;
    }

    public byte[] getFromBytes() {
        return fromBytes;
    }
}
