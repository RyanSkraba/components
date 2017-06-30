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
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.blob.AzureStorageBlobService;
import org.talend.components.azurestorage.blob.helpers.FileMaskTable;
import org.talend.components.azurestorage.blob.tazurestorageput.TAzureStoragePutProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import com.microsoft.azure.storage.StorageException;

public class AzureStoragePutRuntimeTest {

    public static final String PROP_ = "PROP_";

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageGetRuntimeTest.class);

    private RuntimeContainer runtimeContainer;

    private TAzureStoragePutProperties properties;

    private AzureStoragePutRuntime storagePut;

    @Mock
    private AzureStorageBlobService blobService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private String localFolderPath;

    @Before
    public void setup() throws IOException {
        properties = new TAzureStoragePutProperties(PROP_ + "Put");
        properties.setupProperties();
        // valid connection
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.protocol.setValue(Protocol.HTTP);
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");
        properties.container.setValue("goog-container-name-1");

        runtimeContainer = new RuntimeContainerMock();
        this.storagePut = new AzureStoragePutRuntime();

        localFolderPath = getClass().getClassLoader().getResource("azurestorage-put").getPath();
    }

    @Test
    public void testEmptyLocalFolder() {
        ValidationResult validationResult = storagePut.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(messages.getMessage("error.EmptyLocalFolder"), validationResult.getMessage());
    }

    @Test
    public void testEmptyFileList() {
        properties.localFolder.setValue(localFolderPath);
        properties.useFileList.setValue(true);
        properties.files = new FileMaskTable("fileMaskTable");
        properties.files.fileMask.setValue(new ArrayList<String>());

        ValidationResult validationResult = storagePut.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(messages.getMessage("error.EmptyFileList"), validationResult.getMessage());
    }

    @Test
    public void testValidProperties() {
        properties.localFolder.setValue(localFolderPath);
        properties.useFileList.setValue(true);
        properties.files = new FileMaskTable("fileMaskTable");
        properties.files.fileMask.setValue(new ArrayList<String>());
        properties.files.newName.setValue(new ArrayList<String>());

        properties.files.fileMask.getValue().add("blob1*");
        properties.files.newName.getValue().add("blob");

        ValidationResult validationResult = storagePut.initialize(runtimeContainer, properties);
        assertNull(validationResult.getMessage());
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
    }

    @Test
    public void testRunAtDriverHandleStorageException() {

        properties.localFolder.setValue(localFolderPath);
        properties.useFileList.setValue(true);
        properties.files = new FileMaskTable("fileMaskTable");
        properties.files.fileMask.setValue(new ArrayList<String>());
        properties.files.newName.setValue(new ArrayList<String>());
        properties.files.fileMask.getValue().add("blob1*");
        properties.files.newName.getValue().add("blob");

        ValidationResult validationResult = storagePut.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());

        storagePut.azureStorageBlobService = blobService;
        try {
            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    throw new StorageException("some error code", "some storage exception", new RuntimeException());
                }
            }).when(blobService).upload(anyString(), anyString(), any(InputStream.class), anyLong());
            this.storagePut.runAtDriver(runtimeContainer);

        } catch (InvalidKeyException | StorageException | IOException | URISyntaxException e) {
            fail("should not throw " + e.getMessage());
        }
    }

    @Test(expected = ComponentException.class)
    public void testRunAtDriverHandleDieOnError() {

        properties.localFolder.setValue(localFolderPath);
        properties.useFileList.setValue(true);
        properties.files = new FileMaskTable("fileMaskTable");
        properties.files.fileMask.setValue(new ArrayList<String>());
        properties.files.newName.setValue(new ArrayList<String>());
        properties.files.fileMask.getValue().add("blob1*");
        properties.files.newName.getValue().add("blob");
        properties.dieOnError.setValue(true);

        ValidationResult validationResult = storagePut.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());

        storagePut.azureStorageBlobService = blobService;
        try {
            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    throw new StorageException("some error code", "some storage exception", new RuntimeException());
                }
            }).when(blobService).upload(anyString(), anyString(), any(InputStream.class), anyLong());
            this.storagePut.runAtDriver(runtimeContainer);

        } catch (InvalidKeyException | StorageException | IOException | URISyntaxException e) {
            fail("should not throw " + e.getMessage());
        }

    }

    @Test
    public void testRunAtDriverValid() {

        properties.localFolder.setValue(localFolderPath);
        properties.useFileList.setValue(true);
        properties.files = new FileMaskTable("fileMaskTable");
        properties.files.fileMask.setValue(new ArrayList<String>());
        properties.files.newName.setValue(new ArrayList<String>());
        properties.files.fileMask.getValue().add("blob1*");
        properties.files.newName.getValue().add("blob");

        ValidationResult validationResult = storagePut.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());

        storagePut.azureStorageBlobService = blobService;
        try {
            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(blobService).upload(anyString(), anyString(), any(InputStream.class), anyLong());
            this.storagePut.runAtDriver(runtimeContainer);

        } catch (InvalidKeyException | StorageException | IOException | URISyntaxException e) {
            fail("should not throw " + e.getMessage());
        }

    }

}
