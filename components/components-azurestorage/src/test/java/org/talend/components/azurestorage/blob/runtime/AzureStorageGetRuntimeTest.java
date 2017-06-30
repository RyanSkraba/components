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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azurestorage.FileUtils;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.blob.AzureStorageBlobService;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobsGetTable;
import org.talend.components.azurestorage.blob.tazurestorageget.TAzureStorageGetProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class AzureStorageGetRuntimeTest {

    public static final String PROP_ = "PROP_";

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageGetRuntimeTest.class);

    private RuntimeContainer runtimeContainer;

    private TAzureStorageGetProperties properties;

    private AzureStorageGetRuntime storageGet;

    private File localFolder;

    @Mock
    private AzureStorageBlobService blobService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() throws IOException {
        properties = new TAzureStorageGetProperties(PROP_ + "Get");
        properties.setupProperties();
        // valid connection
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.protocol.setValue(Protocol.HTTP);
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");
        properties.container.setValue("goog-container-name-1");

        runtimeContainer = new RuntimeContainerMock();
        this.storageGet = new AzureStorageGetRuntime();

        localFolder = FileUtils.createTempDirectory();
    }

    @After
    public void dispose() {
        localFolder.delete();
    }

    @Test
    public void testInitializeEmptyBlobs() {
        properties.remoteBlobsGet = new RemoteBlobsGetTable("RemoteBlobsGetTable");
        properties.remoteBlobsGet.prefix.setValue(new ArrayList<String>());
        ValidationResult validationResult = storageGet.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(messages.getMessage("error.EmptyBlobs"), validationResult.getMessage());
    }

    @Test
    public void testInitializeValidProperties() {
        properties.remoteBlobsGet = new RemoteBlobsGetTable("RemoteBlobsGetTable");
        properties.remoteBlobsGet.prefix.setValue(new ArrayList<String>());
        properties.remoteBlobsGet.prefix.getValue().add("");

        properties.localFolder.setValue(localFolder.getAbsolutePath());
        ValidationResult validationResult = storageGet.initialize(runtimeContainer, properties);
        assertNull(validationResult.getMessage());
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
    }

    @Test
    public void testRunAtDriverValid() {
        String localFolderPath = null;
        try {

            localFolderPath = FileUtils.createTempDirectory().getAbsolutePath();

            properties.remoteBlobsGet = new RemoteBlobsGetTable("RemoteBlobsGetTable");
            properties.remoteBlobsGet.include.setValue(Arrays.asList(true));
            properties.remoteBlobsGet.prefix.setValue(Arrays.asList("block1"));
            properties.remoteBlobsGet.create.setValue(Arrays.asList(false));
            properties.localFolder.setValue(localFolderPath);

            ValidationResult validationResult = storageGet.initialize(runtimeContainer, properties);
            assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());

            final List<CloudBlockBlob> list = new ArrayList<>();
            list.add(new CloudBlockBlob(new URI("https://storagesample.blob.core.windows.net/mycontainer/blob1.txt")));
            when(blobService.listBlobs(anyString(), anyString(), anyBoolean())).thenReturn(new Iterable<ListBlobItem>() {

                @Override
                public Iterator<ListBlobItem> iterator() {
                    return new DummyListBlobItemIterator(list);
                }
            });

            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(blobService).download(any(CloudBlob.class), any(OutputStream.class));
            storageGet.azureStorageBlobService = blobService;
            storageGet.runAtDriver(runtimeContainer);

        } catch (StorageException | URISyntaxException | InvalidKeyException | IOException e) {
            fail("should not throw " + e.getMessage());
        } finally {
            if (localFolderPath != null) {
                Files.delete(new File(localFolderPath));
            }
        }

    }

}
