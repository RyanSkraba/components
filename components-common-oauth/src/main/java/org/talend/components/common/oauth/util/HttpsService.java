package org.talend.components.common.oauth.util;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

/**
 * created by bchen on Aug 26, 2013 Detailled comment
 * 
 */
public class HttpsService {

    Server server;

    /**
     * DOC bchen HttpService constructor comment.
     * 
     * @throws Exception
     */
    public HttpsService(String host, int port, Handler handler) throws Exception {
        server = new Server();

        SslSelectChannelConnector ssl_connector = new SslSelectChannelConnector();
        ssl_connector.setHost(host);
        ssl_connector.setPort(port);
        SslContextFactory cf = ssl_connector.getSslContextFactory();
        cf.setKeyStorePath(HttpsService.class.getResource("sslkey").toString());
        cf.setKeyStorePassword("talend");
        cf.setKeyManagerPassword("talend");
        server.addConnector(ssl_connector);
        server.setHandler(handler);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
        server.join();
    }

}
