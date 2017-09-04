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

/**
 * Oauth 2 supported Flow Types
 */
public enum OAuth2FlowType {

    /**
     * JSON Web Token Flow
     * <a href="https://tools.ietf.org/html/draft-ietf-oauth-jwt-bearer-12">more details</a>
     */
    JWT_Flow,

    /**
     * Implicit Flow using web server. This flow is deprecated for batch
     * <a href="https://tools.ietf.org/html/rfc6749#section-1.3.2">more details</a>
     */
    Implicit_Flow;
}
