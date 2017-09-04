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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

public class Auth2ImplicitGrantServerTest {

    private static final String hostName = "localhost";

    private static final String CRLF = "\r\n";

    /**
     * Test server timeout is effective
     */
    @Test
    public void testSocketServerTimeout() throws IOException, InterruptedException {

        final OAuth2ImplicitGrantServer server = new OAuth2ImplicitGrantServer("localhost", 0, 1000);
        ExecutorService executors = Executors.newFixedThreadPool(1);
        Future<?> f = executors.submit(server);
        try {
            f.get();
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof RuntimeException) && !(e.getCause().getCause() instanceof SocketTimeoutException)) {
                fail("Expected RuntimeException caused by SocketTimeoutException but was " + e);
            }
            return;
        } finally {
            server.stop();
        }
        fail("Expected RuntimeException caused by SocketTimeoutException but was no error");
    }

    @Test
    public void testReturnedAuthCodeOk() throws IOException, InterruptedException {
        final OAuth2ImplicitGrantServer server = new OAuth2ImplicitGrantServer(hostName, 0, 60 * 1000);
        ExecutorService executors = Executors.newFixedThreadPool(1);
        executors.submit(server);
        Thread.sleep(1000);// wait for the server to start
        assertNotEquals(-1, server.getLocalPort()); // Correctly binded to an address and port
        Socket client = null;
        try {
            // connect to the server socket
            // simulate incoming authorization code
            client = new Socket(hostName, server.getLocalPort());
            PrintWriter out = new PrintWriter(client.getOutputStream(), false);
            String authCode = "LEAMKFITGFK125421";
            out.write("GET /?code=" + authCode + " HTTP/1.1" + CRLF);
            out.flush();
            long start = System.currentTimeMillis();
            while (server.getAuthorizationCode() == null && (System.currentTimeMillis() - start) < 2000) { // wait for server
                // response max 2 sec
                Thread.sleep(500);
            }
            assertEquals(authCode, server.getAuthorizationCode());
        } finally {
            if (client != null) {
                client.close();
            }
            server.stop();
        }
    }

    @Test
    public void testReturnedAuthCodeEmpty() throws IOException, InterruptedException {
        final OAuth2ImplicitGrantServer server = new OAuth2ImplicitGrantServer(hostName, 0, 60 * 1000);
        ExecutorService executors = Executors.newFixedThreadPool(1);
        executors.submit(server);
        Thread.sleep(1000);// wait for the server to start
        assertNotEquals(-1, server.getLocalPort()); // Correctly binded to an address and port
        Socket client = null;
        try {
            // connect to the server socket
            // simulate empty incoming authorization code
            client = new Socket(hostName, server.getLocalPort());
            PrintWriter out = new PrintWriter(client.getOutputStream(), false);
            out.write("GET /?code= HTTP/1.1" + CRLF);
            out.flush();
            long start = System.currentTimeMillis();
            while (server.getAuthorizationCode() == null && (System.currentTimeMillis() - start) < 2000) { // wait for server
                                                                                                           // response max 2 sec
                Thread.sleep(500);
            }
            assertNull(server.getAuthorizationCode());
        } finally {
            if (client != null) {
                client.close();
            }
            server.stop();
        }
    }

    @Test
    public void testReturnedAuthCodeAbsent() throws IOException, InterruptedException {
        final OAuth2ImplicitGrantServer server = new OAuth2ImplicitGrantServer(hostName, 0, 60 * 1000);
        ExecutorService executors = Executors.newFixedThreadPool(1);
        executors.submit(server);
        Thread.sleep(1000);// wait for the server to start
        assertNotEquals(-1, server.getLocalPort()); // Correctly binded to an address and port
        Socket client = null;
        try {
            // connect to the server socket
            // simulate no incoming authorization code
            client = new Socket(hostName, server.getLocalPort());
            PrintWriter out = new PrintWriter(client.getOutputStream(), false);
            out.write("GET / HTTP/1.1" + CRLF);
            out.flush();
            long start = System.currentTimeMillis();
            while (server.getAuthorizationCode() == null && (System.currentTimeMillis() - start) < 2000) { // wait for server
                                                                                                           // response max 2 sec
                Thread.sleep(500);
            }
            assertNull(server.getAuthorizationCode());
        } finally {
            if (client != null) {
                client.close();
            }
            server.stop();
        }
    }
}
