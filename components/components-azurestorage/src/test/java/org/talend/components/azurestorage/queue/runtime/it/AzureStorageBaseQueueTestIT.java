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

import org.junit.BeforeClass;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.azurestorage.AzureStorageBaseTestIT;
import org.talend.components.azurestorage.AzureStorageProvideConnectionProperties;
import org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSource;
import org.talend.components.azurestorage.queue.tazurestoragequeuecreate.TAzureStorageQueueCreateProperties;

import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;

public class AzureStorageBaseQueueTestIT extends AzureStorageBaseTestIT {

    protected static final String TEST_QUEUE_NAME = "test-queue";

    protected static final String TEST_QUEUE_NAME_CREATE = "test-queue-create";

    protected static CloudQueue queue;

    protected static CloudQueueClient queueClient;

    protected String[] messages = { "A message to you rudy", "Message in a bottle", "Alert Message" };

    public AzureStorageBaseQueueTestIT(String testName) {
        super(testName);
    }

    @BeforeClass
    public static void createTestQueue() throws Throwable {
        TAzureStorageQueueCreateProperties properties = new TAzureStorageQueueCreateProperties("tests");
        properties = (TAzureStorageQueueCreateProperties) setupConnectionProperties(
                (AzureStorageProvideConnectionProperties) properties);
        properties.setupProperties();
        properties.queueName.setValue(TEST_QUEUE_NAME);
        AzureStorageQueueSource source = new AzureStorageQueueSource();
        source.initialize(null, properties);
        queueClient = source.getStorageQueueClient(null);
        queue = source.getCloudQueue(null, TEST_QUEUE_NAME);
        queue.createIfNotExists();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> BoundedReader<T> createBoundedReader(ComponentProperties props) {
        AzureStorageQueueSource source = new AzureStorageQueueSource();
        source.initialize(null, props);
        source.validate(null);
        return source.createReader(null);
    }

}
