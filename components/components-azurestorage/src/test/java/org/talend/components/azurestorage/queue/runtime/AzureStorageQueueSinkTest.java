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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.azurestorage.queue.tazurestoragequeueoutput.TAzureStorageQueueOutputProperties;

public class AzureStorageQueueSinkTest {

    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSink#createWriteOperation()}.
     */
    @Test
    public final void testCreateWriteOperation() {
        AzureStorageQueueSink sink = new AzureStorageQueueSink();
        TAzureStorageQueueOutputProperties p = new TAzureStorageQueueOutputProperties("test");
        p.connection.setupProperties();
        p.setupProperties();
        sink.initialize(null, p);
        assertNotNull(sink.createWriteOperation());
        assertNotNull(sink.createWriteOperation().createWriter(null));
        assertEquals(sink, sink.createWriteOperation().getSink());

        List<Result> writerResults = new ArrayList<Result>();
        Result r = new Result("test");
        writerResults.add(r);
        AzureStorageQueueWriteOperation wo = (AzureStorageQueueWriteOperation) sink.createWriteOperation();
        wo.initialize(null);
        assertNotNull(wo.finalize(writerResults, null));
    }

}
