package org.talend.components.azurestorage.queue.runtime;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.regex.Pattern;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.blob.runtime.AzureStorageRuntime;
import org.talend.components.azurestorage.queue.AzureStorageQueueProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;

public class AzureStorageQueueRuntime extends AzureStorageRuntime {

    private static final long serialVersionUID = 6643780058392016608L;

    private final Pattern queueCheckNamePattern = Pattern.compile("[a-z0-9]{2,63}");

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageQueueRuntime.class);

    protected String QueueName;

    /**
     * getServiceClient.
     *
     * @param runtimeContainer
     *            {@link RuntimeContainer} container
     * @return {@link CloudBlobClient} cloud blob client
     */
    public CloudQueueClient getQueueServiceClient(RuntimeContainer runtimeContainer)
            throws InvalidKeyException, URISyntaxException {
        return getStorageAccount(runtimeContainer).createCloudQueueClient();
    }

    /**
     * getStorageContainerReference.
     *
     * @param runtimeContainer
     *            {@link RuntimeContainer} container
     * @param containerName
     *            {@link String} storage container
     * @return {@link CloudBlobContainer} cloud blob container
     * @throws StorageException
     * @throws URISyntaxException
     * @throws InvalidKeyException
     */
    public CloudQueue getCloudQueue(RuntimeContainer runtimeContainer, String queueName)
            throws InvalidKeyException, URISyntaxException, StorageException {

        return getQueueServiceClient(runtimeContainer).getQueueReference(queueName);
    }

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {
        // init
        AzureStorageQueueProperties componentProperties = (AzureStorageQueueProperties) properties;

        this.QueueName = componentProperties.queueName.getValue();
        

        // validate
        ValidationResult validationResult = super.initialize(runtimeContainer, properties);
        if (validationResult.getStatus() == ValidationResult.Result.ERROR) {
            return validationResult;
        }

        String errorMessage = "";
        if (QueueName.isEmpty()) {
            errorMessage = messages.getMessage("error.NameEmpty");
            return createValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }
        if (QueueName.length() < 3 || QueueName.length() > 63) {
            errorMessage = messages.getMessage("error.LengthError");
            return createValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }
        if (QueueName.indexOf("--") > -1) {
            errorMessage = messages.getMessage("error.TwoDashError");
            return createValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }

        if (!queueCheckNamePattern.matcher(QueueName.replaceAll("-", "")).matches()) {
            errorMessage = messages.getMessage("error.QueueNameError");
            return createValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }

            return ValidationResult.OK;

    }

}
