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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageRuntimeTest {

    public static final String PROP_CONNECTION = "PROP_CONNECTION";

    //
    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageRuntimeTest.class);

    //
    private RuntimeContainer runtimeContainer;

    private TAzureStorageConnectionProperties properties;

    private AzureStorageRuntime azureStorageRuntime;
    //

    @Before
    public void setup() {
        runtimeContainer = new RuntimeContainerMock();
        this.azureStorageRuntime = new AzureStorageRuntime();
        properties = new TAzureStorageConnectionProperties(PROP_CONNECTION);
        properties.setupProperties();
    }

    @After
    public void dispose() {
        this.runtimeContainer = null;
        this.azureStorageRuntime = null;
        this.properties = null;
    }

    @Test
    public void testInvalidAccountNameAndKey() {
        ValidationResult validationResult = this.azureStorageRuntime.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(messages.getMessage("error.EmptyKey"), validationResult.getMessage());
    }

    @Test
    public void testInvalidAccountEmptyName() {
        properties.accountName.setValue("");
        properties.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");
        ValidationResult validationResult = this.azureStorageRuntime.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(messages.getMessage("error.EmptyKey"), validationResult.getMessage());
    }

    @Test
    public void testInvalidAccountEmptyKey() {
        properties.accountName.setValue("fakeAccountName");
        properties.accountKey.setValue("");
        ValidationResult validationResult = this.azureStorageRuntime.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(messages.getMessage("error.EmptyKey"), validationResult.getMessage());
    }

    @Test
    public void testInvalidSas() {
        properties.useSharedAccessSignature.setValue(true);
        ValidationResult validationResult = this.azureStorageRuntime.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.Result.ERROR, validationResult.getStatus());
        assertEquals(messages.getMessage("error.EmptySAS"), validationResult.getMessage());
    }

    @Test
    public void testValidSas() {
        properties.useSharedAccessSignature.setValue(true);
        properties.sharedAccessSignature.setValue("FakeSignature=ALKNFJHGIKHJ");
        ValidationResult validationResult = this.azureStorageRuntime.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
    }

    @Test
    public void testValidAccountNameAndKey() {
        properties.accountName.setValue("fakeAccountName");
        properties.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");
        ValidationResult validationResult = this.azureStorageRuntime.initialize(runtimeContainer, properties);
        assertEquals(ValidationResult.OK.getStatus(), validationResult.getStatus());
    }

}
