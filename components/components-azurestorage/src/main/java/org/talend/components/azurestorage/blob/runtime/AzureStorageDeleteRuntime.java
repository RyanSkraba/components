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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.blob.AzureStorageContainerDefinition;
import org.talend.components.azurestorage.blob.helpers.RemoteBlob;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobsTable;
import org.talend.components.azurestorage.blob.tazurestoragedelete.TAzureStorageDeleteProperties;
import org.talend.components.azurestorage.utils.AzureStorageUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class AzureStorageDeleteRuntime extends AzureStorageContainerRuntime
        implements ComponentDriverInitialization<ComponentProperties> {

    private static final long serialVersionUID = -1061435894570205595L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageDeleteRuntime.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageDeleteRuntime.class);

    //
    private RemoteBlobsTable remoteBlobsTable;

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {
        ValidationResult validationResult = super.initialize(runtimeContainer, properties);
        if (validationResult.getStatus() == ValidationResult.Result.ERROR) {
            return validationResult;
        }

        TAzureStorageDeleteProperties componentProperties = (TAzureStorageDeleteProperties) properties;
        remoteBlobsTable = componentProperties.remoteBlobs;
        this.dieOnError = componentProperties.dieOnError.getValue();

        return componentProperties.remoteBlobs.getValidationResult();
    }

    @Override
    public void runAtDriver(RuntimeContainer runtimeContainer) {
        deleteIfExist(runtimeContainer);
        setReturnValues(runtimeContainer);
    }

    public void deleteIfExist(RuntimeContainer runtimeContainer) {

        try {
            CloudBlobContainer container = getAzureStorageBlobContainerReference(runtimeContainer, containerName);
            List<RemoteBlob> remoteBlobs = createRemoteBlobFilter();
            for (RemoteBlob rmtb : remoteBlobs) {
                for (ListBlobItem blob : container.listBlobs(rmtb.prefix, rmtb.include)) {
                    if (blob instanceof CloudBlockBlob) {
                        // FIXME - problem with blobs with space in name...
                        boolean successfulyDeleted = ((CloudBlockBlob) blob).deleteIfExists();
                        if (!successfulyDeleted) {
                            LOGGER.warn(messages.getMessage("warn.FaildDelete", ((CloudBlockBlob) blob).getName()));
                        }
                    }
                }
            }
        } catch (StorageException | InvalidKeyException | URISyntaxException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (dieOnError) {
                throw new ComponentException(e);
            }
        }
    }

    /**
     * Create remote blob table used in filtering remote blob
     */
    private List<RemoteBlob> createRemoteBlobFilter() {
        List<RemoteBlob> remoteBlobs = new ArrayList<RemoteBlob>();
        for (int idx = 0; idx < this.remoteBlobsTable.prefix.getValue().size(); idx++) {
            String prefix = "";
            boolean include = false;
            if (this.remoteBlobsTable.prefix.getValue().get(idx) != null) {
                prefix = this.remoteBlobsTable.prefix.getValue().get(idx);
            }
            if (remoteBlobsTable.include.getValue().get(idx) != null) {
                include = remoteBlobsTable.include.getValue().get(idx);
            }

            remoteBlobs.add(new RemoteBlob(prefix, include));
        }

        return remoteBlobs;
    }

    public void setReturnValues(RuntimeContainer runtimeContainer) {

        String componentId = runtimeContainer.getCurrentComponentId();
        String containerKey = AzureStorageUtils.getStudioNameFromProperty(AzureStorageContainerDefinition.RETURN_CONTAINER);
        //
        runtimeContainer.setComponentData(componentId, containerKey, this.containerName);

    }

}
