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
package org.talend.components.snowflake.runtime;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

import org.talend.components.snowflake.SnowflakeConnectionProperties;

/**
 * Contains only runtime helper classes, mainly to do with logging.
 */
public class SnowflakeRuntimeHelper {

    private SnowflakeRuntimeHelper() {
    }

    public static Connection getConnection(SnowflakeConnectionProperties connProps, Driver driver) throws Exception{
        DriverManager.registerDriver(driver);
        return DriverManager.getConnection(connProps.getConnectionUrl(), connProps.getJdbcProperties());
    }

}
