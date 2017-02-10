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

import org.junit.BeforeClass;
import org.talend.components.azurestorage.queue.tazurestoragequeueinputloop.TAzureStorageQueueInputLoopProperties;

public class AzureStorageQueueInputLoopReaderTestIT extends AzureStorageBaseQueueTestIT {

    static AzureStorageQueueInputLoopReader reader;

    static TAzureStorageQueueInputLoopProperties properties;

    static AzureStorageQueueSource source;

    @BeforeClass
    public static void setupBeforeClass() throws Exception {
        properties = new TAzureStorageQueueInputLoopProperties("test");
        properties.setupProperties();
        source = new AzureStorageQueueSource();
        source.initialize(null, properties);
        reader = (AzureStorageQueueInputLoopReader) source.createReader(null);
    }

    public AzureStorageQueueInputLoopReaderTestIT() {
        super("test-queue-input-loop");
    }

}
