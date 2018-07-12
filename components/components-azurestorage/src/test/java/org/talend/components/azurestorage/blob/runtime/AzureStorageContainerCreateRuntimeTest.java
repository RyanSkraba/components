// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

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
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties;
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties
        .AccessControl;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;

public class AzureStorageContainerCreateRuntimeTest {

    public static final String PROP_ = "PROP_";

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
                                                           .getI18nMessages(AzureStorageContainerCreateRuntime.class);

    private RuntimeContainer runtimeContainer;

    private TAzureStorageContainerCreateProperties properties;

    private AzureStorageContainerCreateRuntime containerCreate;

    @Mock
    private AzureStorageBlobService blobService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() {
        properties = new TAzureStorageContainerCreateProperties(PROP_ + "CreateContainer");
        properties.setupProperties();
        // valid connection
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.protocol.setValue(Protocol.HTTP);
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");
        properties.accessControl.setValue(AccessControl.Public);

        runtimeContainer = new RuntimeContainerMock();
        this.containerCreate = new AzureStorageContainerCreateRuntime();
    }

    @Test
    public void testInitializeNameContainerEmpty() {
        ValidationResult validationResult = containerCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(messages.getMessage("error.ContainerEmpty"), validationResult.getMessage());
    }

    @Test
    public void testInitializeNameContainerNonAlphaNumeric() {
        properties.container.setValue("N@n_alpha_numeric#");
        ValidationResult validationResult = containerCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(messages.getMessage("error.IncorrectName"), validationResult.getMessage());
    }

    @Test
    public void testInitializeNameContainerNonAllLowerCase() {
        properties.container.setValue("NonAllLowerCase");
        ValidationResult validationResult = containerCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(messages.getMessage("error.UppercaseName"), validationResult.getMessage());
    }

    @Test
    public void testInitializeNameContainerLengthError() {
        properties.container.setValue("aa"); // container name length between 3 and 63
        ValidationResult validationResult = containerCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(messages.getMessage("error.LengthError"), validationResult.getMessage());

        // generate 64 string name for the container witch is invalide
        properties.container.setValue(String.format("%0" + 64 + "d", 0)
                                            .replace("0", "a"));
        validationResult = containerCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(messages.getMessage("error.LengthError"), validationResult.getMessage());
    }

    @Test
    public void testInitializeNameContainerValide() {
        properties.container.setValue("container-name-ok-14"); // container name length between 3 and 63
        ValidationResult validationResult = containerCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
    }

    /**
     * The method {@link AzureStorageContainerCreateRuntime#runAtDriver(RuntimeContainer)} should not throw any
     * exception if the
     * dieOnError is not set to true.
     */
    @Test
    public void testRunAtDriverHandleStorageException() {

        properties.container.setValue("container-name-ok");
        ValidationResult validationResult = containerCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        containerCreate.blobService = blobService;

        try {

            when(blobService.createContainerIfNotExist(anyString(), any(BlobContainerPublicAccessType.class))).thenThrow(
                    new StorageException("errorCode", "storage exception message", new RuntimeException()));
            containerCreate.runAtDriver(runtimeContainer);

        } catch (StorageException | URISyntaxException | InvalidKeyException e) {
            fail("should handle this error correctly " + e.getMessage());
        }

    }

    /**
     * The method {@link AzureStorageContainerCreateRuntime#runAtDriver(RuntimeContainer)} should not throw any
     * exception if the
     * dieOnError is not set to true.
     */
    @Test
    public void testRunAtDriverHandleURISyntaxException() {

        properties.container.setValue("container-name-ok");
        ValidationResult validationResult = containerCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        containerCreate.blobService = blobService;

        try {
            when(blobService.createContainerIfNotExist(anyString(), any(BlobContainerPublicAccessType.class))).thenThrow(
                    new URISyntaxException("bad url", "some reason"));
            containerCreate.runAtDriver(runtimeContainer);

        } catch (StorageException | URISyntaxException | InvalidKeyException e) {
            fail("should handle this error correctly " + e.getMessage());
        }

    }

    @Test(expected = ComponentException.class)
    public void testRunAtDriverDieOnError() {

        properties.container.setValue("container-name-ok");
        properties.dieOnError.setValue(true);
        ValidationResult validationResult = containerCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
        containerCreate.blobService = blobService;

        try {
            when(blobService.createContainerIfNotExist(anyString(), any(BlobContainerPublicAccessType.class))).thenThrow(
                    new StorageException("errorCode", "storage exception message", new RuntimeException()));
            containerCreate.runAtDriver(runtimeContainer);

        } catch (StorageException | URISyntaxException | InvalidKeyException e) {
            fail("should not throw this exception" + e.getMessage());
        }

    }

    @Test
    public void testRunAtDriverValid() {

        properties.container.setValue("container-name-ok");
        ValidationResult validationResult = containerCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());

        try {
            when(blobService.createContainerIfNotExist(anyString(),
                    any(BlobContainerPublicAccessType.class))).thenReturn(true);
            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(blobService);

            containerCreate.blobService = blobService;
            containerCreate.runAtDriver(runtimeContainer);
        } catch (InvalidKeyException | StorageException | URISyntaxException e) {
            fail("should not throw this exception" + e.getMessage());
        }

    }

    @Test
    public void testRunAtDriverContainerAllReadyCreated() {

        properties.container.setValue("container-name-ok");
        ValidationResult validationResult = containerCreate.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());

        try {
            when(blobService.createContainerIfNotExist(anyString(),
                    any(BlobContainerPublicAccessType.class))).thenReturn(false);
            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(blobService);

            containerCreate.blobService = blobService;
            containerCreate.runAtDriver(runtimeContainer);
        } catch (InvalidKeyException | StorageException | URISyntaxException e) {
            fail("should not throw this exception" + e.getMessage());
        }

    }

}
