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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.oauth.Oauth2JwtClient;
import org.talend.components.salesforce.SalesforceConnectionProperties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sforce.ws.ConnectorConfig;

public class SalesforceOAuthConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceOAuthConnection.class.getName());

    private SalesforceConnectionProperties connection;

    private String url;

    private String apiVersion;

    public SalesforceOAuthConnection(SalesforceConnectionProperties connection, String url, String apiVersion) {
        this.connection = connection;
        this.url = url;
        this.apiVersion = apiVersion;
    }

    public void login(ConnectorConfig connect) {

        org.codehaus.jackson.JsonNode accessToken;
        switch (connection.oauth2FlowType.getValue()) {
        case JWT_Flow:
            accessToken = new SalesforceJwtConnection(connection.oauth2JwtFlow, url).getAccessToken();
            connect.setServiceEndpoint(getSOAPEndpoint(accessToken.get(Oauth2JwtClient.KEY_ID).asText(), //
                    accessToken.get(Oauth2JwtClient.KEY_TOKEN_TYPE).asText(), //
                    accessToken.get(Oauth2JwtClient.KEY_ACCESS_TOKEN).asText(), //
                    apiVersion));
            connect.setSessionId(accessToken.get(Oauth2JwtClient.KEY_ACCESS_TOKEN).asText());
            break;
        case Implicit_Flow:
            SalesforceOAuthAccessTokenResponse token = new SalesforceImplicitConnection(connection.oauth, url).getToken();
            connect.setServiceEndpoint(getSOAPEndpoint(token.getID(), token.getTokenType(), token.getAccessToken(), apiVersion));
            connect.setSessionId(token.getAccessToken());
            break;
        default:
            break;
        }

    }

    // it's not necessary for bulk, there is another easy way, looking at genBulkEndpoint
    private String getSOAPEndpoint(String id, String type, String accessToken, String version) {
        String endpointURL = null;
        BufferedReader reader = null;
        try {
            URLConnection idConn = new URL(id).openConnection();
            idConn.setRequestProperty("Authorization", type + " " + accessToken);
            reader = new BufferedReader(new InputStreamReader(idConn.getInputStream()));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(reader);
            JsonNode urls = jsonNode.get("urls");
            endpointURL = urls.get("partner").toString().replace("{version}", version);
            endpointURL = StringUtils.strip(endpointURL, "\"");
        } catch (IOException e) {
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
