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

import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.property.Property;

/*
 * <p>The purpose of this class is to encrypt and decrypt component's password fields with AES algorithm</p> <p>
 * <ul>Decrypt password field with AES or DES algorithm is based on the value of password field</ul> <ul>Encrypt
 * password field with AES algorithm always from now on, since DES is deprecated</ul> </p> Created by bhe on Oct 8, 2019
 */
public class ComponentBasePropertiesImpl extends PropertiesImpl {

    /**
     * @param name
     */
    public ComponentBasePropertiesImpl(String name) {
        super(name);
    }

    @Override
    protected void encryptData(Property property, boolean encrypt) {
        if (property.getStoredValue() == null) {
            return;
        }

        String storedValue = String.valueOf(property.getStoredValue());

        if (encrypt) {
            // always encrypt data by AES
            property.setStoredValue(ComponentEncryption.encrypt(storedValue));
        } else {
            if (ComponentEncryption.isEncrypted(storedValue)) {
                property.setStoredValue(ComponentEncryption.decrypt(storedValue));
            } else {
                // fallback to CryptoHelper
                super.encryptData(property, encrypt);
            }
        }
    }
}
