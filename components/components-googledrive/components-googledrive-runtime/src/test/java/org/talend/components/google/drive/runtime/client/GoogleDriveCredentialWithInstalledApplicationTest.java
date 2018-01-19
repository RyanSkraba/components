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

import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.common.collect.ImmutableList;

public class GoogleDriveCredentialWithInstalledApplicationTest {

    private File dataStoreDir;

    private NetHttpTransport transport;

    private File secretClientFile;

    private Credential credential;

    @Before
    public void setUp() throws Exception {
        dataStoreDir = new File(getClass().getClassLoader().getResource(".").toURI().getPath());
        transport = new NetHttpTransport();
        secretClientFile = new File(getClass().getClassLoader().getResource("installed_application.json").toURI().getPath());
        System.out.println("secretClientFile = " + secretClientFile + secretClientFile.exists());
        System.out.println("dataStoreDir = " + dataStoreDir);
        // spy AuthorizationCodeInstalledApp, mock authorize.
        credential = GoogleDriveCredentialWithAccessToken.builder().accessToken("fake-token").build();
    }

    private static final String CLIENT_ID = "812741506391.apps.googleusercontent.com";

    private static final String CLIENT_SECRET = "{client_secret}";

    @Test
    public void testBuilder() {
        GoogleAuthorizationCodeFlow.Builder builder = new GoogleAuthorizationCodeFlow.Builder(new MockHttpTransport(),
                new JacksonFactory(), CLIENT_ID, CLIENT_SECRET,
                ImmutableList.of("https://www.googleapis.com/auth/userinfo.email"));
        assertNull(builder.getApprovalPrompt());
        assertNull(builder.getAccessType());
    }

}
