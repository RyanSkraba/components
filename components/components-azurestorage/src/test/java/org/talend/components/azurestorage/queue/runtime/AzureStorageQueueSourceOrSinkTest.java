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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.queue.tazurestoragequeueinput.TAzureStorageQueueInputProperties;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListProperties;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageQueueSourceOrSinkTest {

    AzureStorageQueueSourceOrSink sos;

    TAzureStorageQueueListProperties props;

    TAzureStorageQueueInputProperties queueInputProperties;

    private RuntimeContainer runtimeContainer;

    @Before
    public void setUp() throws Exception {
        sos = new AzureStorageQueueSourceOrSink();
        props = new TAzureStorageQueueListProperties("tests");
        props.setupProperties();

        queueInputProperties = new TAzureStorageQueueInputProperties("test");
        queueInputProperties.setupProperties();
        queueInputProperties.connection.accountName.setValue("fakeAccountName");
        queueInputProperties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");

        runtimeContainer = new RuntimeContainerMock();
    }

    @After
    public void dispose() {
        queueInputProperties = null;
        runtimeContainer = null;
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSourceOrSink#validate(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testValidate() {
        assertEquals(ValidationResult.Result.ERROR, sos.initialize(null, props).getStatus());

        queueInputProperties.queueName.setValue("2queue-name-with-numeric8");
        assertEquals(ValidationResult.Result.OK, sos.initialize(runtimeContainer, queueInputProperties).getStatus());
    }

}
