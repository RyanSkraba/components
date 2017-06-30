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
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.blob.runtime.AzureStorageReader;
import org.talend.components.azurestorage.queue.AzureStorageQueueDefinition;
import org.talend.components.azurestorage.queue.AzureStorageQueueService;
import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

public class AzureStorageQueueInputReader extends AzureStorageReader<IndexedRecord> {

    protected String queueName;

    protected int nbMsg;

    protected int visibilityTimeout;

    protected Boolean peek;

    protected boolean dieOnError;

    protected Schema schema;

    protected Iterator<CloudQueueMessage> messages;

    protected CloudQueueMessage current;

    protected boolean delete = false;

    protected boolean startable;

    protected Boolean advanceable;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueueInputReader.class);

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageQueueInputReader.class);

    public AzureStorageQueueService queueService;

    public AzureStorageQueueInputReader(RuntimeContainer container, BoundedSource source,
            TAzureStorageQueueInputProperties properties) {

        super(container, source);
        queueName = properties.queueName.getValue();
        nbMsg = properties.numberOfMessages.getValue();
        visibilityTimeout = properties.visibilityTimeoutInSeconds.getValue();
        peek = properties.peekMessages.getValue();
        delete = properties.deleteMessages.getValue();
        dieOnError = properties.dieOnError.getValue();
        schema = properties.schema.schema.getValue();
        this.queueService = new AzureStorageQueueService(((AzureStorageQueueSource) source).getAzureConnection(container));
    }

    @Override
    public boolean start() throws IOException {

        try {
            if (peek) {
                messages = queueService.peekMessages(queueName, nbMsg).iterator();
            } else {
                messages = queueService.retrieveMessages(queueName, nbMsg, visibilityTimeout).iterator();
            }
            startable = messages.hasNext();
            if (startable) {
                dataCount++;
                current = messages.next();
                if (delete) {
                    try {
                        queueService.deleteMessage(queueName, current);
                    } catch (StorageException e) {
                        LOGGER.error(i18nMessages.getMessage("error.Cannotdelete", current.getId(), e.getLocalizedMessage()));
                    }
                }
            }
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (dieOnError)
                throw new ComponentException(e);
        }
        return startable;
    }

    @Override
    public boolean advance() throws IOException {
        advanceable = messages.hasNext();
        if (advanceable) {
            dataCount++;
            current = messages.next();

            if (delete) {
                try {
                    queueService.deleteMessage(queueName, current);
                } catch (StorageException | InvalidKeyException | URISyntaxException e) {
                    LOGGER.error(i18nMessages.getMessage("error.Cannotdelete", current.getId(), e.getLocalizedMessage()));
                }
            }
        }
        return advanceable;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        if (!startable || (advanceable != null && !advanceable)) {
            throw new NoSuchElementException();
        }

        IndexedRecord record = new GenericData.Record(schema);
        try {
            for (Field f : schema.getFields()) {
                switch (f.name()) {
                case TAzureStorageQueueInputProperties.FIELD_MESSAGE_ID:
                    record.put(f.pos(), current.getMessageId());
                    break;
                case TAzureStorageQueueInputProperties.FIELD_MESSAGE_CONTENT:
                    record.put(f.pos(), current.getMessageContentAsString());
                    break;
                case TAzureStorageQueueInputProperties.FIELD_INSERTION_TIME:
                    record.put(f.pos(), current.getInsertionTime());
                    break;
                case TAzureStorageQueueInputProperties.FIELD_EXPIRATION_TIME:
                    record.put(f.pos(), current.getExpirationTime());
                    break;
                case TAzureStorageQueueInputProperties.FIELD_DEQUEUE_COUNT:
                    record.put(f.pos(), current.getDequeueCount());
                    break;
                case TAzureStorageQueueInputProperties.FIELD_POP_RECEIPT:
                    record.put(f.pos(), current.getPopReceipt());
                    break;
                case TAzureStorageQueueInputProperties.FIELD_NEXT_VISIBLE_TIME:
                    record.put(f.pos(), current.getNextVisibleTime());
                    break;
                default:
                    LOGGER.warn(i18nMessages.getMessage("warn.UnknowField", f));
                }
            }
        } catch (StorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (dieOnError)
                throw new ComponentException(e);
        }

        return record;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> r = super.getReturnValues();
        r.put(AzureStorageQueueDefinition.RETURN_QUEUE_NAME, queueName);
        return r;
    }

}
