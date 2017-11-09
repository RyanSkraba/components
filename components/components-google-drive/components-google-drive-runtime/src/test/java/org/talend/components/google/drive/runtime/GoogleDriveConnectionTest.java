package org.talend.components.google.drive.runtime;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties.OAuthMethod;
import org.talend.daikon.properties.ValidationResult.Result;

public class GoogleDriveConnectionTest extends GoogleDriveTestBaseRuntime {

    private GoogleDriveConnectionProperties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        properties = new GoogleDriveConnectionProperties("test");
        properties.setupProperties();
        properties = (GoogleDriveConnectionProperties) setupConnectionWithInstalledApplicationWithIdAndSecret(properties);
        properties.setupLayout();
    }

    @Test
    public void testValidationOK() throws Exception {
        assertEquals(Result.OK, sourceOrSink.initialize(container, properties).getStatus());
        assertEquals(Result.OK, sourceOrSink.validate(container).getStatus());
    }

    private void assertFalseForPropertyValidation() {
        sourceOrSink.initialize(container, properties);
        assertEquals(Result.ERROR, sourceOrSink.validate(container).getStatus());
    }

    @Test
    public void testValidationApplicationName() throws Exception {
        properties.applicationName.setValue("");
        assertFalseForPropertyValidation();
    }

    @Test
    public void testValidationProxy() throws Exception {
        properties.useProxy.setValue(true);
        properties.proxyHost.setValue("");
        assertFalseForPropertyValidation();
        properties.proxyHost.setValue("127.0.0.1");
        properties.proxyPort.setValue(0);
        assertFalseForPropertyValidation();
    }

    @Test
    public void testValidationSSL() throws Exception {
        properties.useSSL.setValue(true);
        properties.sslAlgorithm.setValue("");
        assertFalseForPropertyValidation();
        properties.sslAlgorithm.setValue("SSL");
        properties.sslTrustStore.setValue("");
        assertFalseForPropertyValidation();
    }

    @Test
    public void testValidationClientId() throws Exception {
        properties.clientId.setValue("");
        assertFalseForPropertyValidation();
    }

    @Test
    public void testValidationClientSecret() throws Exception {
        properties.clientSecret.setValue("");
        assertFalseForPropertyValidation();
    }

    @Test
    public void testValidationDataStorePath() throws Exception {
        properties.datastorePath.setValue("");
        assertFalseForPropertyValidation();
        properties = (GoogleDriveConnectionProperties) setupConnectionWithInstalledApplicationWithJson(properties);
        properties.datastorePath.setValue("");
        assertFalseForPropertyValidation();
    }

    @Test
    public void testValidationClientSecretFile() throws Exception {
        properties = (GoogleDriveConnectionProperties) setupConnectionWithInstalledApplicationWithJson(properties);
        properties.clientSecretFile.setValue("");
        assertFalseForPropertyValidation();
    }

    @Test
    public void testValidationAccessToken() throws Exception {
        properties.oAuthMethod.setValue(OAuthMethod.AccessToken);
        assertFalseForPropertyValidation();
    }

    @Test
    public void testValidationServiceAccount() throws Exception {
        properties.oAuthMethod.setValue(OAuthMethod.ServiceAccount);
        assertFalseForPropertyValidation();
    }

}
