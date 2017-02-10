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
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.queue.tazurestoragequeueinputloop.TAzureStorageQueueInputLoopProperties;

public class AzureStorageQueueInputLoopReaderTest {

    AzureStorageQueueInputLoopReader reader;

    @Before
    public void setUpLoop() throws Exception {
        AzureStorageQueueSource sos = new AzureStorageQueueSource();
        TAzureStorageQueueInputLoopProperties properties = new TAzureStorageQueueInputLoopProperties("tests");
        properties.setupProperties();
        properties.queueName.setValue("test");
        sos.initialize(null, properties);
        reader = (AzureStorageQueueInputLoopReader) sos.createReader(null);
    }

    @Test
    public final void testGetReturnValues() {
        assertEquals("test", reader.getReturnValues().get("queueName"));
    }

    @Test(expected = NullPointerException.class)
    public final void testStart() throws IOException {
        reader.start();
        fail("Should have failed...");
    }

    @Test(expected = NullPointerException.class)
    public final void testAdvance() throws IOException {
        reader.advance();
        fail("Should have failed...");
    }

    @Test(expected = NullPointerException.class)
    public final void testGetCurrent() {
        reader.getCurrent();
        fail("Should have failed...");
    }
}
