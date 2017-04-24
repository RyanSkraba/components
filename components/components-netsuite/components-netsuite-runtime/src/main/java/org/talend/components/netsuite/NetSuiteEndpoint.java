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

package org.talend.components.netsuite;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.netsuite.client.MetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientFactory;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteCredentials;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;

/**
 *
 */
public class NetSuiteEndpoint {

    private NetSuiteClientFactory<?> clientFactory;
    private ConnectionConfig connectionConfig;
    private NetSuiteClientService<?> clientService;

    public NetSuiteEndpoint(NetSuiteClientFactory<?> clientFactory, ConnectionConfig connectionConfig) {
        this.clientFactory = clientFactory;
        this.connectionConfig = connectionConfig;
    }

    public static ConnectionConfig createConnectionConfig(
            NetSuiteConnectionProperties properties) throws NetSuiteException {

        NetSuiteConnectionProperties connProps = properties.getEffectiveConnectionProperties();

        if (StringUtils.isEmpty(connProps.endpoint.getStringValue())) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.endpointUrlRequired"));
        }
        if (StringUtils.isEmpty(connProps.apiVersion.getStringValue())) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.apiVersionRequired"));
        }
        if (StringUtils.isEmpty(connProps.email.getStringValue())) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.emailRequired"));
        }
        if (StringUtils.isEmpty(connProps.password.getStringValue())) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.passwordRequired"));
        }
        if (StringUtils.isEmpty(connProps.account.getStringValue())) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.accountRequired"));
        }
        if (connProps.role.getValue() == null) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.roleRequired"));
        }

        String endpointUrl = connProps.endpoint.getStringValue();

        NetSuiteVersion endpointApiVersion;
        try {
            endpointApiVersion = NetSuiteVersion.detectVersion(endpointUrl);
        } catch (IllegalArgumentException e) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.couldNotDetectApiVersionFromEndpointUrl",
                            endpointUrl));
        }
        String apiVersionString = connProps.apiVersion.getStringValue();
        NetSuiteVersion apiVersion;
        try {
            apiVersion = NetSuiteVersion.parseVersion(apiVersionString);
        } catch (IllegalArgumentException e) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.invalidApiVersion", apiVersionString));
        }

        if (!endpointApiVersion.isSameMajor(apiVersion)) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.endpointUrlApiVersionMismatch",
                            endpointUrl, apiVersionString));
        }

        if (apiVersion.getMajorYear() >= 2015
                && StringUtils.isEmpty(connProps.applicationId.getStringValue())) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.applicationIdRequired"));
        }

        String email = connProps.email.getStringValue();
        String password = connProps.password.getStringValue();
        Integer roleId = connProps.role.getValue();
        String account = connProps.account.getStringValue();
        String applicationId = connProps.applicationId.getStringValue();
        Boolean customizationEnabled = connProps.customizationEnabled.getValue();

        NetSuiteCredentials credentials = new NetSuiteCredentials();
        credentials.setEmail(email);
        credentials.setPassword(password);
        credentials.setRoleId(roleId.toString());
        credentials.setAccount(account);
        credentials.setApplicationId(applicationId);

        try {
            ConnectionConfig connectionConfig = new ConnectionConfig(
                    new URL(endpointUrl), apiVersion.getMajor(), credentials);
            connectionConfig.setCustomizationEnabled(customizationEnabled);
            return connectionConfig;
        } catch (MalformedURLException e) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.invalidEndpointUrl", endpointUrl));
        }
    }

    public NetSuiteClientService<?> connect() throws NetSuiteException {
        clientService = connect(connectionConfig);

        return clientService;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public NetSuiteClientService<?> getClientService() throws NetSuiteException {
        if (clientService == null) {
            clientService = connect();
        }
        return clientService;
    }

    protected NetSuiteClientService<?> connect(ConnectionConfig connectionConfig)
            throws NetSuiteException {

        NetSuiteClientService<?> clientService = clientFactory.createClient();
        clientService.setEndpointUrl(connectionConfig.getEndpointUrl().toString());
        clientService.setCredentials(connectionConfig.getCredentials());

        MetaDataSource metaDataSource = clientService.getMetaDataSource();
        metaDataSource.setCustomizationEnabled(connectionConfig.isCustomizationEnabled());

        clientService.login();

        return clientService;
    }

    public static class ConnectionConfig {
        private URL endpointUrl;
        private NetSuiteVersion apiVersion;
        private NetSuiteCredentials credentials;
        private boolean customizationEnabled;

        public ConnectionConfig() {
        }

        public ConnectionConfig(URL endpointUrl, NetSuiteVersion apiVersion, NetSuiteCredentials credentials) {
            this.endpointUrl = endpointUrl;
            this.apiVersion = apiVersion;
            this.credentials = credentials;
        }

        public URL getEndpointUrl() {
            return endpointUrl;
        }

        public void setEndpointUrl(URL endpointUrl) {
            this.endpointUrl = endpointUrl;
        }

        public NetSuiteVersion getApiVersion() {
            return apiVersion;
        }

        public void setApiVersion(NetSuiteVersion apiVersion) {
            this.apiVersion = apiVersion;
        }

        public NetSuiteCredentials getCredentials() {
            return credentials;
        }

        public void setCredentials(NetSuiteCredentials credentials) {
            this.credentials = credentials;
        }

        public boolean isCustomizationEnabled() {
            return customizationEnabled;
        }

        public void setCustomizationEnabled(boolean customizationEnabled) {
            this.customizationEnabled = customizationEnabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ConnectionConfig that = (ConnectionConfig) o;
            return customizationEnabled == that.customizationEnabled && Objects.equals(endpointUrl, that.endpointUrl) &&
                    Objects.equals(apiVersion, that.apiVersion) && Objects.equals(credentials, that.credentials);
        }

        @Override
        public int hashCode() {
            return Objects.hash(endpointUrl, apiVersion, credentials, customizationEnabled);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ConnectionConfig{");
            sb.append("endpointUrl=").append(endpointUrl);
            sb.append("apiVersion=").append(apiVersion);
            sb.append(", credentials=").append(credentials);
            sb.append(", customizationEnabled=").append(customizationEnabled);
            sb.append('}');
            return sb.toString();
        }
    }
}
