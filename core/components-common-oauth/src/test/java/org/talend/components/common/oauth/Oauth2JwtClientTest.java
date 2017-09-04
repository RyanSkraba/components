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
package org.talend.components.common.oauth;

import static org.junit.Assert.assertNotNull;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.codehaus.jackson.JsonNode;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.common.oauth.Jwt.ClaimsSet;
import org.talend.components.common.oauth.Jwt.JwsHeader;
import org.talend.components.common.oauth.Jwt.JwsHeader.Algorithm;
import org.talend.components.common.oauth.Oauth2JwtClientTest.JwtTestServer.UseCase;

public class Oauth2JwtClientTest {

    public static final String clientId = "3MVGrTwRf4m.I1VyuL3dDK_voQp_k1HOQ.df";

    public static final String subject = "user@talend.com";

    public static final String audience = "http://localhost";

    public static final String endpoint = "localhost";

    @Test
    public void testOK() throws UnsupportedEncodingException, IOException, InterruptedException, URISyntaxException {
        // start server
        JwtTestServer server = new JwtTestServer(UseCase.OK);
        Thread serverThread = new Thread(server);
        serverThread.start();

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");

        // Get Access Token
        JsonNode accessToken = Oauth2JwtClient.builder()//
                .withJwsHeaders(jwsHeaders())//
                .withJwtClaims(claims())//
                .signWithX509Key(x509Key(), org.talend.components.common.oauth.X509Key.Algorithm.SHA256withRSA)//
                .fromTokenEndpoint("http://" + endpoint + ":" + server.getLocalPort())//
                .withPlayloadParams(params)//
                .build()//
                .getAccessToken();

        serverThread.join(30000);

        assertNotNull(accessToken);
        assertNotNull(accessToken.get("access_token"));
    }

    @Test(expected = RuntimeException.class)
    public void testError() throws UnsupportedEncodingException, IOException, InterruptedException, URISyntaxException {
        // start server
        JwtTestServer server = new JwtTestServer(UseCase.ERROR);
        Thread serverThread = new Thread(server);
        serverThread.start();

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");

        // Get Access Token
        JsonNode accessToken = Oauth2JwtClient.builder()//
                .withJwsHeaders(jwsHeaders())//
                .withJwtClaims(claims())//
                .signWithX509Key(x509Key(), org.talend.components.common.oauth.X509Key.Algorithm.SHA256withRSA)//
                .fromTokenEndpoint("http://" + endpoint + ":" + server.getLocalPort())//
                .withPlayloadParams(params)//
                .build()//
                .getAccessToken();
        serverThread.join(30000);

        assertNotNull(accessToken);
        assertNotNull(accessToken.get("access_token"));
    }

    private ClaimsSet claims() {
        final long now = new Date().getTime() / 1000;
        return new ClaimsSet.Builder()//
                .id(UUID.randomUUID().toString())//
                .issuer(clientId)//
                .subject(subject)//
                .audience(audience)//
                .notBeforeTime(now)//
                .expirationTime(now + (5 * 1000))//
                .build();
    }

    private JwsHeader jwsHeaders() {
        return new JwsHeader.Builder()//
                .algorithm(Algorithm.RS256).build();

    }

    private X509Key x509Key() throws URISyntaxException {
        return X509Key.builder()//
                .keyStorePath(getClass().getClassLoader().getResource("00D0Y000001dveq.jks").toURI().getPath())//
                .keyStorePassword("talend2017")// store pwd
                .certificateAlias("jobcert")// certificate alias
                .build();
    }

    public static final class JwtTestServer implements Runnable {

        public static enum UseCase {
            OK,
            ERROR
        }

        private static final Logger logger = LoggerFactory.getLogger(JwtTestServer.class.getCanonicalName());

        private static final String CRLF = "\r\n";

        private UseCase usecase;

        private int localPort;

        public JwtTestServer(UseCase uc) {
            this.usecase = uc;
        }

        public synchronized int getLocalPort() {
            return localPort;
        }

        @Override
        public void run() {
            ServerSocket server = null;
            try {
                InetAddress addr = InetAddress.getByName(endpoint);
                server = new ServerSocket(0, 3, addr);
                localPort = server.getLocalPort();

                logger.info("Talend Server start listening for incoming connections on port " + localPort);
                while (true) {
                    server.setSoTimeout(30 * 1000);
                    try (Socket clientSocket = server.accept()) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String line;
                        int incomingContentLenth = 0;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("<< " + line);
                            if (line.contains("Content-Length")) {
                                incomingContentLenth = Integer.valueOf(line.split(":")[1].trim());
                            }
                            if (line.isEmpty()) {
                                // start reading content playLoad
                                StringBuilder body = new StringBuilder();
                                for (int i = 0; i < incomingContentLenth; i++) {
                                    int c = reader.read();
                                    body.append((char) c);
                                }
                                System.out.println(body.toString());
                                break;
                            }
                        }

                        OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream());
                        String response = "";
                        switch (usecase) {
                        case OK:
                            osw.write("HTTP/1.0 200 OK" + CRLF);
                            response = "{\"access_token\":\"00D0q!AR4AQCQ_Dsb.7Zo_b0ME3G1q\", \"token_type\":\"Bearer\"}";
                            break;
                        case ERROR:
                            osw.write("HTTP/1.0 500 Internal Server Error" + CRLF);
                            response = "error";
                            break;
                        }
                        osw.write("Date:" + new Date() + CRLF);
                        osw.write("Server: Talend/0.1" + CRLF);
                        osw.write("Content-Type: text/html" + CRLF);
                        osw.write("Content-Length:" + response.length() + CRLF);
                        osw.write("\r\n");
                        osw.write(response);
                        osw.flush();

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    logger.info("Talend Server stoped listening for incoming connections");
                    break; // stop listening for connection
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (server != null) {
                    try {
                        server.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

    }

}
