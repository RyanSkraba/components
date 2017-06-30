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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.queue.AzureStorageQueueService;
import org.talend.components.azurestorage.queue.tazurestoragequeuedelete.TAzureStorageQueueDeleteProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;

public class AzureStorageQueueDeleteRuntimeTest {

    public static final String PROP_ = "PROP_";

    private RuntimeContainer runtimeContainer;

    private TAzureStorageQueueDeleteProperties properties;

    private AzureStorageQueueDeleteRuntime queueDelete;

    @Mock
    private AzureStorageQueueService queueService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() throws IOException {
        properties = new TAzureStorageQueueDeleteProperties(PROP_ + "Delete");
        properties.setupProperties();
        // valid connection
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.protocol.setValue(Protocol.HTTP);
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");

        runtimeContainer = new RuntimeContainerMock();
        this.queueDelete = new AzureStorageQueueDeleteRuntime();
    }

    @Test
    public void testInitializeInvalidQueueName() {
        properties.queueName.setValue("");
        ValidationResult validationResult = queueDelete.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
    }

    @Test
    public void testRunAtDriverQueueDeletionSuccess() {
        properties.queueName.setValue("a-good-queue-name");
        queueDelete.initialize(runtimeContainer, properties);
        queueDelete.queueService = queueService;
        try {
            when(queueService.deleteQueueIfExists(anyString())).thenReturn(true);
            queueDelete.runAtDriver(runtimeContainer);
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testRunAtDriverQueueAlReadyExist() {
        properties.queueName.setValue("a-good-queue-name");
        queueDelete.initialize(runtimeContainer, properties);
        queueDelete.queueService = queueService;
        try {
            when(queueService.deleteQueueIfExists(anyString())).thenReturn(false);
            queueDelete.runAtDriver(runtimeContainer);
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testRunAtDriverHandleStorageException() {
        properties.queueName.setValue("a-good-queue-name");
        properties.dieOnError.setValue(false);
        queueDelete.initialize(runtimeContainer, properties);
        queueDelete.queueService = queueService;
        try {
            when(queueService.deleteQueueIfExists(anyString()))
                    .thenThrow(new StorageException("errorCode", "some storage message", new RuntimeException()));
            queueDelete.runAtDriver(runtimeContainer);
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testRunAtDriverHandleInvalidKeyException() {
        properties.queueName.setValue("a-good-queue-name");
        properties.dieOnError.setValue(false);
        queueDelete.initialize(runtimeContainer, properties);
        queueDelete.queueService = queueService;
        try {
            when(queueService.deleteQueueIfExists(anyString())).thenThrow(new InvalidKeyException());
            queueDelete.runAtDriver(runtimeContainer);
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testRunAtDriverHandleURISyntaxException() {
        properties.queueName.setValue("a-good-queue-name");
        properties.dieOnError.setValue(false);
        queueDelete.initialize(runtimeContainer, properties);
        queueDelete.queueService = queueService;
        try {
            when(queueService.deleteQueueIfExists(anyString())).thenThrow(new URISyntaxException("bad uri", "some reason"));
            queueDelete.runAtDriver(runtimeContainer);
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test(expected = ComponentException.class)
    public void testRunAtDriverDieOnError() {
        properties.queueName.setValue("a-good-queue-name");
        properties.dieOnError.setValue(true);
        queueDelete.initialize(runtimeContainer, properties);
        queueDelete.queueService = queueService;
        try {
            when(queueService.deleteQueueIfExists(anyString())).thenThrow(new InvalidKeyException());
            queueDelete.runAtDriver(runtimeContainer);
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

}
