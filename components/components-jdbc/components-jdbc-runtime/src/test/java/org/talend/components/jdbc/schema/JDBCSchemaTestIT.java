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
package org.talend.components.jdbc.schema;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.ComponentConstants;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.runtime.JDBCSource;
import org.talend.components.jdbc.runtime.JdbcRuntimeUtils;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputDefinition;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public class JDBCSchemaTestIT {

    public static AllSetting allSetting;

    private static final String tablename = "JDBCSCHEMA";
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        allSetting = DBTestUtils.createAllSetting();

        DBTestUtils.createTableWithSpecialName(allSetting,tablename);
    }

    @AfterClass
    public static void afterClass() throws ClassNotFoundException, SQLException {
        try (Connection conn = JdbcRuntimeUtils.createConnection(allSetting)) {
            DBTestUtils.dropTestTable(conn, tablename);
        } finally {
            DBTestUtils.shutdownDBIfNecessary();
        }
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
        container.setComponentData(container.getCurrentComponentId(), ComponentConstants.MAPPING_URL_SUBFIX, mappings_url);
        
        Schema schema = source.getEndpointSchema(container, tablename);
        assertEquals(tablename, schema.getName().toUpperCase());
        List<Field> columns = schema.getFields();
        testMetadata(columns);
    }

    private void testMetadata(List<Field> columns) {
        Schema.Field field = columns.get(0);

        assertEquals("P1_VENTE_QT_", field.name());
        assertEquals("P1_VENTE_QTÉ", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        assertEquals(AvroUtils._int(), AvroUtils.unwrapIfNullable(field.schema()));
        assertEquals("INTEGER", field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_TYPE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_SCALE));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals(null, field.getObjectProp(SchemaConstants.TALEND_COLUMN_DEFAULT));
    }


}
