package org.talend.components.salesforce.runtime;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.ProxyAuthenticator;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.talend.components.api.component.EndpointComponentDefinition;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.daikon.properties.ValidationResult;

public class SalesforceProxyTestIT extends SalesforceTestBase {

    private static HttpProxyServer server;

    private static final String proxyUsername = "talend_username";

    private static final String proxyPassword = "talend_password";

    private static final int proxyPort = 9999;

    @BeforeClass
    public static void setupProxy() {
        ProxyAuthenticator auth = new ProxyAuthenticator() {

            @Override
            public boolean authenticate(String username, String password) {
                return proxyUsername.equals(username) && proxyPassword.equals(password);
            }

            @Override
            public String getRealm() {
                return null;
            }

        };
        server = DefaultHttpProxyServer.bootstrap().withPort(proxyPort).withProxyAuthenticator(auth).start();

        setProxySettingForClient();
    }

    @Test
    public void testProxy() {
        TSalesforceConnectionDefinition definition = (TSalesforceConnectionDefinition) getComponentService()
                .getComponentDefinition(TSalesforceConnectionDefinition.COMPONENT_NAME);
        SalesforceConnectionProperties properties = (SalesforceConnectionProperties) definition.createRuntimeProperties();

        properties.userPassword.userId.setValue(userId);
        properties.userPassword.password.setValue(password);
        properties.userPassword.securityKey.setValue(securityKey);

        SourceOrSink sourceOrSink = ((EndpointComponentDefinition) definition).getRuntime();
        sourceOrSink.initialize(null, properties);
        org.talend.daikon.properties.ValidationResult vr = sourceOrSink.validate(null);
        Assert.assertEquals(ValidationResult.Result.OK, vr.getStatus());
    }

    private static void setProxySettingForClient() {
        System.setProperty("http.proxySet", "true");
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "" + proxyPort);
        System.setProperty("http.proxyUser", proxyUsername);
        System.setProperty("http.proxyPassword", proxyPassword);

        Authenticator.setDefault(new Authenticator() {

            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
            }

        });
    }

    private static void removeProxySettingForClient() {
        System.clearProperty("http.proxySet");
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("http.proxyUser");
        System.clearProperty("http.proxyPassword");
    }

    @AfterClass
    public static void closeProxy() {
        removeProxySettingForClient();

        if (server != null) {
            server.stop();
        }
        server = null;
    }
}
