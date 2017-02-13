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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeueoutput.TAzureStorageQueueOutputProperties;

import com.microsoft.azure.storage.queue.CloudQueueMessage;

@Ignore
public class AzureStorageQueueOutputWriterTestIT extends AzureStorageBaseQueueTestIT {

    public AzureStorageQueueOutputWriterTestIT() {
        super("queue-output");
    }

    public Writer<?> createWriter(ComponentProperties properties) {
        AzureStorageQueueSink sink = new AzureStorageQueueSink();
        sink.initialize(null, properties);
        sink.validate(null);
        return sink.createWriteOperation().createWriter(null);
    }

    @Test
    public void testWriteSimpleMessage() throws Throwable {
        queue.clear();
        //
        TAzureStorageQueueOutputProperties properties = new TAzureStorageQueueOutputProperties("tests");
        properties = (TAzureStorageQueueOutputProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        properties.setupProperties();
        properties.queueName.setValue(TEST_QUEUE_NAME);
        Writer<?> writer = createWriter(properties);
        writer.open("test-uid");
        for (String m : messages) {
            IndexedRecord entity = new GenericData.Record(properties.schema.schema.getValue());
            entity.put(0, m + "SIMPLE");
            writer.write(entity);
        }
        writer.close();
        queue.downloadAttributes();
        assertEquals(3, queue.getApproximateMessageCount());
        for (CloudQueueMessage msg : queue.retrieveMessages(3)) {
            assertNotNull(msg.getMessageContentAsString());
            assertTrue(msg.getMessageContentAsString().indexOf("SIMPLE") > 0);
        }
    }

    @Test
    public void testWriteDelayedMessage() throws Throwable {
        queue.clear();
        //
        TAzureStorageQueueOutputProperties properties = new TAzureStorageQueueOutputProperties("tests");
        properties = (TAzureStorageQueueOutputProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        properties.setupProperties();
        properties.queueName.setValue(TEST_QUEUE_NAME);
        properties.initialVisibilityDelayInSeconds.setValue(5);
        Writer<?> writer = createWriter(properties);
        writer.open("test-uid");
        for (String m : messages) {
            IndexedRecord entity = new GenericData.Record(properties.schema.schema.getValue());
            entity.put(0, m + "DLY");
            writer.write(entity);
        }
        writer.close();
        int msgCount = 0;
        for (CloudQueueMessage msg : queue.retrieveMessages(30)) {
            // we shoud not be here ...
            msgCount++;
            assertNotNull(msg.getMessageContentAsString());
            assertTrue(msg.getMessageContentAsString().indexOf("DLY") > 0);
        }
        assertEquals(0, msgCount);
        Thread.sleep(5000);
        msgCount = 0;
        for (CloudQueueMessage msg : queue.retrieveMessages(30)) {
            msgCount++;
            assertNotNull(msg.getMessageContentAsString());
            assertTrue(msg.getMessageContentAsString().indexOf("DLY") > 0);
        }
        assertEquals(3, msgCount);
        queue.downloadAttributes();
        assertEquals(3, queue.getApproximateMessageCount());
    }

    @Test
    public void testWriteTTLMessage() throws Throwable {
        queue.clear();
        //
        TAzureStorageQueueOutputProperties properties = new TAzureStorageQueueOutputProperties("tests");
        properties = (TAzureStorageQueueOutputProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        properties.setupProperties();
        properties.queueName.setValue(TEST_QUEUE_NAME);
        properties.timeToLiveInSeconds.setValue(5);
        Writer<?> writer = createWriter(properties);
        writer.open("test-uid");
        for (String m : messages) {
            IndexedRecord entity = new GenericData.Record(properties.schema.schema.getValue());
            entity.put(0, m + "TTL");
            writer.write(entity);
        }
        writer.close();
        int msgCount = 0;
        for (CloudQueueMessage msg : queue.retrieveMessages(30)) {
            msgCount++;
            assertNotNull(msg.getMessageContentAsString());
            assertTrue(msg.getMessageContentAsString().indexOf("TTL") > 0);
        }
        assertEquals(3, msgCount);
        Thread.sleep(5000);
        msgCount = 0;
        for (CloudQueueMessage msg : queue.retrieveMessages(30)) {
            msgCount++;
            assertNotNull(msg.getMessageContentAsString());
        }
        assertEquals(0, msgCount);
    }

}
