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
import org.talend.components.jdbc.dataprep.di.TDataPrepDBInputDefinition;
import org.talend.components.jdbc.dataprep.di.TDataPrepDBInputProperties;
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.NamedThing;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class TDataPrepDBInputTestIT {

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
        TDataPrepDBInputDefinition definition = new TDataPrepDBInputDefinition();
        TDataPrepDBInputProperties properties = createCommonJDBCInputProperties(definition);

        properties.main.schema.setValue(DBTestUtils.createTestSchema());
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
        TDataPrepDBInputDefinition definition = new TDataPrepDBInputDefinition();
        TDataPrepDBInputProperties properties = createCommonJDBCInputProperties(definition);

        properties.main.schema.setValue(DBTestUtils.createTestSchema());
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
            TDataPrepDBInputDefinition definition = new TDataPrepDBInputDefinition();
            TDataPrepDBInputProperties properties = createCommonJDBCInputProperties(definition);

            properties.main.schema.setValue(DBTestUtils.createTestSchema());
            properties.sql.setValue(DBTestUtils.getSQL());

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
        properties.sql.setValue(DBTestUtils.getSQL());

        Reader reader = DBTestUtils.createCommonJDBCInputReader(properties);

        try {
            IndexedRecordConverter<Object, ? extends IndexedRecord> converter = null;

            for (boolean available = reader.start(); available; available = reader.advance()) {
                converter = DBTestUtils.getIndexRecordConverter(reader, converter);

                IndexedRecord record = converter.convertToAvro(reader.getCurrent());

                assertEquals(Integer.class, record.get(0).getClass());
                assertEquals(String.class, record.get(1).getClass());
            }

            reader.close();
        } finally {
            reader.close();
        }
    }

    private TDataPrepDBInputProperties createCommonJDBCInputProperties(TDataPrepDBInputDefinition definition) {
        TDataPrepDBInputProperties properties = (TDataPrepDBInputProperties) definition.createRuntimeProperties();

        properties.dbTypes.setValue("DERBY");
        properties.jdbcUrl.setValue(allSetting.getJdbcUrl());

        properties.userPassword.userId.setValue(allSetting.getUsername());
        properties.userPassword.password.setValue(allSetting.getPassword());
        return properties;
    }

}
