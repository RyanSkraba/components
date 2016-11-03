package org.talend.components.jdbc.type;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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
import org.talend.components.jdbc.JDBCConnectionTestIT;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.runtime.JDBCSource;
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
import org.talend.daikon.di.DiOutgoingSchemaEnforcer;

public class JDBCTypeMappingTestIT {

    private static String driverClass;

    private static String jdbcUrl;

    private static String userId;

    private static String password;

    public static AllSetting allSetting;

    private static String tablename;

    private static String sql;

    @BeforeClass
    public static void init() throws Exception {
        java.util.Properties props = new java.util.Properties();
        try (InputStream is = JDBCConnectionTestIT.class.getClassLoader().getResourceAsStream("connection.properties")) {
            props = new java.util.Properties();
            props.load(is);
        }

        driverClass = props.getProperty("driverClass");

        jdbcUrl = props.getProperty("jdbcUrl");

        userId = props.getProperty("userId");

        password = props.getProperty("password");

        tablename = props.getProperty("tablename");

        sql = props.getProperty("sql");

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
        DBTestUtils.prepareTableAndDataForEveryType(allSetting);
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

    @Test
    public void testGetSchema() throws Exception {
        TJDBCInputDefinition definition = new TJDBCInputDefinition();
        TJDBCInputProperties properties = createCommonJDBCInputProperties(definition);

        properties.main.schema.setValue(DBTestUtils.createTestSchema3(true));
        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue(sql);

        JDBCSource source = DBTestUtils.createCommonJDBCSource(properties);

        Schema schema = source.getEndpointSchema(null, "TEST");
        assertEquals("TEST", schema.getName().toUpperCase());
        List<Field> columns = schema.getFields();
        testMetadata(columns);
    }

    // we comment the assert which may be database special
    private void testMetadata(List<Field> columns) {
        Schema.Field field = columns.get(0);

        assertEquals("C1", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._int(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals(java.sql.Types.INTEGER, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        // assertEquals(10, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(1);

        assertEquals("C2", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._short(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals(java.sql.Types.SMALLINT, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        // assertEquals(5, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(2);

        assertEquals("C3", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._long(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals(java.sql.Types.BIGINT, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        // assertEquals(19, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(3);

        assertEquals("C4", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        // assertEquals(AvroUtils._float(), AvroUtils.unwrapIfNullable(field.schema()));
        // assertEquals(java.sql.Types.REAL, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        // assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(4);

        assertEquals("C5", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._double(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals(java.sql.Types.DOUBLE, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
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
        assertEquals(java.sql.Types.DECIMAL, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(10, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(2, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(7);

        assertEquals("C8", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._decimal(), AvroUtils.unwrapIfNullable(field.schema()));
        // assertEquals(java.sql.Types.NUMERIC, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(10, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(2, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
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
        assertEquals(java.sql.Types.CHAR, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(64, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(10);

        assertEquals("C11", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._date(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals(java.sql.Types.DATE, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals("yyyy-MM-dd", field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(11);

        assertEquals("C12", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._date(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals(java.sql.Types.TIME, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals("HH:mm:ss", field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(12);

        assertEquals("C13", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._date(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals(java.sql.Types.TIMESTAMP, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals("yyyy-MM-dd HH:mm:ss.SSS", field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        // assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(13);

        assertEquals("C14", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._string(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals(java.sql.Types.VARCHAR, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(64, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(14);

        assertEquals("C15", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._string(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals(java.sql.Types.LONGVARCHAR, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        // assertEquals(32700, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
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
        TJDBCInputProperties properties = createCommonJDBCInputProperties(definition);

        properties.main.schema.setValue(DBTestUtils.createTestSchema3(nullableForAnyColumn));
        properties.tableSelection.tablename.setValue(tablename);
        properties.sql.setValue(sql);

        Reader reader = DBTestUtils.createCommonJDBCInputReader(properties);

        try {
            IndexedRecordConverter<Object, ? extends IndexedRecord> converter = null;

            DiOutgoingSchemaEnforcer current = new DiOutgoingSchemaEnforcer(properties.main.schema.getValue(), false);

            reader.start();

            converter = DBTestUtils.getIndexRecordConverter(reader, converter);

            IndexedRecord unenforced = converter.convertToAvro(reader.getCurrent());
            current.setWrapped(unenforced);

            assertEquals(Integer.class, current.get(0).getClass());
            assertEquals(Short.class, current.get(1).getClass());
            assertEquals(Long.class, current.get(2).getClass());
            assertEquals(Float.class, current.get(3).getClass());
            assertEquals(Double.class, current.get(4).getClass());
            assertEquals(Float.class, current.get(5).getClass());
            assertEquals(BigDecimal.class, current.get(6).getClass());
            assertEquals(BigDecimal.class, current.get(7).getClass());
            assertEquals(Boolean.class, current.get(8).getClass());
            assertEquals(String.class, current.get(9).getClass());
            assertEquals(java.util.Date.class, current.get(10).getClass());
            assertEquals(java.util.Date.class, current.get(11).getClass());
            assertEquals(java.util.Date.class, current.get(12).getClass());
            assertEquals(String.class, current.get(13).getClass());
            assertEquals(String.class, current.get(14).getClass());

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
            TJDBCInputProperties properties = createCommonJDBCInputProperties(definition);

            properties.main.schema.setValue(DBTestUtils.createTestSchema3(nullableForAnyColumn));
            properties.tableSelection.tablename.setValue(tablename);
            properties.sql.setValue(sql);

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

            assertEquals(new Integer("1"), c1);
            assertEquals(new Short("2"), c2);
            assertEquals(new Long("3"), c3);
            Assert.assertNotNull(c4);
            Assert.assertNotNull(c5);
            Assert.assertNotNull(c6);
            assertEquals(new BigDecimal("7.01"), c7);
            assertEquals(new BigDecimal("8.01"), c8);
            assertEquals(Boolean.TRUE, c9);
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

            assertEquals(new Integer("1"), c1);
            assertEquals(new Short("2"), c2);
            assertEquals(new Long("3"), c3);
            Assert.assertNotNull(c4);
            Assert.assertNotNull(c5);
            Assert.assertNotNull(c6);
            assertEquals(new BigDecimal("7.01"), c7);
            assertEquals(new BigDecimal("8.01"), c8);
            assertEquals(Boolean.TRUE, c9);
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

            assertEquals(new Integer("1"), c1);
            assertEquals(new Short("2"), c2);
            assertEquals(new Long("3"), c3);
            Assert.assertNotNull(c4);
            Assert.assertNotNull(c5);
            Assert.assertNotNull(c6);
            assertEquals(new BigDecimal("7.01"), c7);
            assertEquals(new BigDecimal("8.01"), c8);
            assertEquals(Boolean.TRUE, c9);
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

            assertEquals(new Integer("1"), c1);
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
        TJDBCOutputProperties properties = createCommonJDBCOutputProperties(definition);

        Schema schema = DBTestUtils.createTestSchema3(nullableForAnyColumn);
        properties.main.schema.setValue(schema);
        properties.updateOutputSchemas();

        properties.tableSelection.tablename.setValue(tablename);

        properties.dataAction.setValue(DataAction.INSERT);

        JDBCOutputWriter writer = DBTestUtils.createCommonJDBCOutputWriter(definition, properties);

        try {
            writer.open("wid");

            List<IndexedRecord> inputRecords = DBTestUtils.prepareIndexRecords(nullableForAnyColumn);
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
            TJDBCInputProperties properties1 = createCommonJDBCInputProperties(definition1);

            properties1.main.schema.setValue(DBTestUtils.createTestSchema3(nullableForAnyColumn));
            properties1.tableSelection.tablename.setValue(tablename);
            properties1.sql.setValue(sql);

            reader = DBTestUtils.createCommonJDBCInputReader(properties1);

            reader.start();
            int i = 0;
            while ((i++) < 5) {// skip the 5 rows at the head
                reader.advance();
            }

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

            assertEquals(new Integer("1"), c1);
            assertEquals(new Short("2"), c2);
            assertEquals(new Long("3"), c3);
            Assert.assertNotNull(c4);
            Assert.assertNotNull(c5);
            Assert.assertNotNull(c6);
            assertEquals(new BigDecimal("7.01"), c7);
            assertEquals(new BigDecimal("8.01"), c8);
            assertEquals(Boolean.TRUE, c9);
            assertEquals("content : 1", c10.trim());
            Assert.assertNotNull(c11);
            Assert.assertNotNull(c12);
            Assert.assertNotNull(c13);
            assertEquals("wangwei", c14);
            assertEquals("long content : 1", c15);

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

            assertEquals(new Integer("1"), c1);
            assertEquals(new Short("2"), c2);
            assertEquals(new Long("3"), c3);
            Assert.assertNotNull(c4);
            Assert.assertNotNull(c5);
            Assert.assertNotNull(c6);
            assertEquals(new BigDecimal("7.01"), c7);
            assertEquals(new BigDecimal("8.01"), c8);
            assertEquals(Boolean.TRUE, c9);
            assertEquals("content : 2", c10.trim());
            Assert.assertNotNull(c11);
            Assert.assertNotNull(c12);
            Assert.assertNotNull(c13);
            assertEquals("gaoyan", c14);
            assertEquals("long content : 2", c15);

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

            assertEquals(new Integer("1"), c1);
            assertEquals(new Short("2"), c2);
            assertEquals(new Long("3"), c3);
            Assert.assertNotNull(c4);
            Assert.assertNotNull(c5);
            Assert.assertNotNull(c6);
            assertEquals(new BigDecimal("7.01"), c7);
            assertEquals(new BigDecimal("8.01"), c8);
            assertEquals(Boolean.TRUE, c9);
            assertEquals("content : 3", c10.trim());
            Assert.assertNotNull(c11);
            Assert.assertNotNull(c12);
            Assert.assertNotNull(c13);
            assertEquals("dabao", c14);
            assertEquals("long content : 3", c15);

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

            assertEquals(new Integer("1"), c1);
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

}
