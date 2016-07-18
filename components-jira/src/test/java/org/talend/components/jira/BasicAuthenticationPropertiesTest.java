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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit-tests for {@link BasicAuthenticationProperties}
 */
public class BasicAuthenticationPropertiesTest {

    /**
     * Check {@link BasicAuthenticationProperties#getAuthorizationValue()} returns correct base64 encoded
     * authorization value
     */
    @Test
    public void testGetAuthorizationValue() {
        String expectedAuthorization = "Basic dXNlcjpwYXNz";
        BasicAuthenticationProperties properties = new BasicAuthenticationProperties("auth");
        properties.init();
        properties.userId.setValue("user");
        properties.password.setValue("pass");
        assertEquals(expectedAuthorization, properties.getAuthorizationValue());
    }
}
