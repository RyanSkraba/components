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

/**
 * This class hold and provide azure storage connection using a key
 */
public class AzureConnectionWithKeyService implements AzureConnection {

    private String protocol;

    private String accountName;

    private String accountKey;

    AzureConnectionWithKeyService(Builder builder) {
        this.protocol = builder.protocol;
        this.accountName = builder.accountName;
        this.accountKey = builder.accountKey;
    }

    @Override
    public CloudStorageAccount getCloudStorageAccount() throws InvalidKeyException, URISyntaxException {

        StringBuilder connectionString = new StringBuilder();
        connectionString.append("DefaultEndpointsProtocol=").append(protocol) //
                .append(";AccountName=").append(accountName) //
                .append(";AccountKey=").append(accountKey);

        return CloudStorageAccount.parse(connectionString.toString());
    }

    public String getProtocol() {
        return protocol;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public static Protocol builder() {
        return new Builder();
    }

    private static class Builder implements Build, Protocol, AccountName, AccountKey {

        private String protocol;

        private String accountName;

        private String accountKey;

        public AccountName protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public AccountKey accountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        public Build accountKey(String accountKey) {
            this.accountKey = accountKey;
            return this;
        }

        public AzureConnectionWithKeyService build() {
            return new AzureConnectionWithKeyService(this);
        }
    }

    public interface Protocol {

        public AccountName protocol(String protocol);
    }

    public interface AccountName {

        public AccountKey accountName(String accountName);
    }

    public interface AccountKey {

        public Build accountKey(String accountKey);
    }

    public interface Build {

        public AzureConnectionWithKeyService build();
    }

}
