package org.talend.components.google.drive.runtime;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;
import org.talend.daikon.properties.ValidationResult.Result;

import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.User;

public class GoogleDriveRuntimeTest extends GoogleDriveTestBaseRuntime {

    private GoogleDriveRuntime testRuntime;

    private GoogleDriveConnectionProperties properties;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        properties = new GoogleDriveConnectionProperties("test");
        properties.setupProperties();
        properties = (GoogleDriveConnectionProperties) setupConnectionWithInstalledApplicationWithIdAndSecret(properties);
        testRuntime = spy(new GoogleDriveRuntime());
        doReturn(drive).when(testRuntime).getDriveService();
        testRuntime.initialize(container, properties);
    }

    @Test
    public void testInitialize() throws Exception {
        assertEquals(Result.OK, testRuntime.initialize(container, properties).getStatus());
    }

    @Test
    public void testValidateConnectionProperties() throws Exception {
        assertEquals(Result.OK, testRuntime.validateConnectionProperties(testRuntime.getConnectionProperties()).getStatus());
    }

    @Test
    public void testGetConnectionProperties() throws Exception {
        assertEquals(properties, testRuntime.getConnectionProperties());
    }

    @Test
    public void testGetProperties() throws Exception {
        assertEquals(properties, testRuntime.getProperties());
    }

    @Test
    public void testGetHttpTransport() throws Exception {
        assertNotNull(testRuntime.getHttpTransport());
        properties.useProxy.setValue(true);
        testRuntime.initialize(container, properties);
        assertTrue(testRuntime.getConnectionProperties().useProxy.getValue());
        assertTrue(testRuntime.getProperties().getConnectionProperties().useProxy.getValue());
        assertNotNull(testRuntime.getHttpTransport());
    }

    @Test
    public void testGetApplicationName() throws Exception {
        assertThat(testRuntime.getApplicationName(), containsString("(GPN:Talend)"));
    }

    @Test
    public void testGetDriveUtils() throws Exception {
        assertNotNull(testRuntime.getDriveUtils());
    }

    @Test
    public void testValidateConnection() throws Exception {
        About about = new About();
        User user = new User();
        user.setEmailAddress("test@example.org");
        about.setUser(user);
        when(drive.about().get().setFields(anyString()).execute()).thenReturn(about);
        assertEquals(Result.OK, testRuntime.validateConnection(testRuntime.getConnectionProperties()).getStatus());
    }

}
