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
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;

public class GoogleDriveCredentialWithInstalledApplication {

    public static ClientSecretFile builderWithJSON(NetHttpTransport transport, File dataStoreDir)
            throws GeneralSecurityException, IOException {
        return new BuilderWithJSON(transport, dataStoreDir);
    }

    public static ClientId builderWithIdAndSecret(NetHttpTransport transport, File dataStoreDir)
            throws GeneralSecurityException, IOException {
        return new BuilderWithIdAndSecret(transport, dataStoreDir);
    }

    public interface ClientSecretFile {

        Build clientSecretFile(File file);
    }

    public interface ClientId {

        ClientSecret clientId(String id);
    }

    public interface ClientSecret {

        Build clientSecret(String secret);
    }

    public interface Build {

        Credential build() throws IOException, GeneralSecurityException;
    }

    private abstract static class Builder implements Build {

        /**
         * instance of the scopes.
         * <p>
         * If modifying these scopes, delete your previously saved credentials at ~/.credentials/
         */
        protected final List<String> scopes = Arrays.asList(DriveScopes.DRIVE);

        /**
         * instance of the JSON factory.
         */
        protected final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

        /**
         * instance of the HTTP transport.
         */
        protected HttpTransport httpTransport;

        /**
         * instance of the {@link FileDataStoreFactory} to store user credentials for this application.
         */
        protected FileDataStoreFactory dataStoreFactory;

        public Builder(NetHttpTransport transport, File storeDir) throws GeneralSecurityException, IOException {
            httpTransport = transport;
            dataStoreFactory = new FileDataStoreFactory(storeDir);
        }
    }

    public static class BuilderWithJSON extends Builder implements Build, ClientSecretFile {

        File clientSecretFile;

        public BuilderWithJSON(NetHttpTransport transport, File store) throws GeneralSecurityException, IOException {
            super(transport, store);
        }

        @Override
        public Build clientSecretFile(File file) {
            clientSecretFile = file;
            return this;
        }

        @Override
        public Credential build() throws IOException {
            GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(clientSecretFile));
            // make a sanity check (check this.installed) before authorizing
            secrets.getDetails().getClientId();
            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, secrets,
                    scopes).setDataStoreFactory(dataStoreFactory).setAccessType("offline").build();
            return new AuthorizationCodeInstalledAppTalend(flow, new LocalServerReceiver()).authorize("user");
        }
    }

    public static class BuilderWithIdAndSecret extends Builder implements Build, ClientId, ClientSecret {

        String clientId;

        String clientSecret;

        public BuilderWithIdAndSecret(NetHttpTransport transport, File store) throws GeneralSecurityException, IOException {
            super(transport, store);
        }

        @Override
        public ClientSecret clientId(String id) {
            clientId = id;
            return this;
        }

        @Override
        public Build clientSecret(String secret) {
            clientSecret = secret;
            return this;
        }

        @Override
        public Credential build() throws IOException {
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientId,
                    clientSecret, scopes).setDataStoreFactory(dataStoreFactory).setAccessType("offline").build();
            return new AuthorizationCodeInstalledAppTalend(flow, new LocalServerReceiver()).authorize("user");
        }
    }
}
