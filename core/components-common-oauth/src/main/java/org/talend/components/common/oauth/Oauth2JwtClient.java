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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.talend.components.common.oauth.Jwt.ClaimsSet;
import org.talend.components.common.oauth.Jwt.JwsHeader;
import org.talend.components.common.oauth.X509Key.Algorithm;

/**
 * 
 * <p>
 * The OAuth 2.0 JWT bearer token flow is similar to a refresh token flow within OAuth.<br/>
 * The JWT is posted to the OAuth token endpoint, which in turn processes the JWT and issues an access_token based on prior
 * approval of the app.<br/>
 * However, the client doesn’t need to have or store a refresh_token, nor is a client_secret required to be passed to the token
 * endpoint.
 * 
 * <p>
 * Reference:<br/>
 * <a href="https://tools.ietf.org/html/draft-ietf-oauth-jwt-bearer-12">
 * JWT Profile for OAuth 2.0 Client Authentication and Authorization Grants</a>
 * 
 * <a href="https://tools.ietf.org/html/draft-jones-json-web-token-10"> JSON Web Token (JWT)</a><br/>
 * </p>
 * <b>Note</b> : A refresh_token is never issued in this flow.<br/>
 * </p>
 * 
 * This client will construct the Jwt with the signtaure part using an X509 certificate.<br/>
 * it will add play load params for the assertion part under assertion param & client_assertion param
 * 
 */
public class Oauth2JwtClient {

    private static final String UTF8 = "utf-8";

    // Access Token params
    public static final String KEY_ACCESS_TOKEN = "access_token";

    public static final String KEY_TOKEN_TYPE = "token_type";

    public static final String KEY_RESOURCE = "resource";

    public static final String KEY_EXPIRE_ON = "expires_on";

    public static final String KEY_EXT_EXPIRES_IN_ = "ext_expires_in";

    public static final String KEY_EXPIRES_IN = "expires_in";

    public static final String KEY_ID = "id";

    public static final String KEY_SCOPE = "scope";

    public static final String KEY_INSTANCE_URL = "instance_url";

    // HTTP Play load params
    public static final String PARAM_GRANT_TYPE = "grant_type";

    public static final String PARAM_ASSERTION = "assertion";

    public static final String PARAM_CLIENT_ASSERTION = "client_assertion";

    public static final String PARAM_CLIENT_ASSERTION_TYPE = "client_assertion_type";

    public static final String PARAM_CLIENT_ID = "client_id";

    public static final String PARAM_RESOURCE = "resource";

    private String jwt;

    /** Oauth token endpoint */
    private String tokenEndpoint;

    private Map<String, String> postPlayloadParams = new HashMap<>();

    /**
     * Perform a request to the token end point to get an access token
     * 
     * @throws RuntimeException : if the token can't be gathered or the end point token responds with an http status code != 200
     */
    public JsonNode getAccessToken() throws IOException {
        HttpURLConnection connection = createHttpConnection();
        postRequest(connection);
        String response = readResponse(connection);

        JsonNode json = new ObjectMapper().readTree(response);
        if (!json.has("access_token")) {
            throw new RuntimeException("failed to get access token from server response: " + response);
        }

        return json;
    }

    private HttpURLConnection createHttpConnection() throws MalformedURLException, IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(tokenEndpoint).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        return connection;
    }

    private void postRequest(HttpURLConnection connection) throws IOException {

        postPlayloadParams.put(PARAM_ASSERTION, jwt.toString());
        postPlayloadParams.put(PARAM_CLIENT_ASSERTION, jwt.toString());

        //
        StringBuilder postData = new StringBuilder();
        boolean first = true;
        for (Entry<String, String> param : postPlayloadParams.entrySet()) {
            if (!first) {
                postData.append("&");
            }
            postData.append(param.getKey()).append("=").append(param.getValue());
            first = false;
        }

        OutputStream outStream = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream, UTF8));
        writer.write(postData.toString());
        writer.flush();
        writer.close();
        outStream.close();
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        int statusCode = connection.getResponseCode();
        if (statusCode != 200) {
            StringBuilder error = new StringBuilder();
            error.append("HTTP status: " + statusCode + " - " + connection.getResponseMessage() + " message: ");
            String errorLine;
            if (connection.getErrorStream() != null) {
                BufferedReader errorStream = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((errorLine = errorStream.readLine()) != null) {
                    error.append(errorLine);
                }
                errorStream.close();
            }
            throw new RuntimeException(error.toString());
        }

        BufferedReader inStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = inStream.readLine()) != null) {
            response.append(inputLine);
        }
        inStream.close();
        return response.toString();
    }

    private Oauth2JwtClient(Builder builder) {
        this.jwt = builder.jwt.toString();
        this.tokenEndpoint = builder.tokenEndpoint;
        this.postPlayloadParams = builder.postPlayloadParams;
    }

    public static IJwsHeaders builder() {
        return new Builder();
    }

    private static class Builder implements IBuild, IJwsHeaders, IClaims, IX509KeyHandler, ITokenEndpoint {

        private StringBuilder jwt = new StringBuilder();

        private String tokenEndpoint;

        private Map<String, String> postPlayloadParams;

        @Override
        public IBuild withPlayloadParams(Map<String, String> postPlayloadParams) {
            this.postPlayloadParams = postPlayloadParams;
            return this;
        }

        @Override
        public IClaims withJwsHeaders(JwsHeader jwsHeaders) throws UnsupportedEncodingException {
            String encodedJws = Base64.getUrlEncoder().encodeToString(jwsHeaders.toJSONObject().toString().getBytes(UTF8));
            jwt.append(encodedJws);
            return this;
        }

        @Override
        public IX509KeyHandler withJwtClaims(ClaimsSet claims) throws UnsupportedEncodingException {
            String encodedClaims = Base64.getUrlEncoder().encodeToString(claims.toJSONObject().toString().getBytes(UTF8));
            jwt.append(".");
            jwt.append(encodedClaims);
            return this;
        }

        @Override
        public ITokenEndpoint signWithX509Key(X509Key x509Key, Algorithm alg) {
            byte[] signedJwtPart = x509Key.sign(jwt.toString(), alg);
            jwt.append(".");
            jwt.append(Base64.getUrlEncoder().encodeToString(signedJwtPart));
            return this;
        }

        @Override
        public IBuild fromTokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
            return this;
        }

        @Override
        public Oauth2JwtClient build() {
            return new Oauth2JwtClient(this);
        }
    }

    public interface IJwsHeaders {

        public IClaims withJwsHeaders(JwsHeader jwsHeaders) throws UnsupportedEncodingException;
    }

    public interface IClaims {

        public IX509KeyHandler withJwtClaims(ClaimsSet claims) throws UnsupportedEncodingException;
    }

    public interface IX509KeyHandler {

        public ITokenEndpoint signWithX509Key(X509Key x509Key, Algorithm alg);
    }

    public interface ITokenEndpoint {

        public IBuild fromTokenEndpoint(String tokenEndpoint);
    }

    public interface IBuild {

        public IBuild withPlayloadParams(Map<String, String> postPlayloadParams);

        public Oauth2JwtClient build();
    }

}
