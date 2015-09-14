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
import org.talend.components.common.oauth.OauthClient;

import com.sforce.ws.ConnectorConfig;

/**
 * created by bchen on Jul 10, 2014 Detailled comment
 * 
 */
public class SalesforceOAuthConnection {

    private static final String REFRESHTOKEN_KEY = "refreshtoken"; //$NON-NLS-1$

    private final String        login_endpoint;

    private final String        oauth_clientID;

    private final String        oauth_clientSecret;

    private String              tokenFilePath;

    private final String        apiVersion;

    private String              callbackHost;

    private int                 callbackPort;

    private SalesforceOAuthConnection() throws Exception {
        throw new Exception("should use builder to init"); //$NON-NLS-1$
    }

    private SalesforceOAuthConnection(Builder builder) {
        this.login_endpoint = builder.login_endpoint;
        this.oauth_clientID = builder.oauth_clientID;
        this.oauth_clientSecret = builder.oauth_clientSecret;
        this.apiVersion = builder.apiVersion;
        this.tokenFilePath = builder.tokenFilePath;
        this.callbackHost = builder.callbackHost;
        this.callbackPort = builder.callbackPort;
    }

    public void login(ConnectorConfig connect) {
        String session_id = null;
        String refreshToken = null;
        SalesforceOAuthAccessTokenResponse token = null;
        // 1. if tokenFile exist, try refresh token
        if (tokenFilePath != null) {
            Properties prop = new Properties();
            File tokenFile = new File(tokenFilePath);
            if (tokenFile.exists()) {
                FileInputStream inputStream;
                try {
                    inputStream = new FileInputStream(tokenFilePath);
                    prop.load(inputStream);
                    inputStream.close();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String storedRefreshToken = (String) prop.get(REFRESHTOKEN_KEY);
                if (storedRefreshToken != null) {
                    OauthClient oauthClient;
                    try {
                        oauthClient = new OauthClient.RefreshTokenBuilder(new URL(login_endpoint + "/token"), oauth_clientID,
                                oauth_clientSecret).setRefreshToken(storedRefreshToken).build();
                        token = oauthClient.getToken(SalesforceOAuthAccessTokenResponse.class);
                        session_id = token.getAccessToken();
                        refreshToken = token.getRefreshToken();
                    } catch (MalformedURLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        // 2. try to auth if session_id can't be retrieved
        if (session_id == null) {
            OauthClient oauthClient;
            try {
                oauthClient = new OauthClient.AuthorizationCodeBuilder(new URL(login_endpoint + "/token"), //$NON-NLS-1$
                        oauth_clientID, oauth_clientSecret).setAuthorizationLocation(new URL(login_endpoint + "/authorize")) //$NON-NLS-1$
                        .setCallbackURL(new URL("https://" + callbackHost + ":" + callbackPort)).setResponseType("code").build();
                token = oauthClient.getToken(SalesforceOAuthAccessTokenResponse.class);
                session_id = token.getAccessToken();
                refreshToken = token.getRefreshToken();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // 3.if refresh token & tokenFile exist, store
        if (refreshToken != null && tokenFilePath != null) {
            File tokenFile = new File(tokenFilePath);
            if (!tokenFile.exists()) {
                tokenFile.mkdirs();
                try {
                    tokenFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            Properties prop = new Properties();
            prop.setProperty(REFRESHTOKEN_KEY, refreshToken);
            FileOutputStream outputStream;
            try {
                outputStream = new FileOutputStream(tokenFilePath);
                prop.store(outputStream, null);
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        connect.setServiceEndpoint(getEndpoint(token, apiVersion));
        connect.setSessionId(session_id);
    }

    private String getEndpoint(SalesforceOAuthAccessTokenResponse token, String version) {
        // if (SalesforceConnectionType.SOAP == connType) {
        return getSOAPEndpoint(token, version);
        // } else if (SalesforceConnectionType.BULK == connType) {
        // return genBulkEndpoint(token, version);
        // }
        //        throw new RuntimeException("Unspport connection type"); //$NON-NLS-1$
    }

    // private String genBulkEndpoint(SalesforceOAuthAccessTokenResponse token, String version) {
    //        return token.getInstanceURL() + "/services/async/" + version; //$NON-NLS-1$
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

    public static class Builder {

        private final String login_endpoint;

        private final String oauth_clientID;

        private final String oauth_clientSecret;

        private final String apiVersion;

        private String       tokenFilePath = null;

        private String       callbackHost;

        private int          callbackPort;

        public Builder(String login_endpoint, String oauth_clientID, String oauth_clientSecret, String apiVersion) {
            this.login_endpoint = login_endpoint;
            this.oauth_clientID = oauth_clientID;
            this.oauth_clientSecret = oauth_clientSecret;
            this.apiVersion = apiVersion;
        }

        public Builder setCallback(String host, int port) {
            this.callbackHost = host;
            this.callbackPort = port;
            return this;
        }

        public Builder setTokenFilePath(String tokenFilePath) {
            this.tokenFilePath = tokenFilePath;
            return this;
        }

        public SalesforceOAuthConnection build() {
            return new SalesforceOAuthConnection(this);
        }
    }

}
