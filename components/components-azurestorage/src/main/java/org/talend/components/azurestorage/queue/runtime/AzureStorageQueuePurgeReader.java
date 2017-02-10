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
import org.talend.components.azurestorage.blob.runtime.AzureStorageReader;
import org.talend.components.azurestorage.queue.AzureStorageQueueDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeuepurge.TAzureStorageQueuePurgeProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;

public class AzureStorageQueuePurgeReader extends AzureStorageReader<IndexedRecord> {

    private TAzureStorageQueuePurgeProperties properties;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueuePurgeReader.class);

    protected AzureStorageQueuePurgeReader(RuntimeContainer container, BoundedSource source,
            TAzureStorageQueuePurgeProperties properties) {
        super(container, source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        Boolean startable = false;
        String queueName = properties.queueName.getValue();
        try {
            CloudQueue queue = ((AzureStorageQueueSourceOrSink) getCurrentSource()).getCloudQueue(runtime, queueName);
            dataCount = (int) queue.getApproximateMessageCount();
            LOGGER.warn("About to purge {} messages in {}", dataCount, queue.getName());
            queue.clear();
            startable = true;
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
