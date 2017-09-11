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

import java.text.ParseException;
import java.util.*;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Light JWT (Json web Token) / JWS (Json Web signature) Builder
 * for more details please see <a href="https://tools.ietf.org/html/draft-jones-json-web-token-10"> JSON Web Token (JWT)</a>
 */
public class Jwt {

    private final static ObjectMapper mapper = new ObjectMapper();
    /**
     * JWT Claims
     */
    public static class ClaimsSet {

        private static final String CLAIM_ISSUER = "iss";

        private static final String CLAIM_SUBJECT = "sub";

        private static final String CLAIM_AUDIENCE = "aud";

        private static final String CLAIM_EXPIRATION_TIME = "exp";

        private static final String CLAIM_NOT_BEFORE = "nbf";

        private static final String CLAIM_ISSUED_AT = "iat";

        private static final String CLAIM_JWT_ID = "jti";

        private final Map<String, Object> claims = new LinkedHashMap<>();



        public JsonNode toJSONObject() {
            ObjectNode o = mapper.createObjectNode();
            for (Map.Entry<String, Object> claim : claims.entrySet()) {
                if (CLAIM_AUDIENCE.equals(claim.getKey())) {

                    // Serialize single audience list and string
                    List<String> audList = getAudience();
                    if (audList != null && !audList.isEmpty()) {
                        if (audList.size() == 1) {
                            o.put(CLAIM_AUDIENCE, audList.get(0));
                        } else {
                            ArrayNode audArray = mapper.createArrayNode();
                            for (String aud : audList) {
                                audArray.add(aud);
                            }
                            o.put(CLAIM_AUDIENCE, audArray);
                        }
                    }

                } else if (claim.getValue() != null) {
                    o.putPOJO(claim.getKey(), claim.getValue());
                }
            }
            return o;
        }

        private List<String> getAudience() {
            List<String> aud;
            try {
                aud = getStringListClaim(CLAIM_AUDIENCE);
            } catch (ParseException e) {
                return Collections.emptyList();
            }
            return aud != null ? Collections.unmodifiableList(aud) : Collections.<String> emptyList();
        }

        private List<String> getStringListClaim(final String name) throws ParseException {
            String[] stringArray = getStringArrayClaim(name);
            if (stringArray == null) {
                return null;
            }
            return Collections.unmodifiableList(Arrays.asList(stringArray));
        }

        private String[] getStringArrayClaim(final String name) throws ParseException {

            Object value = getClaim(name);
            if (value == null) {
                return null;
            }

            List<?> list;
            try {
                list = (List<?>) getClaim(name);
            } catch (ClassCastException e) {
                throw new ParseException("The \"" + name + "\" claim is not a list / JSON array", 0);
            }

            String[] stringArray = new String[list.size()];
            for (int i = 0; i < stringArray.length; i++) {
                try {
                    stringArray[i] = (String) list.get(i);
                } catch (ClassCastException e) {
                    throw new ParseException("The \"" + name + "\" claim is not a list / JSON array of strings", 0);
                }
            }
            return stringArray;
        }

        public Object getClaim(final String name) {
            return claims.get(name);
        }

        private ClaimsSet(final Map<String, Object> claims) {
            this.claims.putAll(claims);
        }

        public static class Builder {

            private final Map<String, Object> claims = new LinkedHashMap<>();

            public Builder id(String id) {
                claims.put(CLAIM_JWT_ID, id);
                return this;
            }

            public Builder issuer(String issuer) {
                claims.put(CLAIM_ISSUER, issuer);
                return this;
            }

            public Builder subject(String subject) {
                claims.put(CLAIM_SUBJECT, subject);
                return this;
            }

            public Builder audience(String audience) {
                if (audience == null) {
                    claims.put(CLAIM_AUDIENCE, null);
                } else {
                    claims.put(CLAIM_AUDIENCE, Collections.singletonList(audience));
                }
                return this;
            }

            public Builder audience(final List<String> aud) {
                claims.put(CLAIM_AUDIENCE, aud);
                return this;
            }

            public Builder notBeforeTime(Long notBeforeTime) {
                claims.put(CLAIM_NOT_BEFORE, notBeforeTime);
                return this;
            }

            public Builder expirationTime(Long expirationTime) {
                claims.put(CLAIM_EXPIRATION_TIME, expirationTime);
                return this;
            }

            public Builder issueTime(Long issueTime) {
                claims.put(CLAIM_ISSUED_AT, issueTime);
                return this;
            }

            public ClaimsSet build() {
                return new ClaimsSet(claims);
            }
        }
    }

    /**
     * Light JWS Header Builder
     */
    public static class JwsHeader {

        public enum Type {
            JWT
        }

        public enum Algorithm {
            RS256
        }

        private static final String H_ALGORITHM = "alg";

        private static final String H_TYPE = "typ";

        private static final String H_X509CERT_CHAIN = "x5c";

        private static final String H_X509CERT_THUMBPRINT = "x5t";

        private final Map<String, Object> headers = new LinkedHashMap<>();

        private JwsHeader(Map<String, Object> headers) {
            this.headers.putAll(headers);
        }

        public ObjectNode toJSONObject() {
            ObjectNode o = mapper.createObjectNode();
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                o.putPOJO(header.getKey(), header.getValue());
            }
            return o;
        }

        public static class Builder {

            private final Map<String, Object> headers = new LinkedHashMap<>();

            public Builder algorithm(Algorithm algorithm) {
                headers.put(H_ALGORITHM, algorithm.name());
                return this;
            }

            public Builder type(Type type) {
                headers.put(H_TYPE, type.name());
                return this;
            }

            /**
             * @param x509CertChain Base64 url encoded String
             */
            public Builder x509CertChain(List<String> x509CertChain) {
                if (x509CertChain != null) {
                    headers.put(H_X509CERT_CHAIN, Collections.unmodifiableList(x509CertChain));
                } else {
                    headers.put(H_X509CERT_CHAIN, null);
                }
                return this;
            }

            /**
             * @param x509CertThumbprint Base64 url encoded String
             */
            public Builder x509CertThumbprint(String x509CertThumbprint) {
                headers.put(H_X509CERT_THUMBPRINT, x509CertThumbprint);
                return this;
            }

            public JwsHeader build() {
                return new JwsHeader(headers);
            }
        }
    }
}
