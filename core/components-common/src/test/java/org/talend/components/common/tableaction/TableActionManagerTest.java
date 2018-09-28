package org.talend.components.common.tableaction;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.avro.AvroUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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
                .type(AvroUtils._int())
                .withDefault(1)
                .name("string_fld")
                .type(AvroUtils._string())
                .noDefault()
                .name("date_fld")
                .type(AvroUtils._logicalDate())
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

}