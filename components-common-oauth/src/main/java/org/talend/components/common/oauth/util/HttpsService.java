// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.common.oauth.util;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * created by bchen on Aug 26, 2013 Detailled comment
 * 
 */
public class HttpsService {

    private Server server;

    /**
     * Create and start a jetty running HTTPS.
     * 
     * @throws Exception if an error occurs.
     */
    public HttpsService(String host, int port, Handler handler) throws Exception {

        server = new Server();

        // ssl context factory
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(HttpsService.class.getResource("sslkey").toString());
        sslContextFactory.setKeyStorePassword("talend");
        sslContextFactory.setKeyManagerPassword("talend");

        // ssl connection factory (based on the ssl context factory)
        final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());
        HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.setSecureScheme("https");
        httpsConfig.setSecurePort(port);

        // http connection factory (based on the ssl connection factory)
        final HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpsConfig);
        ServerConnector https = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
        https.setPort(port);
        https.setHost(host);

        // server setup
        server.addConnector(https);
        server.setHandler(handler);
        server.start();
    }

    /**
     * Stops the server.
     * @throws Exception if an error occurs.
     */
    public void stop() throws Exception {
        server.stop();
        server.join();
    }

}
