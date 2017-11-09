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

import static org.talend.components.google.drive.put.GoogleDrivePutProperties.UploadMode.UPLOAD_LOCAL_FILE;

import org.talend.components.google.drive.GoogleDriveProvideConnectionProperties;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;
import org.talend.components.google.drive.copy.GoogleDriveCopyProperties;
import org.talend.components.google.drive.create.GoogleDriveCreateProperties;
import org.talend.components.google.drive.delete.GoogleDriveDeleteProperties;
import org.talend.components.google.drive.get.GoogleDriveGetProperties;
import org.talend.components.google.drive.list.GoogleDriveListProperties;
import org.talend.components.google.drive.put.GoogleDrivePutProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

public class GoogleDriveValidator {

    protected static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(GoogleDriveSourceOrSink.class);

    public ValidationResult validateProperties(GoogleDriveProvideConnectionProperties properties) {
        ValidationResultMutable vr = new ValidationResultMutable(Result.OK);

        if (properties instanceof GoogleDriveCreateProperties) {
            return validateCreateProperties((GoogleDriveCreateProperties) properties);
        }
        if (properties instanceof GoogleDriveDeleteProperties) {
            return validateDeleteProperties((GoogleDriveDeleteProperties) properties);
        }
        if (properties instanceof GoogleDriveListProperties) {
            return validateListProperties((GoogleDriveListProperties) properties);
        }
        if (properties instanceof GoogleDriveGetProperties) {
            return validateGetProperties((GoogleDriveGetProperties) properties);
        }
        if (properties instanceof GoogleDrivePutProperties) {
            return validatePutProperties((GoogleDrivePutProperties) properties);
        }
        if (properties instanceof GoogleDriveCopyProperties) {
            return validateCopyProperties((GoogleDriveCopyProperties) properties);
        }

        return vr;
    }

    public ValidationResult validateConnectionProperties(GoogleDriveConnectionProperties properties) {
        ValidationResultMutable vr = new ValidationResultMutable(Result.OK, messages.getMessage("message.parameters.OK"));
        /* validate GoogleDriveConnection settings */
        if (properties.applicationName.getValue().isEmpty()) {
            vr = new ValidationResultMutable(Result.ERROR)
                    .setMessage(messages.getMessage("error.validation.connection.applicationName.empty"));
            return vr;
        }
        // OAuth settings
        switch (properties.oAuthMethod.getValue()) {
        case AccessToken:
            if (properties.accessToken.getValue().isEmpty()) {
                vr = new ValidationResultMutable(Result.ERROR)
                        .setMessage(messages.getMessage("error.validation.connection.accessToken.empty"));
                return vr;
            }
            break;
        case InstalledApplicationWithIdAndSecret:
            if (properties.clientId.getValue().isEmpty() || properties.clientSecret.getValue().isEmpty()) {
                vr = new ValidationResultMutable(Result.ERROR)
                        .setMessage(messages.getMessage("error.validation.connection.clientIdOrClientSecret.empty"));
                return vr;
            }
            if (properties.datastorePath.getValue().isEmpty()) {
                vr = new ValidationResultMutable(Result.ERROR)
                        .setMessage(messages.getMessage("error.validation.connection.datastorePath.empty"));
                return vr;
            }
            break;
        case InstalledApplicationWithJSON:
            if (properties.clientSecretFile.getValue().isEmpty()) {
                vr = new ValidationResultMutable(Result.ERROR)
                        .setMessage(messages.getMessage("error.validation.connection.clientSecretFile.empty"));
                return vr;
            }
            if (properties.datastorePath.getValue().isEmpty()) {
                vr = new ValidationResultMutable(Result.ERROR)
                        .setMessage(messages.getMessage("error.validation.connection.datastorePath.empty"));
                return vr;
            }
            break;
        case ServiceAccount:
            if (properties.serviceAccountFile.getValue().isEmpty()) {
                vr = new ValidationResultMutable(Result.ERROR)
                        .setMessage(messages.getMessage("error.validation.connection.serviceAccountFile.empty"));
                return vr;
            }
            break;
        }
        // Proxy settings
        if (properties.useProxy.getValue()) {
            if (properties.proxyHost.getValue().isEmpty()) {
                vr = new ValidationResultMutable(Result.ERROR)
                        .setMessage(messages.getMessage("error.validation.connection.proxyHost.empty"));
                return vr;
            }
            if (properties.proxyPort.getValue().intValue() == 0) {
                vr = new ValidationResultMutable(Result.ERROR)
                        .setMessage(messages.getMessage("error.validation.connection.proxyPort.invalid"));
                return vr;
            }
        }
        // SSL settings
        if (properties.useSSL.getValue()) {
            if (properties.sslAlgorithm.getValue().isEmpty()) {
                vr = new ValidationResultMutable(Result.ERROR)
                        .setMessage(messages.getMessage("error.validation.connection.sslAlgorithm.empty"));
                return vr;
            }
            if (properties.sslTrustStore.getValue().isEmpty()) {
                vr = new ValidationResultMutable(Result.ERROR)
                        .setMessage(messages.getMessage("error.validation.connection.sslTrustStore.empty"));
                return vr;
            }
        }

        return vr;
    }

    public ValidationResult validateCreateProperties(GoogleDriveCreateProperties properties) {
        ValidationResultMutable vr = new ValidationResultMutable(Result.OK);
        if (properties.parentFolder.getValue().isEmpty()) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(messages.getMessage("error.validation.parentfolder.empty"));
            return vr;
        }
        if (properties.newFolder.getValue().isEmpty()) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(messages.getMessage("error.validation.newfolder.empty"));
            return vr;
        }
        return vr;
    }

    public ValidationResult validateDeleteProperties(GoogleDriveDeleteProperties properties) {
        ValidationResultMutable vr = new ValidationResultMutable(Result.OK);
        if (properties.file.getValue().isEmpty()) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(messages.getMessage("error.validation.delete.file.empty"));
            return vr;
        }
        return vr;
    }

    public ValidationResult validateListProperties(GoogleDriveListProperties properties) {
        ValidationResultMutable vr = new ValidationResultMutable(Result.OK);
        if (properties.folder.getValue().isEmpty()) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(messages.getMessage("error.validation.folder.empty"));
            return vr;
        }
        return vr;
    }

    public ValidationResult validateGetProperties(GoogleDriveGetProperties properties) {
        ValidationResultMutable vr = new ValidationResultMutable(Result.OK);
        if (properties.file.getValue().isEmpty()) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(messages.getMessage("error.validation.filename.empty"));
            return vr;
        }
        return vr;
    }

    public ValidationResult validatePutProperties(GoogleDrivePutProperties properties) {
        ValidationResultMutable vr = new ValidationResultMutable(Result.OK);
        if (properties.fileName.getValue().isEmpty()) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(messages.getMessage("error.validation.filename.empty"));
            return vr;
        }
        if (!UPLOAD_LOCAL_FILE.equals(properties.uploadMode.getValue())) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(messages.getMessage("error.validation.put.invalid.flow"));
            return vr;
        }
        if (properties.destinationFolder.getValue().isEmpty()) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(messages.getMessage("error.validation.parentfolder.empty"));
            return vr;
        }
        if (properties.localFilePath.getValue().isEmpty()) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(messages.getMessage("error.validation.local.filename.empty"));
            return vr;
        }
        return vr;
    }

    public ValidationResult validateCopyProperties(GoogleDriveCopyProperties properties) {
        ValidationResultMutable vr = new ValidationResultMutable(Result.OK);
        if (properties.destinationFolder.getValue().isEmpty()) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(messages.getMessage("error.validation.destinationFolderName.empty"));
            return vr;
        }
        if (properties.source.getValue().isEmpty()) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(messages.getMessage("error.validation.source.empty"));
            return vr;
        }
        if (properties.rename.getValue()) {
            if (properties.newName.getValue().isEmpty()) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.newname.empty"));
                return vr;
            }
        }
        return vr;
    }

}
