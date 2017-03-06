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

package org.talend.components.bigquery.runtime;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.bigquery.BigQueryDatastoreProperties;

import com.google.api.services.bigquery.BigqueryScopes;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;

public class BigQueryConnection {

    public static BigQuery createClient(BigQueryDatastoreProperties datastore) {
        if (StringUtils.isEmpty(datastore.serviceAccountFile.getValue())) {
            return BigQueryOptions.getDefaultInstance().getService();
        } else {
            return BigQueryOptions.newBuilder().setProjectId(datastore.projectName.getValue())
                    .setCredentials(createCredentials(datastore)).build().getService();
        }
    }

    public static Credentials createCredentials(BigQueryDatastoreProperties datastore) {
        try {
            GoogleCredentials credential = GoogleCredentials
                    .fromStream(new FileInputStream(datastore.serviceAccountFile.getValue())).createScoped(BigqueryScopes.all());
            return credential;
        } catch (IOException e) {
            throw new RuntimeException("Exception when read service account file: " + datastore.serviceAccountFile.getValue()
                    + "\nMessage is:" + e.getMessage());
        }
    }

}
