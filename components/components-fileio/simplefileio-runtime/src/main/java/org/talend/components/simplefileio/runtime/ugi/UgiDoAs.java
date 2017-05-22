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
package org.talend.components.simplefileio.runtime.ugi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.Credentials;
import org.apache.hadoop.security.SaslRpcServer;
import org.apache.hadoop.security.UserGroupInformation;
import org.talend.daikon.exception.TalendRuntimeException;

/**
 * A storable, serializable strategy for executing actions using a {@link UserGroupInformation} on a cluster.
 */
public abstract class UgiDoAs implements Serializable {

    /**
     * @param action the action to execute.
     * @param <T> the return type of the action.
     * @return the return value of the action.
     * @throws Exception if there was any error in running the action.
     */
    public abstract <T> T doAs(PrivilegedExceptionAction<T> action) throws Exception;

    /**
     * @param action the action to execute.
     * @param <T> the return type of the action.
     * @return the return value of the action.
     */
    public abstract <T> T doAs(PrivilegedAction<T> action);

    /**
     * @return a strategy that directly executes the action without any {@link UserGroupInformation}.
     */
    public static UgiDoAs ofNone() {
        return new NullDoAs();
    }

    /**
     * @return a strategy that directly executes the action using SIMPLE authentication.
     */
    public static UgiDoAs ofSimple(String username) {
        return new SimpleDoAs(username);
    }

    /**
     * @return a strategy that directly executes the action using KERBEROS authentication.
     */
    public static UgiDoAs ofKerberos(String principal, String keytab) {
        return new KerberosKeytabDoAs(principal, keytab);
    }

    /**
     * Directly runs actions without passing by {@link UserGroupInformation}.
     */
    private static class NullDoAs extends UgiDoAs {

        @Override
        public <T> T doAs(PrivilegedAction<T> action) {
            return action.run();
        }

        @Override
        public <T> T doAs(PrivilegedExceptionAction<T> action) throws Exception {
            return action.run();
        }

    }

    /**
     * Uses a {@link UserGroupInformation} created by logging in as a specified user.
     */
    private static class SimpleDoAs extends UgiDoAs {

        private final String username;

        private transient UserGroupInformation ugi;

        private SimpleDoAs(String username) {
            this.username = username;
        }

        @Override
        public <T> T doAs(PrivilegedAction<T> action) {
            if (ugi == null)
                ugi = UserGroupInformation.createRemoteUser(username);
            return ugi.doAs(action);
        }

        @Override
        public <T> T doAs(PrivilegedExceptionAction<T> action) throws Exception {
            if (ugi == null)
                ugi = UserGroupInformation.createRemoteUser(username);
            return ugi.doAs(action);
        }
    }

    /**
     * Uses a {@link UserGroupInformation} created via Kerberos and a keytab.
     */
    private static class KerberosKeytabDoAs extends UgiDoAs {

        private final String principal;

        private final byte[] credentials;

        private transient UserGroupInformation ugi;

        private KerberosKeytabDoAs(String principal, String keytab) {
            this.principal = principal;

            // Log in immediately with the keytab and save the credentials with a delegation token for the filesystem.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(baos = new ByteArrayOutputStream())) {
                UserGroupInformation kerberosUgi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(principal, keytab);
                final Credentials cred = kerberosUgi.getCredentials();
                kerberosUgi.doAs(new PrivilegedExceptionAction<Credentials>() {

                    @Override
                    public Credentials run() throws IOException {
                        FileSystem.get(new Configuration()).addDelegationTokens("TCOMP", cred);
                        return cred;
                    }
                });

                kerberosUgi.addCredentials(cred);
                cred.write(out);
            } catch (IOException e) {
                // TODO: Meaningful error message on Kerberos login failures.
                throw TalendRuntimeException.createUnexpectedException(e);
            } catch (RuntimeException e) {
                // Propagate runtime exceptions.
                throw e;
            } catch (Exception e) {
                // This can never occur from the UGI privileged action above.
                throw TalendRuntimeException.createUnexpectedException(e);
            }
            credentials = baos.toByteArray();
        }

        private final UserGroupInformation getUgi() {
            // If the UGI has not been created, create it from the credentials, and don't inherit from the current or
            // login user.
            if (ugi == null) {
                // If the UGI has not been initialized, then create a new one with the credentials.
                try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(credentials))) {
                    Credentials cred = new Credentials();
                    cred.readFields(in);
                    ugi = UserGroupInformation.createRemoteUser(principal, SaslRpcServer.AuthMethod.KERBEROS);
                    ugi.addCredentials(cred);
                } catch (IOException e) {
                    throw TalendRuntimeException.createUnexpectedException(e);
                }
            }
            return ugi;
        }

        @Override
        public <T> T doAs(PrivilegedAction<T> action) {
            return getUgi().doAs(action);
        }

        @Override
        public <T> T doAs(PrivilegedExceptionAction<T> action) throws Exception {
            return getUgi().doAs(action);
        }
    }

}
