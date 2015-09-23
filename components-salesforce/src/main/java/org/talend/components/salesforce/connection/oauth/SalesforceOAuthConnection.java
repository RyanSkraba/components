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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.talend.components.common.oauth.OauthClient;
import org.talend.components.common.oauth.OauthProperties;

import com.sforce.ws.ConnectorConfig;

public class SalesforceOAuthConnection {

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
        String session_id = null;
        String refreshToken = null;
        SalesforceOAuthAccessTokenResponse token = null;
        // 1. if tokenFile exist, try refresh token
        String tokenFilePath = oauth.getStringValue(oauth.tokenFile);
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
                        oauthClient = new OauthClient.RefreshTokenBuilder(new URL(url + "/token"),
                                oauth.getStringValue(oauth.clientId), oauth.getStringValue(oauth.clientSecret)).setRefreshToken(
                                storedRefreshToken).build();
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
                oauthClient = new OauthClient.AuthorizationCodeBuilder(new URL(url + "/token"), //$NON-NLS-1$
                        oauth.getStringValue(oauth.clientId), oauth.getStringValue(oauth.clientSecret))
                        .setAuthorizationLocation(new URL(url + "/authorize")) //$NON-NLS-1$
                        .setCallbackURL(
                                new URL("https://" + oauth.getStringValue(oauth.callbackHost) + ":"
                                        + oauth.getIntValue(oauth.callbackPort))).setResponseType("code").build();
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

}
