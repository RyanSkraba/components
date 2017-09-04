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

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.talend.components.common.oauth.Jwt.ClaimsSet;
import org.talend.components.common.oauth.Jwt.JwsHeader;
import org.talend.components.common.oauth.Jwt.JwsHeader.Algorithm;

import com.fasterxml.jackson.databind.JsonNode;

public class JwtTest {

    @Test
    public void claimsBuilderTest() {
        final long now = new Date().getTime() / 1000;
        String uuid = UUID.randomUUID().toString();
        ClaimsSet claims = new ClaimsSet.Builder()//
                .id(uuid)//
                .issuer("3MVG9Hm.I1VyuL3DK_voQ2An7_k1H69KApQ.dfUcQg2dWDw")//
                .subject("user@talend.com")//
                .audience("https://website.com")//
                .notBeforeTime(now)//
                .expirationTime(now + (5 * 1000))//
                .build();

        JsonNode json = claims.toJSONObject();
        assertEquals(uuid, json.get("jti").asText());
        assertEquals("3MVG9Hm.I1VyuL3DK_voQ2An7_k1H69KApQ.dfUcQg2dWDw", json.get("iss").asText());
        assertEquals("user@talend.com", json.get("sub").asText());
        assertEquals("https://website.com", json.get("aud").asText());
        assertEquals(now, json.get("nbf").asLong());
        assertEquals(now + (5 * 1000), json.get("exp").asLong());
    }

    @Test
    public void headerTest() throws CertificateEncodingException, NoSuchAlgorithmException, URISyntaxException {

        // Certificate
        X509Key x509KeyHandler = X509Key.builder()//
                .keyStorePath(getClass().getClassLoader().getResource("00D0Y000001dveq.jks").toURI().getPath())//
                .keyStorePassword("talend2017")// store pwd
                .certificateAlias("jobcert")// certificate alias
                .build();

        String certThumbprint = x509KeyHandler.getPublicCertificateHash();
        String pCertificat = x509KeyHandler.getPublicCertificate();

        List<String> certs = new ArrayList<>();
        certs.add(x509KeyHandler.getPublicCertificate());
        JwsHeader jwsHeaders = new JwsHeader.Builder()//
                .algorithm(Algorithm.RS256)//
                .type(JwsHeader.Type.JWT)//
                .x509CertChain(certs)//
                .x509CertThumbprint(certThumbprint)//
                .build();

        JsonNode json = jwsHeaders.toJSONObject();
        assertEquals(Algorithm.RS256.name(), json.get("alg").asText());
        assertEquals(JwsHeader.Type.JWT.name(), json.get("typ").asText());
        assertEquals(certThumbprint, json.get("x5t").asText());
    }

}
