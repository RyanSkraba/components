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
package org.talend.components.azurestorage.queue.runtime.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSink;
import org.talend.components.azurestorage.queue.tazurestoragequeueoutput.TAzureStorageQueueOutputProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeuepurge.TAzureStorageQueuePurgeProperties;

@Ignore
public class AzureStorageQueuePurgeReaderTestIT extends AzureStorageBaseQueueTestIT {

    public AzureStorageQueuePurgeReaderTestIT() {
        super("queue-purge");
    }

    @Before
    public void fillInQueue() throws Throwable {
        TAzureStorageQueueOutputProperties properties = new TAzureStorageQueueOutputProperties("tests");
        properties = (TAzureStorageQueueOutputProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        properties.setupProperties();
        properties.queueName.setValue(TEST_QUEUE_NAME);
        AzureStorageQueueSink sink = new AzureStorageQueueSink();
        sink.initialize(null, properties);
        sink.validate(null);
        Writer<?> writer = sink.createWriteOperation().createWriter(null);
        writer.open("test-uid");
        for (String m : messages) {
            IndexedRecord entity = new GenericData.Record(properties.schema.schema.getValue());
            entity.put(0, m + "SIMPLE");
            writer.write(entity);
        }
        writer.close();
        queue.downloadAttributes();
        assertTrue(queue.getApproximateMessageCount() > 3);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testPurge() throws Throwable {
        TAzureStorageQueuePurgeProperties properties = new TAzureStorageQueuePurgeProperties("tests");
        properties.setupProperties();
        properties = (TAzureStorageQueuePurgeProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        properties.queueName.setValue(TEST_QUEUE_NAME);
        BoundedReader reader = createBoundedReader(properties);
        assertTrue(reader.start());
        assertFalse(reader.advance());
        reader.close();
        queue.downloadAttributes();
        assertEquals(0, queue.getApproximateMessageCount());

    }
}
