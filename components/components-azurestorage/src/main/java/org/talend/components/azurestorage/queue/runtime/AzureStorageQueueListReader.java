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

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.blob.runtime.AzureStorageReader;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListDefinition;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListProperties;

import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;

public class AzureStorageQueueListReader extends AzureStorageReader<IndexedRecord> {

    private TAzureStorageQueueListProperties properties;

    private Iterator<CloudQueue> queues;

    private CloudQueue current;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueueListReader.class);

    protected AzureStorageQueueListReader(RuntimeContainer container, BoundedSource source,
            TAzureStorageQueueListProperties properties) {
        super(container, source);
        this.properties = properties;
    }

    @Override
    public boolean start() throws IOException {
        Boolean startable = false;
        try {
            CloudQueueClient client = ((AzureStorageQueueSource) getCurrentSource()).getStorageQueueClient(runtime);
            queues = client.listQueues().iterator();
            startable = queues.hasNext();
            if (startable) {
                current = queues.next();
                dataCount++;
            }
        } catch (InvalidKeyException | URISyntaxException e) {
            LOGGER.error(e.getLocalizedMessage());
            if (properties.dieOnError.getValue())
                throw new ComponentException(e);
        }
        return startable;
    }

    @Override
    public boolean advance() throws IOException {
        boolean advanceable = queues.hasNext();
        if (advanceable) {
            current = queues.next();
            dataCount++;
        }
        return advanceable;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        IndexedRecord record = new GenericData.Record(properties.schema.schema.getValue());
        record.put(0, current.getName());
        return record;
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Result res = new Result();
        res.totalCount = dataCount;
        Map<String, Object> r = res.toMap();
        r.put(TAzureStorageQueueListDefinition.RETURN_NB_QUEUE, dataCount);
        return r;
    }

}
