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

package org.talend.components.netsuite.test;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

/**
 * Finds an available port on localhost.
 */
public class FreePortFinder {

    // the ports below 1024 are system ports
    public static final int MIN_PORT_NUMBER = 1024;

    // the ports above 49151 are dynamic and/or private
    public static final int MAX_PORT_NUMBER = 49151;

    /**
     * Finds a free port between
     * {@link #MIN_PORT_NUMBER} and {@link #MAX_PORT_NUMBER}.
     *
     * @return a free port
     * @throw RuntimeException if a port could not be found
     */
    public static int findFreePort() {
        return findFreePort(MIN_PORT_NUMBER, MAX_PORT_NUMBER);
    }

    /**
     * Finds a free port between
     * <code>minPortNumber</code> and <code>maxPortNumber</code>.
     *
     * @return a free port
     * @throw RuntimeException if a port could not be found
     */
    public static int findFreePort(int minPortNumber, int maxPortNumber) {
        for (int i = minPortNumber; i <= maxPortNumber; i++) {
            if (available(i)) {
                return i;
            }
        }
        throw new RuntimeException("Could not find an available port between " +
                minPortNumber + " and " + maxPortNumber);
    }

    /**
     * Returns true if the specified port is available on this host.
     *
     * @param port the port to check
     * @return true if the port is available, false otherwise
     */
    private static boolean available(final int port) {
        ServerSocket serverSocket = null;
        DatagramSocket dataSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            dataSocket = new DatagramSocket(port);
            dataSocket.setReuseAddress(true);
            return true;
        } catch (final IOException e) {
            return false;
        } finally {
            if (dataSocket != null) {
                dataSocket.close();
            }
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (final IOException e) {
                    // can never happen
                }
            }
        }
    }
}