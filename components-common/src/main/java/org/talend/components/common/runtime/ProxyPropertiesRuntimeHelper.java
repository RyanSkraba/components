package org.talend.components.common.runtime;

import org.talend.components.common.ProxyProperties;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class ProxyPropertiesRuntimeHelper {

    /**
     * Set proxy configuration with ProxyProperties type and Proxy type
     */
    public static void setProxy(final ProxyProperties properties, ProxyProperties.ProxyType type) {
        if (properties.useProxy.getValue()) {
            setProxyProperties(properties, type);
        } else {
            removeProxyProperties(type);
        }
    }

    private static void setProxyProperties(ProxyProperties properties, ProxyProperties.ProxyType type) {
        Authenticator authenticator = new ProxyAuth(properties.userPassword.userId.getStringValue(), properties.userPassword.password.getStringValue());
        if (ProxyProperties.ProxyType.HTTP.equals(type)) {
            setPropertyValue("http.proxySet", "true");
            setPropertyValue("http.proxyHost", properties.host.getStringValue());
            setPropertyValue("http.proxyPort", properties.port.getStringValue());
            setPropertyValue("http.nonProxyHosts", "192.168.0.* | localhost");
            setPropertyValue("http.proxyUser", properties.userPassword.userId.getStringValue());
            setPropertyValue("http.proxyPassword", properties.userPassword.password.getStringValue());
            Authenticator.setDefault(authenticator);
        } else if (ProxyProperties.ProxyType.SOCKS.equals(type)) {
            setPropertyValue("socksProxySet", "true");
            setPropertyValue("socksProxyHost", properties.host.getStringValue());
            setPropertyValue("socksProxyPort", properties.port.getStringValue());
            setPropertyValue("java.net.socks.username", properties.userPassword.userId.getStringValue());
            setPropertyValue("java.net.socks.password", properties.userPassword.password.getStringValue());
            Authenticator.setDefault(authenticator);
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

    private static void removeProxyProperties(ProxyProperties.ProxyType type) {
        if (ProxyProperties.ProxyType.HTTP.equals(type)) {
            removeProperty("http.proxySet");
            removeProperty("http.proxyHost");
            removeProperty("http.proxyPort");
            removeProperty("http.nonProxyHosts");
            removeProperty("http.proxyUser");
            removeProperty("http.proxyPassword");
            Authenticator.setDefault(null);
        } else if (ProxyProperties.ProxyType.SOCKS.equals(type)) {
            removeProperty("socksProxySet");
            removeProperty("socksProxyHost");
            removeProperty("socksProxyPort");
            removeProperty("java.net.socks.username");
            removeProperty("java.net.socks.password");
            Authenticator.setDefault(null);
        } else if (ProxyProperties.ProxyType.HTTPS.equals(type)) {
            removeProperty("https.proxyHost");
            removeProperty("https.proxyPort");
        } else if (ProxyProperties.ProxyType.FTP.equals(type)) {
            removeProperty("ftpProxySet");
            removeProperty("ftp.proxyHost");
            removeProperty("ftp.proxyPort");
            removeProperty("ftp.nonProxyHosts");
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

    static class ProxyAuth extends Authenticator {

        private PasswordAuthentication auth;

        private ProxyAuth(String userName, String password) {
            auth = new PasswordAuthentication(userName, password == null ? new char[]{} : password.toCharArray());
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return auth;
        }
    }

}
