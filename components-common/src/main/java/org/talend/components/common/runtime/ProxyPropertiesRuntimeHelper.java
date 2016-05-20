package org.talend.components.common.runtime;

import java.net.Authenticator;

import org.talend.components.common.ProxyProperties;

public class ProxyPropertiesRuntimeHelper {

    /**
     * Set proxy configuration with ProxyProperties type and Proxy type
     */
    public static void setProxy(final ProxyProperties properties, ProxyProperties.ProxyType type) {
        if (!properties.useProxy.getBooleanValue()) {
            return;
        }
        if (ProxyProperties.ProxyType.HTTP.equals(type)) {
            setPropertyValue("http.proxySet", "true");
            setPropertyValue("http.proxyHost", properties.host.getStringValue());
            setPropertyValue("http.proxyPort", properties.port.getStringValue());
            setPropertyValue("http.nonProxyHosts", "192.168.0.* | localhost");
            setPropertyValue("http.proxyUser", properties.userPassword.userId.getStringValue());
            setPropertyValue("http.proxyPassword", properties.userPassword.password.getStringValue());
            Authenticator.setDefault(new java.net.Authenticator() {

                @Override
                public java.net.PasswordAuthentication getPasswordAuthentication() {
                    return new java.net.PasswordAuthentication(properties.userPassword.userId.getStringValue(),
                            properties.userPassword.password.getStringValue().toCharArray());
                }
            });
        } else if (ProxyProperties.ProxyType.SOCKS.equals(type)) {
            setPropertyValue("socksProxySet", "true");
            setPropertyValue("socksProxyHost", properties.host.getStringValue());
            setPropertyValue("socksProxyPort", properties.port.getStringValue());
            setPropertyValue("java.net.socks.username", properties.userPassword.userId.getStringValue());
            setPropertyValue("java.net.socks.password", properties.userPassword.password.getStringValue());
        } else if (ProxyProperties.ProxyType.HTTPS.equals(type)) {
            setPropertyValue("https.proxyHost", properties.host.getStringValue());
            setPropertyValue("https.proxyPort", properties.port.getStringValue());
        } else if (ProxyProperties.ProxyType.FTP.equals(type)) {
            setPropertyValue("ftpProxySet", "true");
            setPropertyValue("ftp.proxyHost", properties.host.getStringValue());
            setPropertyValue("ftp.proxyPort", properties.port.getStringValue());
            setPropertyValue("ftp.nonProxyHosts", "192.168.0.* | localhost");
        }

    }

    public static void setPropertyValue(String name, String value) {
        if (name != null && value != null) {
            System.setProperty(name, value);
        }
    }

    public static String removeProperty(String name) {
        String value = null;
        if (name != null) {
            value = System.getProperty(name);
            System.clearProperty(name);
        }
        return value;
    }
}
