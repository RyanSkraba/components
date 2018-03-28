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
package org.talend.components.google.drive.connection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.talend.components.google.drive.connection.GoogleDriveConnectionProperties.FORM_WIZARD;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.GoogleDriveTestBase;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties.OAuthMethod;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;

public class GoogleDriveConnectionPropertiesTest extends GoogleDriveTestBase {

    GoogleDriveConnectionProperties properties;

    @Before
    public void setUp() throws Exception {
        properties = new GoogleDriveConnectionProperties("test");
        properties.setupProperties();
        properties.setupLayout();
        properties.refreshLayout(properties.getForm(Form.MAIN));
        properties.refreshLayout(properties.getForm(Form.ADVANCED));
    }

    @Test
    public void testOAuthMethod() throws Exception {
        assertEquals("AccessToken", OAuthMethod.AccessToken.name());
        assertEquals(OAuthMethod.AccessToken, OAuthMethod.valueOf("AccessToken"));
        assertEquals("InstalledApplicationWithIdAndSecret", OAuthMethod.InstalledApplicationWithIdAndSecret.name());
        assertEquals(OAuthMethod.InstalledApplicationWithIdAndSecret, OAuthMethod.valueOf("InstalledApplicationWithIdAndSecret"));
        assertEquals("InstalledApplicationWithJSON", OAuthMethod.InstalledApplicationWithJSON.name());
        assertEquals(OAuthMethod.InstalledApplicationWithJSON, OAuthMethod.valueOf("InstalledApplicationWithJSON"));
        assertEquals("ServiceAccount", OAuthMethod.ServiceAccount.name());
        assertEquals(OAuthMethod.ServiceAccount, OAuthMethod.valueOf("ServiceAccount"));
    }

    @Test
    public void testAfterReferencedComponent() throws Exception {
        properties.referencedComponent.componentInstanceId.setValue(GoogleDriveConnectionDefinition.COMPONENT_NAME);
        properties.afterReferencedComponent();
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.oAuthMethod.getName()).isHidden());
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.applicationName.getName()).isHidden());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.datastorePath.getName()).isHidden());
    }

    @Test
    public void testAfterOAuthMethod() throws Exception {
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.accessToken.getName()).isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.clientId.getName()).isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.clientSecret.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.clientSecretFile.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.serviceAccountFile.getName()).isVisible());
        assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.datastorePath.getName()).isVisible());
        properties.oAuthMethod.setValue(OAuthMethod.AccessToken);
        properties.afterOAuthMethod();
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.accessToken.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.clientId.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.clientSecret.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.clientSecretFile.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.serviceAccountFile.getName()).isVisible());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.datastorePath.getName()).isVisible());
        properties.oAuthMethod.setValue(OAuthMethod.InstalledApplicationWithJSON);
        properties.afterOAuthMethod();
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.accessToken.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.clientId.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.clientSecret.getName()).isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.clientSecretFile.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.serviceAccountFile.getName()).isVisible());
        assertTrue(properties.getForm(Form.ADVANCED).getWidget(properties.datastorePath.getName()).isVisible());
        properties.oAuthMethod.setValue(OAuthMethod.ServiceAccount);
        properties.afterOAuthMethod();
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.accessToken.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.clientId.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.clientSecret.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.clientSecretFile.getName()).isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.serviceAccountFile.getName()).isVisible());
        assertFalse(properties.getForm(Form.ADVANCED).getWidget(properties.datastorePath.getName()).isVisible());
    }

    @Test
    public void testAfterUseProxy() throws Exception {
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.proxyHost.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.proxyPort.getName()).isVisible());
        properties.useProxy.setValue(true);
        properties.afterUseProxy();
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.proxyHost.getName()).isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.proxyPort.getName()).isVisible());
    }

    @Test
    public void testAfterUseSSL() throws Exception {
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.sslAlgorithm.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.sslTrustStore.getName()).isVisible());
        assertFalse(properties.getForm(Form.MAIN).getWidget(properties.sslTrustStorePassword.getName()).isVisible());
        properties.useSSL.setValue(true);
        properties.afterUseSSL();
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.sslAlgorithm.getName()).isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.sslTrustStore.getName()).isVisible());
        assertTrue(properties.getForm(Form.MAIN).getWidget(properties.sslTrustStorePassword.getName()).isVisible());
    }

    @Test
    public void testValidateTestConnection() throws Exception {
        try (SandboxedInstanceTestFixture sandboxedInstanceTestFixture = new SandboxedInstanceTestFixture()) {
            sandboxedInstanceTestFixture.setUp();
            properties.validateTestConnection();
            assertTrue(properties.getForm(FORM_WIZARD).isAllowFinish());
            sandboxedInstanceTestFixture.changeValidateConnectionResult(Result.ERROR);
            assertEquals(Result.ERROR, properties.validateTestConnection().getStatus());
            properties.validateTestConnection();
            assertFalse(properties.getForm(FORM_WIZARD).isAllowFinish());
            sandboxedInstanceTestFixture.changeValidateConnectionToThrowException();
            assertThat(properties.validateTestConnection().getMessage(), is("ERROR during validation"));
        }
    }

    @Test
    public void testGetConnectionProperties() throws Exception {
        assertEquals(properties, properties.getConnectionProperties());
    }

    @Test
    public void testGetEffectiveConnectionProperties() throws Exception {
        assertEquals(properties, properties.getEffectiveConnectionProperties());
        properties.referencedComponent.setReference(new GoogleDriveConnectionProperties("referenced"));
        properties.referencedComponent.componentInstanceId.setValue("refcompid");
        assertNotNull(properties.getEffectiveConnectionProperties());
        properties.referencedComponent.setReference(null);
        assertNull(properties.getEffectiveConnectionProperties());
    }

    @Test
    public void testGetReferencedComponentId() throws Exception {
        assertNull(properties.getReferencedComponentId());
        properties.referencedComponent.setReference(new GoogleDriveConnectionProperties("referenced"));
        properties.referencedComponent.componentInstanceId.setValue("refcompid");
        assertNotNull(properties.getReferencedComponentId());
        assertEquals("refcompid", properties.getReferencedComponentId());
    }

    @Test
    public void testGetReferencedConnectionProperties() throws Exception {
        assertNull(properties.getReferencedConnectionProperties());
        properties.referencedComponent.setReference(new GoogleDriveConnectionProperties("referenced"));
        properties.referencedComponent.componentInstanceId.setValue("refcompid");
        assertNotNull(properties.getReferencedConnectionProperties());
    }

    @Test
    public void testSetRepositoryLocation() throws Exception {
        final String repoLoc = "/fake/location";
        assertThat(properties.setRepositoryLocation(repoLoc), is(properties));
        assertThat(properties.getRepositoryLocation(), is(repoLoc));
    }

    @Test
    public void testAfterFormFinishWizard() throws Exception {
        properties.name.setValue("test");
        ValidationResult vr = properties.afterFormFinishWizard(repo);
        assertThat(vr.getStatus(), is(Result.OK));
        repo = mock(TestRepository.class);
        doThrow(new RuntimeException("ERROR during storing connection")).when(repo).storeProperties(any(Properties.class),
                anyString(), anyString(), anyString());
        vr = properties.afterFormFinishWizard(repo);
        assertThat(vr.getStatus(), is(Result.ERROR));
    }

}
