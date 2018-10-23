package org.talend.components.common.tableaction;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.avro.AvroUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TableActionManagerTest {

    private static Schema schema;

    private final static String[] fullTableName = new String[] { "server", "database", "schema", "table" };

    private final static Map<TableAction.TableActionEnum, Class> enumActionClassMap = new HashMap();

    static {
        enumActionClassMap.put(TableAction.TableActionEnum.NONE, NoAction.class);
        enumActionClassMap.put(TableAction.TableActionEnum.CLEAR, DefaultSQLClearTableAction.class);
        enumActionClassMap.put(TableAction.TableActionEnum.TRUNCATE, DefaultSQLTruncateTableAction.class);
        enumActionClassMap.put(TableAction.TableActionEnum.CREATE, DefaultSQLCreateTableAction.class);
        enumActionClassMap.put(TableAction.TableActionEnum.CREATE_IF_NOT_EXISTS, DefaultSQLCreateTableAction.class);
        enumActionClassMap.put(TableAction.TableActionEnum.DROP_CREATE, DefaultSQLCreateTableAction.class);
        enumActionClassMap.put(TableAction.TableActionEnum.DROP_IF_EXISTS_AND_CREATE,
                DefaultSQLCreateTableAction.class);
    }

    @Before
    public void createSchema() {
        schema = SchemaBuilder.builder()
                .record("main")
                .fields()
                .name("integer_fld")
                .type(Schema.createUnion(AvroUtils._int(), Schema.create(Schema.Type.NULL)))
                .withDefault(1)
                .name("string_fld")
                .type(Schema.createUnion(AvroUtils._string(), Schema.create(Schema.Type.NULL)))
                .noDefault()
                .name("date_fld")
                .type(Schema.createUnion(AvroUtils._logicalDate(), Schema.create(Schema.Type.NULL)))
                .noDefault()
                .endRecord();
    }

    @Test
    public void create() {
        for (Map.Entry<TableAction.TableActionEnum, Class> test : enumActionClassMap.entrySet()) {
            TableAction tableAction = TableActionManager.create(test.getKey(), fullTableName, schema);
            assertEquals(test.getValue(), tableAction.getClass());
        }
    }

    @Test
    public void buildQueries(){
        try {
            Map<String, String> dbTypeMap = new HashMap<>();
            dbTypeMap.put("integer_fld", "MY_DB_TYPE");

            TableActionConfig config = new TableActionConfig();
            config.SQL_ESCAPE_ENABLED = true;
            config.SQL_ESCAPE = "|";

            List<String> queries = TableActionManager.buildQueries(TableAction.TableActionEnum.DROP_IF_EXISTS_AND_CREATE,
                    new String[] { "aaa", "bbb", "ccc" }, schema, config,
                    dbTypeMap);

            assertEquals(2, queries.size());
            assertEquals("DROP TABLE IF EXISTS |aaa|.|bbb|.|ccc|", queries.get(0));
            assertEquals("CREATE TABLE |aaa|.|bbb|.|ccc| (|integer_fld| MY_DB_TYPE, |string_fld| VARCHAR, |date_fld| DATE)", queries.get(1));

        }
        catch (Exception e){
            fail("Exception raised : "+e.getMessage());
        }
    }

}