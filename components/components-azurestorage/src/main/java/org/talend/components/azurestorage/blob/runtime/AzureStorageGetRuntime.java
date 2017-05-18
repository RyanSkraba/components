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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.blob.AzureStorageBlobDefinition;
import org.talend.components.azurestorage.blob.AzureStorageContainerDefinition;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobGet;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobsGetTable;
import org.talend.components.azurestorage.blob.tazurestorageget.TAzureStorageGetProperties;
import org.talend.components.azurestorage.utils.AzureStorageUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class AzureStorageGetRuntime extends AzureStorageContainerRuntime
        implements ComponentDriverInitialization<ComponentProperties> {

    private static final long serialVersionUID = 3439883025371560977L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageGetRuntime.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageGetRuntime.class);

    private String localFolder;

    private RemoteBlobsGetTable remoteBlobsGet;

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {
        ValidationResult validationResult = super.initialize(runtimeContainer, properties);
        if (validationResult.getStatus() == ValidationResult.Result.ERROR) {
            return validationResult;
        }

        TAzureStorageGetProperties componentProperties = (TAzureStorageGetProperties) properties;
        localFolder = componentProperties.localFolder.getValue();
        remoteBlobsGet = componentProperties.remoteBlobsGet;
        this.dieOnError = componentProperties.dieOnError.getValue();

        String errorMessage = "";
        if (remoteBlobsGet.prefix.getValue().isEmpty()) {

            errorMessage = messages.getMessage("error.EmptyBlobs"); //$NON-NLS-1$
        }

        if (errorMessage.isEmpty()) { // everything is OK.
            return ValidationResult.OK;
        } else {
            return createValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }
    }

    @Override
    public void runAtDriver(RuntimeContainer runtimeContainer) {
        download(runtimeContainer);
        setReturnValues(runtimeContainer);

    }

    private void download(RuntimeContainer runtimeContainer) {
        FileOutputStream fos = null ;

        try {
            CloudBlobContainer blobContainer = getAzureStorageBlobContainerReference(runtimeContainer, containerName);
            List<RemoteBlobGet> remoteBlobs = createRemoteBlobsGet();
            for (RemoteBlobGet rmtb : remoteBlobs) {
                for (ListBlobItem blob : blobContainer.listBlobs(rmtb.prefix, rmtb.include)) {
                    if (blob instanceof CloudBlob) {
                        // TODO - Action when create is false and include is true ???
                        if (rmtb.create) {
                            new File(localFolder + "/" + ((CloudBlob) blob).getName()).getParentFile().mkdirs();
                        }
                        fos = new FileOutputStream(localFolder + "/" + ((CloudBlob) blob).getName());
                        ((CloudBlob) blob).download(fos);

                    }
                }
            }
        } catch (StorageException | InvalidKeyException | URISyntaxException | FileNotFoundException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (dieOnError) {
                throw new ComponentException(e);
            }
        }finally{
            try {
                fos.close();
            } catch (Exception e) {
                //ignore
            }
        }

    }

    public List<RemoteBlobGet> createRemoteBlobsGet() {

        List<RemoteBlobGet> remoteBlobs = new ArrayList<>();
        for (int idx = 0; idx < remoteBlobsGet.prefix.getValue().size(); idx++) {
            String prefix = "";
            boolean include = false;
            boolean create = false;

            if (remoteBlobsGet.prefix.getValue().get(idx) != null) {
                prefix = remoteBlobsGet.prefix.getValue().get(idx);
            }

            if (remoteBlobsGet.include.getValue().get(idx) != null) {
                include = remoteBlobsGet.include.getValue().get(idx);
            }

            if (remoteBlobsGet.create.getValue().get(idx) != null) {
                create = remoteBlobsGet.create.getValue().get(idx);
            }

            remoteBlobs.add(new RemoteBlobGet(prefix, include, create));
        }
        return remoteBlobs;
    }

    public void setReturnValues(RuntimeContainer runtimeContainer) {

        String componentId = runtimeContainer.getCurrentComponentId();
        String containerKey = AzureStorageUtils.getStudioNameFromProperty(AzureStorageContainerDefinition.RETURN_CONTAINER);
        String localFolderKey = AzureStorageUtils.getStudioNameFromProperty(AzureStorageBlobDefinition.RETURN_LOCAL_FOLDER);

        runtimeContainer.setComponentData(componentId, containerKey, this.containerName);
        runtimeContainer.setComponentData(componentId, localFolderKey, this.localFolder);

    }

}
