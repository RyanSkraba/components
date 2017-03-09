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
package org.talend.components.azurestorage.queue.runtime;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.blob.runtime.AzureStorageSourceOrSink;
import org.talend.components.azurestorage.queue.AzureStorageQueueProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;

public class AzureStorageQueueSourceOrSink extends AzureStorageSourceOrSink implements SourceOrSink {

    private static final long serialVersionUID = -1124608762722267338L;

    protected RuntimeContainer runtime;

    private final Pattern queueCheckNamePattern = Pattern.compile("^[a-z][a-z0-9]{2,61}[a-z]$");
    
    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageQueueSourceOrSink.class);

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.runtime = container;
        this.properties = (AzureStorageProvideConnectionProperties) properties;
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult vr = super.validate(container);
        if (vr != ValidationResult.OK)
            return vr;
        if (properties instanceof TAzureStorageQueueListProperties) {
            // no validation needed...
            return ValidationResult.OK;
        }
        if (properties instanceof AzureStorageQueueProperties) {
            String q = ((AzureStorageQueueProperties) properties).queueName.getValue();
            if (q.isEmpty()) {
                vr = new ValidationResult();
                vr.setStatus(ValidationResult.Result.ERROR);
                vr.setMessage(i18nMessages.getMessage("error.NameEmpty"));
                return vr;
            }
            if (q.length() < 3 || q.length() > 63) {
                vr = new ValidationResult();
                vr.setStatus(ValidationResult.Result.ERROR);
                vr.setMessage(i18nMessages.getMessage("error.LengthError"));
                return vr;
            }
            if (q.indexOf("--") > -1) {
                vr = new ValidationResult();
                vr.setStatus(ValidationResult.Result.ERROR);
                vr.setMessage(i18nMessages.getMessage("error.TwoDashError"));
                return vr;
            }

            if (!queueCheckNamePattern.matcher(q.replaceAll("-", "")).matches()) {
                vr = new ValidationResult();
                vr.setStatus(ValidationResult.Result.ERROR);
                vr.setMessage(i18nMessages.getMessage("error.QueueNameError"));
                return vr;
            }
        }
        if (properties instanceof TAzureStorageQueueInputProperties) {
            int nom = ((TAzureStorageQueueInputProperties) properties).numberOfMessages.getValue();
            if (nom < 1 || nom > 32) {
                vr = new ValidationResult();
                vr.setStatus(ValidationResult.Result.ERROR);
                vr.setMessage(i18nMessages.getMessage("error.ParameterLengthError"));
                return vr;
            }
            int vtimeout = ((TAzureStorageQueueInputProperties) properties).visibilityTimeoutInSeconds.getValue();
            if (vtimeout < 0) {
                vr = new ValidationResult();
                vr.setStatus(ValidationResult.Result.ERROR);
                vr.setMessage(i18nMessages.getMessage("error.ParameterValueError"));
                return vr;
            }

        }
        return ValidationResult.OK;
    }

    public CloudQueueClient getStorageQueueClient(RuntimeContainer runtime) throws InvalidKeyException, URISyntaxException {
        return getStorageAccount(runtime).createCloudQueueClient();
    }

    public CloudQueue getCloudQueue(RuntimeContainer runtime, String queue)
            throws InvalidKeyException, URISyntaxException, StorageException {
        return getStorageQueueClient(runtime).getQueueReference(queue);
    }

    public static List<NamedThing> getSchemaNames(RuntimeContainer container, TAzureStorageConnectionProperties properties)
            throws IOException {
        AzureStorageQueueSourceOrSink sos = new AzureStorageQueueSourceOrSink();
        sos.initialize(container, properties);
        return sos.getSchemaNames(container);
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        List<NamedThing> result = new ArrayList<>();
        try {
            CloudQueueClient client = getStorageQueueClient(container);
            for (CloudQueue q : client.listQueues()) {
                result.add(new SimpleNamedThing(q.getName(), q.getName()));
            }
        } catch (InvalidKeyException | URISyntaxException e) {
            throw new ComponentException(e);
        }
        return result;
    }

}
