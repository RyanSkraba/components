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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.talend.daikon.crypto.KeySource;


/*
* Created by bhe on Oct 30, 2019
*/
public class ComponentKeySource implements KeySource {

    private static final Logger LOGGER = Logger.getLogger(ComponentKeySource.class.getName());

    private static final String ENCRYPTION_KEY_FILE_SYS_PROP = "encryption.keys.file";

    public static final String KEY_SYSTEM_PREFIX = "system.encryption.key.v";

    private static final String DEFAULT_KEY_FILE_NAME = "component.keys";

    private String keyName;

    private final boolean isEncrypt;

    private final Properties availableKeys;

    private ComponentKeySource(Properties allKeys, String keyName, boolean isEncrypt) {
        this.availableKeys = allKeys;
        this.keyName = keyName;
        this.isEncrypt = isEncrypt;
        if (this.isEncrypt) {
            this.keyName = this.availableKeys.stringPropertyNames().stream().max(Comparator.comparing(e -> getVersion(e))).get();
        }
    }

    /**
     * <p>
     * always get encryption key, key name format: {keyname}.{version}
     * </p>
     * <p>
     * for example, system.encryption.key.v1
     * </p>
     * 
     * @param allKeys all of key properties
     * @param keyName requested encryption key name
     * @param isEncrypt indicate whether the encryption key is used for encryption
     */
    public static ComponentKeySource key(Properties allKeys, String keyName, boolean isEncrypt) {
        return new ComponentKeySource(allKeys, keyName, isEncrypt);
    }

    @Override
    public byte[] getKey() throws Exception {
        String keyToLoad = this.getKeyName();
        // load key
        String key = availableKeys.getProperty(keyToLoad);
        if (key == null) {
            LOGGER.log(Level.WARNING, "Can not load " + keyToLoad);
            throw new IllegalArgumentException("Invalid encryption key: " + keyToLoad);
        } else {
            LOGGER.log(Level.FINER, "Loaded " + keyToLoad);
            return Base64.getDecoder().decode(key.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static int getVersion(String keyName) {
        int idx = keyName.lastIndexOf('.');
        try {
            return Integer.parseInt(keyName.substring(idx + 2));
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Parse version of encryption key error, key: " + keyName);
        }
        return 0;
    }

    public String getKeyName() {
        return this.keyName;
    }

    public static Properties loadAllKeys() {

        Properties keyProperties = new Properties();
        // load default keys from jar
        try (InputStream fi = ComponentKeySource.class.getResourceAsStream(DEFAULT_KEY_FILE_NAME)) {
            keyProperties.load(fi);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        // load from file set in system property, so as to override default keys
        String keyPath = System.getProperty(ENCRYPTION_KEY_FILE_SYS_PROP);
        if (keyPath != null) {
            File keyFile = new File(keyPath);
            if (keyFile.exists()) {
                Properties tempProperties = new Properties();
                try (InputStream fi = new FileInputStream(keyFile)) {
                    tempProperties.load(fi);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
                // load key whose name starts with KEY_SYSTEM_PREFIX
                tempProperties.forEach((k, v) -> {
                    String key = String.valueOf(k);
                    if (key.startsWith(KEY_SYSTEM_PREFIX)) {
                        keyProperties.put(key, v);
                    }
                });
            }
        }

        // load system key data from System properties
        System.getProperties().forEach((k, v) -> {
            String key = String.valueOf(k);
            if (key.startsWith(KEY_SYSTEM_PREFIX)) {
                keyProperties.put(key, v);
            }
        });

        if (LOGGER.isLoggable(Level.FINER)) {
            keyProperties.stringPropertyNames().forEach((k) -> LOGGER.log(Level.FINER, k));
        }

        return keyProperties;
    }

}
