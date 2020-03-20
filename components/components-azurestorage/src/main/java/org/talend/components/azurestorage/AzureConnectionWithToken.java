//==============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//==============================================================================

package org.talend.components.azurestorage;

import org.talend.components.azure.runtime.token.AzureActiveDirectoryTokenGetter;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsToken;

public class AzureConnectionWithToken implements AzureConnection {

    private final String accountName;
    private final AzureActiveDirectoryTokenGetter tokenGetter;


    public AzureConnectionWithToken(String accountName, String tenantId, String clientId, String clientSecret) {
        this.accountName = accountName;
        this.tokenGetter = new AzureActiveDirectoryTokenGetter(tenantId, clientId, clientSecret);
    }

    public AzureConnectionWithToken(String accountName, AzureActiveDirectoryTokenGetter tokenGetter) {
        this.accountName = accountName;
        this.tokenGetter = tokenGetter;
    }

    @Override
    public CloudStorageAccount getCloudStorageAccount() {
        try {
            String token = tokenGetter.retrieveAccessToken();

            StorageCredentials credentials = new StorageCredentialsToken(accountName, token);
            return new CloudStorageAccount(credentials, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
