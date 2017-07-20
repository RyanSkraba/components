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

package org.talend.components.adapter.beam.gcp;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import org.apache.beam.sdk.extensions.gcp.auth.CredentialFactory;
import org.apache.beam.sdk.extensions.gcp.auth.GcpCredentialFactory;
import org.apache.beam.sdk.options.PipelineOptions;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;

/**
 * Construct an <a href="https://developers.google.com/identity/protocols/OAuth2ServiceAccount">service account
 * credential</a> to be used by the SDK and the SDK workers. Returns a GCP credential.
 * Extend {@link GcpCredentialFactory} for Service account credential.
 */
public class ServiceAccountCredentialFactory implements CredentialFactory {

    /**
     * The scope cloud-platform provides access to all Cloud Platform resources. cloud-platform isn't sufficient yet for
     * talking to datastore so we request those resources separately.
     *
     * <p>
     * Note that trusted scope relationships don't apply to OAuth tokens, so for services we access directly (GCS) as
     * opposed to through the backend (BigQuery, GCE), we need to explicitly request that scope.
     */
    private static final List<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/cloud-platform",
            "https://www.googleapis.com/auth/devstorage.full_control", "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/datastore", "https://www.googleapis.com/auth/pubsub");

    private final GcpServiceAccountOptions options;

    private ServiceAccountCredentialFactory(GcpServiceAccountOptions options) {
        this.options = options;
    }

    public static ServiceAccountCredentialFactory fromOptions(PipelineOptions options) {
        return new ServiceAccountCredentialFactory(options.as(GcpServiceAccountOptions.class));
    }

    @Override
    public Credentials getCredential() throws IOException, GeneralSecurityException {
        return GoogleCredentials.fromStream(new FileInputStream(options.getServiceAccountFile())).createScoped(SCOPES);
    }
}
