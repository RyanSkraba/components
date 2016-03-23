package org.talend.components.common.runtime;

import org.talend.components.common.ProxyProperties;

import java.net.Authenticator;

public class ProxyPropertiesRuntimeHelper {

    /**
     * Set proxy configuration with ProxyProperties type and Proxy type
     */
    public static void setProxy(ProxyProperties properties, ProxyProperties.ProxyType type) {
        if(!properties.useProxy.getBooleanValue()){
            return;
        }
        if (ProxyProperties.ProxyType.HTTP.equals(type)) {
            System.setProperty("http.proxySet", "true");
            System.setProperty("http.proxyHost", properties.host.getStringValue());
            System.setProperty("http.proxyPort", properties.port.getStringValue());
            System.setProperty("http.nonProxyHosts", "192.168.0.* | localhost");
            System.setProperty("http.proxyUser", properties.userPassword.userId.getStringValue());
            System.setProperty("http.proxyPassword", properties.userPassword.password.getStringValue());
            Authenticator.setDefault(
                    new java.net.Authenticator() {
                        public java.net.PasswordAuthentication getPasswordAuthentication() {
                            return new java.net.PasswordAuthentication(
                                    properties.userPassword.userId.getStringValue(),
                                    properties.userPassword.password.getStringValue().toCharArray());
                        }
                    }
            );
        } else if (ProxyProperties.ProxyType.SOCKS.equals(type)) {
            System.setProperty("socksProxySet", "true");
            System.setProperty("socksProxyHost", properties.host.getStringValue());
            System.setProperty("socksProxyPort", properties.port.getStringValue());
            System.setProperty("java.net.socks.username", properties.userPassword.userId.getStringValue());
            System.setProperty("java.net.socks.password", properties.userPassword.password.getStringValue());
        } else if (ProxyProperties.ProxyType.HTTPS.equals(type)) {
            System.setProperty("https.proxyHost", properties.host.getStringValue());
            System.setProperty("https.proxyPort", properties.port.getStringValue());
        }else if (ProxyProperties.ProxyType.FTP.equals(type)) {
            System.setProperty("ftpProxySet", "true");
            System.setProperty("ftp.proxyHost", properties.host.getStringValue());
            System.setProperty("ftp.proxyPort", properties.port.getStringValue());
            System.setProperty("ftp.nonProxyHosts", "192.168.0.* | localhost");
        }
    }
}
