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
package org.talend.components.google.drive.runtime;

import static java.net.InetSocketAddress.createUnresolved;
import static java.net.Proxy.Type.HTTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.google.drive.GoogleDriveProvideConnectionProperties;
import org.talend.components.google.drive.GoogleDriveProvideRuntime;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties;
import org.talend.components.google.drive.connection.GoogleDriveConnectionProperties.OAuthMethod;
import org.talend.components.google.drive.runtime.client.GoogleDriveCredentialWithAccessToken;
import org.talend.components.google.drive.runtime.client.GoogleDriveCredentialWithInstalledApplication;
import org.talend.components.google.drive.runtime.client.GoogleDriveCredentialWithServiceAccount;
import org.talend.components.google.drive.runtime.client.GoogleDriveService;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport.Builder;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.User;

public class GoogleDriveRuntime extends GoogleDriveValidator
        implements RuntimableRuntime<ComponentProperties>, GoogleDriveProvideConnectionProperties, GoogleDriveProvideRuntime {

    protected static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(GoogleDriveSourceOrSink.class);

    private static final String APPLICATION_SUFFIX_GPN_TALEND = " (GPN:Talend)";

    private transient static final Logger LOG = LoggerFactory.getLogger(GoogleDriveRuntime.class);

    public RuntimeContainer container;

    protected GoogleDriveProvideConnectionProperties properties;

    private Drive service;

    private GoogleDriveUtils utils;

    private NetHttpTransport httpTransport;

    public static String getStudioName(final String inputString) {
        if (inputString == null || inputString.isEmpty()) {
            return "";
        }

        StringBuilder outputString = new StringBuilder();
        for (int i = 0; i < inputString.length(); i++) {
            Character c = inputString.charAt(i);
            outputString.append(Character.isUpperCase(c) && i > 0 ? "_" + c : c); //$NON-NLS-1$
        }
        return outputString.toString().toUpperCase(Locale.ENGLISH);
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (GoogleDriveProvideConnectionProperties) properties;
        this.container = container;
        return validateConnectionProperties(getConnectionProperties());
    }

    @Override
    public GoogleDriveConnectionProperties getConnectionProperties() {
        return properties.getConnectionProperties().getEffectiveConnectionProperties();
    }

    public GoogleDriveProvideConnectionProperties getProperties() {
        return properties;
    }

    public NetHttpTransport getHttpTransport() throws GeneralSecurityException, IOException {
        if (httpTransport == null) {
            GoogleDriveConnectionProperties conn = getConnectionProperties();
            if (conn.useSSL.getValue() || conn.useProxy.getValue()) {
                Builder tmpBuilder = new NetHttpTransport.Builder();
                if (conn.useProxy.getValue()) {
                    Proxy proxy = new Proxy(HTTP, createUnresolved(conn.proxyHost.getValue(), conn.proxyPort.getValue()));
                    tmpBuilder.setProxy(proxy);
                }
                if (conn.useSSL.getValue()) {
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    FileInputStream fi = new FileInputStream(conn.sslTrustStore.getValue());
                    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                    ks.load(fi, conn.sslTrustStorePassword.getValue().toCharArray());
                    fi.close();
                    tmf.init(ks);
                    SSLContext sslContext = SSLContext.getInstance(conn.sslAlgorithm.getValue());
                    sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());
                    tmpBuilder.setSslSocketFactory(sslContext.getSocketFactory());
                }
                httpTransport = tmpBuilder.build();
            } else {
                httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            }
        }

        return httpTransport;
    }

    public String getApplicationName() {
        String appName = getConnectionProperties().applicationName.getValue();
        if (!appName.endsWith(APPLICATION_SUFFIX_GPN_TALEND)) {
            appName = appName + APPLICATION_SUFFIX_GPN_TALEND;
        }
        return appName;
    }

    private File getDatastoreFile(GoogleDriveConnectionProperties connection) {
        return Paths.get(connection.datastorePath.getValue()).resolve(connection.applicationName.getValue()).toFile();
    }

    private Credential getCredential(NetHttpTransport httpTransport) throws IOException, GeneralSecurityException {
        GoogleDriveConnectionProperties conn = getConnectionProperties();
        /* get rid of warning on windows until fixed... https://github.com/google/google-http-java-client/issues/315 */
        final java.util.logging.Logger dsLogger = java.util.logging.Logger.getLogger(FileDataStoreFactory.class.getName());
        dsLogger.setLevel(java.util.logging.Level.SEVERE);
        switch (conn.oAuthMethod.getValue()) {
        case AccessToken:
            return GoogleDriveCredentialWithAccessToken.builder().accessToken(conn.accessToken.getValue()).build();
        case InstalledApplicationWithIdAndSecret:
            return GoogleDriveCredentialWithInstalledApplication.builderWithIdAndSecret(httpTransport, getDatastoreFile(conn))
                    .clientId(conn.clientId.getValue()).clientSecret(conn.clientSecret.getValue()).build();
        case InstalledApplicationWithJSON:
            return GoogleDriveCredentialWithInstalledApplication.builderWithJSON(httpTransport, getDatastoreFile(conn))
                    .clientSecretFile(new File(conn.clientSecretFile.getValue())).build();
        case ServiceAccount:
            return GoogleDriveCredentialWithServiceAccount.builder()
                    .serviceAccountJSONFile(new File(conn.serviceAccountFile.getValue())).build();
        }
        throw new IllegalArgumentException(messages.getMessage("error.credential.oaut.method"));
    }

    public Drive getDriveService() throws GeneralSecurityException, IOException {
        if (service == null) {
            service = new GoogleDriveService(getApplicationName(), getHttpTransport(), getCredential(getHttpTransport()))
                    .getDriveService();
        }
        return service;
    }

    public GoogleDriveUtils getDriveUtils() throws GeneralSecurityException, IOException {
        if (utils == null) {
            utils = new GoogleDriveUtils(getDriveService());
        }
        return utils;
    }

    public ValidationResult testGetDriveUser() {
        ValidationResultMutable vr = new ValidationResultMutable(Result.OK, messages.getMessage("message.connectionSuccessful"));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Callable() {

            public String call() throws Exception {
                // make a dummy call to check drive's connection...
                User u = getDriveService().about().get().setFields("user").execute().getUser();
                return u.toPrettyString();
            }
        });
        try {
            String user = future.get(30, java.util.concurrent.TimeUnit.SECONDS);
            LOG.debug("[testGetDriveUser] Testing User properties: {}.", user);
        } catch (ExecutionException ee) {
            vr.setStatus(Result.ERROR).setMessage(messages.getMessage("error.testConnection.failure", ee.getMessage()));
            LOG.error("[testGetDriveUser] Execution error: {}.", ee.getMessage());
        } catch (TimeoutException | InterruptedException e) {
            vr.setStatus(Result.ERROR).setMessage(messages.getMessage("error.testConnection.timeout"));
            LOG.error("[testGetDriveUser] Operation Timeout.");
        }
        executor.shutdownNow();

        return vr;
    }

    public ValidationResult validateConnection(GoogleDriveConnectionProperties connectionProperties) {
        if ((OAuthMethod.InstalledApplicationWithIdAndSecret.equals(connectionProperties.oAuthMethod.getValue())
                || OAuthMethod.InstalledApplicationWithJSON.equals(connectionProperties.oAuthMethod.getValue()))
                && container == null) {
            cleanupCredentialsStore(getDatastoreFile(connectionProperties).toPath().resolve("StoredCredential"));
        }
        ValidationResultMutable vr = new ValidationResultMutable(validateConnectionProperties(connectionProperties));
        if (Result.ERROR.equals(vr.getStatus())) {
            return vr;
        }

        return testGetDriveUser();
    }

    private void cleanupCredentialsStore(Path store) {
        LOG.debug("[cleanupCredentialsStore] Deleting stored credentials [{}].", store);
        try {
            Files.deleteIfExists(store);
        } catch (IOException e) {
            LOG.warn("[cleanupCredentialsStore] Could not delete {} : {}", store, e.getMessage());
        }
    }

}
