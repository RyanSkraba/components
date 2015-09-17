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
package org.talend.components.salesforce.connection.oauth;

import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;

public class SalesforceOAuthAccessTokenResponse extends OAuthJSONAccessTokenResponse {

    public String getInstanceURL() {
        return getParam("instance_url");
    }

    public String getID() {
        return getParam("id");
    }

    public String getTokenType() {
        return getParam(OAuth.OAUTH_TOKEN_TYPE);
    }

    @Override
    public String toString() {
        return parameters.toString();
    }

}
