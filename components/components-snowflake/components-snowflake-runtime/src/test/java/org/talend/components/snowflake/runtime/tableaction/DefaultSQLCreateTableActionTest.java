// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.runtime.tableaction;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.talend.components.common.tableaction.DefaultSQLCreateTableAction;
import org.talend.components.common.tableaction.TableActionConfig;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Tests Snowflake Create table SQL queries
 */
public class DefaultSQLCreateTableActionTest {

    /**
     * Checks that CREATE TABLE IF NOT EXISTS query creation sets length and precision only in case db type
     * supports it.
     *
     * @throws Exception
     */
    @Test
    public void testGetQueriesDefault() throws Exception {
        String expectedQuery = "CREATE TABLE IF NOT EXISTS \"TEST_TABLE\" " +
                "(\"BOOL\" BOOLEAN, " +
                "\"CHAR\" VARCHAR(10), " +
                "\"BYTE\" SMALLINT, " +
                "\"SHORT\" SMALLINT, " +
                "\"INT\" INTEGER, " +
                "\"LONG\" INTEGER, " +
                "\"STR\" VARCHAR(32), " +
                "\"DEC\" FLOAT, " +
                "\"FLOAT\" FLOAT, " +
                "\"DOUBLE\" FLOAT, " +
                "\"DATE\" DATE)";

        Schema schema = createAllTypesSchema();
        String[] tableName = new String[]{"TEST_TABLE"};
        DefaultSQLCreateTableAction action =
                new DefaultSQLCreateTableAction(tableName, schema, true, false, false);
        TableActionConfig conf = new SnowflakeTableActionConfig(true);
        action.setConfig(conf);

        List<String> queries = action.getQueries();
        assertEquals(1, queries.size());
        assertEquals(expectedQuery, queries.get(0));
    }

    /**
     * Checks that CREATE TABLE IF NOT EXISTS query creation sets length and precision for NUMBER db type
     *
     * @throws Exception
     */
    @Test
    public void testGetQueriesNumberMapping() throws Exception {
        String expectedQuery = "CREATE TABLE IF NOT EXISTS \"TEST_TABLE\" " +
                "(\"DEC\" NUMBER(10, 2))";

        Schema schema = createDecimalSchema();
        String[] tableName = new String[]{"TEST_TABLE"};
        DefaultSQLCreateTableAction action =
                new DefaultSQLCreateTableAction(tableName, schema, true, false, false);
        TableActionConfig conf = new SnowflakeTableActionConfig(true);
        action.setConfig(conf);

        Map<String, String> dbTypeMapping = new HashMap<>();
        dbTypeMapping.put("dec", "NUMBER");
        action.setDbTypeMap(dbTypeMapping);

        List<String> queries = action.getQueries();
        assertEquals(1, queries.size());
        assertEquals(expectedQuery, queries.get(0));
    }

    /**
     * Checks that CREATE TABLE IF NOT EXISTS query creation sets length and precision for NUMERIC db type
     *
     * @throws Exception
     */
    @Test
    public void testGetQueriesNumericMapping() throws Exception {
        String expectedQuery = "CREATE TABLE IF NOT EXISTS \"TEST_TABLE\" " +
                "(\"DEC\" NUMERIC(10, 2))";

        Schema schema = createDecimalSchema();
        String[] tableName = new String[]{"TEST_TABLE"};
        DefaultSQLCreateTableAction action =
                new DefaultSQLCreateTableAction(tableName, schema, true, false, false);
        TableActionConfig conf = new SnowflakeTableActionConfig(true);
        action.setConfig(conf);

        Map<String, String> dbTypeMapping = new HashMap<>();
        dbTypeMapping.put("dec", "NUMERIC");
        action.setDbTypeMap(dbTypeMapping);

        List<String> queries = action.getQueries();
        assertEquals(1, queries.size());
        assertEquals(expectedQuery, queries.get(0));
    }

    /**
     * Checks that CREATE TABLE IF NOT EXISTS query creation doesn't set length and precision for INT db type
     *
     * @throws Exception
     */
    @Test
    public void testGetQueriesIntMapping() throws Exception {
        String expectedQuery = "CREATE TABLE IF NOT EXISTS \"TEST_TABLE\" " +
                "(\"INT\" INT)";

        Schema schema = createIntSchema();
        String[] tableName = new String[]{"TEST_TABLE"};
        DefaultSQLCreateTableAction action =
                new DefaultSQLCreateTableAction(tableName, schema, true, false, false);
        TableActionConfig conf = new SnowflakeTableActionConfig(true);
        action.setConfig(conf);

        Map<String, String> dbTypeMapping = new HashMap<>();
        dbTypeMapping.put("int", "INT");
        action.setDbTypeMap(dbTypeMapping);

        List<String> queries = action.getQueries();
        assertEquals(1, queries.size());
        assertEquals(expectedQuery, queries.get(0));
    }

    /**
     * Checks that CREATE TABLE IF NOT EXISTS query creation doesn't set length and precision for SMALLINT db type
     *
     * @throws Exception
     */
    @Test
    public void testGetQueriesSmallIntMapping() throws Exception {
        String expectedQuery = "CREATE TABLE IF NOT EXISTS \"TEST_TABLE\" " +
                "(\"INT\" SMALLINT)";

        Schema schema = createIntSchema();
        String[] tableName = new String[]{"TEST_TABLE"};
        DefaultSQLCreateTableAction action =
                new DefaultSQLCreateTableAction(tableName, schema, true, false, false);
        TableActionConfig conf = new SnowflakeTableActionConfig(true);
        action.setConfig(conf);

        Map<String, String> dbTypeMapping = new HashMap<>();
        dbTypeMapping.put("int", "SMALLINT");
        action.setDbTypeMap(dbTypeMapping);

        List<String> queries = action.getQueries();
        assertEquals(1, queries.size());
        assertEquals(expectedQuery, queries.get(0));
    }

    /**
     * Checks that CREATE TABLE IF NOT EXISTS query creation doesn't set length and precision for FLOAT8 db type
     *
     * @throws Exception
     */
    @Test
    public void testGetQueriesFloat8Mapping() throws Exception {
        String expectedQuery = "CREATE TABLE IF NOT EXISTS \"TEST_TABLE\" " +
                "(\"DOUBLE\" FLOAT8)";

        Schema schema = createDoubleSchema();
        String[] tableName = new String[]{"TEST_TABLE"};
        DefaultSQLCreateTableAction action =
                new DefaultSQLCreateTableAction(tableName, schema, true, false, false);
        TableActionConfig conf = new SnowflakeTableActionConfig(true);
        action.setConfig(conf);

        Map<String, String> dbTypeMapping = new HashMap<>();
        dbTypeMapping.put("double", "FLOAT8");
        action.setDbTypeMap(dbTypeMapping);

        List<String> queries = action.getQueries();
        assertEquals(1, queries.size());
        assertEquals(expectedQuery, queries.get(0));
    }

    /**
     * Checks that CREATE TABLE IF NOT EXISTS query creation doesn't set length and precision for FLOAT db type
     *
     * @throws Exception
     */
    @Test
    public void testGetQueriesFloatMapping() throws Exception {
        String expectedQuery = "CREATE TABLE IF NOT EXISTS \"TEST_TABLE\" " +
                "(\"DOUBLE\" FLOAT)";

        Schema schema = createDoubleSchema();
        String[] tableName = new String[]{"TEST_TABLE"};
        DefaultSQLCreateTableAction action =
                new DefaultSQLCreateTableAction(tableName, schema, true, false, false);
        TableActionConfig conf = new SnowflakeTableActionConfig(true);
        action.setConfig(conf);

        Map<String, String> dbTypeMapping = new HashMap<>();
        dbTypeMapping.put("double", "FLOAT");
        action.setDbTypeMap(dbTypeMapping);

        List<String> queries = action.getQueries();
        assertEquals(1, queries.size());
        assertEquals(expectedQuery, queries.get(0));
    }

    /**
     * Checks that CREATE TABLE IF NOT EXISTS query creation doesn't set length and precision for REAL db type
     *
     * @throws Exception
     */
    @Test
    public void testGetQueriesRealMapping() throws Exception {
        String expectedQuery = "CREATE TABLE IF NOT EXISTS \"TEST_TABLE\" " +
                "(\"DOUBLE\" REAL)";

        Schema schema = createDoubleSchema();
        String[] tableName = new String[]{"TEST_TABLE"};
        DefaultSQLCreateTableAction action =
                new DefaultSQLCreateTableAction(tableName, schema, true, false, false);
        TableActionConfig conf = new SnowflakeTableActionConfig(true);
        action.setConfig(conf);

        Map<String, String> dbTypeMapping = new HashMap<>();
        dbTypeMapping.put("double", "REAL");
        action.setDbTypeMap(dbTypeMapping);

        List<String> queries = action.getQueries();
        assertEquals(1, queries.size());
        assertEquals(expectedQuery, queries.get(0));
    }

    @Test
    public void testGetQueriesDoublePrecisionMapping() throws Exception {
        String expectedQuery = "CREATE TABLE IF NOT EXISTS \"TEST_TABLE\" " +
                "(\"DOUBLE\" DOUBLE PRECISION)";

        Schema schema = createDoubleSchema();
        String[] tableName = new String[]{"TEST_TABLE"};
        DefaultSQLCreateTableAction action =
                new DefaultSQLCreateTableAction(tableName, schema, true, false, false);
        TableActionConfig conf = new SnowflakeTableActionConfig(true);
        action.setConfig(conf);

        Map<String, String> dbTypeMapping = new HashMap<>();
        dbTypeMapping.put("double", "DOUBLE PRECISION");
        action.setDbTypeMap(dbTypeMapping);

        List<String> queries = action.getQueries();
        assertEquals(1, queries.size());
        assertEquals(expectedQuery, queries.get(0));
    }

    /**
     * Checks that CREATE TABLE IF NOT EXISTS query creation sets length and ignores precision for STRING db type
     *
     * @throws Exception
     */
    @Test
    public void testGetQueriesStringMapping() throws Exception {
        String expectedQuery = "CREATE TABLE IF NOT EXISTS \"TEST_TABLE\" " +
                "(\"STR\" STRING(32))";

        Schema schema = createStringSchema();
        String[] tableName = new String[]{"TEST_TABLE"};
        DefaultSQLCreateTableAction action =
                new DefaultSQLCreateTableAction(tableName, schema, true, false, false);
        TableActionConfig conf = new SnowflakeTableActionConfig(true);
        action.setConfig(conf);

        Map<String, String> dbTypeMapping = new HashMap<>();
        dbTypeMapping.put("str", "STRING");
        action.setDbTypeMap(dbTypeMapping);

        List<String> queries = action.getQueries();
        assertEquals(1, queries.size());
        assertEquals(expectedQuery, queries.get(0));
    }

    /**
     * Checks that CREATE TABLE IF NOT EXISTS query creation sets length and ignores precision for TEXT db type
     *
     * @throws Exception
     */
    @Test
    public void testGetQueriesTextMapping() throws Exception {
        String expectedQuery = "CREATE TABLE IF NOT EXISTS \"TEST_TABLE\" " +
                "(\"STR\" TEXT(32))";

        Schema schema = createStringSchema();
        String[] tableName = new String[]{"TEST_TABLE"};
        DefaultSQLCreateTableAction action =
                new DefaultSQLCreateTableAction(tableName, schema, true, false, false);
        TableActionConfig conf = new SnowflakeTableActionConfig(true);
        action.setConfig(conf);

        Map<String, String> dbTypeMapping = new HashMap<>();
        dbTypeMapping.put("str", "TEXT");
        action.setDbTypeMap(dbTypeMapping);

        List<String> queries = action.getQueries();
        assertEquals(1, queries.size());
        assertEquals(expectedQuery, queries.get(0));
    }

    private Schema createAllTypesSchema() {
        Schema schema = SchemaBuilder.builder().record("main").fields()
                .name("bool")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "10")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "0")
                .type(Schema.createUnion(AvroUtils._boolean(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .name("char")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "10")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "0")
                .type(Schema.createUnion(AvroUtils._character(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .name("byte")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "10")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "0")
                .type(Schema.createUnion(AvroUtils._byte(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .name("short")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "10")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "0")
                .type(Schema.createUnion(AvroUtils._short(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .name("int")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "10")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "0")
                .type(Schema.createUnion(AvroUtils._int(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .name("long")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "10")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "0")
                .type(Schema.createUnion(AvroUtils._long(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .name("str")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "32")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "0")
                .type(Schema.createUnion(AvroUtils._string(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .name("dec")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "10")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "0")
                .type(Schema.createUnion(AvroUtils._decimal(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .name("float")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "10")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "0")
                .type(Schema.createUnion(AvroUtils._float(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .name("double")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "10")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "0")
                .type(Schema.createUnion(AvroUtils._double(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .name("date")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "10")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "0")
                .type(Schema.createUnion(AvroUtils._logicalDate(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .endRecord();
        return schema;
    }

    private Schema createDecimalSchema() {
        return SchemaBuilder.builder().record("main").fields()
                .name("dec")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "10")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "2")
                .type(Schema.createUnion(AvroUtils._decimal(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .endRecord();
    }

    private Schema createIntSchema() {
        return SchemaBuilder.builder().record("main").fields()
                .name("int")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "10")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "2")
                .type(Schema.createUnion(AvroUtils._int(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .endRecord();
    }

    private Schema createDoubleSchema() {
        return SchemaBuilder.builder().record("main").fields()
                .name("double")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "10")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "2")
                .type(Schema.createUnion(AvroUtils._double(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .endRecord();
    }

    private Schema createStringSchema() {
        return SchemaBuilder.builder().record("main").fields()
                .name("str")
                .prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "32")
                .prop(SchemaConstants.TALEND_COLUMN_PRECISION, "4")
                .type(Schema.createUnion(AvroUtils._string(), Schema.create(Schema.Type.NULL)))
                .noDefault()

                .endRecord();
    }
}
