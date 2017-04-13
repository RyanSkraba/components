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

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;

public class AzureStorageQueuePurgeRuntime extends AzureStorageQueueRuntime
        implements ComponentDriverInitialization<ComponentProperties> {
    
    private static final long serialVersionUID = -3360650467409498941L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueuePurgeRuntime.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageQueuePurgeRuntime.class);

    @Override
    public void runAtDriver(RuntimeContainer container) {
        purgeAzureQueue(container);
        setReturnValues(container);
    }

    private boolean purgeAzureQueue(RuntimeContainer container) {
        Boolean purgeResult = false;
        try {
            CloudQueue queue = getCloudQueue(container, QueueName);
            int dataCount = (int) queue.getApproximateMessageCount();
            LOGGER.debug(messages.getMessage("debug.Purgeing", dataCount, queue.getName()));
            queue.clear();
            purgeResult = true;
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (dieOnError)
                throw new ComponentException(e);
        }
        return purgeResult;
    }

    private void setReturnValues(RuntimeContainer container) {
        String componentId = container.getCurrentComponentId();
        String returnQueueName = AzureStorageUtils
                .getStudioNameFromProperty(AzureStorageQueueDefinition.RETURN_QUEUE_NAME);
        container.setComponentData(componentId, returnQueueName, QueueName);
    }

}
