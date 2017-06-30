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
package org.talend.components.azurestorage.blob.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

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
import org.talend.components.azurestorage.blob.AzureStorageBlobService;
import org.talend.components.azurestorage.blob.tazurestoragecontainerexist.TAzureStorageContainerExistProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;

public class AzureStorageContainerExistRuntimeTest {

    public static final String PROP_ = "PROP_";

    private RuntimeContainer runtimeContainer;

    private TAzureStorageContainerExistProperties properties;

    private AzureStorageContainerExistRuntime existContainer;

    @Mock
    private AzureStorageBlobService blobService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() {
        properties = new TAzureStorageContainerExistProperties(PROP_ + "ExistContainer");
        properties.setupProperties();
        // valid connection
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.protocol.setValue(Protocol.HTTP);
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");

        runtimeContainer = new RuntimeContainerMock();
        this.existContainer = new AzureStorageContainerExistRuntime();
    }

    @Test
    public void testInitializeEmptyContainerName() {
        ValidationResult validationResult = existContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
    }

    @Test
    public void testInitializeValide() {
        properties.container.setValue("container");
        ValidationResult validationResult = existContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
    }

    /**
     * The method {@link AzureStorageContainerCreateRuntime#runAtDriver(RuntimeContainer)} should not throw any exception if the
     * dieOnError is not set to true.
     */
    @Test
    public void testRunAtDriverHandleStorageException() {

        properties.container.setValue("container-name-ok");
        ValidationResult validationResult = existContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        existContainer.azureStorageBlobService = blobService;
        // Handle Storage exception
        try {
            when(blobService.containerExist(anyString()))
                    .thenThrow(new StorageException("errorCode", "storage exception message", new RuntimeException()));
            existContainer.runAtDriver(runtimeContainer);

        } catch (InvalidKeyException | StorageException | URISyntaxException e) {
            fail("should not throw exception " + e.getMessage());
        }
    }

    /**
     * The method {@link AzureStorageContainerCreateRuntime#runAtDriver(RuntimeContainer)} should not throw any exception if the
     * dieOnError is not set to true.
     */
    @Test
    public void testRunAtDriverHandleInvalidKeyException() {

        properties.container.setValue("container-name-ok");
        ValidationResult validationResult = existContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        existContainer.azureStorageBlobService = blobService;
        try {

            when(blobService.containerExist(anyString())).thenThrow(new InvalidKeyException());
            existContainer.runAtDriver(runtimeContainer);
        } catch (InvalidKeyException | StorageException | URISyntaxException e) {
            fail("should not throw exception " + e.getMessage());
        }
    }

    /**
     * The method {@link AzureStorageContainerCreateRuntime#runAtDriver(RuntimeContainer)} should not throw any exception if the
     * dieOnError is not set to true.
     */
    @Test
    public void testRunAtDriverHandleURISyntaxException() {

        properties.container.setValue("container-name-ok");
        ValidationResult validationResult = existContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        existContainer.azureStorageBlobService = blobService;
        try {
            when(blobService.containerExist(anyString()))
                    .thenThrow(new URISyntaxException("bad url", "some reason"));
            existContainer.runAtDriver(runtimeContainer);

        } catch (InvalidKeyException | StorageException | URISyntaxException e) {
            fail("should not throw exception " + e.getMessage());
        }
    }

    @Test(expected = ComponentException.class)
    public void testRunAtDriverDieOnError() {

        properties.container.setValue("container-name-ok");
        properties.dieOnError.setValue(true);
        ValidationResult validationResult = existContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        existContainer.azureStorageBlobService = blobService;

        try {
            when(blobService.containerExist(anyString()))
                    .thenThrow(new StorageException("errorCode", "storage exception message", new RuntimeException()));
            existContainer.runAtDriver(runtimeContainer);
        } catch (InvalidKeyException | StorageException | URISyntaxException e) {
            fail("should not throw exception " + e.getMessage());
        }

    }

    @Test
    public void testRunAtDriverContainerDontExist() {

        properties.container.setValue("container-name-ok");
        ValidationResult validationResult = existContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        existContainer.azureStorageBlobService = blobService;
        try {
            when(blobService.containerExist(anyString())).thenReturn(false);
            existContainer.runAtDriver(runtimeContainer);
        } catch (InvalidKeyException | StorageException | URISyntaxException e) {
            fail("should not throw exception " + e.getMessage());
        }
    }

    @Test
    public void testrunAtDriverValid() {
        properties.container.setValue("container");
        ValidationResult validationResult = existContainer.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        existContainer.azureStorageBlobService = blobService;
        try {
            when(blobService.containerExist(anyString())).thenReturn(true);
            existContainer.runAtDriver(runtimeContainer);
        } catch (InvalidKeyException | StorageException | URISyntaxException e) {
            fail("should not throw exception " + e.getMessage());
        }

    }
}
