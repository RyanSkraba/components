// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.adaptor.Adaptor;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.connection.oauth.SalesforceOAuthConnection;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.PropertyFactory;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.schema.Schema;
import org.talend.daikon.schema.SchemaElement;
import org.talend.daikon.schema.SchemaFactory;

import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SessionHeader_element;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.SessionRenewer;

public class SalesforceSourceOrSink implements SourceOrSink {

    private transient static final Logger LOG = LoggerFactory.getLogger(SalesforceSourceOrSink.class);

    protected static final String API_VERSION = "34.0";

    protected ComponentProperties properties;

    public SalesforceSourceOrSink() {
    }

    @Override
    public void initialize(Adaptor adaptor, ComponentProperties properties) {
        this.properties = properties;
    }

    @Override
    public ValidationResult validate(Adaptor adaptor) {
        ValidationResult vr = new ValidationResult();
        try {
            connect();
        } catch (ComponentException ex) {
            // FIXME - do a better job here
            vr.setMessage(ex.getMessage());
            vr.setStatus(ValidationResult.Result.ERROR);
            return vr;
        }
        return vr;
    }

    protected SalesforceConnectionProperties getConnectionProperties() {
        if (properties instanceof SalesforceConnectionProperties)
            return (SalesforceConnectionProperties) properties;
        return ((SalesforceConnectionModuleProperties) properties).connection;
    }

    protected BulkConnection connectBulk(ConnectorConfig config) throws ComponentException {
        /*
         * When PartnerConnection is instantiated, a login is implicitly executed and, if successful, a valid session is
         * stored in the ConnectorConfig instance. Use this key to initialize a BulkConnection:
         */
        ConnectorConfig bulkConfig = new ConnectorConfig();
        bulkConfig.setSessionId(config.getSessionId());
        /*
         * The endpoint for the Bulk API service is the same as for the normal SOAP uri until the /Soap/ part. From here
         * it's '/async/versionNumber'
         */
        String soapEndpoint = config.getServiceEndpoint();
        String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + API_VERSION;
        bulkConfig.setRestEndpoint(restEndpoint);
        // This should only be false when doing debugging.
        bulkConfig.setCompression(true);
        bulkConfig.setTraceMessage(false);
        try {
            return new BulkConnection(bulkConfig);
        } catch (AsyncApiException e) {
            throw new ComponentException(e);
        }
    }

    protected PartnerConnection doConnection(ConnectorConfig config) throws ComponentException {
        SalesforceConnectionProperties connProps = getConnectionProperties();
        if (SalesforceConnectionProperties.LOGIN_OAUTH.equals(connProps.loginType.getValue())) {
            SalesforceOAuthConnection oauthConnection = new SalesforceOAuthConnection(connProps.oauth,
                    SalesforceConnectionProperties.OAUTH_URL, API_VERSION);
            oauthConnection.login(config);
        } else {
            config.setAuthEndpoint(SalesforceConnectionProperties.URL);
        }
        PartnerConnection connection;
        try {
            connection = new PartnerConnection(config);
        } catch (ConnectionException e) {
            throw new ComponentException(e);
        }
        return connection;
    }

    class ConnectionHolder {
        PartnerConnection connection;
    }

    protected PartnerConnection connect() throws ComponentException {
        final SalesforceConnectionProperties connProps = getConnectionProperties();
        final ConnectionHolder ch = new ConnectionHolder();

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(StringUtils.strip(connProps.userPassword.userId.getStringValue(), "\""));
        config.setPassword(StringUtils.strip(connProps.userPassword.password.getStringValue(), "\"")
                + StringUtils.strip(connProps.userPassword.securityKey.getStringValue(), "\""));

        // Notes on how to test this
        // http://thysmichels.com/2014/02/15/salesforce-wsc-partner-connection-session-renew-when-session-timeout/

        config.setSessionRenewer(new SessionRenewer() {

            @Override
            public SessionRenewalHeader renewSession(ConnectorConfig connectorConfig) throws ConnectionException {
                SessionRenewalHeader header = new SessionRenewalHeader();
                // FIXME - session id need to be null for trigger the login?
                // connectorConfig.setSessionId(null);
                doConnection(connectorConfig);

                SessionHeader_element h = ch.connection.getSessionHeader();
                // FIXME - one or the other, I have seen both
                // header.name = new QName("urn:partner.soap.sforce.com", "X-SFDC-Session");
                header.name = new QName("urn:partner.soap.sforce.com", "SessionHeader");
                header.headerElement = h.getSessionId();
                return header;
            }
        });

        if (connProps.timeout.getIntValue() > 0) {
            config.setConnectionTimeout(connProps.timeout.getIntValue());
        }
        config.setCompression(connProps.needCompression.getBooleanValue());
        if (false) {
            config.setTraceMessage(true);
        }

        ch.connection = doConnection(config);
        return ch.connection;
    }

    @Override
    public List<NamedThing> getSchemaNames(Adaptor adaptor) throws ComponentException {
        PartnerConnection connection = connect();
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

    public void setupSchemaElement(Field field, SchemaElement element) {
        String type = field.getType().toString();
        if (type.equals("boolean")) { //$NON-NLS-1$
            element.setType(SchemaElement.Type.BOOLEAN);
        } else if (type.equals("int")) { //$NON-NLS-1$
            element.setType(SchemaElement.Type.INT);
        } else if (type.equals("date")) { //$NON-NLS-1$
            element.setType(SchemaElement.Type.DATE);
            element.setPattern("\"yyyy-MM-dd\""); //$NON-NLS-1$
        } else if (type.equals("datetime")) { //$NON-NLS-1$
            element.setType(SchemaElement.Type.DATETIME);
            element.setPattern("\"yyyy-MM-dd\'T\'HH:mm:ss\'.000Z\'\""); //$NON-NLS-1$
        } else if (type.equals("double")) { //$NON-NLS-1$
            element.setType(SchemaElement.Type.DOUBLE);
        } else if (type.equals("currency")) { //$NON-NLS-1$
            element.setType(SchemaElement.Type.DECIMAL);
        }
        element.setNullable(field.getNillable());

        if (element.getType() == SchemaElement.Type.STRING) {
            element.setSize(field.getLength());
            element.setPrecision(field.getPrecision());
        } else {
            element.setSize(field.getPrecision());
            element.setPrecision(field.getScale());
        }
        element.setDefaultValue(field.getDefaultValueFormula());
    }

    @Override
    public Schema getSchema(Adaptor adaptor, String module) throws ComponentException {
        PartnerConnection connection = connect();
        Schema schema = SchemaFactory.newSchema();
        SchemaElement root = SchemaFactory.newSchemaElement("Root");
        schema.setRoot(root);

        DescribeSObjectResult[] describeSObjectResults = new DescribeSObjectResult[0];
        try {
            describeSObjectResults = connection.describeSObjects(new String[] { module });
        } catch (ConnectionException e) {
            throw new ComponentException(e);
        }
        Field fields[] = describeSObjectResults[0].getFields();
        for (Field field : fields) {
            SchemaElement child = PropertyFactory.newProperty(field.getName());
            setupSchemaElement(field, child);
            root.addChild(child);
        }
        return schema;
    }

}
