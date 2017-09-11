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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.util.*;

import org.codehaus.jackson.JsonNode;
import org.junit.Test;
import org.talend.components.common.oauth.Jwt.ClaimsSet;
import org.talend.components.common.oauth.Jwt.JwsHeader;
import org.talend.components.common.oauth.Jwt.JwsHeader.Algorithm;


public class AzureOauth2JwtExample {

    final static int JWT_TOKEN_LIFETIME_SECONDS = 10 * 60;

    @Test
    public void azureUsageExample() throws IOException, CertificateEncodingException, NoSuchAlgorithmException {

        // Certificate
        X509Key x509KeyHandler = X509Key.builder()//
                .keyStorePath(getClass().getClassLoader().getResource("00D0Y000001dveq.jks").getPath())//
                .keyStorePassword("talend2017")// store pwd
                .certificateAlias("jobcert")// certificate alias
                .build();

        // claims
        final long now = new Date().getTime() / 1000;
        ClaimsSet claims = new ClaimsSet.Builder()//
                .id(UUID.randomUUID().toString())//
                .issuer("d40bef80-d489-468e-93fb-b482667b3c2d")//
                .subject("d40bef80-d489-468e-93fb-b482667b3c2d")//
                .audience("https://login.microsoftonline.com/common/oauth2/token")//
                .notBeforeTime(now)//
                .issueTime(now)//
                .expirationTime(now + JWT_TOKEN_LIFETIME_SECONDS)//
                .build();

        // headers
        List<String> certs = new ArrayList<>();
        certs.add(x509KeyHandler.getPublicCertificate());
        JwsHeader jwsHeaders = new JwsHeader.Builder()//
                .algorithm(Algorithm.RS256)//
                .type(JwsHeader.Type.JWT)//
                .x509CertChain(certs).x509CertThumbprint(x509KeyHandler.getPublicCertificateHash())//
                .build();

        // Additional http playload params
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "client_credentials"); // https://msdn.microsoft.com/en-us/library/mt223862.aspx
        params.put("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        params.put("client_id", "d40bef80-d489-468e-93fb-b482667b3c2d");
        params.put("resource", "https://talend.com/3dc2f938-1650-4f6a-b4ee-2306887796ce");

        String tokenEndpoint = "https://login.microsoftonline.com/common/oauth2/token";

        // Get Access Token
        JsonNode accessToken = Oauth2JwtClient.builder()//
                .withJwsHeaders(jwsHeaders)//
                .withJwtClaims(claims)//
                .signWithX509Key(x509KeyHandler, org.talend.components.common.oauth.X509Key.Algorithm.SHA256withRSA)//
                .fromTokenEndpoint(tokenEndpoint)//
                .withPlayloadParams(params)//
                .build()//
                .getAccessToken();

        // Get Access Token
        System.out.println(accessToken.toString());

    }

}
