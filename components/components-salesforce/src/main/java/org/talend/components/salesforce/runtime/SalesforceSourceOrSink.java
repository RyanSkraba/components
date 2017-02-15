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
package org.talend.components.salesforce.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.runtime.ProxyPropertiesRuntimeHelper;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.components.salesforce.SalesforceProvideConnectionProperties;
import org.talend.components.salesforce.connection.oauth.SalesforceOAuthConnection;
import org.talend.components.salesforce.schema.SalesforceSchemaHelper;
import org.talend.components.salesforce.soql.FieldDescription;
import org.talend.components.salesforce.soql.SoqlQuery;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.soap.partner.*;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.SessionRenewer;

public class SalesforceSourceOrSink implements SourceOrSink, SalesforceSchemaHelper<Schema> {

    private transient static final Logger LOG = LoggerFactory.getLogger(SalesforceSourceOrSink.class);

    private transient static final Schema DEFAULT_GUESS_SCHEMA_TYPE = AvroUtils._string();

    protected static final String API_VERSION = "34.0";

    protected SalesforceProvideConnectionProperties properties;

    protected static final String KEY_CONNECTION = "Connection";

    protected static final String SESSION_ID = "SESSION_ID";

    protected static final String SERVICE_ENDPOINT = "SERVICE_ENDPOINT";

    protected static final String MAX_VALID_SECONDS = "MAX_VALID_SECONDS";

    protected static final String SESSION_FILE_PREFX = "sessionIDFile_";

    private static final String EXCEPTION_MESSAGE = "Failed!";

    private String sessionFilePath;

    private String sessionId;

    private String serviceEndPoint;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (SalesforceProvideConnectionProperties) properties;
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult vr = new ValidationResult();
        try {
            connect(container);
        } catch (IOException ex) {
            return exceptionToValidationResult(ex);
        }
        return vr;
    }

    protected static ValidationResult exceptionToValidationResult(Exception ex) {
        ValidationResult vr = new ValidationResult();
        // FIXME - do a better job here
        vr.setMessage(ex.getMessage());
        vr.setStatus(ValidationResult.Result.ERROR);
        return vr;
    }

    public static ValidationResult validateConnection(SalesforceProvideConnectionProperties properties) {
        ClassLoader classLoader = SalesforceDefinition.class.getClassLoader();
        RuntimeInfo runtimeInfo = SalesforceDefinition.getCommonRuntimeInfo(classLoader, SalesforceSourceOrSink.class);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClassWithCurrentJVMProperties(runtimeInfo,
                classLoader)) {
            SalesforceSourceOrSink ss = (SalesforceSourceOrSink) sandboxedInstance.getInstance();
            ss.initialize(null, (ComponentProperties) properties);
            return ss.validate(null);
        }
    }

    /**
     * If referenceComponentId is not null, it should return the reference connection properties
     */
    public SalesforceConnectionProperties getConnectionProperties() {
        SalesforceConnectionProperties connectionProperties = properties.getConnectionProperties();
        if (connectionProperties.getReferencedComponentId() != null) {
            connectionProperties = connectionProperties.getReferencedConnectionProperties();
        }
        return connectionProperties;
    }

    protected BulkConnection connectBulk(ConnectorConfig config) throws ComponentException {
        final SalesforceConnectionProperties connProps = getConnectionProperties();
        /*
         * When PartnerConnection is instantiated, a login is implicitly executed and, if successful, a valid session is
         * stored in the ConnectorConfig instance. Use this key to initialize a BulkConnection:
         */
        ConnectorConfig bulkConfig = new ConnectorConfig();
        setProxy(bulkConfig);
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
        String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + API_VERSION;
        bulkConfig.setRestEndpoint(restEndpoint);
        // This should only be false when doing debugging.
        bulkConfig.setCompression(connProps.needCompression.getValue());
        bulkConfig.setTraceMessage(connProps.httpTraceMessage.getValue());
        bulkConfig.setValidateSchema(false);
        try {
            return new BulkConnection(bulkConfig);
        } catch (AsyncApiException e) {
            throw new ComponentException(e);
        }
    }

    /**
     * Create a connection with specified connector configuration
     * 
     * @param config connector configuration with endpoint/userId/password
     * @param openNewSession whether need to create new session
     * @return PartnerConnection object with correct session id
     * @throws ConnectionException create connection fails
     */
    protected PartnerConnection doConnection(ConnectorConfig config, boolean openNewSession) throws ConnectionException {
        if (!openNewSession) {
            config.setSessionId(this.sessionId);
            config.setServiceEndpoint(this.serviceEndPoint);
        } else {
            SalesforceConnectionProperties connProps = getConnectionProperties();
            String endpoint = connProps.endpoint.getStringValue();
            endpoint = StringUtils.strip(endpoint, "\"");
            if (SalesforceConnectionProperties.LoginType.OAuth.equals(connProps.loginType.getValue())) {
                SalesforceOAuthConnection oauthConnection = new SalesforceOAuthConnection(connProps.oauth, endpoint, API_VERSION);
                oauthConnection.login(config);
            } else {
                config.setAuthEndpoint(endpoint);
            }
        }
        PartnerConnection connection = new PartnerConnection(config);
        if (openNewSession && isReuseSession()) {
            this.sessionId = config.getSessionId();
            this.serviceEndPoint = config.getServiceEndpoint();
            if (this.sessionId != null && this.serviceEndPoint != null) {
                // update session file with current sessionId/serviceEndPoint
                setupSessionProperties(connection);
            }
        }
        return connection;
    }

    class ConnectionHolder {

        PartnerConnection connection;

        BulkConnection bulkConnection;
    }

    protected ConnectionHolder connect(RuntimeContainer container) throws IOException {
        enableTLSv11AndTLSv12ForJava7();

        final ConnectionHolder ch = new ConnectionHolder();
        SalesforceConnectionProperties connProps = properties.getConnectionProperties();
        String refComponentId = connProps.getReferencedComponentId();
        Object sharedConn = null;
        // Using another component's connection
        if (refComponentId != null) {
            // In a runtime container
            if (container != null) {
                sharedConn = container.getComponentData(refComponentId, KEY_CONNECTION);
                if (sharedConn != null) {
                    if (sharedConn instanceof PartnerConnection) {
                        ch.connection = (PartnerConnection) sharedConn;
                    } else if (sharedConn instanceof BulkConnection) {
                        ch.bulkConnection = (BulkConnection) sharedConn;
                    }
                    return ch;
                }
                throw new IOException("Referenced component: " + refComponentId + " not connected");
            }
            // Design time
            connProps = connProps.getReferencedConnectionProperties();
        }

        // FIXME add back reffed connection

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(StringUtils.strip(connProps.userPassword.userId.getStringValue(), "\""));
        String password = StringUtils.strip(connProps.userPassword.password.getStringValue(), "\"");
        String securityKey = StringUtils.strip(connProps.userPassword.securityKey.getStringValue(), "\"");
        if (!StringUtils.isEmpty(securityKey)) {
            password = password + securityKey;
        }
        config.setPassword(password);

        setProxy(config);

        // Notes on how to test this
        // http://thysmichels.com/2014/02/15/salesforce-wsc-partner-connection-session-renew-when-session-timeout/

        config.setSessionRenewer(new SessionRenewer() {

            @Override
            public SessionRenewalHeader renewSession(ConnectorConfig connectorConfig) throws ConnectionException {
                LOG.debug("renewing session...");
                SessionRenewalHeader header = new SessionRenewalHeader();
                connectorConfig.setSessionId(null);
                PartnerConnection connection = doConnection(connectorConfig, true);
                // update the connection session header
                ch.connection.setSessionHeader(connection.getSessionHeader().getSessionId());

                header.name = new QName("urn:partner.soap.sforce.com", "SessionHeader");
                header.headerElement = connection.getSessionHeader();
                LOG.debug("session renewed!");
                return header;
            }
        });

        if (connProps.timeout.getValue() > 0) {
            config.setConnectionTimeout(connProps.timeout.getValue());
        }
        config.setCompression(connProps.needCompression.getValue());
        if (false) {
            config.setTraceMessage(true);
        }
        config.setUseChunkedPost(connProps.httpChunked.getValue());
        config.setValidateSchema(false);

        try {
            // Get session from session file or new connection
            if (isReuseSession()) {
                Properties properties = getSessionProperties();
                if (properties != null) {
                    this.sessionId = properties.getProperty(SESSION_ID);
                    this.serviceEndPoint = properties.getProperty(SERVICE_ENDPOINT);
                }
            }
            if (this.sessionId != null && this.serviceEndPoint != null) {
                ch.connection = doConnection(config, false);
            } else {
                ch.connection = doConnection(config, true);
            }

            if (ch.connection != null) {
                String clientId = connProps.clientId.getStringValue();
                if (clientId != null) {
                    // Need the test.
                    ch.connection.setCallOptions(clientId, null);
                }
            }
            if (connProps.bulkConnection.getValue()) {
                ch.bulkConnection = connectBulk(ch.connection.getConfig());
                sharedConn = ch.bulkConnection;
            } else {
                sharedConn = ch.connection;
            }
            if (container != null) {
                container.setComponentData(container.getCurrentComponentId(), KEY_CONNECTION, sharedConn);
            }
            return ch;
        } catch (ConnectionException e) {
            throw new IOException(e);
        }
    }

    private void enableTLSv11AndTLSv12ForJava7() {
        String version = System.getProperty("java.version");
        if (version != null && version.startsWith("1.7")) {
            System.setProperty("https.protocols", "TLSv1.1,TLSv1.2");
        }
    }

    public static List<NamedThing> getSchemaNames(RuntimeContainer container, SalesforceProvideConnectionProperties properties)
            throws IOException {
        ClassLoader classLoader = SalesforceDefinition.class.getClassLoader();
        RuntimeInfo runtimeInfo = SalesforceDefinition.getCommonRuntimeInfo(classLoader, SalesforceSourceOrSink.class);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClassWithCurrentJVMProperties(runtimeInfo,
                classLoader)) {
            SalesforceSourceOrSink ss = (SalesforceSourceOrSink) sandboxedInstance.getInstance();
            ss.initialize(null, (ComponentProperties) properties);
            try {
                ss.connect(container);
                return ss.getSchemaNames(container);
            } catch (Exception ex) {
                throw new ComponentException(exceptionToValidationResult(ex));
            }
        }
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return getSchemaNames(connect(container).connection);
    }

    protected List<NamedThing> getSchemaNames(PartnerConnection connection) throws IOException {
        List<NamedThing> returnList = new ArrayList<>();
        DescribeGlobalResult result = null;
        try {
            result = connection.describeGlobal();
        } catch (ConnectionException e) {
            throw new ComponentException(e);
        }
        DescribeGlobalSObjectResult[] objects = result.getSobjects();
        for (DescribeGlobalSObjectResult obj : objects) {
            LOG.debug("module label: " + obj.getLabel() + " name: " + obj.getName());
            returnList.add(new SimpleNamedThing(obj.getName(), obj.getLabel()));
        }
        return returnList;
    }

    public static Schema getSchema(RuntimeContainer container, SalesforceProvideConnectionProperties properties, String module)
            throws IOException {
        ClassLoader classLoader = SalesforceDefinition.class.getClassLoader();
        RuntimeInfo runtimeInfo = SalesforceDefinition.getCommonRuntimeInfo(classLoader, SalesforceSourceOrSink.class);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClassWithCurrentJVMProperties(runtimeInfo,
                classLoader)) {
            SalesforceSourceOrSink ss = (SalesforceSourceOrSink) sandboxedInstance.getInstance();
            ss.initialize(null, (ComponentProperties) properties);
            PartnerConnection connection = null;
            try {
                connection = ss.connect(container).connection;
            } catch (IOException ex) {
                throw new ComponentException(exceptionToValidationResult(ex));
            }
            return ss.getSchema(connection, module);
        }
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return getSchema(connect(container).connection, schemaName);
    }

    protected Schema getSchema(PartnerConnection connection, String module) throws IOException {
        try {
            DescribeSObjectResult[] describeSObjectResults = new DescribeSObjectResult[0];
            describeSObjectResults = connection.describeSObjects(new String[] { module });
            return SalesforceAvroRegistry.get().inferSchema(describeSObjectResults[0]);
        } catch (ConnectionException e) {
            throw new IOException(e);
        }
    }

    private void setProxy(ConnectorConfig config) {
        final ProxyPropertiesRuntimeHelper proxyHelper = new ProxyPropertiesRuntimeHelper(
                properties.getConnectionProperties().proxy);

        if (proxyHelper.getProxyHost() != null) {
            if (proxyHelper.getSocketProxy() != null) {
                config.setProxy(proxyHelper.getSocketProxy());
            } else {
                config.setProxy(proxyHelper.getProxyHost(), Integer.parseInt(proxyHelper.getProxyPort()));
            }

            if (proxyHelper.getProxyUser() != null && proxyHelper.getProxyUser().length() > 0) {
                config.setProxyUsername(proxyHelper.getProxyUser());

                if (proxyHelper.getProxyPwd() != null && proxyHelper.getProxyPwd().length() > 0) {
                    config.setProxyPassword(proxyHelper.getProxyPwd());

                    Authenticator.setDefault(new Authenticator() {

                        @Override
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(proxyHelper.getProxyUser(),
                                    proxyHelper.getProxyPwd().toCharArray());
                        }

                    });
                }
            }
        }
    }

    /**
     * This is for Buck connection session renew It can't called automatically with current force-wsc api
     */
    protected void renewSession(ConnectorConfig config) throws ConnectionException {
        LOG.debug("renew session bulk connection");
        SessionRenewer renewer = config.getSessionRenewer();
        renewer.renewSession(config);
    }

    /**
     * Get the session properties instance
     * 
     * @return session properties
     * @throws ConnectionException connection disable during get user information
     */
    protected Properties getSessionProperties() throws ConnectionException {
        File sessionFile = new File(sessionFilePath);
        try {
            if (sessionFile.exists()) {
                FileInputStream sessionInput = new FileInputStream(sessionFile);
                try {
                    Properties sessionProp = new Properties();
                    sessionProp.load(sessionInput);
                    int maxValidSeconds = Integer.valueOf(sessionProp.getProperty(MAX_VALID_SECONDS));
                    // Check whether the session is timeout
                    if (maxValidSeconds > ((System.currentTimeMillis() - sessionFile.lastModified()) / 1000)) {
                        return sessionProp;
                    }
                } finally {
                    sessionInput.close();
                }
            }
        } catch (IOException e) {
            throw new ConnectionException("Reuse session fails!", e);
        }
        return null;
    }

    /**
     * Save session to target file
     * 
     * @param connection which you want to saved information
     * @throws IOException error during create or write session file
     * @throws ConnectionException connection disable during get user information
     */
    protected void setupSessionProperties(PartnerConnection connection) throws ConnectionException {
        try {
            GetUserInfoResult result = connection.getUserInfo();
            File sessionFile = new File(sessionFilePath);
            if (!sessionFile.exists()) {
                File parentPath = sessionFile.getParentFile();
                if (!parentPath.exists()) {
                    parentPath.mkdirs();
                }
                sessionFile.createNewFile();
            }
            FileOutputStream sessionOutput = null;
            sessionOutput = new FileOutputStream(sessionFile);
            Properties sessionProp = new Properties();
            sessionProp.setProperty(SESSION_ID, sessionId);
            sessionProp.setProperty(SERVICE_ENDPOINT, serviceEndPoint);
            sessionProp.setProperty(MAX_VALID_SECONDS, String.valueOf(result.getSessionSecondsValid()));
            try {
                sessionProp.store(sessionOutput, null);
            } finally {
                sessionOutput.close();
            }
        } catch (IOException e) {
            throw new ConnectionException("Reuse session fails!", e);
        }
    }

    /**
     * Whether reuse session available
     */
    protected boolean isReuseSession() {
        SalesforceConnectionProperties connectionProperties = getConnectionProperties();
        sessionFilePath = connectionProperties.sessionDirectory.getValue() + "/" + SESSION_FILE_PREFX
                + connectionProperties.userPassword.userId.getValue();
        return (SalesforceConnectionProperties.LoginType.Basic == connectionProperties.loginType.getValue())
                && connectionProperties.reuseSession.getValue() && !StringUtils.isEmpty(sessionFilePath);
    }

    @Override
    public Schema guessSchema(String soqlQuery) throws IOException, ConnectionException {
        SoqlQuery query = SoqlQuery.getInstance();
        query.init(soqlQuery);

        List<FieldDescription> fieldDescriptions = query.getFieldDescriptions();
        String drivingEntityName = query.getDrivingEntityName();

        SchemaBuilder.FieldAssembler fieldAssembler = SchemaBuilder.record("GuessedSchema").fields();

        DescribeSObjectResult describeSObjectResult = connect(null).connection.describeSObject(drivingEntityName);

        Schema entitySchema = SalesforceAvroRegistry.get().inferSchema(describeSObjectResult);

        for (FieldDescription fieldDescription : fieldDescriptions) {
            Schema.Field schemaField = entitySchema.getField(fieldDescription.getSimpleName());
            Schema fieldType = null;
            if (schemaField != null) {
                fieldType = schemaField.schema();
            } else {
                fieldType = DEFAULT_GUESS_SCHEMA_TYPE;
            }
            fieldAssembler.name(fieldDescription.getFullName()).type(fieldType).noDefault();
        }

        return (Schema) fieldAssembler.endRecord();
    }

}
