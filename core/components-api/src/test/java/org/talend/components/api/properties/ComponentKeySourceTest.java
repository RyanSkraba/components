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
// ============================================================================
package org.talend.components.api.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.Test;

/*
 * Created by bhe on Nov 4, 2019
 */
public class ComponentKeySourceTest {

    @Test
    public void testLoadAllKeys() {
        /*************************************
         * load system default keys
         *************************************/
        Properties p = ComponentKeySource.loadAllKeys();
        assertNotNull(p);
        assertTrue(p.stringPropertyNames().size() > 0);
        p.stringPropertyNames().forEach((k) -> {
            assertTrue("default keys: " + k,
                    k.startsWith(ComponentKeySource.KEY_SYSTEM_PREFIX));
        });
    }

    @Test
    public void testGetKeyName() throws Exception {
        Properties p = generateKeys();

        /*************************************
         * key for encryption
         *************************************/
        ComponentKeySource ks = ComponentKeySource.key(p, ComponentKeySource.KEY_SYSTEM_PREFIX, true);
        assertEquals("highest version of system encryption key name not equal", ComponentKeySource.KEY_SYSTEM_PREFIX + 3,
                ks.getKeyName());

        assertNotNull(ks.getKey());

        /*************************************
         * key for decryption
         *************************************/
        for (int i = 1; i < 4; i++) {
            // input system key name
            ks = ComponentKeySource.key(p, ComponentKeySource.KEY_SYSTEM_PREFIX + i, false);
            assertEquals("system decryption key name not equal", ComponentKeySource.KEY_SYSTEM_PREFIX + i, ks.getKeyName());

            assertNotNull(ks.getKey());
        }

    }

    public static Properties generateKeys() throws NoSuchAlgorithmException {
        Properties p = new Properties();

        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);

        SecretKey key = null;

        // create system encryption key
        for (int i = 1; i < 4; i++) {
            key = kg.generateKey();
            p.put(ComponentKeySource.KEY_SYSTEM_PREFIX + i, Base64.getEncoder().encodeToString(key.getEncoded()));
        }

        return p;
    }
}
