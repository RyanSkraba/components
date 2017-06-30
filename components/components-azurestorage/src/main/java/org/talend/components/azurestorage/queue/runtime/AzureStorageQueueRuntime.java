package org.talend.components.azurestorage.queue.runtime;

import java.util.regex.Pattern;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.blob.runtime.AzureStorageRuntime;
import org.talend.components.azurestorage.queue.AzureStorageQueueProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

public abstract class AzureStorageQueueRuntime extends AzureStorageRuntime {

    private static final long serialVersionUID = 6643780058392016608L;

    private final Pattern queueCheckNamePattern = Pattern.compile("[a-z0-9]{2,63}");

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageQueueRuntime.class);

    protected String queueName;

    @Override
    public ValidationResult initialize(RuntimeContainer runtimeContainer, ComponentProperties properties) {
        // init
        AzureStorageQueueProperties componentProperties = (AzureStorageQueueProperties) properties;

        this.queueName = componentProperties.queueName.getValue();

        // validate
        ValidationResult validationResult = super.initialize(runtimeContainer, properties);
        if (validationResult.getStatus() == ValidationResult.Result.ERROR) {
            return validationResult;
        }

        String errorMessage = "";
        if (queueName.isEmpty()) {
            errorMessage = messages.getMessage("error.NameEmpty");
            return new ValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }
        if (queueName.length() < 3 || queueName.length() > 63) {
            errorMessage = messages.getMessage("error.LengthError");
            return new ValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }
        if (queueName.indexOf("--") > -1) {
            errorMessage = messages.getMessage("error.TwoDashError");
            return new ValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }

        if (!queueCheckNamePattern.matcher(queueName.replaceAll("-", "")).matches()) {
            errorMessage = messages.getMessage("error.QueueNameError");
            return new ValidationResult(ValidationResult.Result.ERROR, errorMessage);
        }

        return ValidationResult.OK;
    }

}
