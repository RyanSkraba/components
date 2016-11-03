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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.module.PreparedStatementTable;
import org.talend.components.jdbc.runtime.JDBCRowSink;
import org.talend.components.jdbc.runtime.JDBCRowSource;
import org.talend.components.jdbc.runtime.JDBCRowSourceOrSink;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.writer.JDBCRowWriter;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputDefinition;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;
import org.talend.components.jdbc.tjdbcrow.TJDBCRowDefinition;
import org.talend.components.jdbc.tjdbcrow.TJDBCRowProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class JDBCRowTestIT {

    private static String driverClass;

    private static String jdbcUrl;

    private static String userId;

    private static String password;

    private static String tablename;

    public static AllSetting allSetting;

    @BeforeClass
    public static void init() throws Exception {
        java.util.Properties props = new java.util.Properties();
        try (InputStream is = JDBCRowTestIT.class.getClassLoader().getResourceAsStream("connection.properties")) {
            props = new java.util.Properties();
            props.load(is);
        }

        driverClass = props.getProperty("driverClass");

        jdbcUrl = props.getProperty("jdbcUrl");

        userId = props.getProperty("userId");

        password = props.getProperty("password");

        tablename = props.getProperty("tablename");

        allSetting = new AllSetting();
        allSetting.setDriverClass(driverClass);
        allSetting.setJdbcUrl(jdbcUrl);
        allSetting.setUsername(userId);
        allSetting.setPassword(password);
    }

    @AfterClass
    public static void clean() throws ClassNotFoundException, SQLException {
        DBTestUtils.releaseResource(allSetting);
    }

    @Before
    public void before() throws ClassNotFoundException, SQLException, Exception {
        DBTestUtils.prepareTableAndData(allSetting);
    }

    @Test
    public void test_basic_no_connector() throws Exception {
        TJDBCRowDefinition definition = new TJDBCRowDefinition();
        TJDBCRowProperties properties = createCommonJDBCRowProperties(definition);

        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue("insert into test values(4, 'momo')");
        properties.dieOnError.setValue(true);
        randomCommit(properties);

        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(
                definition.getRuntimeInfo(properties, ConnectorTopology.NONE), properties.getClass().getClassLoader())) {
            JDBCRowSourceOrSink sourceOrSink = (JDBCRowSourceOrSink) sandboxedInstance.getInstance();
            sourceOrSink.initialize(null, properties);
            ValidationResult result = sourceOrSink.validate(null);
            Assert.assertTrue(result.status == ValidationResult.Result.OK);
        }

        TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
        TJDBCInputProperties properties1 = createCommonJDBCInputProperties(definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(tablename, DBTestUtils.createTestSchema(),
                definition1, properties1);

        assertThat(records, hasSize(4));
        Assert.assertEquals(4, records.get(3).get(0));
        Assert.assertEquals("momo", records.get(3).get(1));
    }

    @Test
    public void test_use_preparedstatement_no_connector() throws Exception {
        TJDBCRowDefinition definition = new TJDBCRowDefinition();
        TJDBCRowProperties properties = createCommonJDBCRowProperties(definition);

        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue("insert into test values(?, ?)");
        properties.dieOnError.setValue(true);
        randomCommit(properties);

        properties.usePreparedStatement.setValue(true);
        properties.preparedStatementTable.indexs.setValue(Arrays.<Integer> asList(1, 2));
        properties.preparedStatementTable.types.setValue(
                Arrays.<String> asList(PreparedStatementTable.Type.Int.name(), PreparedStatementTable.Type.String.name()));
        properties.preparedStatementTable.values.setValue(Arrays.<Object> asList(4, "momo"));

        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(
                definition.getRuntimeInfo(properties, ConnectorTopology.NONE), properties.getClass().getClassLoader())) {
            JDBCRowSourceOrSink sourceOrSink = (JDBCRowSourceOrSink) sandboxedInstance.getInstance();
            sourceOrSink.initialize(null, properties);
            ValidationResult result = sourceOrSink.validate(null);
            Assert.assertTrue(result.status == ValidationResult.Result.OK);
        }

        TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
        TJDBCInputProperties properties1 = createCommonJDBCInputProperties(definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(tablename, DBTestUtils.createTestSchema(),
                definition1, properties1);

        assertThat(records, hasSize(4));
        Assert.assertEquals(4, records.get(3).get(0));
        Assert.assertEquals("momo", records.get(3).get(1));
    }

    @Test
    public void test_die_on_error_no_connector() throws Exception {
        TJDBCRowDefinition definition = new TJDBCRowDefinition();
        TJDBCRowProperties properties = createCommonJDBCRowProperties(definition);

        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue("insert into test values(4, 'a too long value')");
        properties.dieOnError.setValue(true);
        randomCommit(properties);

        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(
                definition.getRuntimeInfo(properties, ConnectorTopology.NONE), properties.getClass().getClassLoader())) {
            JDBCRowSourceOrSink sourceOrSink = (JDBCRowSourceOrSink) sandboxedInstance.getInstance();
            sourceOrSink.initialize(null, properties);
            ValidationResult result = sourceOrSink.validate(null);
            Assert.assertTrue(result.status == ValidationResult.Result.ERROR);
            Assert.assertNotNull(result.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test_basic_as_input() throws Exception {
        TJDBCRowDefinition definition = new TJDBCRowDefinition();
        TJDBCRowProperties properties = createCommonJDBCRowProperties(definition);

        Schema schema = DBTestUtils.createTestSchema4();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue("select id, name from test");
        properties.dieOnError.setValue(true);
        randomCommit(properties);

        properties.propagateQueryResultSet.setValue(true);// the field is the unique reason to use the component as a input
                                                          // component
        properties.beforeUseColumn();
        properties.useColumn.setValue(properties.useColumn.getPossibleValues().get(0).toString());

        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(
                definition.getRuntimeInfo(properties, ConnectorTopology.OUTGOING), properties.getClass().getClassLoader())) {
            JDBCRowSource source = (JDBCRowSource) sandboxedInstance.getInstance();
            source.initialize(null, properties);
            ValidationResult result = source.validate(null);
            Assert.assertTrue(result.status == ValidationResult.Result.OK);

            Reader reader = source.createReader(null);
            try {
                reader.start();
                IndexedRecord row = (IndexedRecord) reader.getCurrent();
                ResultSet resultSet = (ResultSet) row.get(0);

                resultSet.next();
                Assert.assertEquals(1, resultSet.getInt(1));
                Assert.assertEquals("wangwei", resultSet.getString(2));

                resultSet.next();
                Assert.assertEquals(2, resultSet.getInt(1));
                Assert.assertEquals("gaoyan", resultSet.getString(2));

                resultSet.next();
                Assert.assertEquals(3, resultSet.getInt(1));
                Assert.assertEquals("dabao", resultSet.getString(2));

                resultSet.close();

                Assert.assertFalse(reader.advance());// only output one row when it works as a input component

                reader.close();
            } finally {
                reader.close();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test_use_preparedstatement_as_input() throws Exception {
        TJDBCRowDefinition definition = new TJDBCRowDefinition();
        TJDBCRowProperties properties = createCommonJDBCRowProperties(definition);

        Schema schema = DBTestUtils.createTestSchema4();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue("select id, name from test where id = ?");
        properties.dieOnError.setValue(true);
        randomCommit(properties);

        properties.propagateQueryResultSet.setValue(true);// the field is the unique reason to use the component as a input
                                                          // component
        properties.beforeUseColumn();
        properties.useColumn.setValue(properties.useColumn.getPossibleValues().get(0).toString());

        properties.usePreparedStatement.setValue(true);
        properties.preparedStatementTable.indexs.setValue(Arrays.<Integer> asList(1));
        properties.preparedStatementTable.types.setValue(Arrays.<String> asList(PreparedStatementTable.Type.Int.name()));
        properties.preparedStatementTable.values.setValue(Arrays.<Object> asList(1));

        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(
                definition.getRuntimeInfo(properties, ConnectorTopology.OUTGOING), properties.getClass().getClassLoader())) {
            JDBCRowSource source = (JDBCRowSource) sandboxedInstance.getInstance();
            source.initialize(null, properties);
            ValidationResult result = source.validate(null);
            Assert.assertTrue(result.status == ValidationResult.Result.OK);

            Reader reader = source.createReader(null);
            try {
                reader.start();
                IndexedRecord row = (IndexedRecord) reader.getCurrent();
                ResultSet resultSet = (ResultSet) row.get(0);

                resultSet.next();
                Assert.assertEquals(1, resultSet.getInt(1));
                Assert.assertEquals("wangwei", resultSet.getString(2));

                resultSet.close();

                Assert.assertFalse(reader.advance());// only output one row when it works as a input component

                reader.close();
            } finally {
                reader.close();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test_reject_as_input() throws Exception {
        TJDBCRowDefinition definition = new TJDBCRowDefinition();
        TJDBCRowProperties properties = createCommonJDBCRowProperties(definition);

        Schema schema = DBTestUtils.createTestSchema4();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue("select id, name from notexists");
        properties.dieOnError.setValue(false);
        randomCommit(properties);

        properties.propagateQueryResultSet.setValue(true);// the field is the unique reason to use the component as a input
                                                          // component
        properties.beforeUseColumn();
        properties.useColumn.setValue(properties.useColumn.getPossibleValues().get(0).toString());

        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(
                definition.getRuntimeInfo(properties, ConnectorTopology.OUTGOING), properties.getClass().getClassLoader())) {
            JDBCRowSource source = (JDBCRowSource) sandboxedInstance.getInstance();
            source.initialize(null, properties);
            ValidationResult result = source.validate(null);
            Assert.assertTrue(result.status == ValidationResult.Result.OK);

            Reader reader = source.createReader(null);
            try {
                reader.start();

                reader.getCurrent();

                Assert.fail();// should go to the exception before current statement

                reader.advance();

                reader.close();
            } catch (DataRejectException e) {
                Map<String, Object> info = e.getRejectInfo();
                IndexedRecord data = (IndexedRecord) info.get("talend_record");
                Assert.assertNull(data.get(0));
                Assert.assertNotNull(data.get(1));
                Assert.assertNotNull(data.get(2));
            } finally {
                reader.close();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test_basic_as_output() throws Exception {
        TJDBCRowDefinition definition = new TJDBCRowDefinition();
        TJDBCRowProperties properties = createCommonJDBCRowProperties(definition);

        Schema schema = DBTestUtils.createTestSchema();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue("insert into test values(?,?)");
        properties.dieOnError.setValue(true);
        randomCommit(properties);

        properties.usePreparedStatement.setValue(true);
        properties.preparedStatementTable.indexs.setValue(Arrays.<Integer> asList(1, 2));
        properties.preparedStatementTable.types.setValue(
                Arrays.<String> asList(PreparedStatementTable.Type.Int.name(), PreparedStatementTable.Type.String.name()));
        properties.preparedStatementTable.values.setValue(Arrays.<Object> asList(4, "momo"));

        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(
                definition.getRuntimeInfo(properties, ConnectorTopology.INCOMING_AND_OUTGOING),
                properties.getClass().getClassLoader())) {
            JDBCRowSink sink = (JDBCRowSink) sandboxedInstance.getInstance();
            sink.initialize(null, properties);
            ValidationResult result = sink.validate(null);
            Assert.assertTrue(result.status == ValidationResult.Result.OK);

            WriteOperation operation = sink.createWriteOperation();
            JDBCRowWriter writer = (JDBCRowWriter) operation.createWriter(null);

            try {
                writer.open("wid");

                IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
                r1.put(0, 4);
                r1.put(1, "xiaoming");
                writer.write(r1);

                DBTestUtils.assertSuccessRecord(writer, r1);

                IndexedRecord r2 = new GenericData.Record(properties.main.schema.getValue());
                r2.put(0, 5);
                r2.put(1, "xiaobai");
                writer.write(r2);

                DBTestUtils.assertSuccessRecord(writer, r2);

                writer.close();
            } finally {
                writer.close();
            }
        }

        TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
        TJDBCInputProperties properties1 = createCommonJDBCInputProperties(definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(tablename, schema, definition1, properties1);

        assertThat(records, hasSize(5));
        Assert.assertEquals(4, records.get(3).get(0));
        Assert.assertEquals("momo", records.get(3).get(1));
        Assert.assertEquals(4, records.get(4).get(0));
        Assert.assertEquals("momo", records.get(4).get(1));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test_reject_as_output() throws Exception {
        TJDBCRowDefinition definition = new TJDBCRowDefinition();
        TJDBCRowProperties properties = createCommonJDBCRowProperties(definition);

        Schema schema = DBTestUtils.createTestSchema();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue("insert into test values(?,?)");
        properties.dieOnError.setValue(false);
        randomCommit(properties);

        properties.usePreparedStatement.setValue(true);
        properties.preparedStatementTable.indexs.setValue(Arrays.<Integer> asList(1, 2));
        properties.preparedStatementTable.types.setValue(
                Arrays.<String> asList(PreparedStatementTable.Type.Int.name(), PreparedStatementTable.Type.String.name()));
        properties.preparedStatementTable.values.setValue(Arrays.<Object> asList(4, "a too long value"));

        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(
                definition.getRuntimeInfo(properties, ConnectorTopology.INCOMING_AND_OUTGOING),
                properties.getClass().getClassLoader())) {
            JDBCRowSink sink = (JDBCRowSink) sandboxedInstance.getInstance();
            sink.initialize(null, properties);
            ValidationResult result = sink.validate(null);
            Assert.assertTrue(result.status == ValidationResult.Result.OK);

            WriteOperation operation = sink.createWriteOperation();
            JDBCRowWriter writer = (JDBCRowWriter) operation.createWriter(null);

            try {
                writer.open("wid");

                IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
                r1.put(0, 4);
                r1.put(1, "xiaoming");
                writer.write(r1);

                List<IndexedRecord> rejects = writer.getRejectedWrites();
                assertThat(rejects, hasSize(1));
                IndexedRecord reject = rejects.get(0);
                Assert.assertEquals(4, reject.get(0));
                Assert.assertEquals("xiaoming", reject.get(1));
                Assert.assertNotNull(reject.get(2));
                Assert.assertNotNull(reject.get(3));
                assertThat(writer.getSuccessfulWrites(), empty());

                IndexedRecord r2 = new GenericData.Record(properties.main.schema.getValue());
                r2.put(0, 5);
                r2.put(1, "xiaobai");
                writer.write(r2);

                rejects = writer.getRejectedWrites();
                assertThat(rejects, hasSize(1));
                reject = rejects.get(0);
                Assert.assertEquals(5, reject.get(0));
                Assert.assertEquals("xiaobai", reject.get(1));
                Assert.assertNotNull(reject.get(2));
                Assert.assertNotNull(reject.get(3));
                assertThat(writer.getSuccessfulWrites(), empty());

                writer.close();
            } finally {
                writer.close();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test_die_on_error_as_output() throws Exception {
        TJDBCRowDefinition definition = new TJDBCRowDefinition();
        TJDBCRowProperties properties = createCommonJDBCRowProperties(definition);

        Schema schema = DBTestUtils.createTestSchema();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue("insert into test values(?,?)");
        properties.dieOnError.setValue(true);
        randomCommit(properties);

        properties.usePreparedStatement.setValue(true);
        properties.preparedStatementTable.indexs.setValue(Arrays.<Integer> asList(1, 2));
        properties.preparedStatementTable.types.setValue(
                Arrays.<String> asList(PreparedStatementTable.Type.Int.name(), PreparedStatementTable.Type.String.name()));
        properties.preparedStatementTable.values.setValue(Arrays.<Object> asList(4, "a too long value"));

        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(
                definition.getRuntimeInfo(properties, ConnectorTopology.INCOMING_AND_OUTGOING),
                properties.getClass().getClassLoader())) {
            JDBCRowSink sink = (JDBCRowSink) sandboxedInstance.getInstance();
            sink.initialize(null, properties);
            ValidationResult result = sink.validate(null);
            Assert.assertTrue(result.status == ValidationResult.Result.OK);

            WriteOperation operation = sink.createWriteOperation();
            JDBCRowWriter writer = (JDBCRowWriter) operation.createWriter(null);

            try {
                writer.open("wid");

                IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
                r1.put(0, 4);
                r1.put(1, "xiaoming");
                writer.write(r1);

                writer.close();
            } catch (ComponentException e) {
                Assert.assertNotNull(e.getCause());
            } finally {
                writer.close();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test_propagate_query_result_set_as_output() throws Exception {
        TJDBCRowDefinition definition = new TJDBCRowDefinition();
        TJDBCRowProperties properties = createCommonJDBCRowProperties(definition);

        Schema schema = DBTestUtils.createTestSchema5();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue("select id, name from test where id = ?");
        properties.dieOnError.setValue(true);
        randomCommit(properties);

        properties.usePreparedStatement.setValue(true);
        properties.preparedStatementTable.indexs.setValue(Arrays.<Integer> asList(1));
        properties.preparedStatementTable.types.setValue(Arrays.<String> asList(PreparedStatementTable.Type.Int.name()));
        properties.preparedStatementTable.values.setValue(Arrays.<Object> asList(3));

        properties.propagateQueryResultSet.setValue(true);
        properties.beforeUseColumn();
        properties.useColumn.setValue(properties.useColumn.getPossibleValues().get(2).toString());

        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(
                definition.getRuntimeInfo(properties, ConnectorTopology.INCOMING_AND_OUTGOING),
                properties.getClass().getClassLoader())) {
            JDBCRowSink sink = (JDBCRowSink) sandboxedInstance.getInstance();
            sink.initialize(null, properties);
            ValidationResult result = sink.validate(null);
            Assert.assertTrue(result.status == ValidationResult.Result.OK);

            WriteOperation operation = sink.createWriteOperation();
            JDBCRowWriter writer = (JDBCRowWriter) operation.createWriter(null);

            try {
                writer.open("wid");

                IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
                r1.put(0, 4);
                r1.put(1, "xiaoming");
                writer.write(r1);

                assertThat(writer.getRejectedWrites(), empty());
                List<IndexedRecord> successfulWrites = writer.getSuccessfulWrites();
                assertThat(successfulWrites, hasSize(1));
                IndexedRecord successRecord = successfulWrites.get(0);
                Assert.assertEquals(4, successRecord.get(0));
                Assert.assertEquals("xiaoming", successRecord.get(1));

                ResultSet resultSet = (ResultSet) successRecord.get(2);
                resultSet.next();
                Assert.assertEquals(3, resultSet.getInt(1));
                Assert.assertEquals("dabao", resultSet.getString(2));
                resultSet.close();

                IndexedRecord r2 = new GenericData.Record(properties.main.schema.getValue());
                r2.put(0, 5);
                r2.put(1, "xiaobai");
                writer.write(r2);

                assertThat(writer.getRejectedWrites(), empty());
                successfulWrites = writer.getSuccessfulWrites();
                assertThat(successfulWrites, hasSize(1));
                successRecord = successfulWrites.get(0);
                Assert.assertEquals(5, successRecord.get(0));
                Assert.assertEquals("xiaobai", successRecord.get(1));

                resultSet = (ResultSet) successRecord.get(2);
                resultSet.next();
                Assert.assertEquals(3, resultSet.getInt(1));
                Assert.assertEquals("dabao", resultSet.getString(2));
                resultSet.close();

                writer.close();
            } finally {
                writer.close();
            }
        }
    }

    private String randomCommit(TJDBCRowProperties properties) {
        properties.commitEvery.setValue(DBTestUtils.randomInt());
        return new StringBuilder().append("commitEvery:").append(properties.commitEvery.getValue()).toString();
    }

    private TJDBCRowProperties createCommonJDBCRowProperties(TJDBCRowDefinition definition) {
        TJDBCRowProperties properties = (TJDBCRowProperties) definition.createRuntimeProperties();

        // properties.connection.driverTable.drivers.setValue(Arrays.asList(driverPath));
        properties.connection.driverClass.setValue(driverClass);
        properties.connection.jdbcUrl.setValue(jdbcUrl);
        properties.connection.userPassword.userId.setValue(userId);
        properties.connection.userPassword.password.setValue(password);
        return properties;
    }

    private TJDBCInputProperties createCommonJDBCInputProperties(TJDBCInputDefinition definition) {
        TJDBCInputProperties properties = (TJDBCInputProperties) definition.createRuntimeProperties();

        // properties.connection.driverTable.drivers.setValue(Arrays.asList(driverPath));
        properties.connection.driverClass.setValue(driverClass);
        properties.connection.jdbcUrl.setValue(jdbcUrl);
        properties.connection.userPassword.userId.setValue(userId);
        properties.connection.userPassword.password.setValue(password);
        return properties;
    }

}
