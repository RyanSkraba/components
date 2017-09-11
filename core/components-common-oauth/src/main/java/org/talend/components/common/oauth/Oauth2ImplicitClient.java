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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.oauth.server.OAuth2ImplicitGrantServer;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

/**
 * created by bchen on Sep 11, 2015 Detailled comment
 *
 */
public class Oauth2ImplicitClient {

    private static final Logger logger = LoggerFactory.getLogger(Oauth2ImplicitClient.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(Oauth2ImplicitClient.class);

    private final URL tokenLocation;

    private final String clientID;

    private final String clientSecret;

    private URL authorizationLocation;

    private URL callbackURL;

    private String responseType;

    private String refreshToken;

    private GrantType grantType;

    private Oauth2ImplicitClient(Builder builder) {
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

            // FIXME : remove those Syso when the studio activate the INFO log by default
            System.out.println(messages.getMessage("msg.info.showAuthorizUrl"));
            System.out.println(request.getLocationUri());
            // --
            logger.info(messages.getMessage("msg.info.showAuthorizUrl"));
            logger.info(request.getLocationUri());
            OAuth2ImplicitGrantServer service = new OAuth2ImplicitGrantServer(callbackURL.getHost(), callbackURL.getPort(),
                    10 * 60 * 1000);
            service.run();// <--- this method wait for 10 minutes maximum to grab authorization code
            String code = service.getAuthorizationCode();
            service.stop();
            return code;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends OAuthAccessTokenResponse> T getToken(Class<T> tokenResponseClass) {
        try {
            TokenRequestBuilder builder = OAuthClientRequest//
                    .tokenLocation(tokenLocation.toString())//
                    .setGrantType(grantType)//
                    .setClientId(clientID)//
                    .setClientSecret(clientSecret);

            if (GrantType.AUTHORIZATION_CODE == grantType) {
                builder = builder.setRedirectURI(callbackURL.toString())//
                        .setCode(getAuthorizationCode());
            } else if (GrantType.REFRESH_TOKEN == grantType) {
                builder = builder.setRefreshToken(refreshToken);
            }
            OAuthClientRequest request = builder.buildQueryMessage();
            OAuthClient oauthClient = new OAuthClient(new URLConnectionClient());
            return oauthClient.accessToken(request, tokenResponseClass);
        } catch (OAuthSystemException e) {
            throw new RuntimeException(e);
        } catch (OAuthProblemException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract static class Builder {

        protected final URL tokenLocation;

        protected final String clientID;

        protected final String clientSecret;

        public Builder(URL tokenLocation, String clientID, String clientSecret) {
            this.tokenLocation = tokenLocation;
            this.clientID = clientID;
            this.clientSecret = clientSecret;
        }

        public Oauth2ImplicitClient build() {
            return new Oauth2ImplicitClient(this);
        }

        protected abstract GrantType getGrantType();
    }

    public static class AuthorizationCodeBuilder extends Builder {

        private URL authorizationLocation;

        private URL callbackURL;

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
        public Oauth2ImplicitClient build() {
            Oauth2ImplicitClient oauthClient = new Oauth2ImplicitClient(this);
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
        public Oauth2ImplicitClient build() {
            Oauth2ImplicitClient oauthClient = new Oauth2ImplicitClient(this);
            oauthClient.setRefreshToken(refreshToken);
            return oauthClient;
        }

        @Override
        protected GrantType getGrantType() {
            return GrantType.REFRESH_TOKEN;
        }
    }

}
