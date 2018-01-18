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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.drive.DriveScopes;

public class GoogleDriveCredentialWithServiceAccount {

    public static ServiceAccountJSONFile builder() {
        return new Builder();
    }

    private static class Builder implements ServiceAccountJSONFile, Build {

        /**
         * instance of the scopes.
         * <p>
         * If modifying these scopes, delete your previously saved credentials at ~/.credentials/
         */
        protected final List<String> scopes = Arrays.asList(DriveScopes.DRIVE);

        File serviceAccountJSONFile;

        public Build serviceAccountJSONFile(File file) {
            this.serviceAccountJSONFile = file;
            return this;
        }

        public Credential build() throws IOException, GeneralSecurityException {
            return GoogleCredential.fromStream(new FileInputStream(serviceAccountJSONFile)).createScoped(scopes);
        }
    }

    public interface ServiceAccountJSONFile {

        Build serviceAccountJSONFile(File file);
    }

    public interface Build {

        Credential build() throws IOException, GeneralSecurityException;
    }
}
