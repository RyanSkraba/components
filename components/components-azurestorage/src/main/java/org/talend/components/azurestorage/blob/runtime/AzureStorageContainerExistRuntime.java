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
import org.talend.components.azurestorage.blob.tazurestoragecontainerexist.TAzureStorageContainerExistDefinition;
import org.talend.components.azurestorage.utils.AzureStorageUtils;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

public class AzureStorageContainerExistRuntime extends AzureStorageContainerRuntime
        implements ComponentDriverInitialization<ComponentProperties> {

    private static final long serialVersionUID = 8454949161040534258L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageContainerExistRuntime.class);

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
        boolean containerExist = isAzureStorageBlobContainerExist(runtimeContainer);
        setReturnValues(runtimeContainer, containerExist);
    }

    private Boolean isAzureStorageBlobContainerExist(RuntimeContainer runtimeContainer) {
        try {
            CloudBlobContainer container = getAzureStorageBlobContainerReference(runtimeContainer, this.containerName);
            return container.exists();

        } catch (StorageException | URISyntaxException | InvalidKeyException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (this.dieOnError) {
                throw new ComponentException(e);
            }
            return false;
        }
    }

    private void setReturnValues(RuntimeContainer runtimeContainer, boolean containerExist) {
        String componentId = runtimeContainer.getCurrentComponentId();
        String returnContainer = AzureStorageUtils.getStudioNameFromProperty(AzureStorageContainerDefinition.RETURN_CONTAINER);
        String returnContainerExist = AzureStorageUtils
                .getStudioNameFromProperty(TAzureStorageContainerExistDefinition.RETURN_CONTAINER_EXIST);

        runtimeContainer.setComponentData(componentId, returnContainer, containerName);
        runtimeContainer.setComponentData(componentId, returnContainerExist, containerExist);
    }

}
