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
package org.talend.components.jdbc.dataprep;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.datastore.JDBCDatastoreDefinition;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;
import org.talend.components.jdbc.runtime.JdbcRuntimeUtils;
import org.talend.components.jdbc.runtime.dataprep.JDBCDatasetRuntime;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class JDBCInputTestIT {

    public static AllSetting allSetting;

    @BeforeClass
    public static void beforeClass() throws Exception {
        allSetting = DBTestUtils.createAllSetting();

        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
            DBTestUtils.createTestTable(conn, tablename);
        }
    }

    private static final String tablename = "JDBCINPUTDATASET";

    @AfterClass
    public static void afterClass() throws ClassNotFoundException, SQLException {
        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
            DBTestUtils.dropTestTable(conn, tablename);
        } finally {
            DBTestUtils.shutdownDBIfNecessary();
        }
    }

    @Before
    public void before() throws Exception {
        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
            DBTestUtils.truncateTable(conn, tablename);
            DBTestUtils.loadTestData(conn, tablename);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testReader() {
        Reader reader = null;
        try {
            JDBCInputDefinition definition = new JDBCInputDefinition();
            JDBCInputProperties properties = createCommonJDBCInputProperties(definition);

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

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testType() throws Exception {
        JDBCInputDefinition definition = new JDBCInputDefinition();
        JDBCInputProperties properties = createCommonJDBCInputProperties(definition);

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

    private JDBCInputProperties createCommonJDBCInputProperties(JDBCInputDefinition definition) {
        JDBCDatastoreDefinition datastore_def = new JDBCDatastoreDefinition();
        JDBCDatastoreProperties datastore_props = new JDBCDatastoreProperties("datastore");

        datastore_props.dbTypes.setValue("DERBY");
        datastore_props.afterDbTypes();

        datastore_props.jdbcUrl.setValue(allSetting.getJdbcUrl());
        datastore_props.userId.setValue(allSetting.getUsername());
        datastore_props.password.setValue(allSetting.getPassword());

        JDBCDatasetProperties dataset = (JDBCDatasetProperties) datastore_def.createDatasetProperties(datastore_props);
        dataset.sql.setValue(DBTestUtils.getSQL(tablename));

        updateSchema(dataset);

        JDBCInputProperties properties = (JDBCInputProperties) definition.createRuntimeProperties();
        properties.setDatasetProperties(dataset);

        return properties;
    }

    protected void updateSchema(JDBCDatasetProperties dataset) {
        JDBCDatasetRuntime runtime = new JDBCDatasetRuntime();
        runtime.initialize(null, dataset);
        dataset.main.schema.setValue(runtime.getSchema());
    }

}
