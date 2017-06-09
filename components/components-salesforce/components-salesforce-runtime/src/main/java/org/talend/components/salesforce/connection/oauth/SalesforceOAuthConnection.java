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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.oauth.OauthClient;
import org.talend.components.common.oauth.OauthProperties;

import com.sforce.ws.ConnectorConfig;

public class SalesforceOAuthConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceOAuthConnection.class.getName());

    private static final String REFRESHTOKEN_KEY = "refreshtoken"; //$NON-NLS-1$

    private OauthProperties oauth;

    private String url;

    private String apiVersion;

    public SalesforceOAuthConnection(OauthProperties oauthProperties, String url, String apiVersion) {
        this.oauth = oauthProperties;
        this.url = url;
        this.apiVersion = apiVersion;
    }

    public void login(ConnectorConfig connect) {
        SalesforceOAuthAccessTokenResponse token = null;

        // 1. if tokenFile exist, try refresh token
        String tokenFilePath = oauth.tokenFile.getStringValue();
        if (tokenFilePath != null) {
            token = tryTokenRefresh(tokenFilePath);
        }
        // 2. try to auth if session_id can't be retrieved
        if (token == null) {
            token = doAuthenticate();
        }
        // 3.if refresh token & tokenFile exist, store
        if (token != null && token.getRefreshToken() != null && tokenFilePath != null) {
            storeToken(token, tokenFilePath);
        }

        connect.setServiceEndpoint(getEndpoint(token, apiVersion));
        connect.setSessionId(token.getAccessToken());
    }

    /**
     * Try a token refresh from the file passed in the parameter.
     * 
     * @param tokenFilePath : path to the file that contain the REFRESH TOKEN
     * @return a valid token if refresh is done correctly, <code>null<code> otherwise
     */
    private SalesforceOAuthAccessTokenResponse tryTokenRefresh(String tokenFilePath) {
        File tokenFile = new File(tokenFilePath);
        if (tokenFile.exists()) {
            try (FileInputStream inputStream = new FileInputStream(tokenFilePath)) {
                Properties prop = new Properties();
                prop.load(inputStream);
                String storedRefreshToken = (String) prop.get(REFRESHTOKEN_KEY);
                if (storedRefreshToken != null) {
                    OauthClient oauthClient = new OauthClient.RefreshTokenBuilder(new URL(url + "/token"),
                            oauth.clientId.getStringValue(), oauth.clientSecret.getStringValue())
                                    .setRefreshToken(storedRefreshToken).build();
                    return oauthClient.getToken(SalesforceOAuthAccessTokenResponse.class);
                }

            } catch (FileNotFoundException e) {// ignored exception
                LOGGER.warn("We can't refresh the token, The token file doesn't exist.", e);
            } catch (IOException e) {// ignored exception
                LOGGER.warn("We can't refresh the token, an unexpected error occurred.", e);
            }
        }

        return null;
    }

    /**
     * Get the authentication token from salesforce
     * 
     * @return a valid token
     * @throws RuntimeException if authentication failed, or the returned token is <code>null</code>
     */
    private SalesforceOAuthAccessTokenResponse doAuthenticate() {
        try {
            OauthClient oauthClient = new OauthClient.AuthorizationCodeBuilder(new URL(url + "/token"), //$NON-NLS-1$
                    oauth.clientId.getStringValue(), oauth.clientSecret.getStringValue())
                            .setAuthorizationLocation(new URL(url + "/authorize")) //$NON-NLS-1$
                            .setCallbackURL(new URL(
                                    "https://" + oauth.callbackHost.getStringValue() + ":" + oauth.callbackPort.getValue()))
                            .setResponseType("code").build();
            SalesforceOAuthAccessTokenResponse token = oauthClient.getToken(SalesforceOAuthAccessTokenResponse.class);
            if (token == null) {
                throw new RuntimeException("The returned authentication Token is null, please check your login settings");
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
                new RuntimeException("The token file creation failed, the refresh token can't be stored correctly.", e);
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(tokenFile)) {
            Properties prop = new Properties();
            prop.setProperty(REFRESHTOKEN_KEY, token.getRefreshToken());
            prop.store(outputStream, null);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected error, the refresh token can't be stored correctly.", e);
        }

    }

    private String getEndpoint(SalesforceOAuthAccessTokenResponse token, String version) {
        // if (SalesforceConnectionType.SOAP == connType) {
        return getSOAPEndpoint(token, version);
        // } else if (SalesforceConnectionType.BULK == connType) {
        // return genBulkEndpoint(token, version);
        // }
        // throw new RuntimeException("Unspport connection type"); //$NON-NLS-1$
    }

    // private String genBulkEndpoint(SalesforceOAuthAccessTokenResponse token, String version) {
    // return token.getInstanceURL() + "/services/async/" + version; //$NON-NLS-1$
    // }

    // it's not necessary for bulk, there is another easy way, looking at genBulkEndpoint
    private String getSOAPEndpoint(SalesforceOAuthAccessTokenResponse token, String version) {
        String endpointURL = null;
        BufferedReader reader = null;
        try {
            URLConnection idConn = new URL(token.getID()).openConnection();
            idConn.setRequestProperty("Authorization", token.getTokenType() + " " + token.getAccessToken());
            reader = new BufferedReader(new InputStreamReader(idConn.getInputStream()));
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(reader);
            JSONObject urls = (JSONObject) json.get("urls");
            endpointURL = urls.get("partner").toString().replace("{version}", version);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {

                }
            }
        }
        return endpointURL;
    }

}
