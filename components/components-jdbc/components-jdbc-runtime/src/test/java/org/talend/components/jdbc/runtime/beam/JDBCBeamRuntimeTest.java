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

package org.talend.components.jdbc.runtime.beam;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.jdbc.ClientDataSource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.jdbc.dataprep.JDBCInputProperties;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;
import org.talend.components.jdbc.datastream.JDBCOutputProperties;
import org.talend.daikon.properties.test.PropertiesTestUtils;

public class JDBCBeamRuntimeTest implements Serializable {

    static final String DRIVER = "org.apache.derby.jdbc.ClientDriver";

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCBeamRuntimeTest.class);

    private static final String TABLE_IN = "table_in";

    private static final String TABLE_OUT = "table_out";

    private static NetworkServerControl derbyServer;

    private static ClientDataSource dataSource;

    private static int port;

    private static String JDBC_URL;

    Map<Integer, String> assertRows = new HashMap<>();

    @BeforeClass
    public static void registerPaxUrlMavenHandler() {
        PropertiesTestUtils.setupPaxUrlFromMavenLaunch();
    }

    @BeforeClass
    public static void startDatabase() throws Exception {
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        LOGGER.info("Starting Derby database on {}", port);

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

    @Ignore
    @Test
    public void testPipeline() throws Exception {
        JDBCDatastoreProperties jdbcDatastoreProperties;
        jdbcDatastoreProperties = new JDBCDatastoreProperties("datastore");
        jdbcDatastoreProperties.init();
        jdbcDatastoreProperties.dbTypes.setValue("DERBY");
        jdbcDatastoreProperties.jdbcUrl.setValue(JDBC_URL);

        Pipeline pipeline = TestPipeline.create();

        JDBCDatasetProperties inputDatasetProperties = new JDBCDatasetProperties("inputDataset");
        inputDatasetProperties.init();
        inputDatasetProperties.setDatastoreProperties(jdbcDatastoreProperties);
        inputDatasetProperties.sql.setValue("select * from " + TABLE_IN);
        inputDatasetProperties.updateSchema();

        JDBCInputProperties inputProperties = new JDBCInputProperties("input");
        inputProperties.init();
        inputProperties.setDatasetProperties(inputDatasetProperties);

        JDBCDatasetProperties outputDatasetProperties = new JDBCDatasetProperties("outputDataset");
        outputDatasetProperties.init();
        outputDatasetProperties.setDatastoreProperties(jdbcDatastoreProperties);
        outputDatasetProperties.sourceType.setValue(JDBCDatasetProperties.SourceType.TABLE_NAME);
        outputDatasetProperties.tableName.setValue(TABLE_OUT);
        outputDatasetProperties.updateSchema();

        JDBCOutputProperties outputProperties = new JDBCOutputProperties("output");
        outputProperties.init();
        outputProperties.setDatasetProperties(outputDatasetProperties);
        outputProperties.dataAction.setValue(JDBCOutputProperties.DataAction.INSERT);

        JDBCInputPTransformRuntime inputRuntime = new JDBCInputPTransformRuntime();
        inputRuntime.initialize(null, inputProperties);

        JDBCOutputPTransformRuntime outputRuntime = new JDBCOutputPTransformRuntime();
        outputRuntime.initialize(null, outputProperties);

        pipeline.apply(inputRuntime).apply(outputRuntime);

        PipelineResult pipelineResult = pipeline.run();

        Map<Integer, String> results = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("select * from " + TABLE_OUT)) {
                    while (resultSet.next()) {
                        results.put(resultSet.getInt(1), resultSet.getString(2));
                    }
                }
            }
        }
        assertEquals(assertRows, results);

    }

}
