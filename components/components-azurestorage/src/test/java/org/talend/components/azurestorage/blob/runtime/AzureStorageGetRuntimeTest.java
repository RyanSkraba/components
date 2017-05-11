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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azurestorage.FileUtils;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.blob.helpers.RemoteBlobsGetTable;
import org.talend.components.azurestorage.blob.tazurestorageget.TAzureStorageGetProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageGetRuntimeTest {

    public static final String PROP_ = "PROP_";

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageGetRuntimeTest.class);

    private RuntimeContainer runtimeContainer;

    private TAzureStorageGetProperties properties;

    private AzureStorageGetRuntime storageGet;

    private File localFolder;

    @Before
    public void setup() throws IOException {
        properties = new TAzureStorageGetProperties(PROP_ + "Get");
        properties.setupProperties();
        // valid connection
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");
        properties.container.setValue("goog-container-name-1");

        runtimeContainer = new RuntimeContainerMock();
        this.storageGet = new AzureStorageGetRuntime();

        localFolder = FileUtils.createTempDirectory();
    }

    @After
    public void dispose() {
        this.storageGet = null;
        properties = null;
        runtimeContainer = null;
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

}
