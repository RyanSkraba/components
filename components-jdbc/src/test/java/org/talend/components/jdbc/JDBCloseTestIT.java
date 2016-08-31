// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jdbc;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.JDBCCloseSourceOrSink;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.components.jdbc.tjdbcclose.TJDBCCloseDefinition;
import org.talend.components.jdbc.tjdbcclose.TJDBCCloseProperties;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties;
import org.talend.daikon.properties.ValidationResult;

public class JDBCloseTestIT {

    private static String driverClass;

    private static String jdbcUrl;

    private static String userId;

    private static String password;

    private static JDBCConnectionModule connectionInfo;

    private final String refComponentId = "tJDBCConnection1";

    RuntimeContainer container = new RuntimeContainer() {

        private Map<String, Object> map = new HashMap<>();

        public Object getComponentData(String componentId, String key) {
            return map.get(componentId + "_" + key);
        }

        public void setComponentData(String componentId, String key, Object data) {
            map.put(componentId + "_" + key, data);
        }

        public String getCurrentComponentId() {
            return refComponentId;
        }

        @Override
        public Object getGlobalData(String key) {
            return null;
        }

    };

    @BeforeClass
    public static void init() throws Exception {
        java.util.Properties props = new java.util.Properties();
        try (InputStream is = JDBCloseTestIT.class.getClassLoader().getResourceAsStream("connection.properties")) {
            props = new java.util.Properties();
            props.load(is);
        }

        driverClass = props.getProperty("driverClass");

        jdbcUrl = props.getProperty("jdbcUrl");

        userId = props.getProperty("userId");

        password = props.getProperty("password");

        connectionInfo = new JDBCConnectionModule("connection");

        connectionInfo.driverClass.setValue(driverClass);
        connectionInfo.jdbcUrl.setValue(jdbcUrl);
        connectionInfo.userPassword.userId.setValue(userId);
        connectionInfo.userPassword.password.setValue(password);
    }

    @AfterClass
    public static void clean() throws ClassNotFoundException, SQLException {
        DBTestUtils.shutdownDBIfNecessary();
    }

    @Test
    public void testClose() {
        TJDBCConnectionDefinition connectionDefinition = new TJDBCConnectionDefinition();
        TJDBCConnectionProperties properties = createCommonJDBCConnectionProperties(connectionDefinition);

        JDBCSourceOrSink sourceOrSink = (JDBCSourceOrSink) connectionDefinition.getRuntime();
        sourceOrSink.initialize(null, properties);

        ValidationResult result = sourceOrSink.validate(container);
        assertTrue(result.getStatus() == ValidationResult.Result.OK);

        TJDBCCloseDefinition closeDefinition = new TJDBCCloseDefinition();
        TJDBCCloseProperties closeProperties = (TJDBCCloseProperties) closeDefinition.createRuntimeProperties();

        closeProperties.referencedComponent.componentInstanceId.setValue(refComponentId);

        JDBCCloseSourceOrSink closeSourceOrSink = (JDBCCloseSourceOrSink) closeDefinition.getRuntime();
        closeSourceOrSink.initialize(container, closeProperties);
        closeSourceOrSink.validate(container);

        java.sql.Connection conn = (java.sql.Connection) container.getComponentData(refComponentId,
                ComponentConstants.CONNECTION_KEY);
        if (conn != null) {
            try {
                Assert.assertTrue(conn.isClosed());
            } catch (SQLException e) {
                Assert.fail(e.getMessage());
            }
        }

    }

    private TJDBCConnectionProperties createCommonJDBCConnectionProperties(TJDBCConnectionDefinition connectionDefinition) {
        TJDBCConnectionProperties properties = (TJDBCConnectionProperties) connectionDefinition.createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue("port", props.getProperty("port"));
        properties.connection.driverClass.setValue(driverClass);
        properties.connection.jdbcUrl.setValue(jdbcUrl);
        properties.connection.userPassword.userId.setValue(userId);
        properties.connection.userPassword.password.setValue(password);
        return properties;
    }

}
