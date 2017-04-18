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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

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
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterDataSupplier;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.runtime.JDBCSink;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.writer.JDBCOutputWriter;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputDefinition;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputDefinition;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties.DataAction;
import org.talend.daikon.java8.Supplier;

public class JDBCOutputTestIT {

    private static AllSetting allSetting;

    @BeforeClass
    public static void beforeClass() throws Exception {
        allSetting = DBTestUtils.createAllSetting();

        DBTestUtils.createTable(allSetting);
    }

    @AfterClass
    public static void afterClass() throws ClassNotFoundException, SQLException {
        DBTestUtils.releaseResource(allSetting);
    }

    @Before
    public void before() throws Exception {
        DBTestUtils.truncateTableAndLoadData(allSetting);
    }

    @Test
    public void testInsertWithDataSupplier() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = DBTestUtils.createCommonJDBCOutputProperties(allSetting, definition);

        Schema schema = DBTestUtils.createTestSchema();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
        properties.dataAction.setValue(DataAction.INSERT);
        properties.dieOnError.setValue(true);

        randomBatchAndCommit(properties);

        JDBCSink sink = new JDBCSink();
        sink.initialize(null, properties);

        WriteOperation writerOperation = sink.createWriteOperation();

        Supplier<IndexedRecord> indexRecordSupplier = createDataSupplier(properties.main.schema.getValue());
        new WriterDataSupplier<>(writerOperation, indexRecordSupplier, null).writeData();
        ;


        TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
        TJDBCInputProperties properties1 = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(DBTestUtils.getTablename(), schema, definition1,
                properties1);

        assertThat(records, hasSize(5));
        Assert.assertEquals("4", records.get(3).get(0));
        Assert.assertEquals("xiaoming", records.get(3).get(1));
        Assert.assertEquals("5", records.get(4).get(0));
        Assert.assertEquals("xiaobai", records.get(4).get(1));
    }

    private Supplier<IndexedRecord> createDataSupplier(final Schema schema) {
        return new Supplier<IndexedRecord>() {

            int recordCount = 1;
            @Override
            public IndexedRecord get() {
                switch (recordCount++) {
                case 1:
                    IndexedRecord r1 = new GenericData.Record(schema);
                    r1.put(0, 4);
                    r1.put(1, "xiaoming");
                    return r1;
                case 2:

                    IndexedRecord r2 = new GenericData.Record(schema);
                    r2.put(0, 5);
                    r2.put(1, "xiaobai");
                    return r2;
                default:
                    return null;
                }
            }
        };
    }

    @Test
    public void testInsert() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = DBTestUtils.createCommonJDBCOutputProperties(allSetting, definition);

        Schema schema = DBTestUtils.createTestSchema();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
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
        TJDBCInputProperties properties1 = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(DBTestUtils.getTablename(), schema, definition1,
                properties1);

        assertThat(records, hasSize(5));
        Assert.assertEquals("4", records.get(3).get(0));
        Assert.assertEquals("xiaoming", records.get(3).get(1));
        Assert.assertEquals("5", records.get(4).get(0));
        Assert.assertEquals("xiaobai", records.get(4).get(1));
    }

    @Test
    public void testInsertReject() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = DBTestUtils.createCommonJDBCOutputProperties(allSetting, definition);

        Schema schema = DBTestUtils.createTestSchema();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
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
        TJDBCInputProperties properties1 = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(DBTestUtils.getTablename(), schema, definition1,
                properties1);

        assertThat(records, hasSize(6));
        Assert.assertEquals("4", records.get(3).get(0));
        Assert.assertEquals("wangwei", records.get(3).get(1));
        Assert.assertEquals("6", records.get(4).get(0));
        Assert.assertEquals("gaoyan", records.get(4).get(1));
        Assert.assertEquals("8", records.get(5).get(0));
        Assert.assertEquals("dabao", records.get(5).get(1));
    }

    @Test
    public void testUpdate() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = DBTestUtils.createCommonJDBCOutputProperties(allSetting, definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
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
        TJDBCInputProperties properties1 = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(DBTestUtils.getTablename(), schema, definition1,
                properties1);

        assertThat(records, hasSize(3));
        Assert.assertEquals("1", records.get(0).get(0));
        Assert.assertEquals("wangwei1", records.get(0).get(1));
        Assert.assertEquals("2", records.get(1).get(0));
        Assert.assertEquals(randomInfo, "gaoyan1", records.get(1).get(1));
        Assert.assertEquals("3", records.get(2).get(0));
        Assert.assertEquals("dabao", records.get(2).get(1));
    }

    @Test
    public void testUpdateReject() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = DBTestUtils.createCommonJDBCOutputProperties(allSetting, definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
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
        TJDBCInputProperties properties1 = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(DBTestUtils.getTablename(), schema, definition1,
                properties1);

        assertThat(records, hasSize(3));
        Assert.assertEquals("1", records.get(0).get(0));
        Assert.assertEquals("wangwei", records.get(0).get(1));
        Assert.assertEquals("2", records.get(1).get(0));
        Assert.assertEquals("gaoyan1", records.get(1).get(1));
        Assert.assertEquals("3", records.get(2).get(0));
        Assert.assertEquals("dabao1", records.get(2).get(1));
    }

    @Test
    public void testDelete() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = DBTestUtils.createCommonJDBCOutputProperties(allSetting, definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
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
        TJDBCInputProperties properties1 = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(DBTestUtils.getTablename(), schema, definition1,
                properties1);

        assertThat(records, hasSize(1));
        Assert.assertEquals("3", records.get(0).get(0));
        Assert.assertEquals("dabao", records.get(0).get(1));
    }

    // TODO how to make a delete action fail?
    @Test
    public void testDeleteReject() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = DBTestUtils.createCommonJDBCOutputProperties(allSetting, definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
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
        TJDBCInputProperties properties1 = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(DBTestUtils.getTablename(), schema, definition1,
                properties1);

        assertThat(records, hasSize(0));
    }

    @Test
    public void testInsertOrUpdate() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = DBTestUtils.createCommonJDBCOutputProperties(allSetting, definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
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
        TJDBCInputProperties properties1 = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(DBTestUtils.getTablename(), schema, definition1,
                properties1);

        assertThat(records, hasSize(4));
        Assert.assertEquals("1", records.get(0).get(0));
        Assert.assertEquals("wangwei1", records.get(0).get(1));
        Assert.assertEquals("2", records.get(1).get(0));
        Assert.assertEquals("gaoyan1", records.get(1).get(1));
        Assert.assertEquals("3", records.get(2).get(0));
        Assert.assertEquals("dabao", records.get(2).get(1));
        Assert.assertEquals("4", records.get(3).get(0));
        Assert.assertEquals("new one", records.get(3).get(1));
    }

    @Test
    public void testUpdateOrInsert() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = DBTestUtils.createCommonJDBCOutputProperties(allSetting, definition);

        Schema schema = DBTestUtils.createTestSchema2();

        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
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
        TJDBCInputProperties properties1 = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(DBTestUtils.getTablename(), schema, definition1,
                properties1);

        assertThat(records, hasSize(4));
        Assert.assertEquals("1", records.get(0).get(0));
        Assert.assertEquals("wangwei1", records.get(0).get(1));
        Assert.assertEquals("2", records.get(1).get(0));
        Assert.assertEquals("gaoyan1", records.get(1).get(1));
        Assert.assertEquals("3", records.get(2).get(0));
        Assert.assertEquals("dabao", records.get(2).get(1));
        Assert.assertEquals("4", records.get(3).get(0));
        Assert.assertEquals("new one", records.get(3).get(1));
    }

    @Test
    public void testClearDataInTable() throws Exception {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = DBTestUtils.createCommonJDBCOutputProperties(allSetting, definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());

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
        TJDBCInputProperties properties1 = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition1);
        List<IndexedRecord> records = DBTestUtils.fetchDataByReaderFromTable(DBTestUtils.getTablename(), schema, definition1,
                properties1);

        if (action == DataAction.INSERT || action == DataAction.INSERTORUPDATE || action == DataAction.UPDATEORINSERT) {
            assertThat(records, hasSize(2));
            Assert.assertEquals("4", records.get(0).get(0));
            Assert.assertEquals("xiaoming", records.get(0).get(1));
            Assert.assertEquals("5", records.get(1).get(0));
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
        TJDBCOutputProperties properties = DBTestUtils.createCommonJDBCOutputProperties(allSetting, definition);

        Schema schema = DBTestUtils.createTestSchema2();
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
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

}
