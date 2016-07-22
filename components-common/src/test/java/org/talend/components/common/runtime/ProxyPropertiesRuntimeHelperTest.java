package org.talend.components.common.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.talend.components.common.ProxyProperties;

public class ProxyPropertiesRuntimeHelperTest {

    @Test
    public void testSetProxy() {

        ProxyProperties proxyProperties = (ProxyProperties) new ProxyProperties("proxy").init();
        proxyProperties.useProxy.setValue(true);
        proxyProperties.host.setValue("192.168.32.162");
        proxyProperties.port.setValue(1080);
        proxyProperties.userPassword.userId.setValue("talend");
        proxyProperties.userPassword.password.setValue("talend");

        final ProxyPropertiesRuntimeHelper proxyHelper1 = new ProxyPropertiesRuntimeHelper(proxyProperties);

        assertEquals("192.168.32.162", proxyHelper1.getProxyHost());
        assertEquals("1080", proxyHelper1.getProxyPort());
        assertEquals("talend", proxyHelper1.getProxyUser());
        assertEquals("talend", proxyHelper1.getProxyPwd());
        assertNotNull(proxyHelper1.getSocketProxy());

        proxyProperties.useProxy.setValue(false);
        final ProxyPropertiesRuntimeHelper proxyHelper2 = new ProxyPropertiesRuntimeHelper(proxyProperties);

        assertNull(proxyHelper2.getProxyHost());
        assertNull(proxyHelper2.getProxyPort());
        assertNull(proxyHelper2.getProxyUser());
        assertNull(proxyHelper2.getProxyPwd());
        assertNull(proxyHelper2.getSocketProxy());

        setProxySettingForHttp();
        proxyProperties.useProxy.setValue(false);
        try {
            final ProxyPropertiesRuntimeHelper proxyHelper3 = new ProxyPropertiesRuntimeHelper(proxyProperties);
            assertEquals("192.168.32.162", proxyHelper3.getProxyHost());
            assertEquals("1080", proxyHelper3.getProxyPort());
            assertEquals("talend", proxyHelper3.getProxyUser());
            assertEquals("talend", proxyHelper3.getProxyPwd());
            assertNull(proxyHelper3.getSocketProxy());
        } finally {
            removeProxySettingForHttp();
        }

        setProxySettingForSocks();
        proxyProperties.useProxy.setValue(false);
        try {
            final ProxyPropertiesRuntimeHelper proxyHelper4 = new ProxyPropertiesRuntimeHelper(proxyProperties);
            assertEquals("192.168.32.162", proxyHelper4.getProxyHost());
            assertEquals("1080", proxyHelper4.getProxyPort());
            assertEquals("talend", proxyHelper4.getProxyUser());
            assertEquals("talend", proxyHelper4.getProxyPwd());
            assertNotNull(proxyHelper4.getSocketProxy());
        } finally {
            removeProxySettingForSocks();
        }

        setProxySettingForHttps();
        proxyProperties.useProxy.setValue(false);
        try {
            final ProxyPropertiesRuntimeHelper proxyHelper5 = new ProxyPropertiesRuntimeHelper(proxyProperties);
            assertEquals("192.168.32.162", proxyHelper5.getProxyHost());
            assertEquals("1080", proxyHelper5.getProxyPort());
            assertEquals("talend", proxyHelper5.getProxyUser());
            assertEquals("talend", proxyHelper5.getProxyPwd());
            assertNull(proxyHelper5.getSocketProxy());
        } finally {
            removeProxySettingForHttps();
        }

    }

    private void setProxySettingForHttp() {
        System.setProperty("http.proxyHost", "192.168.32.162");
        System.setProperty("http.proxyPort", "1080");
        System.setProperty("http.proxyUser", "talend");
        System.setProperty("http.proxyPassword", "talend");
    }

    private void removeProxySettingForHttp() {
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("http.proxyUser");
        System.clearProperty("http.proxyPassword");
    }

    private void setProxySettingForSocks() {
        System.setProperty("socksProxyHost", "192.168.32.162");
        System.setProperty("socksProxyPort", "1080");
        System.setProperty("java.net.socks.username", "talend");
        System.setProperty("java.net.socks.password", "talend");
    }

    private void removeProxySettingForSocks() {
        System.clearProperty("socksProxyHost");
        System.clearProperty("socksProxyPort");
        System.clearProperty("java.net.socks.username");
        System.clearProperty("java.net.socks.password");
    }

    private void setProxySettingForHttps() {
        System.setProperty("https.proxyHost", "192.168.32.162");
        System.setProperty("https.proxyPort", "1080");
        System.setProperty("https.proxyUser", "talend");
        System.setProperty("https.proxyPassword", "talend");
    }

    private void removeProxySettingForHttps() {
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("https.proxyUser");
        System.clearProperty("https.proxyPassword");
    }
}
