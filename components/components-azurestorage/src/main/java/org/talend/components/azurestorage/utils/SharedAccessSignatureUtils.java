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
package org.talend.components.azurestorage.utils;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SharedAccessSignatureUtils {

    String sharedAccessSignature;

    String protocol;

    String account;

    String service;

    String token;

    private static final String SAS_PATTERN = "(http.?)?://(.*)\\.(blob|file|queue|table)\\.core\\.windows\\.net\\/(.*)";

    public SharedAccessSignatureUtils(String sas, String protocol, String account, String service, String sap) {
        this.sharedAccessSignature = sas;
        this.protocol = protocol;
        this.account = account;
        this.service = service;
        this.token = sap;
    }

    public static SharedAccessSignatureUtils getSharedAccessSignatureUtils(String sas) {
        Matcher m = Pattern.compile(SharedAccessSignatureUtils.SAS_PATTERN).matcher(sas);
        if (m.matches()) {
            return new SharedAccessSignatureUtils(sas, m.group(1), m.group(2), m.group(3), m.group(4));
        }
        return null;
    }

    public URI getURI() throws Throwable {
        return new URI(sharedAccessSignature);
    }

    public String getSharedAccessSignature() {
        return sharedAccessSignature;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getAccount() {
        return account;
    }

    public String getService() {
        return service;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return String.format("protocol=%1$s; service=%2$s; account=%3$s", protocol, service, account);
    }

}
