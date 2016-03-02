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
package org.talend.components.salesforce.runtime.tmp;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.util.UnshardedInput;
import org.talend.components.common.oauth.OauthProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.connection.oauth.SalesforceOAuthConnection;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SessionHeader_element;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.SessionRenewer;

/**
 * 
 */
public abstract class SalesforceUnshardedInputBase implements UnshardedInput<IndexedRecord> {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    protected static final Logger LOGGER = LoggerFactory.getLogger(SalesforceUnshardedInputBase.class);

    protected static final String API_VERSION = "34.0";

    private static final String OAUTH = "oauth";

    private final String username;

    private final String password;

    private final int timeout;

    private final boolean needCompression;

    private final String loginType;

    private final String clientId;

    private final String clientSecret;

    private final String callbackHost;

    private final int callbackPort;

    private final String tokenFile;

    protected transient PartnerConnection connection;

    protected transient QueryResult inputResult;

    private transient SObject[] inputRecords;

    private transient int inputRecordsIndex;

    public SalesforceUnshardedInputBase(String username, String password, int timeout, boolean needCompression, String loginType,
            String clientId, String clientSecret, String callbackHost, int callbackPort, String tokenFile) {
        this.username = username;
        this.password = password;
        this.timeout = timeout;
        this.needCompression = needCompression;
        this.loginType = loginType;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.callbackHost = callbackHost;
        this.callbackPort = callbackPort;
        this.tokenFile = tokenFile;
    }

    public SalesforceUnshardedInputBase(SalesforceConnectionProperties connProps) {
        this.username = connProps.userPassword.userId.getStringValue();
        this.password = connProps.userPassword.password.getStringValue() + connProps.userPassword.securityKey.getStringValue();
        this.timeout = connProps.timeout.getIntValue();
        this.needCompression = connProps.needCompression.getBooleanValue();
        this.loginType = connProps.loginType.getStringValue();
        this.clientId = connProps.oauth.clientId.getStringValue();
        this.clientSecret = connProps.oauth.clientSecret.getStringValue();
        this.callbackHost = connProps.oauth.callbackHost.getStringValue();
        this.callbackPort = connProps.oauth.callbackPort.getIntValue();
        this.tokenFile = connProps.oauth.tokenFile.getStringValue();
    }

    /**
     * @return The list of SObject that should be used as Salesforce data. This must be non-null.
     * @throws IOException If the query could not be executed.
     */
    protected abstract QueryResult executeSalesforceQuery() throws ConnectionException;

    @Override
    public void setup() throws IOException {
        LOGGER.info("logging in: username=" + username + ", timeout=" + timeout + ", needCompression=" + needCompression); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connection = newConnection();
    }

    @Override
    public boolean hasNext() {
        try {
            // Initialize the input records on the first encounter if they are null.
            if (inputResult == null) {
                inputResult = executeSalesforceQuery();
                if (inputResult.getSize() == 0) {
                    return false;
                }
                inputRecords = inputResult.getRecords();
                inputRecordsIndex = 0;
            }

            // Fast return conditions.
            if (inputRecordsIndex < inputRecords.length) {
                return true;
            }
            if (inputResult.isDone()) {
                return false;
            }

            inputResult = connection.queryMore(inputResult.getQueryLocator());
            inputRecords = inputResult.getRecords();
            inputRecordsIndex = 0;
            return inputResult.getSize() > 0;

        } catch (ConnectionException e) {
            // Wrap the exception in an IOException.
            throw new RuntimeException(new IOException(e));
        }
    }

    /**
     * @return the next SObject to be processed. This will only return an object if {@link #hasNext()} has returned
     * true, and can be used to implement the real {@link #next()} iterator method.
     */
    protected SObject nextSObjectToWrap() {
        return inputRecords[inputRecordsIndex++];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        LOGGER.info("close"); //$NON-NLS-1$
    }

    private PartnerConnection newConnection() throws IOException {

        final PartnerConnection[] holder = { null };

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(username);
        config.setPassword(password);

        // Notes on how to test this
        // http://thysmichels.com/2014/02/15/salesforce-wsc-partner-connection-session-renew-when-session-timeout/

        config.setSessionRenewer(new SessionRenewer() {

            @Override
            public SessionRenewalHeader renewSession(ConnectorConfig connectorConfig) throws ConnectionException {
                SessionRenewalHeader header = new SessionRenewalHeader();
                // FIXME - session id need to be null for trigger the login?
                // connectorConfig.setSessionId(null);
                doConnection(connectorConfig);

                SessionHeader_element h = holder[0].getSessionHeader();
                // FIXME - one or the other, I have seen both
                // header.name = new QName("urn:partner.soap.sforce.com", "X-SFDC-Session");
                header.name = new QName("urn:partner.soap.sforce.com", "SessionHeader"); //$NON-NLS-1$ //$NON-NLS-2$
                header.headerElement = h.getSessionId();
                return header;
            }
        });

        if (timeout > 0) {
            config.setConnectionTimeout(timeout);
        }
        config.setCompression(needCompression);
        if (false) {
            config.setTraceMessage(true);
        }

        try {
            holder[0] = doConnection(config);
            return holder[0];
        } catch (ConnectionException e) {
            throw new IOException(e);
        }

    }

    private PartnerConnection doConnection(ConnectorConfig config) throws ConnectionException {
        if (SalesforceConnectionProperties.LOGIN_OAUTH.equals(loginType)) {
            OauthProperties oauth = new OauthProperties(OAUTH);
            oauth.clientId.setValue(clientId);
            oauth.clientSecret.setValue(clientSecret);
            oauth.callbackHost.setValue(callbackHost);
            oauth.callbackPort.setValue(callbackPort);
            oauth.tokenFile.setValue(tokenFile);
            SalesforceOAuthConnection oauthConnection = new SalesforceOAuthConnection(oauth,
                    SalesforceConnectionProperties.OAUTH_URL, API_VERSION);
            oauthConnection.login(config);
        } else {
            config.setAuthEndpoint(SalesforceConnectionProperties.URL);
        }
        return new PartnerConnection(config);
    }
}
