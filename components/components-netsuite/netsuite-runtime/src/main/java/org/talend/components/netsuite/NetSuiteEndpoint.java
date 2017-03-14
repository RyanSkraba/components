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

        NetSuiteConnectionProperties connProps = properties.getConnectionProperties();

        if (StringUtils.isEmpty(connProps.endpoint.getValue())) {
            throw new NetSuiteException("Invalid endpoint URL");
        }
        if (StringUtils.isEmpty(connProps.email.getValue())) {
            throw new NetSuiteException("Invalid email");
        }
        if (StringUtils.isEmpty(connProps.password.getValue())) {
            throw new NetSuiteException("Invalid password");
        }
        if (StringUtils.isEmpty(connProps.account.getValue())) {
            throw new NetSuiteException("Invalid account");
        }

        String endpointUrl = connProps.endpoint.getStringValue();
        String email = connProps.email.getStringValue();
        String password = connProps.password.getStringValue();
        Integer roleId = connProps.role.getValue();
        String account = connProps.account.getStringValue();
        String applicationId = connProps.applicationId.getStringValue();

        NetSuiteCredentials credentials = new NetSuiteCredentials();
        credentials.setEmail(email);
        credentials.setPassword(password);
        credentials.setRoleId(roleId.toString());
        credentials.setAccount(account);
        credentials.setApplicationId(applicationId);

        try {
            return new ConnectionConfig(new URL(endpointUrl), credentials);
        } catch (MalformedURLException e) {
            throw new NetSuiteException("Invalid endpoint URL: " + endpointUrl);
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

        clientService.login();

        return clientService;
    }

    public static class ConnectionConfig {
        private URL endpointUrl;
        private NetSuiteCredentials credentials;

        public ConnectionConfig() {
        }

        public ConnectionConfig(URL endpointUrl, NetSuiteCredentials credentials) {
            this.endpointUrl = endpointUrl;
            this.credentials = credentials;
        }

        public URL getEndpointUrl() {
            return endpointUrl;
        }

        public void setEndpointUrl(URL endpointUrl) {
            this.endpointUrl = endpointUrl;
        }

        public NetSuiteCredentials getCredentials() {
            return credentials;
        }

        public void setCredentials(NetSuiteCredentials credentials) {
            this.credentials = credentials;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ConnectionConfig that = (ConnectionConfig) o;
            return Objects.equals(endpointUrl, that.endpointUrl) && Objects.equals(credentials, that.credentials);
        }

        @Override
        public int hashCode() {
            return Objects.hash(endpointUrl, credentials);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ConnectionConfig{");
            sb.append("endpointUrl=").append(endpointUrl);
            sb.append(", credentials=").append(credentials);
            sb.append('}');
            return sb.toString();
        }
    }
}
