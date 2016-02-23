// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Property;

public class SalesforceTestHelper {

    public static final boolean ADD_QUOTES = true;

    static final String userId = System.getProperty("salesforce.user");

    static final String password = System.getProperty("salesforce.password");

    static final String securityKey = System.getProperty("salesforce.key");

    public static SalesforceConnectionProperties setupProps(SalesforceConnectionProperties props, boolean addQuotes) {
        if (props == null) {
            props = (SalesforceConnectionProperties) new SalesforceConnectionProperties("foo").init();
        }
        ComponentProperties userPassword = (ComponentProperties) props.getProperty("userPassword");
        ((Property) userPassword.getProperty("userId")).setValue(addQuotes ? "\"" + userId + "\"" : userId);
        ((Property) userPassword.getProperty("password")).setValue(addQuotes ? "\"" + password + "\"" : password);
        ((Property) userPassword.getProperty("securityKey")).setValue(addQuotes ? "\"" + securityKey + "\"" : securityKey);
        return props;
    }

    public static final String EXISTING_MODULE_NAME = "Account";

    public static final String NOT_EXISTING_MODULE_NAME = "foobar";

}
