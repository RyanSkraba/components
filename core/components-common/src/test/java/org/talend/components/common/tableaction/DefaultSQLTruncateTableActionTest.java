package org.talend.components.common.tableaction;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DefaultSQLTruncateTableActionTest {

    private static Schema schema;

    @Before
    public void createSchema() {
        schema = SchemaBuilder.builder()
                .record("main")
                .fields()
                .name("id")
                .prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
                .type(AvroUtils._int())
                .withDefault(1)
                .name("name")
                .prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255")
                .prop(SchemaConstants.TALEND_COLUMN_DEFAULT, "\"ok\"")
                .type(AvroUtils._string())
                .noDefault()
                .name("date")
                .type(AvroUtils._logicalDate())
                .noDefault()
                .name("salary")
                .prop(SchemaConstants.TALEND_COLUMN_DB_TYPE, "MY_DOUBLE")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "38")
                .prop(SchemaConstants.TALEND_COLUMN_SCALE, "4")
                .type(AvroUtils._double())
                .withDefault("0")
                .name("updated")
                .type(AvroUtils._logicalTimestamp())
                .noDefault()
                .endRecord();
    }

    @Test
    public void truncateNCreateTable() {
        DefaultSQLTruncateTableAction action = new DefaultSQLTruncateTableAction(new String[] { "MyTable" });
        TableActionConfig conf = new TableActionConfig();
        conf.SQL_ESCAPE_ENABLED = false;
        action.setConfig(conf);
        try {
            List<String> queries = action.getQueries();
            assertEquals(1, queries.size());
            assertEquals("TRUNCATE TABLE MyTable", queries.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void truncateNCreateTableWithFullName() {
        DefaultSQLTruncateTableAction action =
                new DefaultSQLTruncateTableAction(new String[] { "mydatabase", "myschema", "MyTable" });
        TableActionConfig conf = new TableActionConfig();
        action.setConfig(conf);
        try {
            List<String> queries = action.getQueries();
            assertEquals(1, queries.size());
            assertEquals("TRUNCATE TABLE \"mydatabase\".\"myschema\".\"MyTable\"", queries.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void truncateNCreateTableBadName() {
        try {
            DefaultSQLTruncateTableAction action = new DefaultSQLTruncateTableAction(null);
            fail("Should fail since table name is null.");
        } catch (Exception e) {
        }

        try {
            DefaultSQLTruncateTableAction action = new DefaultSQLTruncateTableAction(new String[] {});
            fail("Should fail since table name is empty.");
        } catch (Exception e) {
        }
    }

}