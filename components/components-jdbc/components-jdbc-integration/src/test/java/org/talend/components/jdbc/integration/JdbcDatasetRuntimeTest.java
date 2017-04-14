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

package org.talend.components.jdbc.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.jdbc.ClientDataSource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.common.dataset.runtime.DatasetRuntime;
import org.talend.components.jdbc.dataset.JDBCDatasetDefinition;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * Unit tests for {@link JDBCDatasetDefinition} runtimes loaded dynamically.
 */
public class JdbcDatasetRuntimeTest {

    static final String DRIVER = "org.apache.derby.jdbc.ClientDriver";
    private static final String TABLE_IN = "table_in";
    private static final String TABLE_OUT = "table_out";
    private static NetworkServerControl derbyServer;
    private static ClientDataSource dataSource;
    private static int port;
    private static String JDBC_URL;
    /**
     * Instance to test. Definitions are immutable.
     */
    private final JDBCDatasetDefinition def = new JDBCDatasetDefinition();
    Map<Integer, String> assertRows = new HashMap<>();

    @BeforeClass
    public static void startDatabase() throws Exception {
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        JDBC_URL = "jdbc:derby://localhost:" + port + "/target/tcomp";

        System.setProperty("derby.stream.error.file", "target/derby.log");

        derbyServer = new NetworkServerControl(InetAddress.getByName("localhost"), port);
        derbyServer.start(null);

        dataSource = new ClientDataSource();
        dataSource.setCreateDatabase("create");
        dataSource.setDatabaseName("target/tcomp");
        dataSource.setServerName("localhost");
        dataSource.setPortNumber(port);

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("create table " + TABLE_IN + "(id INT, name VARCHAR(500))");
                statement.executeUpdate("create table " + TABLE_OUT + "(id INT, name VARCHAR(500))");
            }
        }
    }

    @AfterClass
    public static void shutDownDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("drop table " + TABLE_IN);
                statement.executeUpdate("drop table " + TABLE_OUT);
            }
        } finally {
            if (derbyServer != null) {
                derbyServer.shutdown();
            }
        }
    }

    /**
     * @return the properties for this dataset, fully initialized with the default values.
     */
    public static JDBCDatasetProperties createDatasetProperties() {
        JDBCDatastoreProperties jdbcDatastoreProperties;
        jdbcDatastoreProperties = new JDBCDatastoreProperties("datastore");
        jdbcDatastoreProperties.init();
        jdbcDatastoreProperties.dbTypes.setValue("DERBY");
        jdbcDatastoreProperties.jdbcUrl.setValue(JDBC_URL);

        JDBCDatasetProperties inputDatasetProperties = new JDBCDatasetProperties("inputDataset");
        inputDatasetProperties.init();
        inputDatasetProperties.setDatastoreProperties(jdbcDatastoreProperties);
        inputDatasetProperties.sql.setValue("select * from " + TABLE_IN);
        return inputDatasetProperties;
    }

    @Before
    public void initTable() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("delete from " + TABLE_IN);
                statement.executeUpdate("delete from " + TABLE_OUT);
            }

            String[] scientists = { "Einstein", "Darwin", "Copernicus", "Pasteur", "Curie", "Faraday", "Newton", "Bohr",
                    "Galilei", "Maxwell" };
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement("insert into " + TABLE_IN + " values (?,?)")) {
                for (int i = 0; i < 2; i++) {
                    int index = i % scientists.length;
                    preparedStatement.clearParameters();
                    preparedStatement.setInt(1, i);
                    preparedStatement.setString(2, scientists[index]);
                    preparedStatement.executeUpdate();
                    assertRows.put(i, scientists[index]);
                }
            }

            connection.commit();
        }
    }

    @Test
    public void testBasic() throws Exception {
        JDBCDatasetProperties props = createDatasetProperties();

        final List<IndexedRecord> consumed = new ArrayList<>();

        RuntimeInfo ri = def.getRuntimeInfo(props);
        try (SandboxedInstance si = RuntimeUtil.createRuntimeClass(ri, getClass().getClassLoader())) {

            DatasetRuntime runtime = (DatasetRuntime) si.getInstance();
            runtime.initialize(null, props);
            assertThat(runtime, not(nullValue()));

            Schema s = runtime.getSchema();
            assertThat(s, not(nullValue()));

            runtime.getSample(100, new Consumer<IndexedRecord>() {

                @Override
                public void accept(IndexedRecord ir) {
                    consumed.add(ir);
                }
            });
        }

        assertThat(consumed, hasSize(2));
    }
}
