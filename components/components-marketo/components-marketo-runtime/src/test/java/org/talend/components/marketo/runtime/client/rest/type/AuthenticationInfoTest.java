// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.runtime.client.rest.type;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class AuthenticationInfoTest {

    AuthenticationInfo auth;

    @Before
    public void setUp() throws Exception {
        auth = new AuthenticationInfo("secret", "user");
    }

    @Test
    public void testGetSecretKey() throws Exception {
        assertEquals("secret", auth.getSecretKey());
    }

    @Test
    public void testGetClientAccessID() throws Exception {
        assertEquals("user", auth.getClientAccessID());
    }

    @Test
    public void testSetters() throws Exception {
        auth = new AuthenticationInfo();
        auth.setClientAccessID("client");
        auth.setSecretKey("key");
        assertEquals("client", auth.getClientAccessID());
        assertEquals("key", auth.getSecretKey());
    }

}
