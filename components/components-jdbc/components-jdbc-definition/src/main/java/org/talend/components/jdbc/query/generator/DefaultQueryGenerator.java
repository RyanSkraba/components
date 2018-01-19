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
package org.talend.components.jdbc.query.generator;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.talend.components.jdbc.query.EDatabaseTypeName;
import org.talend.components.jdbc.query.IQueryGenerator;
import org.talend.components.jdbc.query.SQLTextUtils;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.avro.SchemaConstants;

public class DefaultQueryGenerator implements IQueryGenerator {

    private final EDatabaseTypeName dbType;

    private String databaseDisplayed;

    private String dbschemaDisplayed;

    private String tableDisplayed;

    private AllSetting setting;

    public DefaultQueryGenerator(EDatabaseTypeName dbType) {
        this.dbType = dbType;
    }

    public void setParameters(String databaseDisplayed, String dbschemaDisplayed, String tableDisplayed, AllSetting setting) {
        this.databaseDisplayed = databaseDisplayed;
        this.dbschemaDisplayed = dbschemaDisplayed;
        this.tableDisplayed = tableDisplayed;
        this.setting = setting;
    }

    private String getDBName() {
        String tablename = removeTextFenceIfJavaLiteral(databaseDisplayed);
        if (!EMPTY.equals(tablename)) {
            return tablename;
        }
        return DEFAULT_TABLE_NAME;
    }

    private String getDBSchemaName() {
        String tablename = removeTextFenceIfJavaLiteral(dbschemaDisplayed);
        if (!EMPTY.equals(tablename)) {
            return tablename;
        }
        return DEFAULT_TABLE_NAME;
    }

    private String getDBTableName() {
        String tablename = removeTextFenceIfJavaLiteral(tableDisplayed);
        if (!EMPTY.equals(tablename)) {
            return tablename;
        }
        return DEFAULT_TABLE_NAME;
    }

    /*
     * "table" ==> table
     * any var which contains context var will keep the same like these :
     * "mytableprefix" + context.table ==> "mytableprefix" + context.table
     * "mytableprefix" + context.table + "mytablesubfix" ==> "mytableprefix" + context.table + "mytablesubfix"
     * 
     * Why do like this?
     * As :
     * We need to add special database text fence for every object name like database/catalog name, schema name, table name,
     * column
     * name,
     * for example :
     * 
     * "mytableprefix" + context.table + "mytablesubfix"
     * We will close the quote after this method, then it will like this :
     * "[" + "mytableprefix" + context.table + "mytablesubfix" + "]"
     * 
     * case 1 :
     * "schema1", "table1" ==> "[schema1].[table1]"
     * 
     * case 2 :
     * context.schema1, "table1" ==> context.schema1 + "." + "[table1]"
     * 
     * case 3 :
     * context.schema1, context.table1 ==> context.schema1 + "." + context.table1
     * 
     * case 4 :
     * "[myschemaprefix" + context.schema1 + "myschemasubfix]" + "." + "[mytableprefix" + context.table1 + "mytablesubfix]"
     */
    private String removeTextFenceIfJavaLiteral(String value) {
        if (value != null) {
            if (SQLTextUtils.containContextVariables(value)) {
                return value;
            } else {
                return SQLTextUtils.removeJavaTextFence(value);
            }
        }
        return null;
    }

    protected char getSQLFieldConnector() {
        return '.';
    }

    private String getDatabaseFieldTextFence(boolean left) {
        String database_object_text_fence = SQLTextUtils.getDatabaseObjectTextFenceByDBType(dbType, left);

        if (JAVA_TEXT_FENCE.equals(database_object_text_fence)) {// need to escape for java env
            return "\\" + JAVA_TEXT_FENCE;
        }

        return database_object_text_fence;
    }

    // expect result like this :
    // [" + var + "].[" + var + "].[" + var + "]
    // [database].[schema].[table]
    // [" + var + "].[schema].[table]
    // [" + var + "].[" + var + "].[table]
    // [" + var + "].[schema].[" + var + "]
    protected String getTableNameWithDBAndSchema(final String dbName, final String schema, String tableName) {
        if (tableName == null || EMPTY.equals(tableName.trim())) {
            tableName = DEFAULT_TABLE_NAME;
        }

        final StringBuffer tableNameWithDBAndSchema = new StringBuffer();

        if (dbName != null && !EMPTY.equals(dbName)) {
            tableNameWithDBAndSchema.append(checkContextAndAddQuote(dbName));
            tableNameWithDBAndSchema.append(getSQLFieldConnector());
        }

        if (schema != null && !EMPTY.equals(schema)) {
            tableNameWithDBAndSchema.append(checkContextAndAddQuote(schema));
            tableNameWithDBAndSchema.append(getSQLFieldConnector());
        }

        tableNameWithDBAndSchema.append(checkContextAndAddQuote(tableName));

        return tableNameWithDBAndSchema.toString();
    }

    // expect result like this :
    // [" + var + "]
    // [databaseobjectname]
    private String checkContextAndAddQuote(String field) {
        return checkContextAndAddQuote(field, true);
    }

    protected String checkContextAndAddQuote(String field, boolean needTextFence4SQL) {
        StringBuffer fieldSB = new StringBuffer();

        if (SQLTextUtils.containContextVariables(field)) {
            if (needTextFence4SQL && needTextFence4SQL()) {
                final String leftDatabaseObjectTextFence = getDatabaseFieldTextFence(true);
                fieldSB.append(leftDatabaseObjectTextFence);
            }

            fieldSB.append(SQLTextUtils.getStringDeclare());
            fieldSB.append(SQLTextUtils.getStringConnect());

            fieldSB.append(field);

            fieldSB.append(SQLTextUtils.getStringConnect());
            fieldSB.append(SQLTextUtils.getStringDeclare());

            if (needTextFence4SQL()) {
                String rightDatabaseObjectTextFence = getDatabaseFieldTextFence(false);
                fieldSB.append(rightDatabaseObjectTextFence);
            }
        } else {
            fieldSB.append(addTextFenceForSQLIfNecessary(field, needTextFence4SQL));
        }

        return fieldSB.toString();
    }

    private String addTextFenceForSQLIfNecessary(String field, boolean needTextFence4SQL) {
        if (needTextFence4SQL && needTextFence4SQL()) {
            // "[abc]" or "[" + var + "]"
            String quoteStr = SQLTextUtils.addSQLTextFenceByDbType(field, dbType, true);
            // [abc] or [" + var + "]
            quoteStr = SQLTextUtils.removeJavaTextFence(quoteStr);
            return quoteStr;
        }

        return field;
    }

    private String addTextFenceForSQLIfNecessary(String field) {
        return addTextFenceForSQLIfNecessary(field, true);
    }

    // remove the usueful tail like +""
    private String processResultSQL(String sql) {
        if (sql != null) {
            String suffix = SQLTextUtils.getStringConnect() + JAVA_TEXT_FENCE + JAVA_TEXT_FENCE;
            if (sql.endsWith(suffix)) {
                sql = sql.substring(0, sql.length() - suffix.length());
            }
        }
        return sql;
    }

    private String generateColumnFields(final String tableNameWithDBAndSchema) {
        StringBuffer fieldsSQL = new StringBuffer(100);

        Schema schema = setting.getSchema();
        List<Field> fields = schema.getFields();

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            fieldsSQL.append(ENTER);
            fieldsSQL.append(SPACE);
            fieldsSQL.append(SPACE);
            fieldsSQL.append(tableNameWithDBAndSchema);
            fieldsSQL.append(getSQLFieldConnector());

            fieldsSQL.append(addTextFenceForSQLIfNecessary(field.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME)));
            if (i < fields.size() - 1) {
                fieldsSQL.append(SQL_SPLIT_FIELD);
                fieldsSQL.append(SPACE);
            }
        }
        return fieldsSQL.toString();
    }

    @Override
    public String generateQuery() {
        Schema schema = setting.getSchema();
        List<Field> fields = schema.getFields();
        if (fields != null && !fields.isEmpty()) {
            final String tableNameWithDBAndSchema = getTableNameWithDBAndSchema(this.getDBName(), this.getDBSchemaName(),
                    getDBTableName());
            String columnField = null;
            if (needFullName4Column()) {
                columnField = generateColumnFields(tableNameWithDBAndSchema);
            } else {
                columnField = generateColumnFields(getDBTableName());
            }

            StringBuffer sql = new StringBuffer(100);
            sql.append(JAVA_TEXT_FENCE);
            sql.append(SQL_SELECT);
            sql.append(SPACE);

            sql.append(columnField);

            sql.append(ENTER);
            sql.append(SQL_FROM);
            sql.append(SPACE);
            sql.append(tableNameWithDBAndSchema);

            sql.append(JAVA_TEXT_FENCE);

            return processResultSQL(sql.toString());
        }

        return EMPTY;
    }

    protected boolean needFullName4Column() {
        return true;
    }

    protected boolean needTextFence4SQL() {
        return true;
    }

}
