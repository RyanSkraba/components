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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

public class GoogleDriveCredentialWithAccessToken {

    public static AccessToken builder() {
        return new Builder();
    }

    private static class Builder implements Build, AccessToken {

        private String accessToken;

        public Build accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Credential build() {
            return new GoogleCredential().setAccessToken(accessToken);
        }
    }

    public interface AccessToken {

        Build accessToken(String accessToken);
    }

    public interface Build {

        Credential build();
    }
}
