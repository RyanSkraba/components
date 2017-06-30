package org.talend.components.azurestorage.queue.runtime;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.queue.AzureStorageQueueService;
import org.talend.components.azurestorage.queue.AzureStorageQueueDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeuecreate.TAzureStorageQueueCreateProperties;
import org.talend.components.azurestorage.utils.AzureStorageUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;

public class AzureStorageQueueCreateRuntime extends AzureStorageQueueRuntime
        implements ComponentDriverInitialization<ComponentProperties> {

    private static final long serialVersionUID = 4538178425922351172L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueueCreateRuntime.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageQueueCreateRuntime.class);

    private boolean dieOnError;

    public AzureStorageQueueService queueService;

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {
        ValidationResult vr = super.initialize(runtimeContainer, properties);
        if (!ValidationResult.OK.getStatus().equals(vr.getStatus())) {
            return vr;
        }

        this.dieOnError = ((TAzureStorageQueueCreateProperties) properties).dieOnError.getValue();
        this.queueService = new AzureStorageQueueService(getAzureConnection(runtimeContainer));

        return ValidationResult.OK;
    }

    @Override
    public void runAtDriver(RuntimeContainer container) {
        createAzureQueue(container);
        setReturnValues(container);

    }

    private boolean createAzureQueue(RuntimeContainer container) {
        Boolean createResult = false;
        try {
            LOGGER.debug(messages.getMessage("debug.QueuePrecreate", queueName));
            createResult = queueService.createQueueIfNotExists(queueName);
            if (!createResult) {
                LOGGER.warn(messages.getMessage("warn.QueueExist", queueName));
            }
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (dieOnError) {
                throw new ComponentException(e);
            }
        }
        return createResult;
    }

    private void setReturnValues(RuntimeContainer container) {
        String componentId = container.getCurrentComponentId();
        String returnQueueName = AzureStorageUtils.getStudioNameFromProperty(AzureStorageQueueDefinition.RETURN_QUEUE_NAME);
        container.setComponentData(componentId, returnQueueName, queueName);
    }

}
