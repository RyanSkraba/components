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

import static org.talend.components.netsuite.util.ComponentExceptions.exceptionToValidationResult;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.netsuite.client.NetSuiteClientFactory;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

/**
 * Base class NetSuite sources and sinks.
 */
public class NetSuiteSourceOrSink implements SourceOrSink {

    protected transient final Logger logger = LoggerFactory.getLogger(getClass());

    /** Creates instance of NetSuite client. */
    protected NetSuiteClientFactory<?> clientFactory;

    /** Properties to be used by source/sink. */
    protected NetSuiteProvideConnectionProperties properties;

    /** Endpoint object. */
    protected transient NetSuiteEndpoint endpoint;

    public NetSuiteClientFactory<?> getClientFactory() {
        return clientFactory;
    }

    public void setClientFactory(NetSuiteClientFactory<?> clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public ValidationResult initialize(final RuntimeContainer container, final ComponentProperties properties) {
        this.properties = (NetSuiteProvideConnectionProperties) properties;
        NetSuiteEndpoint.ConnectionConfig connectionConfig =
                NetSuiteEndpoint.createConnectionConfig(getConnectionProperties());
        assertApiVersion(connectionConfig.getApiVersion());
        this.endpoint = new NetSuiteEndpoint(clientFactory, connectionConfig);
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        try {
            endpoint.connect();

            return ValidationResult.OK;
        } catch (NetSuiteException e) {
            return exceptionToValidationResult(e);
        }
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        try {
            NetSuiteDatasetRuntime dataSetRuntime = new NetSuiteDatasetRuntimeImpl(
                    getClientService().getMetaDataSource());
            return dataSetRuntime.getRecordTypes();
        } catch (NetSuiteException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        try {
            NetSuiteDatasetRuntime dataSetRuntime = new NetSuiteDatasetRuntimeImpl(
                    getClientService().getMetaDataSource());
            return dataSetRuntime.getSchema(schemaName);
        } catch (NetSuiteException e) {
            throw new IOException(e);
        }
    }

    /**
     * Get connection properties used by this source/sink.
     *
     * @return connection properties
     */
    public NetSuiteConnectionProperties getConnectionProperties() {
        return properties.getConnectionProperties();
    }

    /**
     * Get properties used by this source/sink.
     *
     * @return properties
     */
    public NetSuiteProvideConnectionProperties getProperties() {
        return properties;
    }

    /**
     * Get NetSuite client used by this source/sink.
     *
     * @return client
     * @throws NetSuiteException if an error occurs during connecting
     */
    public NetSuiteClientService<?> getClientService() throws NetSuiteException {
        return endpoint.getClientService();
    }

    /**
     * Assert that given version of NetSuite API match the version implemented by runtime.
     *
     * @param apiVersion version to be checked
     */
    protected void assertApiVersion(final NetSuiteVersion apiVersion) {
        if (!clientFactory.getApiVersion().isSameMajor(apiVersion)) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.runtimeVersionMismatch",
                            apiVersion.getAsString("."),
                            clientFactory.getApiVersion().getMajorAsString(".")));
        }
    }
}
