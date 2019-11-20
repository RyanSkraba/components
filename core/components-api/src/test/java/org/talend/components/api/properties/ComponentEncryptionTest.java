package org.talend.components.api.properties;

// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ComponentEncryptionTest {

    @Test
    public void testAESEncrypt() throws Exception {

        String input1 = "Talend";

        // always encrypt data by highest version of system key
        String encrypted = ComponentEncryption.encrypt(input1);
        // should match
        assertTrue(ComponentEncryption.isEncrypted(encrypted));
        assertNotEquals(input1, encrypted);
        assertEquals(input1, ComponentEncryption.decrypt(encrypted));

        // already encrypted
        assertEquals(encrypted, ComponentEncryption.encrypt(encrypted));

        assertEquals(null, ComponentEncryption.encrypt(null));
        assertEquals(null, ComponentEncryption.decrypt(null));

        // not encrypted
        assertEquals(input1, ComponentEncryption.decrypt(input1));
    }
}
