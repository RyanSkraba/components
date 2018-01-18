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

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;

public class GoogleDriveCredentialWithServiceAccountTest {

    @Test
    public void testBuilder() throws Exception {
        File secretClientFile = new File(getClass().getClassLoader().getResource("service_account.json").toURI().getPath());
        Credential credential = GoogleDriveCredentialWithServiceAccount.builder().serviceAccountJSONFile(secretClientFile)
                .build();
        assertNotNull(credential);
    }
}
