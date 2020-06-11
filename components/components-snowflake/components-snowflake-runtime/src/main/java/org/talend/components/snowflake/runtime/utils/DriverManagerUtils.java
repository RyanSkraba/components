// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.runtime.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.runtime.SnowflakeConstants;
import org.talend.components.snowflake.tsnowflakeconnection.AuthenticationType;

/**
 * This utils class is only for test purpose.
 *
 */
public class DriverManagerUtils {

    public static Connection getConnection(SnowflakeConnectionProperties properties) throws IOException {
        try {
            Driver driver = (Driver) Class.forName(SnowflakeConstants.SNOWFLAKE_DRIVER).newInstance();

            DriverManager.registerDriver(driver);
            return DriverManager
                    .getConnection(properties.getConnectionUrl(),
                            AuthenticationType.OAUTH == properties.authenticationType.getValue()
                                    ? properties.getJdbcProperties(OauthTokenUtils.getToken(properties.getOauthProperties()))
                                    : properties.getJdbcProperties());
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw IllegalArgumentException.class.cast(e);
            } else if(e.getMessage().contains("HTTP status=403")) {
                throw new IllegalArgumentException(e.getMessage(), e);
            } else {
                throw new IOException(e);
            }
        }

    }

}
