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

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.queue.tazurestoragequeuelist.TAzureStorageQueueListProperties;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageQueueSourceOrSinkTest {

    AzureStorageQueueSourceOrSink sos;

    TAzureStorageQueueListProperties props;

    @Before
    public void setUp() throws Exception {
        sos = new AzureStorageQueueSourceOrSink();
        props = new TAzureStorageQueueListProperties("tests");
        props.setupProperties();
        sos.initialize(null, props);
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSourceOrSink#validate(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testValidate() {
        assertEquals(ValidationResult.Result.ERROR, sos.validate(null).getStatus());
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSourceOrSink#getEndpointSchema(org.talend.components.api.container.RuntimeContainer, java.lang.String)}.
     */
    @Test
    public final void testGetEndpointSchema() {
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSourceOrSink#getSchemaNames(org.talend.components.api.container.RuntimeContainer, org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties)}.
     */
    @Test
    public final void testGetSchemaNamesRuntimeContainerTAzureStorageConnectionProperties() {
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSourceOrSink#getSchemaNames(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testGetSchemaNamesRuntimeContainer() {
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSourceOrSink#getStorageQueueClient(org.talend.components.api.container.RuntimeContainer)}.
     */
    @Test
    public final void testGetStorageQueueClient() {
    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.queue.runtime.AzureStorageQueueSourceOrSink#getCloudQueue(org.talend.components.api.container.RuntimeContainer, java.lang.String)}.
     */
    @Test
    public final void testGetCloudQueue() {
    }

}
