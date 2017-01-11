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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
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
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.components.jdbc.runtime.reader.JDBCInputReader;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputDefinition;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class JDBCInputTestIT {

    public static AllSetting allSetting;

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
    public void before() throws SQLException, ClassNotFoundException {
        DBTestUtils.truncateTableAndLoadData(allSetting);
    }

    @Test
    public void testGetSchemaNames() throws Exception {
        TJDBCInputDefinition definition = new TJDBCInputDefinition();
        TJDBCInputProperties properties = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition);

        properties.main.schema.setValue(DBTestUtils.createTestSchema());
        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
        properties.sql.setValue(DBTestUtils.getSQL());

        JDBCSource source = DBTestUtils.createCommonJDBCSource(properties);

        List<NamedThing> schemaNames = source.getSchemaNames(null);
        assertTrue(schemaNames != null);
        assertTrue(!schemaNames.isEmpty());

        boolean exists = false;
        for (NamedThing name : schemaNames) {
            if ("TEST".equals(name.getName().toUpperCase())) {
                exists = true;
                break;
            }
        }

        assertTrue(exists);
    }

    @Test
    public void testGetSchema() throws Exception {
        TJDBCInputDefinition definition = new TJDBCInputDefinition();
        TJDBCInputProperties properties = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition);

        properties.main.schema.setValue(DBTestUtils.createTestSchema());
        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
        properties.sql.setValue(DBTestUtils.getSQL());

        JDBCSource source = DBTestUtils.createCommonJDBCSource(properties);

        Schema schema = source.getEndpointSchema(null, "TEST");
        assertEquals("TEST", schema.getName().toUpperCase());
        List<Field> columns = schema.getFields();
        DBTestUtils.testMetadata(columns);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testReader() {
        Reader reader = null;
        try {
            TJDBCInputDefinition definition = new TJDBCInputDefinition();
            TJDBCInputProperties properties = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition);

            properties.main.schema.setValue(DBTestUtils.createTestSchema());
            properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
            properties.sql.setValue(DBTestUtils.getSQL());

            reader = DBTestUtils.createCommonJDBCInputReader(properties);

            reader.start();

            IndexedRecord row = (IndexedRecord) reader.getCurrent();
            String id = (String) row.get(0);
            String name = (String) row.get(1);

            assertEquals("1", id);
            assertEquals("wangwei", name);

            reader.advance();

            row = (IndexedRecord) reader.getCurrent();
            id = (String) row.get(0);
            name = (String) row.get(1);

            assertEquals("2", id);
            assertEquals("gaoyan", name);

            reader.advance();

            row = (IndexedRecord) reader.getCurrent();
            id = (String) row.get(0);
            name = (String) row.get(1);

            assertEquals("3", id);
            assertEquals("dabao", name);

            reader.close();

            Map<String, Object> returnMap = reader.getReturnValues();
            Assert.assertEquals(3, returnMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
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
    
    /**
     * Checks {@link JDBCInputReader} outputs {@link IndexedRecord} which contains nullable String type data for every SQL/JDBC type
     */
    @Test
    public void testReaderAllTypesString() throws IOException {
        TJDBCInputDefinition definition = new TJDBCInputDefinition();
        TJDBCInputProperties properties = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition);

        properties.main.schema.setValue(DBTestUtils.createAllTypesSchema());
        properties.tableSelection.tablename.setValue(DBTestUtils.getAllTypesTablename());
        properties.sql.setValue(DBTestUtils.getAllTypesSQL());

        Reader reader = DBTestUtils.createCommonJDBCInputReader(properties);

        reader.start();

        IndexedRecord record = (IndexedRecord) reader.getCurrent();
        String col0 = (String) record.get(0);
        String col1 = (String) record.get(1);
        String col2 = (String) record.get(2);
        String col3 = (String) record.get(3);
        String col4 = (String) record.get(4);
        String col5 = (String) record.get(5);
        String col6 = (String) record.get(6);
        String col7 = (String) record.get(7);
        String col8 = (String) record.get(8);
        String col9 = (String) record.get(9);
        String col10 = (String) record.get(10);
        String col11 = (String) record.get(11);
        String col12 = (String) record.get(12);
        String col13 = (String) record.get(13);
        
        assertEquals("32767", col0);
        assertEquals("2147483647", col1);
        assertEquals("9223372036854775807", col2);
        assertEquals("1.1111112", col3);
        assertEquals("2.222222222", col4);
        assertEquals("1234567890.1234567890", col5);
        assertEquals("abcd", col6);
        assertEquals("abcdefg", col7);
        assertEquals("00010203040506070809", col8);
        assertEquals("abcdefg", col9);
        assertEquals("2016-12-28", col10);
        assertEquals("14:30:33", col11);
        assertEquals("2016-12-28 14:31:56.12345", col12);
        assertEquals("true", col13);
        
        Schema actualSchema = record.getSchema();
        List<Field> actualFields = actualSchema.getFields();
        
        assertEquals(14, actualFields.size());
        
        Schema nullableStringSchema = AvroUtils.wrapAsNullable(AvroUtils._string());
        assertEquals(nullableStringSchema, actualFields.get(0).schema());
        assertEquals(nullableStringSchema, actualFields.get(1).schema());
        assertEquals(nullableStringSchema, actualFields.get(2).schema());
        assertEquals(nullableStringSchema, actualFields.get(3).schema());
        assertEquals(nullableStringSchema, actualFields.get(4).schema());
        assertEquals(nullableStringSchema, actualFields.get(5).schema());
        assertEquals(nullableStringSchema, actualFields.get(6).schema());
        assertEquals(nullableStringSchema, actualFields.get(7).schema());
        assertEquals(nullableStringSchema, actualFields.get(8).schema());
        assertEquals(nullableStringSchema, actualFields.get(9).schema());
        assertEquals(nullableStringSchema, actualFields.get(10).schema());
        assertEquals(nullableStringSchema, actualFields.get(11).schema());
        assertEquals(nullableStringSchema, actualFields.get(12).schema());
        assertEquals(nullableStringSchema, actualFields.get(13).schema());
        
        reader.close();
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testType() throws Exception {
        TJDBCInputDefinition definition = new TJDBCInputDefinition();
        TJDBCInputProperties properties = DBTestUtils.createCommonJDBCInputProperties(allSetting, definition);

        properties.main.schema.setValue(DBTestUtils.createTestSchema());
        properties.tableSelection.tablename.setValue(DBTestUtils.getTablename());
        properties.sql.setValue(DBTestUtils.getSQL());

        Reader reader = DBTestUtils.createCommonJDBCInputReader(properties);

        try {
            IndexedRecordConverter<Object, ? extends IndexedRecord> converter = null;

            for (boolean available = reader.start(); available; available = reader.advance()) {
                converter = DBTestUtils.getIndexRecordConverter(reader, converter);

                IndexedRecord record = converter.convertToAvro(reader.getCurrent());

                assertEquals(String.class, record.get(0).getClass());
                assertEquals(String.class, record.get(1).getClass());
            }

            reader.close();
        } finally {
            reader.close();
        }
    }

}
