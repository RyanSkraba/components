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
package org.talend.components.common.oauth.server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

/**
 * A simple socket server to retrieve authorization code in OAuth 2 implicit flow.<br/>
 * This socket server is used as a callback url in the implicit flow.
 * After the user authentication and consent. the oauth provider redirect the user to the callbakc url handled by this server.
 * This server handles only one connection then stop listening for farther incoming connections.
 * Note that this is not an HTTP server even if the incoming requests are HTTP formated.
 */
public class OAuth2ImplicitGrantServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2ImplicitGrantServer.class.getCanonicalName());

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(OAuth2ImplicitGrantServer.class);

    private static final String serverName = "Talend/Oauth2/0.0.1";

    private static final String CRLF = "\r\n";

    private static final String[] supportedCodeParams = { "code" };

    /** Requested maximum length of the queue of incoming connections. */
    private static final int backlog = 5;

    private int timeout;

    private String host;

    private int port;

    private ServerSocket server;

    private String authorizationCode;

    /**
     * @param host
     * @param port
     */
    public OAuth2ImplicitGrantServer(String host, int port, int timeOut) {
        super();
        this.host = host;
        this.port = port;
        this.timeout = timeOut;
    }

    @Override
    public void run() {

        try {
            InetAddress addr = InetAddress.getByName(host);
            server = new ServerSocket(port, backlog, addr);
            logger.info(messages.getMessage("msg.info.serverStarted", getLocalPort()));
            while (true) {
                authorizationCode = null;
                server.setSoTimeout(timeout);
                try (Socket clientSocket = server.accept()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null && !line.isEmpty()) {
                        logger.trace("<< " + line);
                        if (validGetRequestLine(line)) {
                            authorizationCode = extractAuthorizationCode(line);
                        }

                        if (authorizationCode != null) {
                            break;
                        }
                    }

                    OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream());
                    Date now = new Date();
                    String response = "";
                    if (authorizationCode == null) {
                        response = "<h1>ERROR 500</h1><p>Can't retrieve authorization code</p>";
                        osw.write("HTTP/1.0 500 Can't retrieve authorization code" + CRLF);
                    } else {
                        response = "<p>Authorization code extracted successfully!</p>";
                        osw.write("HTTP/1.0 200 OK" + CRLF);
                    }
                    osw.write("Date:" + now + CRLF);
                    osw.write("Server: " + serverName + CRLF);
                    osw.write("Content-Type: text/html" + CRLF);
                    osw.write("Content-Length:" + response.length() + CRLF);
                    osw.write("\r\n");
                    osw.write(response);
                    osw.flush();
                    logger.info(messages.getMessage("msg.info.serverStoppedListening"));
                    break; // stop listening for connection
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extract authorization code form HTTP request line
     * 
     * @throws UnsupportedEncodingException
     */
    private String extractAuthorizationCode(String line) throws UnsupportedEncodingException {
        String requestParam = line.replace("GET", "");
        requestParam = requestParam.replace("/", "");
        requestParam = requestParam.replace("?", "");
        requestParam = requestParam.substring(0, requestParam.indexOf("HTTP")).trim();
        String[] params = requestParam.split("&");
        for (int i = 0; i < params.length; i++) {
            String[] keyValue = params[i].split("=");
            if (keyValue.length == 2) { // check if the both key & value exist
                for (String param : supportedCodeParams) {
                    if (param.equals(keyValue[0])) {
                        return URLDecoder.decode(keyValue[1], "UTF-8");
                    }
                }
            }
        }

        return null;
    }

    /**
     * return local port or -1 if server is not started or is null
     */
    public int getLocalPort() {
        if (server != null && server.isBound()) {
            return server.getLocalPort();
        }
        return -1;
    }

    /**
     * Check if the HTTP request is a GET request and that it contains valid request parameters
     */
    private boolean validGetRequestLine(String line) {
        if (line != null && !line.isEmpty() && line.contains("GET")) {
            for (String param : supportedCodeParams) {
                if (line.contains(param)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public synchronized void stop() throws IOException {
        if (server != null && !server.isClosed()) {
            server.close();
            logger.info(messages.getMessage("msg.info.serverStopped"));
        }
    }
}
