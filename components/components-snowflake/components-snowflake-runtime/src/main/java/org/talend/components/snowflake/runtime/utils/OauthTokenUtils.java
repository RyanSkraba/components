// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.runtime.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import static java.util.Optional.ofNullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.talend.components.snowflake.SnowflakeOauthConnectionProperties;
import org.talend.components.snowflake.runtime.SnowflakeSourceOrSink;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class OauthTokenUtils {

    private static final I18nMessages I18N = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(SnowflakeSourceOrSink.class);

    private static final String GRANT_TYPE = "grant_type";

    private static final String CLIENT_CREDENTIALS = "client_credentials";

    private static final String SCOPE = "scope";

    private static final String ACCESS_TOKEN = "access_token";

    private static final String ERROR_DESCRIPTION = "error_description";

    private static final String ERROR_SUMMARY = "errorSummary";

    private static final String CONTENT_ENCODING = "UTF-8";

    public static String getToken(SnowflakeOauthConnectionProperties oauthProperties) {
        String oauthTokenEndpoint = oauthProperties.oauthTokenEndpoint.getValue();
        if (StringUtils.isBlank(oauthTokenEndpoint)) {
            throw new IllegalArgumentException(I18N.getMessage("error.missingOAuthEndpoint"));
        }
        HttpPost postRequest = new HttpPost(oauthTokenEndpoint);
        String authParameters = oauthProperties.clientId.getValue() + ":" + oauthProperties.clientSecret.getValue();
        String encodedAuthParameters = Base64.getEncoder().encodeToString(authParameters.getBytes());
        postRequest.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuthParameters);

        List<NameValuePair> urlEncodedProperties = new ArrayList<NameValuePair>();
        urlEncodedProperties.add(new BasicNameValuePair(GRANT_TYPE, CLIENT_CREDENTIALS));
        urlEncodedProperties.add(new BasicNameValuePair(SCOPE, oauthProperties.scope.getValue()));

        try {
            postRequest.setEntity(new UrlEncodedFormEntity(urlEncodedProperties, CONTENT_ENCODING));

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = httpClient.execute(postRequest);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getEntity().getContent());
            if (response.getStatusLine().getStatusCode() != 200) {
                String errorMessage =
                        ofNullable(node.get(ERROR_DESCRIPTION)).orElse(node.get(ERROR_SUMMARY)).asText();
                throw new IllegalArgumentException(errorMessage);
            }
            return node.get(ACCESS_TOKEN).asText();
        } catch (IOException ioe) {
            throw new IllegalArgumentException(ioe.getMessage(), ioe);
        }
    }
}
