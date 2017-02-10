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

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.blob.runtime.AzureStorageReader;
import org.talend.components.azurestorage.queue.AzureStorageQueueDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeuecreate.TAzureStorageQueueCreateProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;

public class AzureStorageQueueCreateReader extends AzureStorageReader<IndexedRecord> {

    private TAzureStorageQueueCreateProperties properties;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueueCreateReader.class);

    public AzureStorageQueueCreateReader(RuntimeContainer container, BoundedSource source, ComponentProperties properties2) {
        super(container, source);
        this.properties = (TAzureStorageQueueCreateProperties) properties2;
    }

    @Override
    public boolean start() throws IOException {
        Boolean startable = false;
        String queue = properties.queueName.getValue();
        try {
            CloudQueue cqueue = ((AzureStorageQueueSource) getCurrentSource()).getCloudQueue(runtime, queue);
            LOGGER.debug("Queue {} is about to be created.", cqueue.getName());
            try {
                startable = cqueue.createIfNotExists();
            } catch (StorageException e) {
                if (!e.getErrorCode().equals(StorageErrorCodeStrings.QUEUE_BEING_DELETED)) {
                    throw e;
                }
                LOGGER.error("Queue '{}' is currently being deleted. We'll retry in a few moments...", cqueue.getName());
                // Documentation doesn't specify how many seconds at least to wait.
                // 40 seconds before retrying.
                // See https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/delete-queue3
                try {
                    Thread.sleep(40000);
                } catch (InterruptedException eint) {
                    throw new IOException("Wait process for recreating table interrupted.");
                }
                startable = cqueue.createIfNotExists();
                LOGGER.info("Container {} created.", cqueue.getName());
            }
            if (startable) {
                dataCount++;
            } else {
                LOGGER.warn("Queue {} could not be created or already existed!", cqueue.getName());
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
        return false;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        Schema s = SchemaBuilder.record("createQueue").fields().name("queueName")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "63").prop(SchemaConstants.TALEND_IS_LOCKED, "true")
                .type(AvroUtils._string()).noDefault().endRecord();
        IndexedRecord record = new GenericData.Record(s);
        record.put(0, properties.queueName.getValue());
        return record;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Map<String, Object> r = super.getReturnValues();
        r.put(AzureStorageQueueDefinition.RETURN_QUEUE_NAME, properties.queueName.getValue());
        return r;
    }
}
