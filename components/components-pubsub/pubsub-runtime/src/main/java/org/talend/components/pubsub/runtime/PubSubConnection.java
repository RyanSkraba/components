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

package org.talend.components.pubsub.runtime;

import java.io.FileInputStream;
import java.io.IOException;

import org.talend.components.pubsub.PubSubDatastoreProperties;

import com.google.api.services.pubsub.PubsubScopes;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.PubSub;
import com.google.cloud.pubsub.PubSubOptions;

public class PubSubConnection {

    public static PubSub createClient(PubSubDatastoreProperties datastore) {
        if (datastore.serviceAccountFile.getValue() == null) {
            return PubSubOptions.getDefaultInstance().getService();
        } else {
            return PubSubOptions.newBuilder().setProjectId(datastore.projectName.getValue())
                    .setCredentials(createCredentials(datastore)).build().getService();
        }
    }

    public static Credentials createCredentials(PubSubDatastoreProperties datastore) {
        try {
            GoogleCredentials credential = GoogleCredentials
                    .fromStream(new FileInputStream(datastore.serviceAccountFile.getValue())).createScoped(PubsubScopes.all());
            return credential;
        } catch (IOException e) {
            throw new RuntimeException("Exception when read service account file: " + datastore.serviceAccountFile.getValue()
                    + "\nMessage is:" + e.getMessage());
        }
    }
}
