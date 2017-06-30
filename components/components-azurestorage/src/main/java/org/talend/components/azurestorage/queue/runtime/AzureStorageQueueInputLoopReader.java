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
import org.talend.components.azurestorage.queue.tazurestoragequeueinputloop.TAzureStorageQueueInputLoopProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.microsoft.azure.storage.StorageException;

public class AzureStorageQueueInputLoopReader extends AzureStorageQueueInputReader {

    private int loopWaitTime;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueueInputLoopReader.class);

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageQueueInputLoopReader.class);

    public AzureStorageQueueInputLoopReader(RuntimeContainer container, BoundedSource source,
            TAzureStorageQueueInputLoopProperties properties) {

        super(container, source, properties);
        loopWaitTime = properties.loopWaitTime.getValue();

    }

    @Override
    public boolean start() throws IOException {
        // acquire queue and first messages
        startable = retrieveMessages(false); // no sleep
        while (!startable) {
            startable = retrieveMessages(true); // sleep and retry
        }

        return startable;
    }

    @Override
    public boolean advance() throws IOException {
        advanceable = messages.hasNext();
        if (messages.hasNext()) {
            current = messages.next();
            dataCount++;
            try {
                queueService.deleteMessage(queueName, current);
            } catch (InvalidKeyException | URISyntaxException | StorageException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
            return true;
        }

        while (!advanceable) {
            advanceable = retrieveMessages(true);
        }

        return advanceable;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        return super.getCurrent();
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return super.getReturnValues();
    }

    private boolean retrieveMessages(boolean sleepFirst) {

        try {

            if (sleepFirst) {
                Thread.sleep((long) loopWaitTime * 1000);
            }

            messages = queueService.retrieveMessages(queueName, nbMsg).iterator();
            if (messages.hasNext()) {
                current = messages.next();
                dataCount++;
                queueService.deleteMessage(queueName, current);
                return true;
            }

        } catch (InterruptedException | StorageException | InvalidKeyException | URISyntaxException e) {
            LOGGER.error(e.getLocalizedMessage());
        }

        return false;
    }

}
