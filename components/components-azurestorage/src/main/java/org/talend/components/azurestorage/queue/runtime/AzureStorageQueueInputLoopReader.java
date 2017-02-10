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
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.queue.tazurestoragequeueinputloop.TAzureStorageQueueInputLoopProperties;

import com.microsoft.azure.storage.StorageException;

public class AzureStorageQueueInputLoopReader extends AzureStorageQueueInputReader {

    private TAzureStorageQueueInputLoopProperties properties;

    private int nbMsg;

    private int loopWaitTime;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueueInputLoopReader.class);

    public AzureStorageQueueInputLoopReader(RuntimeContainer container, BoundedSource source,
            TAzureStorageQueueInputLoopProperties properties) {
        super(container, source, properties);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        LOGGER.warn("start");
        Boolean startable = false;
        String queueName = properties.queueName.getValue();
        loopWaitTime = properties.loopWaitTime.getValue();
        nbMsg = properties.numberOfMessages.getValue();
        // acquire queue and first messages
        try {
            queue = ((AzureStorageQueueSource) getCurrentSource()).getCloudQueue(runtime, queueName);
            messages = queue.retrieveMessages(nbMsg).iterator();
            LOGGER.warn("Messages : {}.", messages);

        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue())
                throw new ComponentException(e);
        }
        //
        try {
            startable = messages.hasNext();
            if (startable) {
                dataCount++;
                current = messages.next();
                queue.deleteMessage(current);
                return startable;
            }
        } catch (StorageException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue())
                throw new ComponentException(e);
        }
        // first loop to wait for a first message batch
        while (true) {
            try {
                LOGGER.warn("Checking for new messages");
                messages = queue.retrieveMessages(nbMsg).iterator();
                startable = messages.hasNext();
                if (startable) {
                    dataCount++;
                    current = messages.next();
                    queue.deleteMessage(current);
                    return startable;
                }
                Thread.sleep((long) loopWaitTime * 1000);
            } catch (InterruptedException | StorageException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public boolean advance() throws IOException {
        LOGGER.warn("advance");
        Boolean advanceable = messages.hasNext();
        if (advanceable) {
            try {
                dataCount++;
                current = messages.next();
                queue.deleteMessage(current);
                return advanceable;
            } catch (StorageException e) {
                LOGGER.error("Could not delete message {} ! Cause: {}.", current.getId(), e.getLocalizedMessage());
            }
        }
        // loop to wait for a message batch
        while (true) {
            try {
                LOGGER.warn("Checking for new messages");
                messages = queue.retrieveMessages(nbMsg).iterator();
                advanceable = messages.hasNext();
                if (advanceable) {
                    dataCount++;
                    current = messages.next();
                    queue.deleteMessage(current);
                    return advanceable;
                }
                Thread.sleep((long) loopWaitTime * 1000);
            } catch (InterruptedException | StorageException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        LOGGER.warn("getCurrent");
        return super.getCurrent();
    }

    @Override
    public Map<String, Object> getReturnValues() {
        LOGGER.warn("getReturnValues");
        return super.getReturnValues();
    }

}
