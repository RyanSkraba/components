// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.common.oauth;

import java.net.URL;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.AuthenticationRequestBuilder;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.TokenRequestBuilder;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.talend.components.common.oauth.util.AuthorizationCodeCallBackHandler;
import org.talend.components.common.oauth.util.HttpsService;

/**
 * created by bchen on Sep 11, 2015 Detailled comment
 *
 */
public class OauthClient {

    private static final String HTTPS = "https";      //$NON-NLS-1$

    private final URL           tokenLocation;

    private final String        clientID;

    private final String        clientSecret;

    private URL                 authorizationLocation;

    private URL                 callbackURL;

    private String              responseType;

    private String              refreshToken;

    private GrantType           grantType;

    private OauthClient(Builder builder) {
        this.tokenLocation = builder.tokenLocation;
        this.clientID = builder.clientID;
        this.clientSecret = builder.clientSecret;
        this.grantType = builder.getGrantType();
    }

    private void setAuthorizationLocation(URL authorizationLocation) {
        this.authorizationLocation = authorizationLocation;
    }

    private void setCallbackURL(URL callbackURL) {
        this.callbackURL = callbackURL;
    }

    private void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    private void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    private String getAuthorizationCode() {
        try {
            AuthenticationRequestBuilder builder = OAuthClientRequest.authorizationLocation(authorizationLocation.toString())
                    .setClientId(clientID).setRedirectURI(callbackURL.toString());
            if (responseType != null) {
                builder.setResponseType(responseType);
            }
            OAuthClientRequest request = builder.buildQueryMessage();
            System.out.println("Paste this URL into a web browser to authorize Salesforce Access:");
            System.out.println(request.getLocationUri());
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }
        String code = null;
        if (callbackURL.getProtocol().equalsIgnoreCase(HTTPS)) {
            HttpsService service;
            try {
                AuthorizationCodeCallBackHandler handler = new AuthorizationCodeCallBackHandler();
                service = new HttpsService(callbackURL.getHost(), callbackURL.getPort(), handler);
                while ((code = handler.getCode()) == null) {
                    Thread.sleep(2 * 1000);
                }
                service.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return code;
    }

    public <T extends OAuthAccessTokenResponse> T getToken(Class<T> tokenResponseClass) {
        try {
            OAuthClientRequest request = null;
            TokenRequestBuilder builder = OAuthClientRequest.tokenLocation(tokenLocation.toString()).setGrantType(grantType)
                    .setClientId(clientID).setClientSecret(clientSecret);
            if (GrantType.AUTHORIZATION_CODE == grantType) {
                builder = builder.setRedirectURI(callbackURL.toString()).setCode(getAuthorizationCode());
            } else if (GrantType.REFRESH_TOKEN == grantType) {
                builder = builder.setRefreshToken(refreshToken);
            }
            request = builder.buildQueryMessage();
            OAuthClient oauthClient = new OAuthClient(new URLConnectionClient());
            return oauthClient.accessToken(request, tokenResponseClass);
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract static class Builder {

        protected final URL    tokenLocation;

        protected final String clientID;

        protected final String clientSecret;

        public Builder(URL tokenLocation, String clientID, String clientSecret) {
            this.tokenLocation = tokenLocation;
            this.clientID = clientID;
            this.clientSecret = clientSecret;
        }

        public OauthClient build() {
            return new OauthClient(this);
        }

        protected abstract GrantType getGrantType();
    }

    public static class AuthorizationCodeBuilder extends Builder {

        private URL    authorizationLocation;

        private URL    callbackURL;

        private String responseType;

        public AuthorizationCodeBuilder(URL tokenLocation, String clientID, String clientSecret) {
            super(tokenLocation, clientID, clientSecret);
        }

        public AuthorizationCodeBuilder setAuthorizationLocation(URL authorizationLocation) {
            this.authorizationLocation = authorizationLocation;
            return this;
        }

        public AuthorizationCodeBuilder setCallbackURL(URL callbackURL) {
            this.callbackURL = callbackURL;
            return this;
        }

        public AuthorizationCodeBuilder setResponseType(String responseType) {
            this.responseType = responseType;
            return this;
        }

        @Override
        public OauthClient build() {
            OauthClient oauthClient = new OauthClient(this);
            oauthClient.setAuthorizationLocation(authorizationLocation);
            oauthClient.setCallbackURL(callbackURL);
            oauthClient.setResponseType(responseType);
            return oauthClient;
        }

        @Override
        protected GrantType getGrantType() {
            return GrantType.AUTHORIZATION_CODE;
        }
    }

    public static class RefreshTokenBuilder extends Builder {

        private String refreshToken;

        public RefreshTokenBuilder(URL tokenLocation, String clientID, String clientSecret) {
            super(tokenLocation, clientID, clientSecret);
        }

        public RefreshTokenBuilder setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        @Override
        public OauthClient build() {
            OauthClient oauthClient = new OauthClient(this);
            oauthClient.setRefreshToken(refreshToken);
            return oauthClient;
        }

        @Override
        protected GrantType getGrantType() {
            return GrantType.REFRESH_TOKEN;
        }
    }

}
