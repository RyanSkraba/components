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

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.netsuite.client.NetSuiteClientFactory;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.util.ComponentExceptions;
import org.talend.daikon.properties.ValidationResult;

/**
 * Base class for all implementations of {@link NetSuiteRuntime}.
 *
 * Each version of NetSuite runtime should provide concrete implementation of this class.
 */
public abstract class AbstractNetSuiteRuntime implements NetSuiteRuntime {
    protected NetSuiteClientFactory<?> clientFactory;
    protected NetSuiteRuntime.Context context;

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }

    public NetSuiteClientFactory<?> getClientFactory() {
        return clientFactory;
    }

    public void setClientFactory(NetSuiteClientFactory<?> clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public NetSuiteDatasetRuntime getDatasetRuntime(NetSuiteProvideConnectionProperties properties) {
        return getDatasetRuntime(null, properties);
    }

    @Override
    public NetSuiteDatasetRuntime getDatasetRuntime(RuntimeContainer container, NetSuiteProvideConnectionProperties properties) {
        NetSuiteEndpoint endpoint = getEndpoint(context, properties);
        return new NetSuiteDatasetRuntimeImpl(endpoint.getClientService(container).getMetaDataSource());
    }

    @Override
    public ValidationResult validateConnection(NetSuiteProvideConnectionProperties properties) {
        try {
            NetSuiteEndpoint endpoint = getEndpoint(context, properties);
            endpoint.getClientService(null);
            return ValidationResult.OK;
        } catch (NetSuiteException e) {
            return ComponentExceptions.exceptionToValidationResult(e);
        }
    }

    /**
     * Get NetSuite endpoint object using given context and connection properties.
     *
     * <p>If context specifies that caching is enabled then the method first tries to
     * load endpoint object from context. If endpoint object for given connection configuration
     * doesn't exist then the method creates new endpoint object and stores it in context.
     *
     * @param context context
     * @param properties connection properties
     * @return endpoint object
     * @throws NetSuiteException if an error occurs during obtaining of endpoint object
     */
    protected NetSuiteEndpoint getEndpoint(final NetSuiteRuntime.Context context,
            final NetSuiteProvideConnectionProperties properties) throws NetSuiteException {

        // Create connection configuration for given connection properties.
        NetSuiteEndpoint.ConnectionConfig connectionConfig = NetSuiteEndpoint.createConnectionConfig(properties);

        NetSuiteEndpoint endpoint = null;
        // If caching is enabled then we should first try to get cached endpoint object.
        if (context != null && context.isCachingEnabled()) {
            NetSuiteEndpoint.ConnectionConfig cachedConnectionConfig =
                    (NetSuiteEndpoint.ConnectionConfig) context.getAttribute(NetSuiteEndpoint.ConnectionConfig.class.getName());
            // If any of key properties in connection properties was changed then
            // we should not use this cached object, we should create new.
            if (cachedConnectionConfig != null && connectionConfig.equals(cachedConnectionConfig)) {
                endpoint = (NetSuiteEndpoint) context.getAttribute(NetSuiteEndpoint.class.getName());
            }
        }
        if (endpoint == null) {
            endpoint = new NetSuiteEndpoint(clientFactory, NetSuiteEndpoint.createConnectionConfig(properties));
            if (context != null && context.isCachingEnabled()) {
                // Store connection configuration and endpoint in context.
                context.setAttribute(NetSuiteEndpoint.class.getName(), endpoint);
                context.setAttribute(NetSuiteEndpoint.ConnectionConfig.class.getName(), endpoint.getConnectionConfig());
            }
        }

        return endpoint;
    }
}
