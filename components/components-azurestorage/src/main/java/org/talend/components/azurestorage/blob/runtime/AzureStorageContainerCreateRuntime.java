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
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties.AccessControl;
import org.talend.components.azurestorage.utils.AzureStorageUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

/**
 * Runtime implementation for Azure storage container create feature.<br/>
 * These methods are called only on Driver node in following order: <br/>
 * 1) {@link this#initialize(RuntimeContainer, TAzureStorageContainerCreateProperties)} <br/>
 * 2) {@link this#runAtDriver(RuntimeContainer)} <br/>
 * <b>Instances of this class should not be serialized and sent on worker nodes</b>
 */
public class AzureStorageContainerCreateRuntime extends AzureStorageContainerRuntime
        implements ComponentDriverInitialization<ComponentProperties> {

    private static final long serialVersionUID = -8413348199906078372L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageContainerCreateRuntime.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageContainerCreateRuntime.class);

    private AccessControl access;

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {
        ValidationResult validationResult = super.initialize(runtimeContainer, properties);
        if (validationResult.getStatus() == ValidationResult.Result.ERROR) {
            return validationResult;
        }

        TAzureStorageContainerCreateProperties componentProperties = (TAzureStorageContainerCreateProperties) properties;
        this.access = componentProperties.accessControl.getValue();
        this.dieOnError = componentProperties.dieOnError.getValue();

        return ValidationResult.OK;
    }

    @Override
    public void runAtDriver(RuntimeContainer runtimeContainer) {

        createAzureStorageBlobContainer(runtimeContainer);
        setReturnValues(runtimeContainer);
    }

    private void createAzureStorageBlobContainer(RuntimeContainer runtimeContainer) {
        try {
            boolean containerCreated;
            CloudBlobContainer cloudBlobContainer = getAzureStorageBlobContainerReference(runtimeContainer, containerName);
            containerCreated = createContainerIfNotExist(cloudBlobContainer);
            // Manage accessControl
            if (AccessControl.Public.equals(access) && containerCreated) {
                // Create a permissions object.
                BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
                // Include public access in the permissions object.
                containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
                // Set the permissions on the container.
                cloudBlobContainer.uploadPermissions(containerPermissions);
            }
            if (!containerCreated) {
                LOGGER.warn(messages.getMessage("warn.ContainerExists", containerName));
            }
        } catch (StorageException | InvalidKeyException | URISyntaxException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (dieOnError) {
                throw new ComponentException(e);
            }
        }
    }

    private boolean createContainerIfNotExist(CloudBlobContainer cloudBlobContainer) throws StorageException {
        boolean containerCreated;
        try {
            containerCreated = cloudBlobContainer.createIfNotExists();
        } catch (StorageException e) {
            if (!e.getErrorCode().equals(StorageErrorCodeStrings.CONTAINER_BEING_DELETED)) {
                throw e;
            }
            LOGGER.warn(messages.getMessage("error.CONTAINER_BEING_DELETED", containerName));
            // wait 40 seconds (min is 30s) before retrying.
            // See
            // https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/delete-container
            try {
                Thread.sleep(40000);
            } catch (InterruptedException eint) {
                LOGGER.error(messages.getMessage("error.InterruptedException"));
                throw new ComponentException(eint);
            }
            containerCreated = cloudBlobContainer.createIfNotExists();
            LOGGER.debug(messages.getMessage("debug.ContainerCreated", containerName));
        }

        return containerCreated;
    }

    private void setReturnValues(RuntimeContainer runtimeContainer) {
        String componentId = runtimeContainer.getCurrentComponentId();
        String returnContainer = AzureStorageUtils.getStudioNameFromProperty(AzureStorageContainerDefinition.RETURN_CONTAINER);
        runtimeContainer.setComponentData(componentId, returnContainer, containerName);
    }

}
