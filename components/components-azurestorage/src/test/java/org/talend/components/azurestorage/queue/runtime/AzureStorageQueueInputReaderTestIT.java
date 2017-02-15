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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputProperties;

import com.microsoft.azure.storage.queue.CloudQueueMessage;

@Ignore
public class AzureStorageQueueInputReaderTestIT extends AzureStorageBaseQueueTestIT {

    private TAzureStorageQueueInputProperties inputProps;

    public AzureStorageQueueInputReaderTestIT() {
        super("queue-input");
        try {
            //
            queue.clear();
            for (int idx = 0; idx < 50; idx++) {
                queue.addMessage(new CloudQueueMessage("messageContent#" + idx));
            }
            //
            inputProps = new TAzureStorageQueueInputProperties("tests");
            inputProps = (TAzureStorageQueueInputProperties) setupConnectionProperties(
                    (AzureStorageProvideConnectionProperties) inputProps);
            inputProps.setupProperties();
            inputProps.queueName.setValue(TEST_QUEUE_NAME);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testReadOneMessage() throws Throwable {
        inputProps.numberOfMessages.setValue(1);
        BoundedReader reader = createBoundedReader(inputProps);
        assertTrue(reader.start());
        assertFalse(reader.advance());
        reader.close();
    }
}
