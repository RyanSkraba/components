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
package org.talend.components.jira.testutils;


/**
 * Jira docker image server constants, which are used in tests.
 * One single place to change this properties
 */
public final class JiraTestConstants {

    /**
     * Jira server host and port
     */
    public static final String HOST_PORT;
    
    /**
     * Incorrect host and port
     */
    public static final String INCORRECT_HOST_PORT = "http://incorrecthost.com";

    /**
     * Jira server user id
     */
    public static final String USER = "root";
    
    /**
     * Jira server wrong user id
     */
    public static final String WRONG_USER = "wrongUser";

    /**
     * Empty user constant
     */
    public static final String ANONYMOUS_USER = "";

    /**
     * Jira server user id
     */
    public static final String PASS = "123456";
    
    /**
     * Sets default HOST_PORT in case of running tests from IDE
     */
    static {
        String systemPropertyHost = System.getProperty("jira.host");
        HOST_PORT = systemPropertyHost != null ?  systemPropertyHost : "http://192.168.99.100:8080/";
    }
}
