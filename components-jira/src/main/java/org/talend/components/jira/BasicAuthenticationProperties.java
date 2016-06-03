// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jira;

import org.apache.commons.codec.binary.Base64;
import org.talend.components.common.UserPasswordProperties;

/**
 * Http Basic authentication {@link Properties}
 */
public class BasicAuthenticationProperties extends UserPasswordProperties {

    /**
     * Constructor sets Properties name
     * 
     * @param name Properties name
     */
    public BasicAuthenticationProperties(String name) {
        super(name);
    }
    
    /**
     * Returns value for Http Authorization header. For example, for "user" userId and "pass" password
     * it returns "Basic dXNlcjpwYXNz" 
     * 
     * @return value for Http Authorization header
     */
    public String getAuthorizationValue() {
        String credentials = userId + ":" + password;
        String encodedCredentials = base64(credentials);
        String authorizationValue = "Basic " + encodedCredentials;
        return authorizationValue;
    }
    
    /**
     * Encodes specified string to base64 and returns result
     * 
     * @param str string to be encoded
     * @return base64 encoded string
     */
    private String base64(String str) {
        return Base64.encodeBase64String(str.getBytes());
    }

}
