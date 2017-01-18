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

import java.io.Serializable;
import java.security.PrivilegedExceptionAction;

import org.apache.hadoop.security.UserGroupInformation;

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

        private final String keytab;

        private transient UserGroupInformation ugi;

        private KerberosKeytabDoAs(String principal, String keytab) {
            this.principal = principal;
            this.keytab = keytab;
        }

        @Override
        public <T> T doAs(PrivilegedExceptionAction<T> action) throws Exception {
            if (ugi == null)
                ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(principal, keytab);
            return ugi.doAs(action);
        }
    }

}
