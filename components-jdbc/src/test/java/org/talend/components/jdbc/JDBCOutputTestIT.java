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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.writer.JDBCOutputWriter;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputDefinition;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputDefinition;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties.DataAction;

public class JDBCOutputTestIT {

    private static String driverClass;

    private static String jdbcUrl;

    private static String userId;

    private static String password;

    private static String tablename;

    private static AllSetting allSetting;

    @BeforeClass
    public static void init() throws Exception {
        java.util.Properties props = new java.util.Properties();
        try (InputStream is = JDBCOutputTestIT.class.getClassLoader().getResourceAsStream("connection.properties")) {
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
    public void testInsert() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = createCommonJDBCOutputProperties(definition);

        Schema schema = DBTestUtils.createTestSchema();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.dataAction.setValue(DataAction.INSERT);
        properties.dieOnError.setValue(true);

        randomBatchAndCommit(properties);

        JDBCOutputWriter writer = DBTestUtils.createCommonJDBCOutputWriter(definition, properties);

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

        TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
        TJDBCInputProperties properties1 = createCommonJDBCInputProperties(definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(tablename, schema, definition1, properties1);

        assertThat(records, hasSize(5));
        Assert.assertEquals(4, records.get(3).get(0));
        Assert.assertEquals("xiaoming", records.get(3).get(1));
        Assert.assertEquals(5, records.get(4).get(0));
        Assert.assertEquals("xiaobai", records.get(4).get(1));
    }

    @Test
    public void testInsertReject() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = createCommonJDBCOutputProperties(definition);

        Schema schema = DBTestUtils.createTestSchema();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.dataAction.setValue(DataAction.INSERT);
        properties.useBatch.setValue(false);// reject function can't work with batch function

        properties.commitEvery.setValue(DBTestUtils.randomInt());

        JDBCOutputWriter writer = DBTestUtils.createCommonJDBCOutputWriter(definition, properties);

        try {
            writer.open("wid");

            IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
            r1.put(0, 4);
            r1.put(1, "wangwei");
            writer.write(r1);

            DBTestUtils.assertSuccessRecord(writer, r1);

            IndexedRecord r2 = new GenericData.Record(properties.main.schema.getValue());
            r2.put(0, 5);
            r2.put(1, "the line should be rejected as it's too long");
            writer.write(r2);

            DBTestUtils.assertRejectRecord(writer);

            IndexedRecord r3 = new GenericData.Record(properties.main.schema.getValue());
            r3.put(0, 6);
            r3.put(1, "gaoyan");
            writer.write(r3);

            DBTestUtils.assertSuccessRecord(writer, r3);

            IndexedRecord r4 = new GenericData.Record(properties.main.schema.getValue());
            r4.put(0, 7);
            r4.put(1, "the line should be rejected as it's too long");
            writer.write(r4);

            DBTestUtils.assertRejectRecord(writer);

            IndexedRecord r5 = new GenericData.Record(properties.main.schema.getValue());
            r5.put(0, 8);
            r5.put(1, "dabao");
            writer.write(r5);

            DBTestUtils.assertSuccessRecord(writer, r5);

            writer.close();
        } finally {
            writer.close();
        }

        TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
        TJDBCInputProperties properties1 = createCommonJDBCInputProperties(definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(tablename, schema, definition1, properties1);

        assertThat(records, hasSize(6));
        Assert.assertEquals(4, records.get(3).get(0));
        Assert.assertEquals("wangwei", records.get(3).get(1));
        Assert.assertEquals(6, records.get(4).get(0));
        Assert.assertEquals("gaoyan", records.get(4).get(1));
        Assert.assertEquals(8, records.get(5).get(0));
        Assert.assertEquals("dabao", records.get(5).get(1));
    }

    @Test
    public void testUpdate() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = createCommonJDBCOutputProperties(definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.dataAction.setValue(DataAction.UPDATE);
        properties.dieOnError.setValue(true);

        String randomInfo = randomBatchAndCommit(properties);

        JDBCOutputWriter writer = DBTestUtils.createCommonJDBCOutputWriter(definition, properties);

        try {
            writer.open("wid");

            IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
            r1.put(0, 1);
            r1.put(1, "wangwei1");
            writer.write(r1);

            DBTestUtils.assertSuccessRecord(writer, r1);

            IndexedRecord r2 = new GenericData.Record(properties.main.schema.getValue());
            r2.put(0, 2);
            r2.put(1, "gaoyan1");
            writer.write(r2);

            DBTestUtils.assertSuccessRecord(writer, r2);

            writer.close();
        } finally {
            writer.close();
        }

        TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
        TJDBCInputProperties properties1 = createCommonJDBCInputProperties(definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(tablename, schema, definition1, properties1);

        assertThat(records, hasSize(3));
        Assert.assertEquals(1, records.get(0).get(0));
        Assert.assertEquals("wangwei1", records.get(0).get(1));
        Assert.assertEquals(2, records.get(1).get(0));
        Assert.assertEquals(randomInfo, "gaoyan1", records.get(1).get(1));
        Assert.assertEquals(3, records.get(2).get(0));
        Assert.assertEquals("dabao", records.get(2).get(1));
    }

    @Test
    public void testUpdateReject() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = createCommonJDBCOutputProperties(definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.dataAction.setValue(DataAction.UPDATE);
        properties.useBatch.setValue(false);// reject function can't work with batch function

        properties.commitEvery.setValue(DBTestUtils.randomInt());

        JDBCOutputWriter writer = DBTestUtils.createCommonJDBCOutputWriter(definition, properties);

        try {
            writer.open("wid");

            IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
            r1.put(0, 1);
            r1.put(1, "the line should be rejected as it's too long");
            writer.write(r1);

            DBTestUtils.assertRejectRecord(writer);

            IndexedRecord r2 = new GenericData.Record(properties.main.schema.getValue());
            r2.put(0, 4);
            r2.put(1, "newkey");
            writer.write(r2);

            DBTestUtils.assertSuccessRecord(writer, r2);

            IndexedRecord r3 = new GenericData.Record(properties.main.schema.getValue());
            r3.put(0, 2);
            r3.put(1, "gaoyan1");
            writer.write(r3);

            DBTestUtils.assertSuccessRecord(writer, r3);

            IndexedRecord r4 = new GenericData.Record(properties.main.schema.getValue());
            r4.put(0, 5);
            r4.put(1, "the line is not rejected though it's too long as the key is not found in the table");
            writer.write(r4);

            DBTestUtils.assertSuccessRecord(writer, r4);

            IndexedRecord r5 = new GenericData.Record(properties.main.schema.getValue());
            r5.put(0, 3);
            r5.put(1, "dabao1");
            writer.write(r5);

            DBTestUtils.assertSuccessRecord(writer, r5);

            writer.close();
        } finally {
            writer.close();
        }

        TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
        TJDBCInputProperties properties1 = createCommonJDBCInputProperties(definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(tablename, schema, definition1, properties1);

        assertThat(records, hasSize(3));
        Assert.assertEquals(1, records.get(0).get(0));
        Assert.assertEquals("wangwei", records.get(0).get(1));
        Assert.assertEquals(2, records.get(1).get(0));
        Assert.assertEquals("gaoyan1", records.get(1).get(1));
        Assert.assertEquals(3, records.get(2).get(0));
        Assert.assertEquals("dabao1", records.get(2).get(1));
    }

    @Test
    public void testDelete() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = createCommonJDBCOutputProperties(definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.dataAction.setValue(DataAction.DELETE);
        properties.dieOnError.setValue(true);

        randomBatchAndCommit(properties);

        JDBCOutputWriter writer = DBTestUtils.createCommonJDBCOutputWriter(definition, properties);

        try {
            writer.open("wid");

            IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
            r1.put(0, 1);
            writer.write(r1);

            DBTestUtils.assertSuccessRecord(writer, r1);

            IndexedRecord r2 = new GenericData.Record(properties.main.schema.getValue());
            r2.put(0, 2);
            writer.write(r2);

            DBTestUtils.assertSuccessRecord(writer, r2);

            writer.close();
        } finally {
            writer.close();
        }

        TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
        TJDBCInputProperties properties1 = createCommonJDBCInputProperties(definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(tablename, schema, definition1, properties1);

        assertThat(records, hasSize(1));
        Assert.assertEquals(3, records.get(0).get(0));
        Assert.assertEquals("dabao", records.get(0).get(1));
    }

    // TODO how to make a delete action fail?
    @Test
    public void testDeleteReject() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = createCommonJDBCOutputProperties(definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.dataAction.setValue(DataAction.DELETE);
        properties.useBatch.setValue(false);// reject function can't work with batch function

        properties.commitEvery.setValue(DBTestUtils.randomInt());

        JDBCOutputWriter writer = DBTestUtils.createCommonJDBCOutputWriter(definition, properties);

        try {
            writer.open("wid");

            IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
            r1.put(0, 1);
            r1.put(1, "wangwei1");
            writer.write(r1);

            DBTestUtils.assertSuccessRecord(writer, r1);

            IndexedRecord r2 = new GenericData.Record(properties.main.schema.getValue());
            r2.put(0, 4);
            r2.put(1, "newkey");
            writer.write(r2);

            DBTestUtils.assertSuccessRecord(writer, r2);

            IndexedRecord r3 = new GenericData.Record(properties.main.schema.getValue());
            r3.put(0, 2);
            r3.put(1, "gaoyan1");
            writer.write(r3);

            DBTestUtils.assertSuccessRecord(writer, r3);

            IndexedRecord r4 = new GenericData.Record(properties.main.schema.getValue());
            r4.put(0, 5);
            r4.put(1, "the line is not rejected though it's too long as only key is used by deleting action");
            writer.write(r4);

            DBTestUtils.assertSuccessRecord(writer, r4);

            IndexedRecord r5 = new GenericData.Record(properties.main.schema.getValue());
            r5.put(0, 3);
            r5.put(1, "dabao1");
            writer.write(r5);

            DBTestUtils.assertSuccessRecord(writer, r5);

            writer.close();
        } finally {
            writer.close();
        }

        TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
        TJDBCInputProperties properties1 = createCommonJDBCInputProperties(definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(tablename, schema, definition1, properties1);

        assertThat(records, hasSize(0));
    }

    @Test
    public void testInsertOrUpdate() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = createCommonJDBCOutputProperties(definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.dataAction.setValue(DataAction.INSERTORUPDATE);
        properties.dieOnError.setValue(true);

        properties.commitEvery.setValue(DBTestUtils.randomInt());

        JDBCOutputWriter writer = DBTestUtils.createCommonJDBCOutputWriter(definition, properties);

        try {
            writer.open("wid");

            IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
            r1.put(0, 1);
            r1.put(1, "wangwei1");
            writer.write(r1);

            DBTestUtils.assertSuccessRecord(writer, r1);

            IndexedRecord r2 = new GenericData.Record(properties.main.schema.getValue());
            r2.put(0, 2);
            r2.put(1, "gaoyan1");
            writer.write(r2);

            DBTestUtils.assertSuccessRecord(writer, r2);

            IndexedRecord r3 = new GenericData.Record(properties.main.schema.getValue());
            r3.put(0, 4);
            r3.put(1, "new one");
            writer.write(r3);

            DBTestUtils.assertSuccessRecord(writer, r3);

            writer.close();
        } finally {
            writer.close();
        }

        TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
        TJDBCInputProperties properties1 = createCommonJDBCInputProperties(definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(tablename, schema, definition1, properties1);

        assertThat(records, hasSize(4));
        Assert.assertEquals(1, records.get(0).get(0));
        Assert.assertEquals("wangwei1", records.get(0).get(1));
        Assert.assertEquals(2, records.get(1).get(0));
        Assert.assertEquals("gaoyan1", records.get(1).get(1));
        Assert.assertEquals(3, records.get(2).get(0));
        Assert.assertEquals("dabao", records.get(2).get(1));
        Assert.assertEquals(4, records.get(3).get(0));
        Assert.assertEquals("new one", records.get(3).get(1));
    }

    @Test
    public void testUpdateOrInsert() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = createCommonJDBCOutputProperties(definition);

        Schema schema = DBTestUtils.createTestSchema2();

        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        properties.dataAction.setValue(DataAction.UPDATEORINSERT);
        properties.dieOnError.setValue(true);

        properties.commitEvery.setValue(DBTestUtils.randomInt());

        JDBCOutputWriter writer = DBTestUtils.createCommonJDBCOutputWriter(definition, properties);

        try {
            writer.open("wid");

            IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
            r1.put(0, 1);
            r1.put(1, "wangwei1");
            writer.write(r1);

            DBTestUtils.assertSuccessRecord(writer, r1);

            IndexedRecord r2 = new GenericData.Record(properties.main.schema.getValue());
            r2.put(0, 2);
            r2.put(1, "gaoyan1");
            writer.write(r2);

            DBTestUtils.assertSuccessRecord(writer, r2);

            IndexedRecord r3 = new GenericData.Record(properties.main.schema.getValue());
            r3.put(0, 4);
            r3.put(1, "new one");
            writer.write(r3);

            DBTestUtils.assertSuccessRecord(writer, r3);

            writer.close();
        } finally {
            writer.close();
        }

        TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
        TJDBCInputProperties properties1 = createCommonJDBCInputProperties(definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(tablename, schema, definition1, properties1);

        assertThat(records, hasSize(4));
        Assert.assertEquals(1, records.get(0).get(0));
        Assert.assertEquals("wangwei1", records.get(0).get(1));
        Assert.assertEquals(2, records.get(1).get(0));
        Assert.assertEquals("gaoyan1", records.get(1).get(1));
        Assert.assertEquals(3, records.get(2).get(0));
        Assert.assertEquals("dabao", records.get(2).get(1));
        Assert.assertEquals(4, records.get(3).get(0));
        Assert.assertEquals("new one", records.get(3).get(1));
    }

    @Test
    public void testClearDataInTable() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = createCommonJDBCOutputProperties(definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);

        DataAction action = DBTestUtils.randomDataAction();
        properties.dataAction.setValue(action);
        properties.dieOnError.setValue(DBTestUtils.randomBoolean());
        randomBatchAndCommit(properties);

        properties.clearDataInTable.setValue(true);

        JDBCOutputWriter writer = DBTestUtils.createCommonJDBCOutputWriter(definition, properties);

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

        TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
        TJDBCInputProperties properties1 = createCommonJDBCInputProperties(definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(tablename, schema, definition1, properties1);

        if (action == DataAction.INSERT || action == DataAction.INSERTORUPDATE || action == DataAction.UPDATEORINSERT) {
            assertThat(records, hasSize(2));
            Assert.assertEquals(4, records.get(0).get(0));
            Assert.assertEquals("xiaoming", records.get(0).get(1));
            Assert.assertEquals(5, records.get(1).get(0));
            Assert.assertEquals("xiaobai", records.get(1).get(1));
        } else {
            assertThat(records, hasSize(0));
        }
    }

    private String randomBatchAndCommit(TJDBCOutputProperties properties) {
        properties.useBatch.setValue(DBTestUtils.randomBoolean());
        properties.batchSize.setValue(DBTestUtils.randomInt());
        properties.commitEvery.setValue(DBTestUtils.randomInt());
        return new StringBuilder().append("useBatch: ").append(properties.useBatch.getValue()).append(", batchSize: ")
                .append(properties.batchSize.getValue()).append(", commitEvery:").append(properties.commitEvery.getValue())
                .toString();
    }

    @Test(expected = ComponentException.class)
    public void testDieOnError() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = createCommonJDBCOutputProperties(definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);
        DataAction action = DBTestUtils.randomDataActionExceptDelete();
        properties.dataAction.setValue(action);
        properties.dieOnError.setValue(true);

        randomBatchAndCommit(properties);

        JDBCOutputWriter writer = DBTestUtils.createCommonJDBCOutputWriter(definition, properties);

        try {
            writer.open("wid");

            IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
            r1.put(0, 1);
            r1.put(1, "xiaoming");
            writer.write(r1);

            DBTestUtils.assertSuccessRecord(writer, r1);

            IndexedRecord r2 = new GenericData.Record(properties.main.schema.getValue());
            r2.put(0, 2);
            r2.put(1, "too long value");
            writer.write(r2);

            writer.close();

            Assert.fail("should not run here");
        } finally {
            writer.close();
        }
    }

    private TJDBCOutputProperties createCommonJDBCOutputProperties(TJDBCOutputDefinition definition) {
        TJDBCOutputProperties properties = (TJDBCOutputProperties) definition.createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue(null);
        properties.connection.driverClass.setValue(driverClass);
        properties.connection.jdbcUrl.setValue(jdbcUrl);
        properties.connection.userPassword.userId.setValue(userId);
        properties.connection.userPassword.password.setValue(password);
        return properties;
    }

    private TJDBCInputProperties createCommonJDBCInputProperties(TJDBCInputDefinition definition) {
        TJDBCInputProperties properties = (TJDBCInputProperties) definition.createRuntimeProperties();

        // TODO now framework doesn't support to load the JDBC jar by the setting
        // properties.connection.driverJar.setValue("port", props.getProperty("port"));
        properties.connection.driverClass.setValue(driverClass);
        properties.connection.jdbcUrl.setValue(jdbcUrl);
        properties.connection.userPassword.userId.setValue(userId);
        properties.connection.userPassword.password.setValue(password);
        return properties;
    }

}
