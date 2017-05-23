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
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.queue.tazurestoragequeueoutput.TAzureStorageQueueOutputProperties;
import org.talend.daikon.avro.AvroUtils;

public class AzureStorageQueueWriterTest {

    AzureStorageQueueWriter writer;

    @Before
    public void setUp() throws Exception {
        AzureStorageQueueSink sink = new AzureStorageQueueSink();
        TAzureStorageQueueOutputProperties p = new TAzureStorageQueueOutputProperties("test");
        p.connection.setupProperties();
        p.setupProperties();
        sink.initialize(null, p);
        writer = (AzureStorageQueueWriter) sink.createWriteOperation().createWriter(null);
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueWriter#open(java.lang.String)}.
     *
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testOpen() throws IOException {
        writer.open("test");
        fail("Should have failed...");
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueWriter#write(java.lang.Object)}.
     *
     * @throws IOException
     */
    @Test(expected = NullPointerException.class)
    public final void testWrite() throws IOException {
        try {
            writer.open("testWrite");
        } catch (Exception e) {
        }
        IndexedRecord record = new GenericData.Record(SchemaBuilder.builder().record("Main").fields().name("message")
                .type(AvroUtils._string()).noDefault().endRecord());
        record.put(0, "a message");
        writer.write(record);
        writer.close();
        fail("Should have failed...");
    }

    @Test
    public final void testWriteNull() throws IOException {
        writer.write(null);
    }

    /**
     * Test method for {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueWriter#close()}.
     *
     * @throws IOException
     */
    @Test
    public final void testClose() throws IOException {
        try {
            writer.open("testOpen");
        } catch (Exception e) {
        }
        assertEquals("testOpen", writer.close().getuId());
        assertEquals(0, writer.close().getSuccessCount());
        assertEquals(0, writer.close().getRejectCount());
        assertEquals(0, writer.close().getTotalCount());
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueWriter#getWriteOperation()}.
     */
    @Test
    public final void testGetWriteOperation() {
        assertNotNull(writer.getWriteOperation());
    }
}
