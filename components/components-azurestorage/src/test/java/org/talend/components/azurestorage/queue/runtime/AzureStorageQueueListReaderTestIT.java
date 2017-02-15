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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.azurestorage.AzureStorageDefinition;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListProperties;

@Ignore
public class AzureStorageQueueListReaderTestIT extends AzureStorageBaseQueueTestIT {

    public AzureStorageQueueListReaderTestIT() {
        super("test-queue-list");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testListQueues() throws Throwable {
        TAzureStorageQueueListProperties properties = new TAzureStorageQueueListProperties("tests");
        properties.setupProperties();
        properties = (TAzureStorageQueueListProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        BoundedReader reader = createBoundedReader(properties);
        assertTrue(reader.start());
        do {
            IndexedRecord current = (IndexedRecord) reader.getCurrent();
            assertNotNull(current);
            assertTrue(current.get(0) instanceof String);
        } while (reader.advance());
        assertTrue((int) reader.getReturnValues().get(AzureStorageDefinition.RETURN_TOTAL_RECORD_COUNT) > 0);
        reader.close();
    }
}
