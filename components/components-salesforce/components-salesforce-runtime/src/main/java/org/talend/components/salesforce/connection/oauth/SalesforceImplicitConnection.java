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
package org.talend.components.salesforce.connection.oauth;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.oauth.Oauth2ImplicitClient;
import org.talend.components.common.oauth.properties.Oauth2ImplicitFlowProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

/**
 * This use the Oauth2 implicit flow that need a user interaction.
 * This connection get and store a refresh token after user manual consent and store it in a local file to reuse it for future
 * authentication
 */
public class SalesforceImplicitConnection {

    private final Logger LOGGER = LoggerFactory.getLogger(SalesforceImplicitConnection.class.getName());

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(SalesforceImplicitConnection.class);

    private static final String REFRESHTOKEN_KEY = "refreshtoken"; //$NON-NLS-1$

    private String url;

    private Oauth2ImplicitFlowProperties implicitFlowProp;

    public SalesforceImplicitConnection(Oauth2ImplicitFlowProperties oauthProperties, String url) {
        this.implicitFlowProp = oauthProperties;
        this.url = url;
    }

    /**
     * Try to get a token using the stored refresh token. if no refresh token. we try to get it and stored in a local file
     */
    public SalesforceOAuthAccessTokenResponse getToken() {
        SalesforceOAuthAccessTokenResponse token = null;
        // 1. if tokenFile exist, try refresh token
        String tokenFilePath = implicitFlowProp.tokenFile.getStringValue();
        if (tokenFilePath != null) {
            token = tryTokenRefresh(tokenFilePath, implicitFlowProp.clientId.getStringValue(),
                    implicitFlowProp.clientSecret.getStringValue());
        }
        // 2. try to auth if session_id can't be retrieved
        if (token == null) {
            token = doAuthenticate(implicitFlowProp.clientId.getStringValue(), implicitFlowProp.clientSecret.getStringValue(),
                    implicitFlowProp.callbackHost.getStringValue(), implicitFlowProp.callbackPort.getValue());
        }
        // 3.if refresh token & tokenFile exist, store
        if (token != null && token.getRefreshToken() != null && tokenFilePath != null) {
            storeToken(token, tokenFilePath);
        }

        return token;
    }

    /**
     * Try a token refresh from the file passed in the parameter.
     * 
     * @param tokenFilePath : path to the file that contain the REFRESH TOKEN
     * @param clientSecret
     * @param clientId
     * @return a valid token if refresh is done correctly, <code>null<code> otherwise
     */
    private SalesforceOAuthAccessTokenResponse tryTokenRefresh(String tokenFilePath, String clientId, String clientSecret) {
        File tokenFile = new File(tokenFilePath);
        if (tokenFile.exists()) {
            try (FileInputStream inputStream = new FileInputStream(tokenFilePath)) {
                Properties prop = new Properties();
                prop.load(inputStream);
                String storedRefreshToken = (String) prop.get(REFRESHTOKEN_KEY);
                if (storedRefreshToken != null) {
                    Oauth2ImplicitClient oauthClient = new Oauth2ImplicitClient.RefreshTokenBuilder(new URL(url + "/token"),
                            clientId, clientSecret).setRefreshToken(storedRefreshToken).build();
                    return oauthClient.getToken(SalesforceOAuthAccessTokenResponse.class);
                }

            } catch (FileNotFoundException e) {// ignored exception
                LOGGER.warn(messages.getMessage("warn.notFoundRefreshToken"), e);
            } catch (IOException e) {// ignored exception
                LOGGER.warn(messages.getMessage("warn.cantRefreshToken"), e);
            }
        }

        return null;
    }

    /**
     * Get the authentication token from salesforce
     * 
     * @param callBackPort
     * @param callBackHost
     * @param clientSecret
     * @param clientId
     * 
     * @return a valid token
     * @throws RuntimeException if authentication failed, or the returned token is <code>null</code>
     */
    private SalesforceOAuthAccessTokenResponse doAuthenticate(String clientId, String clientSecret, String callbackHost,
            Integer callbackPort) {
        try {
            Oauth2ImplicitClient oauthClient = new Oauth2ImplicitClient.AuthorizationCodeBuilder(new URL(url + "/token"), //$NON-NLS-1$
                    clientId, clientSecret).setAuthorizationLocation(new URL(url + "/authorize")) //$NON-NLS-1$
                            .setCallbackURL(new URL("https://" + callbackHost + ":" + callbackPort)).setResponseType("code")
                            .build();
            SalesforceOAuthAccessTokenResponse token = oauthClient.getToken(SalesforceOAuthAccessTokenResponse.class);
            if (token == null) {
                throw new RuntimeException(messages.getMessage("err.nullToken"));
            }

            return token;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Store the token to a file
     * 
     * @param token : valid token that contain the refresh token
     * @param tokenFilePath : path to the file where the refresh token will be stored
     * 
     * @throws RuntimeException if the refresh token can't be stored
     */
    private void storeToken(SalesforceOAuthAccessTokenResponse token, String tokenFilePath) {
        File tokenFile = new File(tokenFilePath);
        if (!tokenFile.exists()) {
            try {
                tokenFile.getParentFile().mkdirs();
                tokenFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(messages.getMessage("err.tokenFileCreationFailed"), e);
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(tokenFile)) {
            Properties prop = new Properties();
            prop.setProperty(REFRESHTOKEN_KEY, token.getRefreshToken());
            prop.store(outputStream, null);
        } catch (IOException e) {
            throw new RuntimeException(messages.getMessage("err.unexpectedTokenFileCreationError"), e);
        }

    }

}
