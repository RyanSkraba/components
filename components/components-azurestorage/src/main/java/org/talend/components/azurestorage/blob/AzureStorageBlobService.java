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
package org.talend.components.azurestorage.blob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.AzureConnection;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

/**
 * This class encapsulate and provide azure storage blob services
 */
public class AzureStorageBlobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageBlobService.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
                                                           .getI18nMessages(AzureStorageBlobService.class);

    private AzureConnection connection;

    /**
     * @param connection
     */
    public AzureStorageBlobService(final AzureConnection connection) {
        super();
        this.connection = connection;
    }

    /**
     * This method create an azure container if it doesn't exist and set it access policy
     *
     * @param containerName : the name of the container to be created
     * @return true if the container was created, false otherwise
     */
    public boolean createContainerIfNotExist(final String containerName, final BlobContainerPublicAccessType accessType)
            throws StorageException, URISyntaxException, InvalidKeyException {
        CloudBlobClient cloudBlobClient = connection.getCloudStorageAccount()
                                                    .createCloudBlobClient();
        CloudBlobContainer cloudBlobContainer = cloudBlobClient.getContainerReference(containerName);

        boolean containerCreated;
        try {
            containerCreated = cloudBlobContainer.createIfNotExists(accessType, null, null);
        } catch (StorageException e) {
            if (!e.getErrorCode()
                  .equals(StorageErrorCodeStrings.CONTAINER_BEING_DELETED)) {
                throw e;
            }
            LOGGER.warn(messages.getMessage("error.CONTAINER_BEING_DELETED", containerName));
            // wait 40 seconds (min is 30s) before retrying.
            // See https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/delete-container
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

    /**
     * This method delete the container if exist
     */
    public boolean deleteContainerIfExist(final String containerName)
            throws StorageException, URISyntaxException, InvalidKeyException {
        CloudBlobClient cloudBlobClient = connection.getCloudStorageAccount()
                                                    .createCloudBlobClient();
        CloudBlobContainer cloudBlobContainer = cloudBlobClient.getContainerReference(containerName);
        return cloudBlobContainer.deleteIfExists();
    }

    /**
     * @return true if the a container exist with the given name, false otherwise
     */
    public boolean containerExist(final String containerName)
            throws StorageException, URISyntaxException, InvalidKeyException {
        CloudBlobClient cloudBlobClient = connection.getCloudStorageAccount()
                                                    .createCloudBlobClient();
        CloudBlobContainer cloudBlobContainer = cloudBlobClient.getContainerReference(containerName);
        return cloudBlobContainer.exists();
    }

    public Iterable<CloudBlobContainer> listContainers() throws InvalidKeyException, URISyntaxException {
        CloudBlobClient cloudBlobClient = connection.getCloudStorageAccount()
                                                    .createCloudBlobClient();
        return cloudBlobClient.listContainers();
    }

    public Iterable<ListBlobItem> listBlobs(final String containerName, final String prefix, final boolean
            useFlatBlobListing)
            throws URISyntaxException, StorageException, InvalidKeyException {
        CloudBlobClient cloudBlobClient = connection.getCloudStorageAccount()
                                                    .createCloudBlobClient();
        CloudBlobContainer cloudBlobContainer = cloudBlobClient.getContainerReference(containerName);
        return cloudBlobContainer.listBlobs(prefix, useFlatBlobListing);
    }

    public boolean deleteBlobBlockIfExist(final CloudBlockBlob block) throws StorageException {
        return block.deleteIfExists();
    }

    public void download(final CloudBlob blob, final OutputStream outStream) throws StorageException {
        blob.download(outStream);
    }

    public void upload(final String containerName, final String blobName, final InputStream sourceStream, final long
            length)
            throws StorageException, IOException, URISyntaxException, InvalidKeyException {
        CloudBlobClient cloudBlobClient = connection.getCloudStorageAccount()
                                                    .createCloudBlobClient();
        CloudBlobContainer cloudBlobContainer = cloudBlobClient.getContainerReference(containerName);
        CloudBlockBlob blob = cloudBlobContainer.getBlockBlobReference(blobName);
        blob.upload(sourceStream, length);
    }

}
