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
package org.talend.components.azurestorage.tazurestorageconnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.azurestorage.RuntimeContainerMock;
import org.talend.components.azurestorage.blob.runtime.AzureStorageContainerCreateRuntime;
import org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties.Protocol;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;


public class TAzureStorageConnectionPropertiesTest {

    TAzureStorageConnectionProperties props;

    @Before
    public void setUp() throws Exception {
        props = new TAzureStorageConnectionProperties("test");
        props.setupProperties();
        props.setupLayout();
        

    }

    /**
     * Test method for
     * {@link org.talend.components.azurestorage.tazurestorageconnection.TAzureStorageConnectionProperties#setupProperties()}.
     */
    @Test
    public final void testSetupProperties() {
        assertEquals(props.authenticationType.getValue(), AuthType.BASIC);
        assertEquals(props.protocol.getValue(), Protocol.HTTPS);
        assertEquals("", props.accountName.getValue());
        assertEquals("", props.accountKey.getValue());
        assertEquals(false, props.useSharedAccessSignature.getValue());
    }

    @Test
    public final void testSetupLayout() {
        props.afterUseSharedAccessSignature();
        props.afterReferencedComponent();
    }

    @Test
    public void testAfterWizardFinish() throws Exception {

        props.setupProperties();
        ValidationResult vr = props.afterFormFinishWizard(null);
        assertEquals(Result.ERROR, vr.getStatus());
    }
    
    @Test
    public final void testvalidateTestConnection() throws Exception {
        props.useSharedAccessSignature.setValue(true);
        props.sharedAccessSignature.setValue("https://talendrd.blob.core.windows.net/?sv=2016-05-31&ss=f&srt=sco&sp=rwdlacup&se=2017-06-07T23:50:05Z&st=2017-05-24T15:50:05Z&spr=https&sig=fakeSASfakeSASfakeSASfakeSASfakeSASfakeSASfakeSASfakeSAS");
        assertEquals(ValidationResult.Result.OK,props.validateTestConnection().getStatus());

    }

    @Test
    public final void testGetReferencedConnectionProperties() {
        assertNull(props.getReferencedComponentId());
        assertNull(props.getReferencedConnectionProperties());
        // props.referencedComponent.componentProperties = props;
        // props.referencedComponent.
        // assertNotNull(props.getReferencedComponentId());
        // assertNotNull(props.getReferencedConnectionProperties());

    }

    @Test
    public void testEnums() {
        assertEquals(Protocol.HTTP, Protocol.valueOf("HTTP"));
        assertEquals(Protocol.HTTPS, Protocol.valueOf("HTTPS"));
    }

    @Test
    public void testRefreshLayoutAfterChangeAuthTypeToActiveDirectory() {
        props.authenticationType.setValue(AuthType.ACTIVE_DIRECTORY_CLIENT_CREDENTIAL);
        props.afterAuthenticationType();

        Form mainForm = props.getForm(Form.MAIN);

        assertTrue(mainForm.getWidget(props.accountName).isVisible());
        assertTrue(mainForm.getWidget(props.tenantId).isVisible());
        assertTrue(mainForm.getWidget(props.clientId).isVisible());
        assertTrue(mainForm.getWidget(props.clientSecret).isVisible());
        assertTrue(mainForm.getWidget(props.accountKey).isHidden());
        assertTrue(mainForm.getWidget(props.protocol).isHidden());
        assertTrue(mainForm.getWidget(props.useSharedAccessSignature).isHidden());
        assertTrue(mainForm.getWidget(props.sharedAccessSignature).isHidden());
    }

    @Test
    public void testRefreshLayoutAfterChangeAuthTypeTwice() {
        props.authenticationType.setValue(AuthType.ACTIVE_DIRECTORY_CLIENT_CREDENTIAL);
        props.afterAuthenticationType();
        props.authenticationType.setValue(AuthType.BASIC);
        props.afterAuthenticationType();

        Form mainForm = props.getForm(Form.MAIN);

        assertTrue(mainForm.getWidget(props.accountName).isVisible());
        assertTrue(mainForm.getWidget(props.tenantId).isHidden());
        assertTrue(mainForm.getWidget(props.clientId).isHidden());
        assertTrue(mainForm.getWidget(props.clientSecret).isHidden());
        assertTrue(mainForm.getWidget(props.accountKey).isVisible());
        assertTrue(mainForm.getWidget(props.protocol).isVisible());
        assertTrue(mainForm.getWidget(props.useSharedAccessSignature).isVisible());
        assertTrue(mainForm.getWidget(props.sharedAccessSignature).isHidden());
    }
}
