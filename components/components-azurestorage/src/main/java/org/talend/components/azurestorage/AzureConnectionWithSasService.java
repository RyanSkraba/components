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
package org.talend.components.azurestorage;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;

/**
 * This class hold and provide azure storage connection using a sas token
 */
public class AzureConnectionWithSasService implements AzureConnection {

    private final String accountName;

    private final String sasToken;

    private final String endpointSuffix;

    public String getAccountName() {
        return accountName;
    }

    public String getSasToken() {
        return sasToken;
    }

    public String getEndpointSuffix(){ return  endpointSuffix; }

    @Override
    public CloudStorageAccount getCloudStorageAccount() throws InvalidKeyException, URISyntaxException {
        StorageCredentials credentials = new StorageCredentialsSharedAccessSignature(sasToken);
        return new CloudStorageAccount(credentials, true, endpointSuffix, accountName);
    }

    private AzureConnectionWithSasService(Builder builder) {
        this.accountName = builder.accountName;
        this.sasToken = builder.sasToken;
        this.endpointSuffix = builder.endpointSuffix;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String accountName;

        private String sasToken;

        private String endpointSuffix;

        public Builder accountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        public Builder sasToken(String sasToken) {
            this.sasToken = sasToken;
            return this;
        }

        public Builder endpointSuffix(String endpointSuffix) {
            this.endpointSuffix = endpointSuffix;
            return this;
        }

        public AzureConnectionWithSasService build() {
            return new AzureConnectionWithSasService(this);
        }
    }

}
