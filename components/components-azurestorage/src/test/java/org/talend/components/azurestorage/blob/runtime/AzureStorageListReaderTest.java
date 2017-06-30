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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.blob.AzureStorageBlobService;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobsTable;
import org.talend.components.azurestorage.blob.tazurestoragelist.TAzureStorageListProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class AzureStorageListReaderTest {

    private AzureStorageListReader reader;

    private TAzureStorageListProperties properties;

    public static final String PROP_ = "PROP_";

    private RuntimeContainer runtimeContainer;

    @Mock
    private AzureStorageBlobService blobService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {

        runtimeContainer = new RuntimeContainerMock();

        AzureStorageSource source = new AzureStorageSource();
        properties = new TAzureStorageListProperties(PROP_ + "List");
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.protocol.setValue(Protocol.HTTP);
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");
        properties.setupProperties();

        source.initialize(runtimeContainer, properties);
        reader = (AzureStorageListReader) source.createReader(runtimeContainer);
        reader.azureStorageBlobService = blobService;
    }

    @Test
    public void testStartAsNonStartable() {
        try {

            when(blobService.listBlobs(anyString(), anyString(), anyBoolean())).thenReturn(new Iterable<ListBlobItem>() {

                @Override
                public Iterator<ListBlobItem> iterator() {
                    return new DummyListBlobItemIterator(new ArrayList<CloudBlockBlob>());
                }
            });

            properties.remoteBlobs = new RemoteBlobsTable("RemoteBlobsTable");
            properties.remoteBlobs.include.setValue(Arrays.asList(true));
            properties.remoteBlobs.prefix.setValue(Arrays.asList("dummyFilter"));

            boolean startable = reader.start();
            assertFalse(startable);
            assertFalse(reader.advance());

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test
    public void testStartAsStartabke() {

        try {
            final List<CloudBlockBlob> list = new ArrayList<>();
            list.add(new CloudBlockBlob(new URI("https://storagesample.blob.core.windows.net/mycontainer/blob1.txt")));
            list.add(new CloudBlockBlob(new URI("https://storagesample.blob.core.windows.net/mycontainer/blob2.txt")));
            list.add(new CloudBlockBlob(new URI("https://storagesample.blob.core.windows.net/mycontainer/blob3.txt")));
            when(blobService.listBlobs(anyString(), anyString(), anyBoolean())).thenReturn(new Iterable<ListBlobItem>() {

                @Override
                public Iterator<ListBlobItem> iterator() {
                    return new DummyListBlobItemIterator(list);
                }
            });

            properties.remoteBlobs = new RemoteBlobsTable("RemoteBlobsTable");
            properties.remoteBlobs.include.setValue(Arrays.asList(true));
            properties.remoteBlobs.prefix.setValue(Arrays.asList("someFilter"));

            boolean startable = reader.start();
            assertTrue(startable);
            assertNotNull(reader.getCurrent());
            while (reader.advance()) {
                assertNotNull(reader.getCurrent());
            }
            assertNotNull(reader.getReturnValues());
            assertEquals(3, reader.getReturnValues().get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void getCurrentOnNonStartableReader() {
        reader.getCurrent();
        fail("should throw NoSuchElementException");
    }

    @Test(expected = NoSuchElementException.class)
    public void getCurrentOnNonAdvancableReader() {
        try {
            final List<CloudBlockBlob> list = new ArrayList<>();
            list.add(new CloudBlockBlob(new URI("https://storagesample.blob.core.windows.net/mycontainer/blob1.txt")));

            when(blobService.listBlobs(anyString(), anyString(), anyBoolean())).thenReturn(new Iterable<ListBlobItem>() {

                @Override
                public Iterator<ListBlobItem> iterator() {
                    return new DummyListBlobItemIterator(list);
                }
            });

            properties.remoteBlobs = new RemoteBlobsTable("RemoteBlobsTable");
            properties.remoteBlobs.include.setValue(Arrays.asList(true));
            properties.remoteBlobs.prefix.setValue(Arrays.asList("someFilter"));

            boolean startable = reader.start();
            assertTrue(startable);
            assertNotNull(reader.getCurrent());
            assertFalse(reader.advance());

            reader.getCurrent();
            fail("should throw NoSuchElementException");

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            fail("should not throw " + e.getMessage());
        }
    }

}
