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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.azurestorage.AzureStorageDefinition;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeuecreate.TAzureStorageQueueCreateProperties;

import com.microsoft.azure.storage.queue.CloudQueue;

@Ignore
public class AzureStorageQueueCreateReaderTestIT extends AzureStorageBaseQueueTestIT {

    public AzureStorageQueueCreateReaderTestIT() {
        super("test-queue-create");
    }

    @AfterClass
    public static void removeQueues() throws Throwable {
        for (CloudQueue q : queueClient.listQueues(TEST_QUEUE_NAME_CREATE)) {
            q.delete();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testCreateNewQueue() throws Throwable {
        TAzureStorageQueueCreateProperties properties = new TAzureStorageQueueCreateProperties("tests");
        properties.setupProperties();
        properties = (TAzureStorageQueueCreateProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        String fqueue = TEST_QUEUE_NAME_CREATE + getRandomTestUID() + "a";
        properties.queueName.setValue(fqueue);
        BoundedReader reader = createBoundedReader(properties);
        assertTrue(reader.start());
        assertNotNull(reader.getCurrent());
        assertTrue(((IndexedRecord) reader.getCurrent()).get(0) instanceof String);
        assertEquals(((IndexedRecord) reader.getCurrent()).get(0), fqueue);
        assertTrue((int) reader.getReturnValues().get(AzureStorageDefinition.RETURN_TOTAL_RECORD_COUNT) > 0);
        reader.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testCreateExistingQueue() throws Throwable {
        TAzureStorageQueueCreateProperties properties = new TAzureStorageQueueCreateProperties("tests");
        properties.setupProperties();
        properties = (TAzureStorageQueueCreateProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        String fqueue = TEST_QUEUE_NAME_CREATE + getRandomTestUID() + "b";
        properties.queueName.setValue(fqueue);
        BoundedReader reader = createBoundedReader(properties);
        assertTrue(reader.start());
        assertTrue((int) reader.getReturnValues().get(AzureStorageDefinition.RETURN_TOTAL_RECORD_COUNT) > 0);
        reader.close();
        //
        assertFalse(reader.start());
        assertTrue((int) reader.getReturnValues().get(AzureStorageDefinition.RETURN_TOTAL_RECORD_COUNT) > 0);
        reader.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testDeleteAndRecreateQueue() throws Exception {
        TAzureStorageQueueCreateProperties properties = new TAzureStorageQueueCreateProperties("tests");
        properties.setupProperties();
        properties = (TAzureStorageQueueCreateProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        String fqueue = TEST_QUEUE_NAME_CREATE + "deleteandrecreate";
        properties.queueName.setValue(fqueue);
        BoundedReader reader = createBoundedReader(properties);
        assertTrue(reader.start());
        reader.close();
        //
        queueClient.getQueueReference(fqueue).delete();
        //
        assertTrue(reader.start());
        reader.close();
    }

}
