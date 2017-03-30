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
import org.talend.components.azurestorage.blob.tazurestoragecontainercreate.TAzureStorageContainerCreateProperties;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

public class AzureStorageContainerCreateRuntimeTest {

    public static final String PROP_ = "PROP_";

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(AzureStorageContainerCreateRuntime.class);

    private RuntimeContainer runtimeContainer;

    private TAzureStorageContainerCreateProperties properties;

    private AzureStorageContainerCreateRuntime containerCreate;

    @Before
    public void setup() {
        properties = new TAzureStorageContainerCreateProperties(PROP_ + "CreateContainer");
        properties.setupProperties();
        // valid connection
        properties.connection = new TAzureStorageConnectionProperties(PROP_ + "Connection");
        properties.connection.accountName.setValue("fakeAccountName");
        properties.connection.accountKey.setValue("fakeAccountKey=ANBHFYRJJFHRIKKJFU");

        runtimeContainer = new RuntimeContainerMock();
        this.containerCreate = new AzureStorageContainerCreateRuntime();
    }

    @After
    public void dispose() {
        this.containerCreate = null;
        properties = null;
        runtimeContainer = null;
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
        properties.container.setValue(String.format("%0" + 64 + "d", 0).replace("0", "a"));
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

}
