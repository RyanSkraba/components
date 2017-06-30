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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
import org.talend.components.azurestorage.blob.tazurestoragedelete.TAzureStorageDeleteProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class AzureStorageDeleteRuntimeTest {

    public static final String PROP_ = "PROP_";

    private RuntimeContainer runtimeContainer;

    private TAzureStorageDeleteProperties properties;

    private AzureStorageDeleteRuntime deleteBlock;

    @Mock
    private AzureStorageBlobService blobService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() {
        properties = new TAzureStorageDeleteProperties(PROP_ + "DeleteBlock");
        properties.setupProperties();
        // valid connection
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.protocol.setValue(Protocol.HTTP);
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");

        properties.container.setValue("valide-container-name-1");

        runtimeContainer = new RuntimeContainerMock();
        this.deleteBlock = new AzureStorageDeleteRuntime();
    }

    @Test
    public void testInitializeWithEmptyPrefix() {
        ValidationResult validationResult = deleteBlock.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
    }

    @Test
    public void testInitializeValid() {
        properties.remoteBlobs.include.setValue(Arrays.asList(true, false));
        properties.remoteBlobs.prefix.setValue(Arrays.asList("block1", "block2"));
        ValidationResult validationResult = deleteBlock.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
    }

    @Test
    public void testRunAtDriverNotSuccessfullyDeleted() {
        properties.remoteBlobs.include.setValue(Arrays.asList(true));
        properties.remoteBlobs.prefix.setValue(Arrays.asList("block1"));
        ValidationResult validationResult = deleteBlock.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        deleteBlock.azureStorageBlobService = blobService;

        try {
            final List<CloudBlockBlob> list = new ArrayList<>();
            list.add(new CloudBlockBlob(new URI("https://storagesample.blob.core.windows.net/mycontainer/blob1.txt")));

            when(blobService.listBlobs(anyString(), anyString(), anyBoolean())).thenReturn(new Iterable<ListBlobItem>() {

                @Override
                public Iterator<ListBlobItem> iterator() {
                    return new DummyListBlobItemIterator(list);
                }
            });

            when(blobService.deleteBlobBlockIfExist(any(CloudBlockBlob.class))).thenReturn(false);
            deleteBlock.runAtDriver(runtimeContainer);

        } catch (StorageException | URISyntaxException | InvalidKeyException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testRunAtDriverValid() {
        properties.remoteBlobs.include.setValue(Arrays.asList(true));
        properties.remoteBlobs.prefix.setValue(Arrays.asList("block1"));
        ValidationResult validationResult = deleteBlock.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        deleteBlock.azureStorageBlobService = blobService;
        try {
            final List<CloudBlockBlob> list = new ArrayList<>();
            list.add(new CloudBlockBlob(new URI("https://storagesample.blob.core.windows.net/mycontainer/blob1.txt")));

            when(blobService.listBlobs(anyString(), anyString(), anyBoolean())).thenReturn(new Iterable<ListBlobItem>() {

                @Override
                public Iterator<ListBlobItem> iterator() {
                    return new DummyListBlobItemIterator(list);
                }
            });

            when(blobService.deleteBlobBlockIfExist(any(CloudBlockBlob.class))).thenReturn(true);

            deleteBlock.runAtDriver(runtimeContainer);
        } catch (StorageException | URISyntaxException | InvalidKeyException e) {
            fail("should not throw " + e.getMessage());
        }

    }

    @Test
    public void testRunAtDriverHandleError() {
        properties.remoteBlobs.include.setValue(Arrays.asList(true));
        properties.remoteBlobs.prefix.setValue(Arrays.asList("block1"));
        ValidationResult validationResult = deleteBlock.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        deleteBlock.azureStorageBlobService = blobService;

        try {
            when(blobService.listBlobs(anyString(), anyString(), anyBoolean()))
                    .thenThrow(new StorageException("some error code", "dummy message", new RuntimeException()));
            deleteBlock.runAtDriver(runtimeContainer);

        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test(expected = ComponentException.class)
    public void testRunAtDriverDieOnError() {
        properties.remoteBlobs.include.setValue(Arrays.asList(true));
        properties.remoteBlobs.prefix.setValue(Arrays.asList("block1"));
        properties.dieOnError.setValue(true);
        ValidationResult validationResult = deleteBlock.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        deleteBlock.azureStorageBlobService = blobService;
        // prepare test data and mocks
        try {
            when(blobService.listBlobs(anyString(), anyString(), anyBoolean()))
                    .thenThrow(new StorageException("some error code", "dummy message", new RuntimeException()));
            deleteBlock.runAtDriver(runtimeContainer);
        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            fail("should not throw " + e.getMessage());
        }
    }

}
