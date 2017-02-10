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
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.queue.tazurestoragequeuecreate.TAzureStorageQueueCreateProperties;

public class AzureStorageQueueCreateReaderTest {

    AzureStorageQueueCreateReader reader;

    @Before
    public void setUp() throws Exception {
        AzureStorageQueueSource sos = new AzureStorageQueueSource();
        TAzureStorageQueueCreateProperties properties = new TAzureStorageQueueCreateProperties("tests");
        properties.setupProperties();
        properties.queueName.setValue("test");
        sos.initialize(null, properties);
        reader = (AzureStorageQueueCreateReader) sos.createReader(null);
    }

    @Test
    public final void testGetReturnValues() {
        // assertEquals(0, reader.getReturnValues().get("numberOfQueues"));
        assertEquals("test", reader.getReturnValues().get("queueName"));
    }

    @Test(expected = NullPointerException.class)
    public final void testStart() throws IOException {
        reader.start();
        fail("Should have failed...");
    }

    @Test
    public final void testAdvance() throws IOException {
        assertFalse(reader.advance());
    }

    @Test
    public final void testGetCurrent() {
        assertEquals("test", reader.getCurrent().get(0));
    }
}
