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
package org.talend.components.azurestorage.queue;

import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import com.microsoft.azure.storage.queue.QueueListingDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.azurestorage.AzureConnection;
import org.talend.components.azurestorage.utils.AzureStorageUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

/**
 * This class encapsulate and provide azure storage blob services
 */
public class AzureStorageQueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueueService.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageQueueService.class);

    private AzureConnection connection;

    /**
     * @param connection
     */
    public AzureStorageQueueService(final AzureConnection connection) {
        super();
        this.connection = connection;
    }

    /**
     * This method create a queue if it doesn't exist
     */
    public boolean createQueueIfNotExists(String queueName) throws InvalidKeyException, URISyntaxException, StorageException {
        CloudQueueClient client = connection.getCloudStorageAccount().createCloudQueueClient();
        CloudQueue queueRef = client.getQueueReference(queueName);
        boolean creationResult;
        try {
            creationResult = queueRef.createIfNotExists(null, AzureStorageUtils.getTalendOperationContext());
        } catch (StorageException e) {
            if (!e.getErrorCode().equals(StorageErrorCodeStrings.QUEUE_BEING_DELETED)) {
                throw e;
            }
            LOGGER.warn(messages.getMessage("error.QueueDeleted", queueRef.getName()));
            // Documentation doesn't specify how many seconds at least to wait.
            // 40 seconds before retrying.
            // See https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/delete-queue3
            try {
                Thread.sleep(40000);
            } catch (InterruptedException eint) {
                throw new RuntimeException(messages.getMessage("error.InterruptedException"));
            }
            creationResult = queueRef.createIfNotExists(null, AzureStorageUtils.getTalendOperationContext());
            LOGGER.debug(messages.getMessage("debug.QueueCreated", queueRef.getName()));
        }

        return creationResult;
    }

    public boolean deleteQueueIfExists(String queueName) throws InvalidKeyException, URISyntaxException, StorageException {
        CloudQueueClient client = connection.getCloudStorageAccount().createCloudQueueClient();
        CloudQueue queueRef = client.getQueueReference(queueName);
        return queueRef.deleteIfExists(null, AzureStorageUtils.getTalendOperationContext());
    }

    public Iterable<CloudQueueMessage> peekMessages(String queueName, int numberOfMessages)
            throws InvalidKeyException, URISyntaxException, StorageException {

        CloudQueueClient client = connection.getCloudStorageAccount().createCloudQueueClient();
        CloudQueue queueRef = client.getQueueReference(queueName);
        return queueRef.peekMessages(numberOfMessages, null, AzureStorageUtils.getTalendOperationContext());
    }

    public Iterable<CloudQueueMessage> retrieveMessages(String queueName, int numberOfMessages)
            throws InvalidKeyException, URISyntaxException, StorageException {

        return retrieveMessages(queueName, numberOfMessages, 30);
    }

    public Iterable<CloudQueueMessage> retrieveMessages(String queueName, int numberOfMessages, int visibilityTimeoutInSeconds)
            throws InvalidKeyException, URISyntaxException, StorageException {

        CloudQueueClient client = connection.getCloudStorageAccount().createCloudQueueClient();
        CloudQueue queueRef = client.getQueueReference(queueName);
        return queueRef.retrieveMessages(numberOfMessages, visibilityTimeoutInSeconds, null, AzureStorageUtils.getTalendOperationContext());
    }

    public void deleteMessage(String queueName, CloudQueueMessage message)
            throws InvalidKeyException, URISyntaxException, StorageException {

        CloudQueueClient client = connection.getCloudStorageAccount().createCloudQueueClient();
        CloudQueue queueRef = client.getQueueReference(queueName);
        queueRef.deleteMessage(message, null, AzureStorageUtils.getTalendOperationContext());
    }

    public Iterable<CloudQueue> listQueues() throws InvalidKeyException, URISyntaxException {

        CloudQueueClient client = connection.getCloudStorageAccount().createCloudQueueClient();
        return client.listQueues(null, QueueListingDetails.NONE, null, AzureStorageUtils.getTalendOperationContext());
    }

    public long getApproximateMessageCount(String queueName) throws InvalidKeyException, URISyntaxException, StorageException {
        CloudQueueClient client = connection.getCloudStorageAccount().createCloudQueueClient();
        CloudQueue queueRef = client.getQueueReference(queueName);
        return queueRef.getApproximateMessageCount();
    }

    public void clear(String queueName) throws InvalidKeyException, URISyntaxException, StorageException {
        CloudQueueClient client = connection.getCloudStorageAccount().createCloudQueueClient();
        CloudQueue queueRef = client.getQueueReference(queueName);
        queueRef.clear(null, AzureStorageUtils.getTalendOperationContext());
    }

}
