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

package org.talend.components.hadoopcluster.runtime.configuration;

public class HadoopClusterTestConstants {

    public static final String CDH58_MANAGER_URL;

    public static final String CDH58_MANAGER_USER;

    public static final String CDH58_MANAGER_PASS;

    static {
        CDH58_MANAGER_URL = System.getProperty("hadoop.cm58.url");
        CDH58_MANAGER_USER = System.getProperty("hadoop.cm58.user");
        CDH58_MANAGER_PASS = System.getProperty("hadoop.cm58.pass");
    }
}
