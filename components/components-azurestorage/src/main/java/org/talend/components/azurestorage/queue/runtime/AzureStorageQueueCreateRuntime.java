package org.talend.components.azurestorage.queue.runtime;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.queue.AzureStorageQueueDefinition;
import org.talend.components.azurestorage.utils.AzureStorageUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;

public class AzureStorageQueueCreateRuntime extends AzureStorageQueueRuntime
        implements ComponentDriverInitialization<ComponentProperties>{
    
    private static final long serialVersionUID = 4538178425922351172L;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueueCreateRuntime.class);
    
    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageQueueCreateRuntime.class);
    
    @Override
    public void runAtDriver(RuntimeContainer container){
        createAzureQueue(container);
        setReturnValues(container);
        
    }
    
    private boolean createAzureQueue(RuntimeContainer container){
        Boolean createResult = false;
        try {
            CloudQueue cqueue = getCloudQueue(container, QueueName);
            LOGGER.debug(messages.getMessage("debug.QueuePrecreate", cqueue.getName()));
            try {
                createResult = cqueue.createIfNotExists();
            } catch (StorageException e) {
                if (!e.getErrorCode().equals(StorageErrorCodeStrings.QUEUE_BEING_DELETED)) {
                    throw e;
                }
                LOGGER.error(messages.getMessage("error.QueueDeleted", cqueue.getName()));
                // Documentation doesn't specify how many seconds at least to wait.
                // 40 seconds before retrying.
                // See https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/delete-queue3
                try {
                    Thread.sleep(40000);
                } catch (InterruptedException eint) {
                        throw new RuntimeException(messages.getMessage("error.InterruptedException"));
                }
                createResult = cqueue.createIfNotExists();
                LOGGER.debug(messages.getMessage("debug.QueueCreated", cqueue.getName()));
            }
            if (!createResult) {
                LOGGER.warn(messages.getMessage("warn.QueueExist", cqueue.getName()));
            }
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (dieOnError)
                throw new ComponentException(e);
        }
        return createResult;
    }
    
    
    private void setReturnValues (RuntimeContainer container){
        String componentId = container.getCurrentComponentId();
        String returnQueueName = AzureStorageUtils.getStudioNameFromProperty(AzureStorageQueueDefinition.RETURN_QUEUE_NAME);
        container.setComponentData(componentId, returnQueueName, QueueName);
    }
    

}
