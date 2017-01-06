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
package org.talend.components.common.runtime;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

import org.talend.components.common.ProxyProperties;

public class ProxyPropertiesRuntimeHelper {

    private String proxyHost;

    private String proxyPort;

    private String proxyUser;

    private String proxyPwd;

    private Proxy socketProxy;

    public ProxyPropertiesRuntimeHelper(ProxyProperties proxySetting) {
        if (proxySetting.useProxy.getValue()) {// proxy setting from component setting
            proxyHost = proxySetting.host.getStringValue();
            proxyPort = proxySetting.port.getStringValue();
            proxyUser = proxySetting.userPassword.userId.getStringValue();
            proxyPwd = proxySetting.userPassword.password.getStringValue();

            // use socks as default like before
            SocketAddress addr = new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort));
            socketProxy = new Proxy(Proxy.Type.SOCKS, addr);
        } else if (System.getProperty("https.proxyHost") != null) {// set by other components like tSetProxy
            proxyHost = System.getProperty("https.proxyHost");
            proxyPort = System.getProperty("https.proxyPort");
            proxyUser = System.getProperty("https.proxyUser");
            proxyPwd = System.getProperty("https.proxyPassword");
        } else if (System.getProperty("http.proxyHost") != null) {
            proxyHost = System.getProperty("http.proxyHost");
            proxyPort = System.getProperty("http.proxyPort");
            proxyUser = System.getProperty("http.proxyUser");
            proxyPwd = System.getProperty("http.proxyPassword");
        } else if (System.getProperty("socksProxyHost") != null) {
            proxyHost = System.getProperty("socksProxyHost");
            proxyPort = System.getProperty("socksProxyPort");
            proxyUser = System.getProperty("java.net.socks.username");
            proxyPwd = System.getProperty("java.net.socks.password");

            SocketAddress addr = new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort));
            socketProxy = new Proxy(Proxy.Type.SOCKS, addr);
        }
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public String getProxyPwd() {
        return proxyPwd;
    }

    public Proxy getSocketProxy() {
        return socketProxy;
    }

}
