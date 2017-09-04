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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.oauth.Jwt.ClaimsSet;
import org.talend.components.common.oauth.Jwt.JwsHeader;
import org.talend.components.common.oauth.Jwt.JwsHeader.Algorithm;
import org.talend.components.common.oauth.Oauth2JwtClient;
import org.talend.components.common.oauth.X509Key;
import org.talend.components.common.oauth.properties.Oauth2JwtFlowProperties;

/**
 * This use server to server jwt connection that do not need user interaction
 * see {@link Oauth2JwtClient} for farther details on the process.
 */
public class SalesforceJwtConnection {

    private Oauth2JwtFlowProperties oauth2Prop;

    private String tokenEndpoint;

    /**
     * @param oauth2Prop
     * @param tokenEndpoint
     */
    public SalesforceJwtConnection(Oauth2JwtFlowProperties oauth2Prop, String tokenEndpoint) {
        super();
        this.oauth2Prop = oauth2Prop;
        this.tokenEndpoint = tokenEndpoint;
    }

    /**
     * Return the access Token in a Json format. the values of the token can be accessed using keys defined in
     * {@link Oauth2JwtClient#KEY_ACCESS_TOKEN}
     */
    public JsonNode getAccessToken() {
        try {
            return Oauth2JwtClient.builder()//
                    .withJwsHeaders(jwsHeaders())//
                    .withJwtClaims(jwtClaims())//
                    .signWithX509Key(x509Key(), org.talend.components.common.oauth.X509Key.Algorithm.SHA256withRSA)//
                    .fromTokenEndpoint(tokenEndpoint + "/token")//
                    .withPlayloadParams(playLoadParams())//
                    .build()//
                    .getAccessToken();

        } catch (IOException e) {
            throw new ComponentException(e);
        }
    }

    private JwsHeader jwsHeaders() {
        return new JwsHeader.Builder()//
                .algorithm(Algorithm.RS256)//
                .build();
    }

    private ClaimsSet jwtClaims() {
        final long now = System.currentTimeMillis() / 1000;
        return new ClaimsSet.Builder()//
                .id(UUID.randomUUID().toString())//
                .issuer(oauth2Prop.issuer.getStringValue())// consumer id
                .subject(oauth2Prop.subject.getStringValue())// user id
                .audience("https://login.salesforce.com")//
                .issueTime(now)//
                .notBeforeTime(now)//
                .expirationTime(now + oauth2Prop.expirationTime.getValue())//
                .build();
    }

    private X509Key x509Key() {
        return X509Key.builder()//
                .keyStorePath(StringUtils.strip(oauth2Prop.keyStore.getStringValue(), "\""))//
                .keyStorePassword(oauth2Prop.keyStorePassword.getStringValue())//
                .certificateAlias(oauth2Prop.certificateAlias.getStringValue())// certificate alias
                .build();
    }

    private Map<String, String> playLoadParams() {
        Map<String, String> params = new HashMap<>();
        params.put(Oauth2JwtClient.PARAM_GRANT_TYPE, "urn:ietf:params:oauth:grant-type:jwt-bearer");
        return params;
    }
}