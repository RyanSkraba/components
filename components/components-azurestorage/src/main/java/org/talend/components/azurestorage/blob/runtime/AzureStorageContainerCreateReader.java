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

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.blob.AzureStorageContainerDefinition;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

public class AzureStorageContainerCreateReader extends AzureStorageReader<Boolean> {

    private TAzureStorageContainerCreateProperties properties;

    private Boolean result = Boolean.FALSE;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageContainerCreateReader.class);
    
    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
    		             .getI18nMessages(AzureStorageContainerCreateReader.class);

    public AzureStorageContainerCreateReader(RuntimeContainer container, BoundedSource source,
            TAzureStorageContainerCreateProperties properties) {
        super(container, source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        try {
            String mycontainer = properties.container.getValue();
            String access = properties.accessControl.getStringValue();
            CloudBlobContainer container = ((AzureStorageSource) getCurrentSource()).getStorageContainerReference(runtime,
                    mycontainer);
            try {
                result = container.createIfNotExists();
            } catch (StorageException e) {
                if (!e.getErrorCode().equals(StorageErrorCodeStrings.CONTAINER_BEING_DELETED)) {
                    throw e;
                }
                LOGGER.error(messages.getMessage("error.CONTAINER_BEING_DELETED", mycontainer));
                // wait 40 seconds (min is 30s) before retrying.
                // See https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/delete-container
                try {
                    Thread.sleep(40000);
                } catch (InterruptedException eint) {
                    throw new IOException(messages.getMessage("error.InterruptedException"));
                }
                result = container.createIfNotExists();
                LOGGER.debug(messages.getMessage("debug.ContainerCreated", mycontainer));
            }
            // Manage accessControl
            if (access.equals("Public") && result) {
                // Create a permissions object.
                BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
                // Include public access in the permissions object.
                containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
                // Set the permissions on the container.
                container.uploadPermissions(containerPermissions);
            }
            if (!result) {
                LOGGER.warn(messages.getMessage("warn.ContainerExists", mycontainer));
            }
            dataCount++;
            return result;
        } catch (StorageException | InvalidKeyException | URISyntaxException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue())
                throw new ComponentException(e);
            return result;
        }
    }

    @Override
    public boolean advance() throws IOException {
        return false;
    }

    @Override
    public Boolean getCurrent() throws NoSuchElementException {
        return result;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> resultMap = super.getReturnValues();
        resultMap.put(AzureStorageContainerDefinition.RETURN_CONTAINER, properties.container.getValue());

        return resultMap;
    }
}
