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
package org.talend.components.jdbc.dataprep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.dataprep.TDataPrepDBInputProperties.DBType;
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.NamedThing;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.daikon.di.DiOutgoingSchemaEnforcer;

public class TDataPrepDBInputTestIT {

    private static String driverClass;

    private static String jdbcUrl;

    private static String userId;

    private static String password;

    private static String sql;

    public static AllSetting allSetting;

    @BeforeClass
    public static void init() throws Exception {
        java.util.Properties props = new java.util.Properties();
        try (InputStream is = TDataPrepDBInputTestIT.class.getClassLoader().getResourceAsStream("connection.properties")) {
            props = new java.util.Properties();
            props.load(is);
        }

        driverClass = props.getProperty("driverClass");

        jdbcUrl = props.getProperty("jdbcUrl");

        userId = props.getProperty("userId");

        password = props.getProperty("password");

        sql = props.getProperty("sql");

        allSetting = new AllSetting();
        allSetting.setDriverClass(driverClass);
        allSetting.setJdbcUrl(jdbcUrl);
        allSetting.setUsername(userId);
        allSetting.setPassword(password);

        DBTestUtils.prepareTableAndData(allSetting);
    }

    @AfterClass
    public static void clean() throws ClassNotFoundException, SQLException {
        DBTestUtils.releaseResource(allSetting);
    }

    @Test
    public void testGetSchemaNames() throws Exception {
        TDataPrepDBInputDefinition definition = new TDataPrepDBInputDefinition();
        TDataPrepDBInputProperties properties = createCommonJDBCInputProperties(definition);

        properties.main.schema.setValue(DBTestUtils.createTestSchema());
        properties.sql.setValue(sql);

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
        TDataPrepDBInputDefinition definition = new TDataPrepDBInputDefinition();
        TDataPrepDBInputProperties properties = createCommonJDBCInputProperties(definition);

        properties.main.schema.setValue(DBTestUtils.createTestSchema());
        properties.sql.setValue(sql);

        JDBCSource source = DBTestUtils.createCommonJDBCSource(properties);

        Schema schema = source.getEndpointSchema(null, "TEST");
        assertEquals("TEST", schema.getName().toUpperCase());
        List<Field> columns = schema.getFields();
        testMetadata(columns);
    }

    private void testMetadata(List<Field> columns) {
        Schema.Field field = columns.get(0);

        assertEquals("ID", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(Schema.Type.INT, AvroUtils.unwrapIfNullable(field.schema()).getType());
        assertEquals(java.sql.Types.INTEGER, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(10, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));

        field = columns.get(1);

        assertEquals("NAME", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(Schema.Type.STRING, AvroUtils.unwrapIfNullable(field.schema()).getType());
        assertEquals(java.sql.Types.VARCHAR, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(8, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PRECISION));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testReader() {
        Reader reader = null;
        try {
            TDataPrepDBInputDefinition definition = new TDataPrepDBInputDefinition();
            TDataPrepDBInputProperties properties = createCommonJDBCInputProperties(definition);

            properties.main.schema.setValue(DBTestUtils.createTestSchema());
            properties.sql.setValue(sql);

            reader = DBTestUtils.createCommonJDBCInputReader(properties);

            reader.start();

            IndexedRecord row = (IndexedRecord) reader.getCurrent();
            Integer id = (Integer) row.get(0);
            String name = (String) row.get(1);

            assertEquals(new Integer("1"), id);
            assertEquals("wangwei", name);

            reader.advance();

            row = (IndexedRecord) reader.getCurrent();
            id = (Integer) row.get(0);
            name = (String) row.get(1);

            assertEquals(new Integer("2"), id);
            assertEquals("gaoyan", name);

            reader.advance();

            row = (IndexedRecord) reader.getCurrent();
            id = (Integer) row.get(0);
            name = (String) row.get(1);

            assertEquals(new Integer("3"), id);
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

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testType() throws Exception {
        TDataPrepDBInputDefinition definition = new TDataPrepDBInputDefinition();
        TDataPrepDBInputProperties properties = createCommonJDBCInputProperties(definition);

        properties.main.schema.setValue(DBTestUtils.createTestSchema());
        properties.sql.setValue(sql);

        Reader reader = DBTestUtils.createCommonJDBCInputReader(properties);

        try {
            IndexedRecordConverter<Object, ? extends IndexedRecord> converter = null;

            DiOutgoingSchemaEnforcer current = new DiOutgoingSchemaEnforcer(properties.main.schema.getValue(), false);

            for (boolean available = reader.start(); available; available = reader.advance()) {
                converter = DBTestUtils.getIndexRecordConverter(reader, converter);

                IndexedRecord unenforced = converter.convertToAvro(reader.getCurrent());
                current.setWrapped(unenforced);

                assertEquals(Integer.class, current.get(0).getClass());
                assertEquals(String.class, current.get(1).getClass());
            }

            reader.close();
        } finally {
            reader.close();
        }
    }

    private TDataPrepDBInputProperties createCommonJDBCInputProperties(TDataPrepDBInputDefinition definition) {
        TDataPrepDBInputProperties properties = (TDataPrepDBInputProperties) definition.createRuntimeProperties();

        properties.dbTypes.setValue(DBType.DERBY);
        properties.jdbcUrl.setValue(jdbcUrl);
        properties.userPassword.userId.setValue(userId);
        properties.userPassword.password.setValue(password);
        return properties;
    }

}
