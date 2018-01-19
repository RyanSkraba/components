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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;

public class GoogleDriveCredentialWithAccessTokenTest {

    @Test
    public void testBuilder() throws Exception {
        String token = "my-test-token";
        Credential credential = GoogleDriveCredentialWithAccessToken.builder().accessToken(token).build();
        assertNotNull(credential);
        assertThat(token, equalTo(credential.getAccessToken()));
    }
}
