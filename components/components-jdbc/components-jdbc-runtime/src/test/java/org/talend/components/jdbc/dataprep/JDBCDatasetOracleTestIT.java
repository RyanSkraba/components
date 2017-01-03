// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.math.BigDecimal;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.datastore.JDBCDatastoreDefinition;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;
import org.talend.components.jdbc.runtime.dataprep.JDBCDatasetRuntime;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.java8.Consumer;

/**
 * enable the test when we support to test more than one type database in one test run
 *
 */
public class JDBCDatasetOracleTestIT {

    public static AllSetting allSetting;

    @BeforeClass
    public static void beforeClass() throws Exception {
        allSetting = new AllSetting();

        // please set your connection parameter
        allSetting.setDriverClass("oracle.jdbc.OracleDriver");
        allSetting.setJdbcUrl("jdbc:oracle:thin:@host:1521:db");
        allSetting.setUsername("");
        allSetting.setPassword("");

        DBTestUtils.createTable(allSetting);
        DBTestUtils.truncateTableAndLoadData(allSetting);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        DBTestUtils.releaseResource(allSetting);
    }

    @Ignore
    @Test
    public void testGetSchema() {
        JDBCDatasetProperties dataset = createDatasetProperties();

        JDBCDatasetRuntime runtime = new JDBCDatasetRuntime();
        runtime.initialize(null, dataset);
        Schema schema = runtime.getSchema();

        Assert.assertNotNull(schema);
        Assert.assertEquals(2, schema.getFields().size());
    }

    @Ignore
    @Test
    public void testGetSampleWithoutDesignSchema() {
        JDBCDatasetProperties dataset = createDatasetProperties();
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
        Assert.assertEquals(new BigDecimal("1"), record[0].get(0));
        Assert.assertEquals("wangwei", record[0].get(1));
    }

    private JDBCDatasetProperties createDatasetProperties() {
        JDBCDatastoreDefinition def = new JDBCDatastoreDefinition();
        JDBCDatastoreProperties datastore = new JDBCDatastoreProperties("datastore");

        datastore.dbTypes.setValue("ORACLE");

        datastore.jdbcUrl.setValue(allSetting.getJdbcUrl());
        datastore.userId.setValue(allSetting.getUsername());
        datastore.password.setValue(allSetting.getPassword());

        JDBCDatasetProperties dataset = (JDBCDatasetProperties) def.createDatasetProperties(datastore);
        dataset.sql.setValue(DBTestUtils.getSQL());

        return dataset;
    }

}
