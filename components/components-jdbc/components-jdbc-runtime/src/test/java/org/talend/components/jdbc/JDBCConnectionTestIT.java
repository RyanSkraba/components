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
package org.talend.components.jdbc;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionDefinition;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class JDBCConnectionTestIT {

    public static AllSetting allSetting;

    @BeforeClass
    public static void beforeClass() throws Exception {
        allSetting = DBTestUtils.createAllSetting();
    }

    // we want to test the dynamic library loading with the field driverPath, but fail, so seems that no way to test it
    // now
    @Ignore
    @Test
    public void testDynamicLibLoad() {
        TJDBCConnectionDefinition definition = new TJDBCConnectionDefinition();
        TJDBCConnectionProperties properties = DBTestUtils.createCommonJDBCConnectionProperties(allSetting, definition);

        // the maven path below is a library at my local repository, you can change it to any jdbc driver which exists in your
        // maven repository for running the test. I can't use derby for test as i have added the derby dependency in the pom file.
        properties.connection.driverTable.drivers
                .setValue(Arrays.asList("mvn:org.talend.libraries/mysql-connector-java-5.1.30-bin/6.0.0"));

        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.NONE);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo,
                definition.getClass().getClassLoader())) {
            sandboxedInstance.getInstance();
            Class.forName("org.gjt.mm.mysql.Driver");
        } catch (ClassNotFoundException e) {
            Assert.fail("can't find the jdbc driver class, fail to load the library");
        }
    }

    public void testConnection() {
        TJDBCConnectionDefinition definition = new TJDBCConnectionDefinition();
        TJDBCConnectionProperties properties = DBTestUtils.createCommonJDBCConnectionProperties(allSetting, definition);

        JDBCSourceOrSink sourceOrSink = new JDBCSourceOrSink();
        sourceOrSink.initialize(null, properties);
        ValidationResult result = sourceOrSink.validate(null);
        assertTrue(result.getStatus() == ValidationResult.Result.OK);
        try (Connection conn1 = sourceOrSink.getConnection(null); Connection conn2 = sourceOrSink.getConnection(null)) {
            assertTrue(conn1 == conn2);
            assertTrue(!conn1.isClosed());
        } catch (ClassNotFoundException | SQLException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testConnectionWithWrongDriver() {
        TJDBCConnectionDefinition definition = new TJDBCConnectionDefinition();
        TJDBCConnectionProperties properties = (TJDBCConnectionProperties) definition.createRuntimeProperties();

        properties.connection.driverClass.setValue("wrongDriver");
        properties.connection.jdbcUrl.setValue(allSetting.getJdbcUrl());
        properties.connection.userPassword.userId.setValue(allSetting.getUsername());
        properties.connection.userPassword.password.setValue(allSetting.getPassword());

        JDBCSourceOrSink sourceOrSink = new JDBCSourceOrSink();
        sourceOrSink.initialize(null, properties);
        ValidationResult result = sourceOrSink.validate(null);
        assertTrue(result.getStatus() == ValidationResult.Result.ERROR);
        assertTrue(result.getMessage() != null && !result.getMessage().isEmpty());
    }

    @Test
    public void testConnectionWithWrongURL() {
        TJDBCConnectionDefinition definition = new TJDBCConnectionDefinition();
        TJDBCConnectionProperties properties = (TJDBCConnectionProperties) definition.createRuntimeProperties();

        properties.connection.driverClass.setValue(allSetting.getDriverClass());
        properties.connection.jdbcUrl.setValue("wrongUrl");
        properties.connection.userPassword.userId.setValue(allSetting.getUsername());
        properties.connection.userPassword.password.setValue(allSetting.getPassword());

        JDBCSourceOrSink sourceOrSink = new JDBCSourceOrSink();
        sourceOrSink.initialize(null, properties);
        ValidationResult result = sourceOrSink.validate(null);
        assertTrue(result.getStatus() == ValidationResult.Result.ERROR);
        assertTrue(result.getMessage() != null && !result.getMessage().isEmpty());
    }

    @Test
    public void testNotAutoCommit() {
        TJDBCConnectionDefinition definition = new TJDBCConnectionDefinition();
        TJDBCConnectionProperties properties = DBTestUtils.createCommonJDBCConnectionProperties(allSetting, definition);

        properties.useAutoCommit.setValue(true);
        properties.autocommit.setValue(false);

        JDBCSourceOrSink sourceOrSink = new JDBCSourceOrSink();
        sourceOrSink.initialize(null, properties);
        ValidationResult result = sourceOrSink.validate(null);
        assertTrue(result.getStatus() == ValidationResult.Result.OK);

        try (Connection conn = sourceOrSink.getConnection(null)) {
            assertTrue(!conn.getAutoCommit());
            assertTrue(!conn.isClosed());
        } catch (ClassNotFoundException | SQLException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testAutoCommit() {
        TJDBCConnectionDefinition definition = new TJDBCConnectionDefinition();
        TJDBCConnectionProperties properties = DBTestUtils.createCommonJDBCConnectionProperties(allSetting, definition);

        properties.useAutoCommit.setValue(true);
        properties.autocommit.setValue(true);

        JDBCSourceOrSink sourceOrSink = new JDBCSourceOrSink();
        sourceOrSink.initialize(null, properties);
        ValidationResult result = sourceOrSink.validate(null);
        assertTrue(result.getStatus() == ValidationResult.Result.OK);

        try (Connection conn = sourceOrSink.getConnection(null)) {
            assertTrue(conn.getAutoCommit());
            assertTrue(!conn.isClosed());
        } catch (ClassNotFoundException | SQLException e) {
            Assert.fail(e.getMessage());
        }
    }

}
