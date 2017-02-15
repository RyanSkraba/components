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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.azurestorage.AzureStorageDefinition;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeuecreate.TAzureStorageQueueCreateProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeuedelete.TAzureStorageQueueDeleteProperties;

@Ignore
public class AzureStorageQueueDeleteReaderTestIT extends AzureStorageBaseQueueTestIT {

    public AzureStorageQueueDeleteReaderTestIT() {
        super("test-queue-create");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testDeleteQueue() throws Throwable {
        //
        TAzureStorageQueueCreateProperties props = new TAzureStorageQueueCreateProperties("tests");
        props.setupProperties();
        props = (TAzureStorageQueueCreateProperties) setupConnectionProperties((AzureStorageProvideConnectionProperties) props);
        props.queueName.setValue(TEST_QUEUE_NAME_CREATE);
        BoundedReader reader = createBoundedReader(props);
        reader.start();
        reader.close();
        //
        TAzureStorageQueueDeleteProperties properties = new TAzureStorageQueueDeleteProperties("tests");
        properties.setupProperties();
        properties = (TAzureStorageQueueDeleteProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        properties.queueName.setValue(TEST_QUEUE_NAME_CREATE);
        reader = createBoundedReader(properties);
        assertTrue(reader.start());
        assertNotNull(reader.getCurrent());
        assertTrue(((IndexedRecord) reader.getCurrent()).get(0) instanceof String);
        assertEquals(((IndexedRecord) reader.getCurrent()).get(0), TEST_NAME);
        assertTrue((int) reader.getReturnValues().get(AzureStorageDefinition.RETURN_TOTAL_RECORD_COUNT) == 1);
        reader.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testDeleteNonExistingQueue() throws Throwable {
        TAzureStorageQueueDeleteProperties properties = new TAzureStorageQueueDeleteProperties("tests");
        properties.setupProperties();
        properties = (TAzureStorageQueueDeleteProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        properties.queueName.setValue(TEST_QUEUE_NAME_CREATE + "toto");
        BoundedReader reader = createBoundedReader(properties);
        assertFalse(reader.start());
        reader.close();
    }

}
