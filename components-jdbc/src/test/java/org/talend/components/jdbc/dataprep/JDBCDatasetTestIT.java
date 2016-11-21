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

import org.apache.avro.Schema;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.datastore.JDBCDatastoreDefinition;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.ValidationResult;

public class JDBCDatasetTestIT {

    public static AllSetting allSetting;

    @BeforeClass
    public static void beforeClass() throws Exception {
        allSetting = DBTestUtils.createAllSetting();
        DBTestUtils.createTable(allSetting);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        DBTestUtils.releaseResource(allSetting);
    }

    @Test
    public void testUpdateSchema() {
        JDBCDatastoreDefinition def = new JDBCDatastoreDefinition();
        JDBCDatastoreProperties datastore = new JDBCDatastoreProperties("datastore");

        datastore.dbTypes.setValue("DERBY");
        datastore.afterDbTypes();

        datastore.driverClass.setValue(allSetting.getDriverClass());
        datastore.jdbcUrl.setValue(allSetting.getJdbcUrl());
        datastore.userPassword.userId.setValue(allSetting.getUsername());
        datastore.userPassword.password.setValue(allSetting.getPassword());

        ValidationResult result = datastore.validateTestConnection();
        assertEquals("result should be ok, but not", ValidationResult.OK, result);

        JDBCDatasetProperties dataset = (JDBCDatasetProperties) def.createDatasetProperties(datastore);
        dataset.sql.setValue(DBTestUtils.getSQL());

        dataset.updateSchema();

        Schema schema = dataset.main.schema.getValue();
        DBTestUtils.testMetadata(schema.getFields());
    }

}
