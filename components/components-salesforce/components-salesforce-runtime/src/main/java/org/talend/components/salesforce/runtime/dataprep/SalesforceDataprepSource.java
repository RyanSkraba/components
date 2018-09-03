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
package org.talend.components.salesforce.runtime.dataprep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.common.SalesforceRuntimeSourceOrSink;
import org.talend.components.salesforce.dataprep.SalesforceInputProperties;
import org.talend.components.salesforce.dataset.SalesforceDatasetProperties;
import org.talend.components.salesforce.datastore.SalesforceDatastoreProperties;
import org.talend.components.salesforce.runtime.common.ConnectionHolder;
import org.talend.components.salesforce.runtime.common.SalesforceRuntimeCommon;
import org.talend.components.salesforce.schema.SalesforceSchemaHelper;
import org.talend.components.salesforce.soql.FieldDescription;
import org.talend.components.salesforce.soql.SoqlQuery;
import org.talend.daikon.NamedThing;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.properties.ValidationResult;

import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.SessionRenewer;

public class SalesforceDataprepSource
        implements BoundedSource, SalesforceRuntimeSourceOrSink, SalesforceSchemaHelper<Schema> {

    private static final long serialVersionUID = 1930140572051028338L;

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceDataprepSource.class);

    private static final int DEFAULT_TIMEOUT = 60000;

    private SalesforceInputProperties properties;

    private SalesforceDatasetProperties dataset;

    private SalesforceDatastoreProperties datastore;

    private String endpoint = SalesforceConnectionProperties.URL;

    private transient ConnectionHolder connectionHolder;

    private int timeout = DEFAULT_TIMEOUT;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (SalesforceInputProperties) properties;
        dataset = this.properties.getDatasetProperties();
        datastore = dataset.getDatastoreProperties();

        this.endpoint = datastore.getEndPoint();

        String timeout = datastore.getSalesforceProperties().getProperty("timeout");
        if (timeout != null && !timeout.isEmpty()) {
            this.timeout = Integer.parseInt(timeout);
        }

        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        try {
            getConnectionHolder();
        } catch (IOException ex) {
            return SalesforceRuntimeCommon.exceptionToValidationResult(ex);
        }
        return ValidationResult.OK;
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        List<BoundedSource> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

    @Override
    public BoundedReader createReader(RuntimeContainer container) {
        return new SalesforceBulkQueryReader(container, this, properties);
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return SalesforceRuntimeCommon.getSchemaNames(getConnectionHolder().connection);
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        try {
            DescribeSObjectResult[] describeSObjectResults = new DescribeSObjectResult[0];
            describeSObjectResults = getConnectionHolder().connection.describeSObjects(new String[] { schemaName });
            return SalesforceAvroRegistryString.get().inferSchema(describeSObjectResults[0]);
        } catch (ConnectionException e) {
            throw new IOException(e);
        }
    }

    ConnectionHolder connect() throws IOException {
        SalesforceRuntimeCommon.enableTLSv11AndTLSv12ForJava7();

        final ConnectionHolder ch = new ConnectionHolder();

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(datastore.userId.getValue());
        String password = datastore.password.getValue();
        String securityKey = datastore.securityKey.getValue();
        if (!StringUtils.isEmpty(securityKey)) {
            password = password + securityKey;
        }
        config.setPassword(password);

        // Notes on how to test this
        // http://thysmichels.com/2014/02/15/salesforce-wsc-partner-connection-session-renew-when-session-timeout/
        config.setSessionRenewer(new SessionRenewer() {

            @Override
            public SessionRenewalHeader renewSession(ConnectorConfig connectorConfig) throws ConnectionException {
                LOG.debug("renewing session...");
                SessionRenewalHeader header = new SessionRenewalHeader();
                connectorConfig.setSessionId(null);
                PartnerConnection connection = doConnection(connectorConfig);
                // update the connection session header
                ch.connection.setSessionHeader(connection.getSessionHeader().getSessionId());

                header.name = new QName("urn:partner.soap.sforce.com", "SessionHeader");
                header.headerElement = connection.getSessionHeader();
                LOG.debug("session renewed!");
                return header;
            }
        });

        config.setConnectionTimeout(timeout);
        config.setCompression(true);// This should only be false when doing debugging.
        config.setUseChunkedPost(true);
        config.setValidateSchema(false);

        try {
            ch.connection = doConnection(config);
            ch.bulkConnection = connectBulk(ch.connection.getConfig());
            return ch;
        } catch (ConnectionException e) {
            throw new IOException(e);
        }
    }

    protected BulkConnection connectBulk(ConnectorConfig config) throws ComponentException {
        /*
         * When PartnerConnection is instantiated, a login is implicitly executed and, if successful, a valid session is
         * stored in the ConnectorConfig instance. Use this key to initialize a BulkConnection:
         */
        ConnectorConfig bulkConfig = new ConnectorConfig();
        bulkConfig.setSessionId(config.getSessionId());
        // For session renew
        bulkConfig.setSessionRenewer(config.getSessionRenewer());
        bulkConfig.setUsername(config.getUsername());
        bulkConfig.setPassword(config.getPassword());
        /*
         * The endpoint for the Bulk API service is the same as for the normal SOAP uri until the /Soap/ part. From here
         * it's '/async/versionNumber'
         */
        String soapEndpoint = config.getServiceEndpoint();
        // set it by a default property file

        // Service endpoint should be like this:
        // https://ap1.salesforce.com/services/Soap/u/37.0/00D90000000eSq3
        String apiVersion = soapEndpoint.substring(soapEndpoint.lastIndexOf("/services/Soap/u/") + 17);
        apiVersion = apiVersion.substring(0, apiVersion.indexOf("/"));
        String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + apiVersion;
        bulkConfig.setRestEndpoint(restEndpoint);
        bulkConfig.setCompression(true);// This should only be false when doing debugging.
        bulkConfig.setTraceMessage(false);
        bulkConfig.setValidateSchema(false);
        try {
            return new BulkConnection(bulkConfig);
        } catch (AsyncApiException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public Schema guessSchema(String soqlQuery) throws IOException {
        SoqlQuery query = SoqlQuery.getInstance();
        query.init(soqlQuery);

        List<FieldDescription> fieldDescriptions = query.getFieldDescriptions();
        String drivingEntityName = query.getDrivingEntityName();

        DescribeSObjectResult describeSObjectResult = null;

        try {
            describeSObjectResult = getConnectionHolder().connection.describeSObject(drivingEntityName);
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }

        Schema runtimeSchema = SalesforceAvroRegistryString.get().inferSchema(describeSObjectResult);

        Schema newSchema = Schema.createRecord("GuessedSchema", runtimeSchema.getDoc(), runtimeSchema.getNamespace(),
                runtimeSchema.isError());
        List<Schema.Field> newFieldList = new ArrayList<>();

        for (FieldDescription fieldDescription : fieldDescriptions) {
            Schema.Field runtimeField = runtimeSchema.getField(fieldDescription.getFullName());

            if (runtimeField != null) {
                Schema.Field newField = new Schema.Field(runtimeField.name(), runtimeField.schema(), runtimeField.doc(),
                        runtimeField.defaultVal(), runtimeField.order());
                newField.getObjectProps().putAll(runtimeField.getObjectProps());
                for (Map.Entry<String, Object> entry : runtimeField.getObjectProps().entrySet()) {
                    newField.addProp(entry.getKey(), entry.getValue());
                }
                newFieldList.add(newField);
            } else {
                Schema.Field newField =
                        new Schema.Field(fieldDescription.getFullName(), AvroUtils._string(), null, (String) null);
                newFieldList.add(newField);
            }
        }

        newSchema.setFields(newFieldList);
        for (Map.Entry<String, Object> entry : runtimeSchema.getObjectProps().entrySet()) {
            newSchema.addProp(entry.getKey(), entry.getValue());
        }

        return newSchema;
    }

    private PartnerConnection doConnection(ConnectorConfig config) throws ConnectionException {
        config.setAuthEndpoint(endpoint);
        PartnerConnection connection = new PartnerConnection(config);
        return connection;
    }

    public String guessQuery(Schema schema, String entityName) {
        // not necessary for dataprep
        return null;
    }

    public ConnectionHolder getConnectionHolder() throws IOException {
        if (connectionHolder == null) {
            connectionHolder = connect();
            return connectionHolder;
        }
        return connectionHolder;
    }
}
