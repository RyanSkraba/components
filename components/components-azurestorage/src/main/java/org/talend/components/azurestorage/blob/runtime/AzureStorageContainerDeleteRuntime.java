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
package org.talend.components.azurestorage.blob.runtime;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.blob.AzureStorageContainerDefinition;
import org.talend.components.azurestorage.utils.AzureStorageUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

public class AzureStorageContainerDeleteRuntime extends AzureStorageContainerRuntime
        implements ComponentDriverInitialization<ComponentProperties> {

    private static final long serialVersionUID = -4320180294438040454L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageContainerDeleteRuntime.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageContainerDeleteRuntime.class);

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {
        ValidationResult validationResult = super.initialize(runtimeContainer, properties);
        if (validationResult.getStatus() == ValidationResult.Result.ERROR) {
            return validationResult;
        }

        return ValidationResult.OK;
    }

    @Override
    public void runAtDriver(RuntimeContainer runtimeContainer) {

        deleteBlobContainerIfExists(runtimeContainer);
        setReturnValues(runtimeContainer);
    }

    private void deleteBlobContainerIfExists(RuntimeContainer runtimeContainer) {
        try {

            CloudBlobContainer container = getAzureStorageBlobContainerReference(runtimeContainer, this.containerName);
            boolean result = container.deleteIfExists();
            if (!result) {
                LOGGER.warn(messages.getMessage("error.ContainerNotExist", this.containerName));
            }

        } catch (StorageException | InvalidKeyException | URISyntaxException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (this.dieOnError) {
                throw new ComponentException(e);
            }
        }
    }

    private void setReturnValues(RuntimeContainer runtimeContainer) {
        String componentId = runtimeContainer.getCurrentComponentId();
        String returnContainer = AzureStorageUtils.getStudioNameFromProperty(AzureStorageContainerDefinition.RETURN_CONTAINER);

        runtimeContainer.setComponentData(componentId, returnContainer, containerName);
    }

}
