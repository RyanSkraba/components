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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.queue.tazurestoragequeuecreate.TAzureStorageQueueCreateProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeuedelete.TAzureStorageQueueDeleteProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeueinputloop.TAzureStorageQueueInputLoopProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeueoutput.TAzureStorageQueueOutputProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeuepurge.TAzureStorageQueuePurgeProperties;

public class AzureStorageQueueSourceTest {

    AzureStorageQueueSource source;

    @Before
    public void setUp() throws Exception {
        source = new AzureStorageQueueSource();
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSource#createReader(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testCreateReader() {

        // if (properties instanceof TAzureStorageQueuePurgeProperties) {
        // return new AzureStorageQueuePurgeReader(container, this, (TAzureStorageQueuePurgeProperties) properties);
        // if (properties instanceof TAzureStorageQueueInputProperties) {
        // return new AzureStorageQueueInputReader(container, this, (TAzureStorageQueueInputProperties) properties);
        // return null;
        source.initialize(null, new TAzureStorageQueueCreateProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageQueueCreateReader);

        source.initialize(null, new TAzureStorageQueueDeleteProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageQueueDeleteReader);

        source.initialize(null, new TAzureStorageQueueListProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageQueueListReader);

        source.initialize(null, new TAzureStorageQueuePurgeProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageQueuePurgeReader);

        source.initialize(null, new TAzureStorageQueueInputProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageQueueInputReader);

        source.initialize(null, new TAzureStorageQueueInputLoopProperties("test"));
        assertTrue(source.createReader(null) instanceof AzureStorageQueueInputLoopReader);

        source.initialize(null, new TAzureStorageQueueOutputProperties("test"));
        assertNull(source.createReader(null));

    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSource#splitIntoBundles(long, org.talend.components.api.container.RuntimeContainer)}.
     *
     * @throws Exception
     */
    @Test
    public final void testSplitIntoBundles() throws Exception {
        assertNull(source.splitIntoBundles(0, null));
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSource#getEstimatedSizeBytes(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testGetEstimatedSizeBytes() {
        assertEquals(0, source.getEstimatedSizeBytes(null));
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSource#producesSortedKeys(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testProducesSortedKeys() {
        assertFalse(source.producesSortedKeys(null));
    }

}
