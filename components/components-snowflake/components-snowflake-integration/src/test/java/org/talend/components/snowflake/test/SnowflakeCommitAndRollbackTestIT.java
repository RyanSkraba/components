// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.test;

import org.junit.*;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.SnowflakeFamilyDefinition;
import org.talend.components.snowflake.SnowflakeRollbackAndCommitProperties;
import org.talend.components.snowflake.runtime.*;
import org.talend.components.snowflake.runtime.utils.DriverManagerUtils;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;
import org.talend.daikon.definition.service.DefinitionRegistryService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;

public class SnowflakeCommitAndRollbackTestIT extends SnowflakeTestIT {

    private static final String AUTO_COMMIT_REF_NAME = "autocommit";

    private static final String TRANSACTION_REF_NAME = "transaction";

    private static final RuntimeContainer RUNTIME_CONTAINER = new DefaultComponentRuntimeContainerImpl();

    private static final String CREATE_TABLE =
            "CREATE TABLE " + testTable + " (firstName varchar(20), lastName varchar(20))";

    private static final String DROP_TABLE = "DROP TABLE " + testTable;

    private static final String TRUNCATE_TABLE = "TRUNCATE TABLE " + testTable;

    private static final String SELECT_COUNT = "SELECT COUNT(*) FROM " + testTable;
    private static final SnowflakeConnectionProperties connectionProperties =
            new SnowflakeConnectionProperties("transactionMode");
    private static final SnowflakeConnectionProperties connectionPropertiesAutoCommit =
            new SnowflakeConnectionProperties("autoCommit");
    private static SnowflakeRollbackAndCommitProperties rollbackAndCommitProperties =
            new SnowflakeRollbackAndCommitProperties("common");
    private static SnowflakeRollbackSourceOrSink rollback;
    private static SnowflakeCommitSourceOrSink commit;
    private static Connection connection;
    private static Connection connectionRead;

    @BeforeClass
    public static void setupTest() throws Exception {
        setupProps(connectionProperties);
        connectionProperties.autoCommit.setValue(false);
        connectionProperties.loginTimeout.setValue(1);
        connectionProperties.schemaName.setValue(SCHEMA);

        setupProps(connectionPropertiesAutoCommit);
        connectionPropertiesAutoCommit.loginTimeout.setValue(1);
        connectionPropertiesAutoCommit.schemaName.setValue(SCHEMA);

        rollback = new SnowflakeRollbackSourceOrSink();
        initializeRollbackOrCommitProperties(rollback);

        commit = new SnowflakeCommitSourceOrSink();
        initializeRollbackOrCommitProperties(commit);

        // Initialize Connection with AutoCommit disabled
        connection = createConnection(connectionProperties, TRANSACTION_REF_NAME);
        // Init separate connection for Read operation
        connectionRead = createConnection(connectionPropertiesAutoCommit, AUTO_COMMIT_REF_NAME);

        try (Statement st = connection.createStatement()) {
            st.executeQuery(CREATE_TABLE);
        }
    }

    @AfterClass
    public static void closeConnections() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            try (Statement st = connection.createStatement()) {
                st.executeQuery(DROP_TABLE);
            }
            connection.close();
        }
        if (connectionRead != null && !connectionRead.isClosed()) {
            connectionRead.close();
        }
    }

    private static Connection createConnection(SnowflakeConnectionProperties properties, String componentInstanceId)
            throws IOException, SQLException {
        Connection conn;
        RUNTIME_CONTAINER
                .setComponentData(componentInstanceId, SnowflakeRuntime.KEY_CONNECTION,
                        conn = DriverManagerUtils.getConnection(properties));
        conn.setAutoCommit(properties.autoCommit.getValue());
        try (Statement st = conn.createStatement()) {
            st.executeQuery("alter user " + USER + " set mins_to_unlock=0");
        }
        return conn;
    }

    private static void initializeRollbackOrCommitProperties(SnowflakeStandaloneOperation standaloneOperation) {
        rollbackAndCommitProperties.referencedComponent.setReference(connectionProperties);
        rollbackAndCommitProperties.referencedComponent.componentInstanceId.setValue(TRANSACTION_REF_NAME);
        standaloneOperation.initialize(RUNTIME_CONTAINER, rollbackAndCommitProperties);
    }

    @Override
    protected DefinitionRegistryService getDefinitionService() {
        DefinitionRegistry definitionRegistry = new DefinitionRegistry();
        definitionRegistry.registerComponentFamilyDefinition(new SnowflakeFamilyDefinition());
        return definitionRegistry;
    }

    @After
    public void truncateTableAtLast() throws SQLException {
        truncateTable();
        connection.commit();
    }

    @Test
    public void testTruncateWithCommit() throws SQLException {
        // Write Records into table
        writeRecords();

        // Commit them using new Component
        commit.validate(RUNTIME_CONTAINER);
        assertEquals(3, getRecordCount());

        // Not committed truncate.
        truncateTable();
        assertEquals(3, getRecordCount());

        // Commit Truncate operation
        commit.validate(RUNTIME_CONTAINER);
        assertEquals(0, getRecordCount());
    }

    @Test
    public void testTruncateWithRollback() throws SQLException {
        // Write Records into table
        writeRecords();

        // Commit them using new Component
        commit.validate(RUNTIME_CONTAINER);
        assertEquals(3, getRecordCount());

        // Not committed truncate.
        truncateTable();
        // Rollback It
        rollback.validate(RUNTIME_CONTAINER);

        writeRecords();

        // Commit Write for new Test operation
        commit.validate(RUNTIME_CONTAINER);
        assertEquals(6, getRecordCount());
    }

    private void writeRecords() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.addBatch("INSERT INTO " + testTable + " (firstName, lastName) VALUES('John', 'Doe')");
            st.addBatch("INSERT INTO " + testTable + " (firstName, lastName) VALUES('Jane', 'Doe')");
            st.addBatch("INSERT INTO " + testTable + " (firstName, lastName) VALUES('Johnas', 'Doey')");
            st.executeBatch();
        }
    }

    private int getRecordCount() throws SQLException {
        try (Statement st = connectionRead.createStatement(); ResultSet rs = st.executeQuery(SELECT_COUNT)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void truncateTable() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeQuery(TRUNCATE_TABLE);
        }
    }

}
