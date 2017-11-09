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
import org.talend.components.google.drive.delete.GoogleDriveDeleteDefinition;
import org.talend.components.google.drive.delete.GoogleDriveDeleteProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

public class GoogleDriveDeleteRuntime extends GoogleDriveRuntime implements ComponentDriverInitialization<ComponentProperties> {

    private GoogleDriveDeleteProperties properties;

    private String fileId;

    private transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveDeleteRuntime.class);

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        ValidationResult vr = super.initialize(container, properties);
        if (vr.getStatus().equals(Result.ERROR)) {
            return vr;
        }
        this.properties = (GoogleDriveDeleteProperties) properties;
        return validateProperties(this.properties);
    }

    @Override
    public void runAtDriver(RuntimeContainer container) {
        delete(container);
        setReturnValues(container);
    }

    public void delete(RuntimeContainer container) {
        try {
            if (properties.deleteMode.getValue().equals(AccessMethod.Name)) {
                fileId = getDriveUtils().deleteResourceByName(properties.file.getValue(), properties.useTrash.getValue());
            } else {
                fileId = getDriveUtils().deleteResourceById(properties.file.getValue(), properties.useTrash.getValue());
            }
        } catch (IOException | GeneralSecurityException e) {
            LOG.error(e.getLocalizedMessage());
            throw new ComponentException(e);
        }
    }

    public void setReturnValues(RuntimeContainer container) {
        String componentId = container.getCurrentComponentId();
        container.setComponentData(componentId, GoogleDriveDeleteDefinition.RETURN_FILE_ID, fileId);
    }
}
