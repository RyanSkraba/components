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
package org.talend.components.jdbc.runtime;

import java.sql.Connection;
import java.sql.SQLException;

import org.talend.components.jdbc.runtime.setting.AllSetting;

public class JdbcRuntimeUtils {

    /**
     * get the JDBC connection object by the runtime setting
     * 
     * @param setting
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection createConnection(AllSetting setting) throws ClassNotFoundException, SQLException {
        java.lang.Class.forName(setting.getDriverClass());
        return java.sql.DriverManager.getConnection(setting.getJdbcUrl(), setting.getUsername(), setting.getPassword());
    }
}
