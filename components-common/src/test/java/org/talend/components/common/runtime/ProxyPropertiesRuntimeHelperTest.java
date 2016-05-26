package org.talend.components.common.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.talend.components.common.ProxyProperties;

public class ProxyPropertiesRuntimeHelperTest {

    @Test
    public void testSetProxy() {

        // Remove Socks proxy for current test

        String useSocks = ProxyPropertiesRuntimeHelper.removeProperty("socksProxySet");
        String socksProxyHost = ProxyPropertiesRuntimeHelper.removeProperty("socksProxyHost");
        String socksProxyPort = ProxyPropertiesRuntimeHelper.removeProperty("socksProxyPort");
        String socksProxyUserName = ProxyPropertiesRuntimeHelper.removeProperty("java.net.socks.username");
        String socksProxyUserPwd = ProxyPropertiesRuntimeHelper.removeProperty("java.net.socks.password");

        assertNull(System.getProperty("socksProxySet"));
        assertNull(System.getProperty("socksProxyHost"));
        assertNull(System.getProperty("socksProxyPort"));
        assertNull(System.getProperty("java.net.socks.username"));
        assertNull(System.getProperty("java.net.socks.password"));

        // Init a ProxyProperties with empty proxy information
        ProxyProperties proxyProperties = (ProxyProperties) new ProxyProperties("foo").init();
        proxyProperties.useProxy.setValue(true);

        ProxyPropertiesRuntimeHelper.setProxy(proxyProperties, ProxyProperties.ProxyType.SOCKS);

        assertEquals("true", System.getProperty("socksProxySet"));
        assertNull(System.getProperty("socksProxyHost"));
        assertNull(System.getProperty("socksProxyPort"));
        assertNull(System.getProperty("java.net.socks.username"));
        assertNull(System.getProperty("java.net.socks.password"));

        proxyProperties.host.setValue("192.168.32.162");
        proxyProperties.port.setValue(1080);
        proxyProperties.userPassword.userId.setValue("talend");

        ProxyPropertiesRuntimeHelper.setProxy(proxyProperties, ProxyProperties.ProxyType.SOCKS);

        assertEquals("true", System.getProperty("socksProxySet"));
        assertEquals("192.168.32.162", System.getProperty("socksProxyHost"));
        assertEquals("1080", System.getProperty("socksProxyPort"));
        assertEquals("talend", System.getProperty("java.net.socks.username"));
        assertNull(System.getProperty("java.net.socks.password"));

        // Revert the modification about the Socks proxy during the test
        ProxyPropertiesRuntimeHelper.setPropertyValue("socksProxySet", useSocks);
        ProxyPropertiesRuntimeHelper.setPropertyValue("socksProxyHost", socksProxyHost);
        ProxyPropertiesRuntimeHelper.setPropertyValue("socksProxyPort", socksProxyPort);
        ProxyPropertiesRuntimeHelper.setPropertyValue("java.net.socks.username", socksProxyUserName);
        ProxyPropertiesRuntimeHelper.setPropertyValue("java.net.socks.password", socksProxyUserPwd);

    }
}
