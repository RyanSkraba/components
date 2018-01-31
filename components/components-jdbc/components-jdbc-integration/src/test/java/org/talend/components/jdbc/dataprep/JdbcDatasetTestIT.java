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

import java.sql.Connection;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.datastore.JDBCDatastoreDefinition;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;
import org.talend.components.jdbc.runtime.JdbcRuntimeUtils;
import org.talend.components.jdbc.runtime.dataprep.JDBCDatasetRuntime;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.test.PropertiesTestUtils;

public class JdbcDatasetTestIT {

    public static AllSetting allSetting;

    @BeforeClass
    public static void beforeClass() throws Exception {
        PropertiesTestUtils.setupPaxUrlFromMavenLaunch();
        allSetting = DBTestUtils.createAllSetting();
        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
          DBTestUtils.createTestTable(conn, tablename);
          DBTestUtils.loadTestData(conn, tablename);
      }
    }

    private static final String tablename = "JDBCDATASETINTEGRATION";

    @AfterClass
    public static void afterClass() throws Exception {
        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
            DBTestUtils.dropTestTable(conn, tablename);
        } finally {
            DBTestUtils.shutdownDBIfNecessary();
        }
    }

    @Test
    public void testUpdateSchema() {
        JDBCDatasetProperties dataset = createDatasetProperties(true);

        Schema schema = dataset.main.schema.getValue();

        Assert.assertNotNull(schema);
        DBTestUtils.testMetadata(schema.getFields(), true);
    }

    @Test
    public void testGetSchema() {
        JDBCDatasetProperties dataset = createDatasetProperties(false);

        JDBCDatasetRuntime runtime = new JDBCDatasetRuntime();
        runtime.initialize(null, dataset);
        Schema schema = runtime.getSchema();

        Assert.assertNotNull(schema);
        DBTestUtils.testMetadata(schema.getFields(),true);
    }

    @Test
    public void testGetSampleWithValidDesignSchema() {
        JDBCDatasetProperties dataset = createDatasetProperties(true);
        getSampleAction(dataset);
    }

    @Test
    public void testGetSampleWithoutDesignSchema() {
        JDBCDatasetProperties dataset = createDatasetProperties(false);
        getSampleAction(dataset);
    }

    private void getSampleAction(JDBCDatasetProperties dataset) {
        JDBCDatasetRuntime runtime = new JDBCDatasetRuntime();
        runtime.initialize(null, dataset);
        final IndexedRecord[] record = new IndexedRecord[1];
        Consumer<IndexedRecord> storeTheRecords = new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord data) {
                record[0] = data;

            }
        };

        runtime.getSample(1, storeTheRecords);
        Assert.assertEquals("1", record[0].get(0));
        Assert.assertEquals("wangwei", record[0].get(1));
    }

    private JDBCDatasetProperties createDatasetProperties(boolean updateSchema) {
        JDBCDatastoreDefinition def = new JDBCDatastoreDefinition();
        JDBCDatastoreProperties datastore = new JDBCDatastoreProperties("datastore");

        datastore.dbTypes.setValue("DERBY");
        datastore.afterDbTypes();

        datastore.jdbcUrl.setValue(allSetting.getJdbcUrl());
        datastore.userId.setValue(allSetting.getUsername());
        datastore.password.setValue(allSetting.getPassword());

        JDBCDatasetProperties dataset = (JDBCDatasetProperties) def.createDatasetProperties(datastore);
        dataset.sql.setValue(DBTestUtils.getSQL(tablename));

        if (updateSchema) {
            dataset.updateSchema();
        }
        return dataset;
    }

}
