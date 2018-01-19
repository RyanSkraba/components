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
package org.talend.components.google.drive.runtime.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;

public class GoogleDriveServiceTest {

    GoogleDriveService svc;

    String application = "app";

    NetHttpTransport transport;

    Credential credential = GoogleDriveCredentialWithAccessToken.builder().accessToken("my-access-token").build();

    @Before
    public void setUp() throws Exception {
        transport = GoogleNetHttpTransport.newTrustedTransport();
        svc = new GoogleDriveService("", null, null);
    }

    @Test
    public void testGetCredential() throws Exception {
        assertNull(svc.getCredential());
    }

    @Test
    public void testSetCredential() throws Exception {
        svc.setCredential(credential);
        assertNotNull(svc.getCredential());
    }

    @Test
    public void testGetApplicationName() throws Exception {
        assertTrue(svc.getApplicationName().isEmpty());
    }

    @Test
    public void testSetApplicationName() throws Exception {
        svc.setApplicationName(application);
        assertEquals(application, svc.getApplicationName());
    }

    @Test
    public void testSetHttpTransport() throws Exception {
        svc.setHttpTransport(transport);
        assertEquals(transport, svc.getHttpTransport());
    }

    @Test
    public void testGetDriveService() throws Exception {
        svc = new GoogleDriveService(application, transport, credential);
        assertNotNull(svc.getDriveService());
    }

    @Test(expected = GoogleJsonResponseException.class)
    public void testExecuteFail() throws Exception {
        Drive drive = new GoogleDriveService(application, transport, credential).getDriveService();
        assertNotNull(drive);
        drive.files().list().execute();
        fail("Should not be here");
    }
}
