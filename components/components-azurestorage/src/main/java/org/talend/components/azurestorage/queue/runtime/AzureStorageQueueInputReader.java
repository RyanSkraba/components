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
import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputProperties;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

public class AzureStorageQueueInputReader extends AzureStorageReader<IndexedRecord> {

    private TAzureStorageQueueInputProperties properties;

    protected Iterator<CloudQueueMessage> messages;

    protected CloudQueueMessage current;

    protected Boolean delete = Boolean.FALSE;

    protected CloudQueue queue;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueueInputReader.class);

    public AzureStorageQueueInputReader(RuntimeContainer container, BoundedSource source,
            TAzureStorageQueueInputProperties properties) {
        super(container, source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        Boolean startable = false;
        String queueName = properties.queueName.getValue();
        int nbMsg = properties.numberOfMessages.getValue();
        int visibilityTimeout = properties.visibilityTimeoutInSeconds.getValue();
        Boolean peek = properties.peekMessages.getValue();
        delete = properties.deleteMessages.getValue();
        try {
            queue = ((AzureStorageQueueSource) getCurrentSource()).getCloudQueue(runtime, queueName);
            if (peek) {
                messages = queue.peekMessages(nbMsg).iterator();
            } else {
                messages = queue.retrieveMessages(nbMsg, visibilityTimeout, null, null).iterator();
            }
            startable = messages.hasNext();
            if (startable) {
                dataCount++;
                current = messages.next();
                if (delete) {
                    try {
                        queue.deleteMessage(current);
                    } catch (StorageException e) {
                        LOGGER.error("Could not delete message {} ! Cause: {}.", current.getId(), e.getLocalizedMessage());
                    }
                }
            }
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue())
                throw new ComponentException(e);
        }
        return startable;
    }

    @Override
    public boolean advance() throws IOException {
        Boolean advanceable = messages.hasNext();
        if (advanceable) {
            dataCount++;
            current = messages.next();
            //
            if (delete) {
                try {
                    queue.deleteMessage(current);
                } catch (StorageException e) {
                    LOGGER.error("Could not delete message {} ! Cause: {}.", current.getId(), e.getLocalizedMessage());
                }
            }
        }
        return advanceable;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        CloudQueueMessage msg = current;
        Schema schema = properties.schema.schema.getValue();
        IndexedRecord record = new GenericData.Record(schema);
        try {
            for (Field f : schema.getFields()) {
                switch (f.name()) {
                case TAzureStorageQueueInputProperties.FIELD_MESSAGE_ID:
                    record.put(f.pos(), msg.getMessageId());
                    break;
                case TAzureStorageQueueInputProperties.FIELD_MESSAGE_CONTENT:
                    record.put(f.pos(), msg.getMessageContentAsString());
                    break;
                case TAzureStorageQueueInputProperties.FIELD_INSERTION_TIME:
                    record.put(f.pos(), msg.getInsertionTime());
                    break;
                case TAzureStorageQueueInputProperties.FIELD_EXPIRATION_TIME:
                    record.put(f.pos(), msg.getExpirationTime());
                    break;
                case TAzureStorageQueueInputProperties.FIELD_DEQUEUE_COUNT:
                    record.put(f.pos(), msg.getDequeueCount());
                    break;
                case TAzureStorageQueueInputProperties.FIELD_POP_RECEIPT:
                    record.put(f.pos(), msg.getPopReceipt());
                    break;
                case TAzureStorageQueueInputProperties.FIELD_NEXT_VISIBLE_TIME:
                    record.put(f.pos(), msg.getNextVisibleTime());
                    break;
                default:
                    LOGGER.warn("Unknow field {}.", f);
                }
            }
        } catch (StorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue())
                throw new ComponentException(e);
        }

        return record;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> r = super.getReturnValues();
        r.put(AzureStorageQueueDefinition.RETURN_QUEUE_NAME, properties.queueName.getValue());
        return r;
    }

}
