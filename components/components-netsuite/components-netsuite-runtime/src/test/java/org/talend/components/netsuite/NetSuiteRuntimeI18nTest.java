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

package org.talend.components.netsuite;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

/**
 *
 */
public class NetSuiteRuntimeI18nTest {

    @Test
    public void testAllMessages() {
        assertI18nMessage("error.endpointUrlRequired");
        assertI18nMessage("error.apiVersionRequired");
        assertI18nMessage("error.emailRequired");
        assertI18nMessage("error.passwordRequired");
        assertI18nMessage("error.accountRequired");
        assertI18nMessage("error.roleRequired");
        assertI18nMessage("error.applicationIdRequired");
        assertI18nMessage("error.invalidEndpointUrl", "test123");
        assertI18nMessage("error.invalidApiVersion", "2016.2");
        assertI18nMessage("error.couldNotDetectApiVersionFromEndpointUrl", "test123");
        assertI18nMessage("error.endpointUrlApiVersionMismatch", "2016.2", "2014.2");
        assertI18nMessage("error.runtimeVersionMismatch", "2016.2", "2014.2");
        assertI18nMessage("error.invalidComponentPropertiesClass", "A", "B");
        assertI18nMessage("error.outputOperationNotImplemented", "QWERTY");
        assertI18nMessage("error.failedToConvertValueToJson", "Json error ...");
        assertI18nMessage("error.failedToConvertValueFromJson", "Json error ...");
        assertI18nMessage("error.searchDateField.invalidDateTimeFormat", "2O170101174306");
        assertI18nMessage("error.failedToInitClient", "null");
        assertI18nMessage("error.couldNotGetWebServiceDomain", "test123");
        assertI18nMessage("error.ssoLoginNotSupported");
        assertI18nMessage("error.failedToLogin", "INVALID_ACCNT Invalid account");
    }

    private void assertI18nMessage(String key, Object...arg) {
        String message = NetSuiteRuntimeI18n.MESSAGES.getMessage(key, arg);
        assertNotEquals(key, message);
    }
}
