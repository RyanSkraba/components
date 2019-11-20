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

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.talend.daikon.crypto.CipherSources;
import org.talend.daikon.crypto.Encryption;

/*
 * Created by bhe on Sep 27, 2019
 */
public class ComponentEncryption {

    private static final String ENCRYPTION_PREFIX = "enc:";

    private static final Pattern REG_ENCRYPTED_DATA = Pattern.compile("^enc\\:system\\.encryption\\.key\\.v\\d\\:\\p{Print}+");

    private static final Logger LOGGER = Logger.getLogger(ComponentEncryption.class.getCanonicalName());

    private static final ThreadLocal<Properties> LOCALCACHEDALLKEYS = ThreadLocal.withInitial(() -> {
        return ComponentKeySource.loadAllKeys();
    });

    private static ComponentKeySource getKeySource(String keyName, boolean isEncrypt) {
        Properties allKeys = LOCALCACHEDALLKEYS.get();
        ComponentKeySource ks = ComponentKeySource.key(allKeys, keyName, isEncrypt);
        try {
            if (ks.getKey() != null) {
                return ks;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Can not load key: " + keyName);
        }

        RuntimeException e = new IllegalArgumentException("Can not load encryption key data: " + keyName);
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
        throw e;
    }

    /**
     * Encrypt input data
     */
    public static String encrypt(String src) {
        if (src == null || isEncrypted(src)) {
            return src;
        }
        try {
            ComponentKeySource ks = getKeySource(ComponentKeySource.KEY_SYSTEM_PREFIX, true);
            StringBuilder sb = new StringBuilder();
            sb.append(ENCRYPTION_PREFIX);
            sb.append(ks.getKeyName());
            sb.append(":");
            sb.append(new Encryption(ks, CipherSources.getDefault()).encrypt(src));
            return sb.toString();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Decrypt input data
     */
    public static String decrypt(String src) {
        if (!isEncrypted(src)) {
            return src;
        }

        try {
            String[] encryptedData = src.split("\\:");
            ComponentKeySource ks = getKeySource(encryptedData[1], false);
            return new Encryption(ks, CipherSources.getDefault()).decrypt(encryptedData[2]);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Test whether input data is encrypted
     */
    public static boolean isEncrypted(String src) {
        return src != null && REG_ENCRYPTED_DATA.matcher(src).matches();
    }

    private ComponentEncryption() {

    }
}
