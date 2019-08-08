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
package org.talend.components.jdbc.type;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.ComponentConstants;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.components.jdbc.runtime.JdbcRuntimeUtils;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.writer.JDBCOutputWriter;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputDefinition;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputDefinition;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties.DataAction;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class JDBCTypeMappingTestIT {

    public static AllSetting allSetting;

    private static final String tablename = "JDBCTYPEMAPPING";
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        allSetting = DBTestUtils.createAllSetting();

        DBTestUtils.createTableForEveryType(allSetting,tablename);
    }

    @AfterClass
    public static void afterClass() throws ClassNotFoundException, SQLException {
        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
            DBTestUtils.dropTestTable(conn,tablename);
        } finally {
            DBTestUtils.shutdownDBIfNecessary();
        }
    }

    @Before
    public void before() throws Exception {
        DBTestUtils.truncateTableAndLoadDataForEveryType(allSetting,tablename);
    }

    @Test
    public void testGetSchema() throws Exception {
        TJDBCInputDefinition definition = new TJDBCInputDefinition();
        TJDBCInputProperties properties = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition);

        properties.main.schema.setValue(DBTestUtils.createTestSchema3(true,tablename));
        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue(DBTestUtils.getSQL(tablename));

        JDBCSource source = DBTestUtils.createCommonJDBCSource(properties);

        RuntimeContainer container = new DefaultComponentRuntimeContainerImpl() {
            @Override
            public String getCurrentComponentId() {
                return "tJDBCInput1";
            }
        };
        java.net.URL mappings_url = this.getClass().getResource("/mappings");
        mappings_url = DBTestUtils.correctURL(mappings_url);
        container.setComponentData(container.getCurrentComponentId(), ComponentConstants.MAPPING_URL_SUBFIX, mappings_url);
        
        Schema schema = source.getEndpointSchema(container, tablename);
        assertEquals(tablename, schema.getName().toUpperCase());
        List<Field> columns = schema.getFields();
        testMetadata(columns);
    }

    // we comment the assert which may be database special
    private void testMetadata(List<Field> columns) {
        Schema.Field field = columns.get(0);

        assertEquals("C1", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._int(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals("INTEGER", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        // assertEquals("10", field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(1);

        assertEquals("C2", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._short(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals("SMALLINT", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        // assertEquals("5", field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(2);

        assertEquals("C3", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._long(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals("BIGINT", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        // assertEquals("19", field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(3);

        assertEquals("C4", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        // assertEquals(AvroUtils._float(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals("REAL", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals("23", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        // assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(4);

        assertEquals("C5", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._double(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals("DOUBLE", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(5);

        assertEquals("C6", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        // assertEquals(AvroUtils._double(), AvroUtils.unwrapIfNullable(field.schema()));
        // assertEquals(java.sql.Types.DOUBLE, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(6);

        assertEquals("C7", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._decimal(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals("DECIMAL", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals("10", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals("2", field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(7);

        assertEquals("C8", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._decimal(), AvroUtils.unwrapIfNullable(field.schema()));
        // assertEquals(java.sql.Types.NUMERIC, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals("10", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals("2", field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(8);

        assertEquals("C9", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        // assertEquals(AvroUtils._boolean(), AvroUtils.unwrapIfNullable(field.schema()));
        // assertEquals(java.sql.Types.BOOLEAN, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(9);

        assertEquals("C10", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._string(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals("CHAR", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals("64", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(10);

        assertEquals("C11", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._date(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals("DATE", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals("yyyy-MM-dd", field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(11);

        assertEquals("C12", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._date(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals("TIME", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals("HH:mm:ss", field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(12);

        assertEquals("C13", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._date(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals("TIMESTAMP", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals("yyyy-MM-dd HH:mm:ss.SSS", field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        // assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(13);

        assertEquals("C14", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._string(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals("VARCHAR", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals("64", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(14);

        assertEquals("C15", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._string(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals("LONG VARCHAR", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        // assertEquals("32700", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));
    }

    @Test
    public void testReadTypeNullable() throws Exception {
        doReadType(true);
    }

    @Test
    public void testReadTypeNotNullable() throws Exception {
        doReadType(false);
    }

    @SuppressWarnings({ "rawtypes" })
    private void doReadType(boolean nullableForAnyColumn) throws IOException {
        TJDBCInputDefinition definition = new TJDBCInputDefinition();
        TJDBCInputProperties properties = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition);

        properties.main.schema.setValue(DBTestUtils.createTestSchema3(nullableForAnyColumn,tablename));
        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue(DBTestUtils.getSQL(tablename));

        Reader reader = DBTestUtils.createCommonJDBCInputReader(properties);

        try {
            IndexedRecordConverter<Object, ? extends IndexedRecord> converter = null;

            reader.start();

            converter = DBTestUtils.getIndexRecordConverter(reader, converter);

            IndexedRecord record = converter.convertToAvro(reader.getCurrent());

            assertEquals(Integer.class, record.get(0).getClass());
            assertEquals(Short.class, record.get(1).getClass());
            assertEquals(Long.class, record.get(2).getClass());
            assertEquals(Float.class, record.get(3).getClass());
            assertEquals(Double.class, record.get(4).getClass());
            assertEquals(Float.class, record.get(5).getClass());
            assertEquals(BigDecimal.class, record.get(6).getClass());
            assertEquals(BigDecimal.class, record.get(7).getClass());
            assertEquals(Boolean.class, record.get(8).getClass());
            assertEquals(String.class, record.get(9).getClass());
            assertEquals(Long.class, record.get(10).getClass());
            assertEquals(Long.class, record.get(11).getClass());
            assertEquals(Long.class, record.get(12).getClass());
            assertEquals(String.class, record.get(13).getClass());
            assertEquals(String.class, record.get(14).getClass());

            reader.close();
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadContentWithAllTypeNullable() {
        doReadContentWithAllType(true);
    }

    @Test
    public void testReadContentWithAllTypeNotNullable() {
        doReadContentWithAllType(false);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void doReadContentWithAllType(boolean nullableForAnyColumn) {
        Reader reader = null;
        try {
            TJDBCInputDefinition definition = new TJDBCInputDefinition();
            TJDBCInputProperties properties = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition);

            properties.main.schema.setValue(DBTestUtils.createTestSchema3(nullableForAnyColumn,tablename));
            properties.tableSelection.tablename.setValue(tablename);
            properties.sql.setValue(DBTestUtils.getSQL(tablename));

            reader = DBTestUtils.createCommonJDBCInputReader(properties);

            reader.start();

            IndexedRecord row = (IndexedRecord) reader.getCurrent();
            Integer c1 = (Integer) row.get(0);
            Short c2 = (Short) row.get(1);
            Long c3 = (Long) row.get(2);
            Float c4 = (Float) row.get(3);
            Double c5 = (Double) row.get(4);
            Float c6 = (Float) row.get(5);
            BigDecimal c7 = (BigDecimal) row.get(6);
            BigDecimal c8 = (BigDecimal) row.get(7);
            Boolean c9 = (Boolean) row.get(8);
            String c10 = (String) row.get(9);
            Long c11 = (Long) row.get(10);
            Long c12 = (Long) row.get(11);
            Long c13 = (Long) row.get(12);
            String c14 = (String) row.get(13);
            String c15 = (String) row.get(14);

            assertEquals(1, c1.intValue());
            assertEquals(2, c2.intValue());
            assertEquals(3, c3.intValue());
            Assert.assertNotNull(c4);
            Assert.assertNotNull(c5);
            Assert.assertNotNull(c6);
            assertEquals(new BigDecimal("7.01"), c7);
            assertEquals(new BigDecimal("8.01"), c8);
            assertEquals(true, c9);
            assertEquals("the first char value", c10.trim());
            Assert.assertNotNull(c11);
            Assert.assertNotNull(c12);
            Assert.assertNotNull(c13);
            assertEquals("wangwei", c14);
            assertEquals("a long one : 1", c15);

            reader.advance();

            row = (IndexedRecord) reader.getCurrent();
            c1 = (Integer) row.get(0);
            c2 = (Short) row.get(1);
            c3 = (Long) row.get(2);
            c4 = (Float) row.get(3);
            c5 = (Double) row.get(4);
            c6 = (Float) row.get(5);
            c7 = (BigDecimal) row.get(6);
            c8 = (BigDecimal) row.get(7);
            c9 = (Boolean) row.get(8);
            c10 = (String) row.get(9);
            c11 = (Long) row.get(10);
            c12 = (Long) row.get(11);
            c13 = (Long) row.get(12);
            c14 = (String) row.get(13);
            c15 = (String) row.get(14);

            assertEquals(1, c1.intValue());
            assertEquals(2, c2.intValue());
            assertEquals(3, c3.intValue());
            Assert.assertNotNull(c4);
            Assert.assertNotNull(c5);
            Assert.assertNotNull(c6);
            assertEquals(new BigDecimal("7.01"), c7);
            assertEquals(new BigDecimal("8.01"), c8);
            assertEquals(true, c9);
            assertEquals("the second char value", c10.trim());
            Assert.assertNotNull(c11);
            Assert.assertNotNull(c12);
            Assert.assertNotNull(c13);
            assertEquals("gaoyan", c14);
            assertEquals("a long one : 2", c15);

            reader.advance();

            row = (IndexedRecord) reader.getCurrent();
            c1 = (Integer) row.get(0);
            c2 = (Short) row.get(1);
            c3 = (Long) row.get(2);
            c4 = (Float) row.get(3);
            c5 = (Double) row.get(4);
            c6 = (Float) row.get(5);
            c7 = (BigDecimal) row.get(6);
            c8 = (BigDecimal) row.get(7);
            c9 = (Boolean) row.get(8);
            c10 = (String) row.get(9);
            c11 = (Long) row.get(10);
            c12 = (Long) row.get(11);
            c13 = (Long) row.get(12);
            c14 = (String) row.get(13);
            c15 = (String) row.get(14);

            assertEquals(1, c1.intValue());
            assertEquals(2, c2.intValue());
            assertEquals(3, c3.intValue());
            Assert.assertNotNull(c4);
            Assert.assertNotNull(c5);
            Assert.assertNotNull(c6);
            assertEquals(new BigDecimal("7.01"), c7);
            assertEquals(new BigDecimal("8.01"), c8);
            assertEquals(true, c9);
            assertEquals("the third char value", c10.trim());
            Assert.assertNotNull(c11);
            Assert.assertNotNull(c12);
            Assert.assertNotNull(c13);
            assertEquals("dabao", c14);
            assertEquals("a long one : 3", c15);

            reader.advance();

            row = (IndexedRecord) reader.getCurrent();
            c1 = (Integer) row.get(0);
            c2 = (Short) row.get(1);
            c3 = (Long) row.get(2);
            c4 = (Float) row.get(3);
            c5 = (Double) row.get(4);
            c6 = (Float) row.get(5);
            c7 = (BigDecimal) row.get(6);
            c8 = (BigDecimal) row.get(7);
            c9 = (Boolean) row.get(8);
            c10 = (String) row.get(9);
            c11 = (Long) row.get(10);
            c12 = (Long) row.get(11);
            c13 = (Long) row.get(12);
            c14 = (String) row.get(13);
            c15 = (String) row.get(14);

            assertEquals(1, c1.intValue());
            Assert.assertNull(c2);
            Assert.assertNull(c3);
            Assert.assertNull(c4);
            Assert.assertNull(c5);
            Assert.assertNull(c6);
            Assert.assertNull(c7);
            Assert.assertNull(c8);
            Assert.assertNull(c9);
            Assert.assertNull(c10);
            Assert.assertNull(c11);
            Assert.assertNull(c12);
            // some database set default value for this column as default
            // Assert.assertNull(c13);
            Assert.assertNull(c14);
            Assert.assertNull(c15);

            reader.advance();

            row = (IndexedRecord) reader.getCurrent();
            c1 = (Integer) row.get(0);
            c2 = (Short) row.get(1);
            c3 = (Long) row.get(2);
            c4 = (Float) row.get(3);
            c5 = (Double) row.get(4);
            c6 = (Float) row.get(5);
            c7 = (BigDecimal) row.get(6);
            c8 = (BigDecimal) row.get(7);
            c9 = (Boolean) row.get(8);
            c10 = (String) row.get(9);
            c11 = (Long) row.get(10);
            c12 = (Long) row.get(11);
            c13 = (Long) row.get(12);
            c14 = (String) row.get(13);
            c15 = (String) row.get(14);

            Assert.assertNull(c1);
            Assert.assertNull(c2);
            Assert.assertNull(c3);
            Assert.assertNull(c4);
            Assert.assertNull(c5);
            Assert.assertNull(c6);
            Assert.assertNull(c7);
            Assert.assertNull(c8);
            Assert.assertNull(c9);
            Assert.assertNull(c10);
            Assert.assertNull(c11);
            Assert.assertNull(c12);
            // some database set default value for this column as default
            // Assert.assertNull(c13);
            Assert.assertEquals("good luck", c14);
            Assert.assertNull(c15);

            reader.close();

            Map<String, Object> returnMap = reader.getReturnValues();
            Assert.assertEquals(5, returnMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            }
        }
    }

    @Test
    public void testWriterWithAllTypeNullable() throws Exception {
        doWriteWithAllType(true);
    }

    @Test
    public void testWriterWithAllTypeNotNullable() throws Exception {
        doWriteWithAllType(false);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void doWriteWithAllType(boolean nullableForAnyColumn) throws IOException {
        TJDBCOutputDefinition definition = new TJDBCOutputDefinition();
        TJDBCOutputProperties properties = DBTestUtils.createCommonJDBCOutputProperties(allSetting, definition);

        Schema schema = DBTestUtils.createTestSchema3(nullableForAnyColumn,tablename);
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);

        properties.dataAction.setValue(DataAction.INSERT);

        JDBCOutputWriter writer = DBTestUtils.createCommonJDBCOutputWriter(definition, properties);

        try {
            writer.open("wid");

            List<IndexedRecord> inputRecords = DBTestUtils.prepareIndexRecords(nullableForAnyColumn,tablename);
            for (IndexedRecord inputRecord : inputRecords) {
                writer.write(inputRecord);

                DBTestUtils.assertSuccessRecord(writer, inputRecord);
            }

            writer.close();
        } finally {
            writer.close();
        }

        // read the inserted data from the target table by the reader
        Reader reader = null;
        try {
            TJDBCInputDefinition definition1 = new TJDBCInputDefinition();
            TJDBCInputProperties properties1 = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition1);

            properties1.main.schema.setValue(DBTestUtils.createTestSchema3(nullableForAnyColumn,tablename));
            properties1.tableSelection.tablename.setValue(tablename);
            properties1.sql.setValue(DBTestUtils.getSQL(tablename));

            reader = DBTestUtils.createCommonJDBCInputReader(properties1);

            reader.start();
            int i = 0;
            while ((i++) < 5) {// skip the 5 rows at the head
                reader.advance();
            }

            byte[] blob = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            
            IndexedRecord row = (IndexedRecord) reader.getCurrent();
            Integer c1 = (Integer) row.get(0);
            Short c2 = (Short) row.get(1);
            Long c3 = (Long) row.get(2);
            Float c4 = (Float) row.get(3);
            Double c5 = (Double) row.get(4);
            Float c6 = (Float) row.get(5);
            BigDecimal c7 = (BigDecimal) row.get(6);
            BigDecimal c8 = (BigDecimal) row.get(7);
            Boolean c9 = (Boolean) row.get(8);
            String c10 = (String) row.get(9);
            Long c11 = (Long) row.get(10);
            Long c12 = (Long) row.get(11);
            Long c13 = (Long) row.get(12);
            String c14 = (String) row.get(13);
            String c15 = (String) row.get(14);
            byte[] c16 = (byte[])row.get(15);

            assertEquals(1, c1.intValue());
            assertEquals(2, c2.intValue());
            assertEquals(3, c3.intValue());
            Assert.assertNotNull(c4);
            Assert.assertNotNull(c5);
            Assert.assertNotNull(c6);
            assertEquals(new BigDecimal("7.01"), c7);
            assertEquals(new BigDecimal("8.01"), c8);
            assertEquals(true, c9);
            assertEquals("content : 1", c10.trim());
            Assert.assertNotNull(c11);
            Assert.assertNotNull(c12);
            Assert.assertNotNull(c13);
            assertEquals("wangwei", c14);
            assertEquals("long content : 1", c15);
            Assert.assertArrayEquals(blob, c16);

            reader.advance();

            row = (IndexedRecord) reader.getCurrent();
            c1 = (Integer) row.get(0);
            c2 = (Short) row.get(1);
            c3 = (Long) row.get(2);
            c4 = (Float) row.get(3);
            c5 = (Double) row.get(4);
            c6 = (Float) row.get(5);
            c7 = (BigDecimal) row.get(6);
            c8 = (BigDecimal) row.get(7);
            c9 = (Boolean) row.get(8);
            c10 = (String) row.get(9);
            c11 = (Long) row.get(10);
            c12 = (Long) row.get(11);
            c13 = (Long) row.get(12);
            c14 = (String) row.get(13);
            c15 = (String) row.get(14);
            c16 = (byte[])row.get(15);

            assertEquals(1, c1.intValue());
            assertEquals(2, c2.intValue());
            assertEquals(3, c3.intValue());
            Assert.assertNotNull(c4);
            Assert.assertNotNull(c5);
            Assert.assertNotNull(c6);
            assertEquals(new BigDecimal("7.01"), c7);
            assertEquals(new BigDecimal("8.01"), c8);
            assertEquals(true, c9);
            assertEquals("content : 2", c10.trim());
            Assert.assertNotNull(c11);
            Assert.assertNotNull(c12);
            Assert.assertNotNull(c13);
            assertEquals("gaoyan", c14);
            assertEquals("long content : 2", c15);
            Assert.assertArrayEquals(blob, c16);

            reader.advance();

            row = (IndexedRecord) reader.getCurrent();
            c1 = (Integer) row.get(0);
            c2 = (Short) row.get(1);
            c3 = (Long) row.get(2);
            c4 = (Float) row.get(3);
            c5 = (Double) row.get(4);
            c6 = (Float) row.get(5);
            c7 = (BigDecimal) row.get(6);
            c8 = (BigDecimal) row.get(7);
            c9 = (Boolean) row.get(8);
            c10 = (String) row.get(9);
            c11 = (Long) row.get(10);
            c12 = (Long) row.get(11);
            c13 = (Long) row.get(12);
            c14 = (String) row.get(13);
            c15 = (String) row.get(14);
            c16 = (byte[])row.get(15);

            assertEquals(1, c1.intValue());
            assertEquals(2, c2.intValue());
            assertEquals(3, c3.intValue());
            Assert.assertNotNull(c4);
            Assert.assertNotNull(c5);
            Assert.assertNotNull(c6);
            assertEquals(new BigDecimal("7.01"), c7);
            assertEquals(new BigDecimal("8.01"), c8);
            assertEquals(true, c9);
            assertEquals("content : 3", c10.trim());
            Assert.assertNotNull(c11);
            Assert.assertNotNull(c12);
            Assert.assertNotNull(c13);
            assertEquals("dabao", c14);
            assertEquals("long content : 3", c15);
            Assert.assertArrayEquals(blob, c16);

            reader.advance();

            row = (IndexedRecord) reader.getCurrent();
            c1 = (Integer) row.get(0);
            c2 = (Short) row.get(1);
            c3 = (Long) row.get(2);
            c4 = (Float) row.get(3);
            c5 = (Double) row.get(4);
            c6 = (Float) row.get(5);
            c7 = (BigDecimal) row.get(6);
            c8 = (BigDecimal) row.get(7);
            c9 = (Boolean) row.get(8);
            c10 = (String) row.get(9);
            c11 = (Long) row.get(10);
            c12 = (Long) row.get(11);
            c13 = (Long) row.get(12);
            c14 = (String) row.get(13);
            c15 = (String) row.get(14);
            c16 = (byte[])row.get(15);

            assertEquals(1, c1.intValue());
            Assert.assertNull(c2);
            Assert.assertNull(c3);
            Assert.assertNull(c4);
            Assert.assertNull(c5);
            Assert.assertNull(c6);
            Assert.assertNull(c7);
            Assert.assertNull(c8);
            Assert.assertNull(c9);
            Assert.assertNull(c10);
            Assert.assertNull(c11);
            Assert.assertNull(c12);
            // some database set default value for this column as default
            // Assert.assertNull(c13);
            Assert.assertNull(c14);
            Assert.assertNull(c15);
            Assert.assertArrayEquals(blob, c16);

            reader.advance();

            row = (IndexedRecord) reader.getCurrent();
            c1 = (Integer) row.get(0);
            c2 = (Short) row.get(1);
            c3 = (Long) row.get(2);
            c4 = (Float) row.get(3);
            c5 = (Double) row.get(4);
            c6 = (Float) row.get(5);
            c7 = (BigDecimal) row.get(6);
            c8 = (BigDecimal) row.get(7);
            c9 = (Boolean) row.get(8);
            c10 = (String) row.get(9);
            c11 = (Long) row.get(10);
            c12 = (Long) row.get(11);
            c13 = (Long) row.get(12);
            c14 = (String) row.get(13);
            c15 = (String) row.get(14);
            c16 = (byte[])row.get(15);

            Assert.assertNull(c1);
            Assert.assertNull(c2);
            Assert.assertNull(c3);
            Assert.assertNull(c4);
            Assert.assertNull(c5);
            Assert.assertNull(c6);
            Assert.assertNull(c7);
            Assert.assertNull(c8);
            Assert.assertNull(c9);
            Assert.assertNull(c10);
            Assert.assertNull(c11);
            Assert.assertNull(c12);
            // some database set default value for this column as default
            // Assert.assertNull(c13);
            Assert.assertEquals("good luck", c14);
            Assert.assertNull(c15);
            Assert.assertArrayEquals(blob, c16);

            reader.close();

            Map<String, Object> returnMap = reader.getReturnValues();
            Assert.assertEquals(10, returnMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            }
        }
    }

}
