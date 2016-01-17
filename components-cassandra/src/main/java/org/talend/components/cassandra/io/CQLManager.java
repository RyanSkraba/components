package org.talend.components.cassandra.io;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.column.type.common.ExternalBaseType;
import org.talend.components.api.schema.internal.DataSchemaElement;
import org.talend.components.cassandra.tCassandraOutput.tCassandraOutputDIProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bchen on 16-1-17.
 */
class Column {

    DataSchemaElement column;
    private String mark = "?";
    private String assignmentOperation = "=";
    private Column assignmentKey;
    private boolean asColumnKey = false;

    public Column(SchemaElement column) {
        this.column = (DataSchemaElement) column;
    }

    public String getName() {
        return column.getName();
    }

    public String getDBName() {
        return column.getAppColName();
    }

    public SchemaElement.Type getTalendType() {
        return column.getType();
    }

    public Class<? extends ExternalBaseType> getDBType() {
        return column.getAppColType();
    }

//    public JavaType getJavaType() {
//        return JavaTypesManager.getJavaTypeFromId(getTalendType());
//    }

    //TODO isNullable is enough?
    public boolean isObject() {
        return column.isNullable();
    }

    public boolean isKey() {
        return column.isKey();
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public void setAssignmentOperation(String op) {
        this.assignmentOperation = op;
    }

    public String getAssignmentOperation() {
        return assignmentOperation;
    }

    public void setAssignmentKey(Column keyColumn) {
        this.assignmentKey = keyColumn;
    }

    public Column getAssignmentKey() {
        return assignmentKey;
    }

    public void setAsColumnKey(boolean asColumnKey) {
        this.asColumnKey = asColumnKey;
    }

    public boolean getAsColumnKey() {
        return asColumnKey;
    }
}

class CQLManager {

    private String[] KeyWords = {"ADD", "ALL", "ALLOW", "ALTER", "AND", "ANY", "APPLY", "AS", "ASC", "ASCII", "AUTHORIZE", "BATCH", "BEGIN", "BIGINT", "BLOB", "BOOLEAN", "BY", "CLUSTERING", "COLUMNFAMILY", "COMPACT", "CONSISTENCY", "COUNT", "COUNTER", "CREATE", "CUSTOM", "DECIMAL", "DELETE", "DESC", "DISTINCT", "DOUBLE", "DROP", "EACH_QUORUM", "EXISTS", "FILTERING", "FLOAT", "FROM", "frozen", "GRANT", "IF", "IN", "INDEX", "INET", "INFINITY", "INSERT", "INT", "INTO", "KEY", "KEYSPACE", "KEYSPACES", "LEVEL", "LIMIT", "LIST", "LOCAL_ONE", "LOCAL_QUORUM", "MAP", "MODIFY", "NAN", "NORECURSIVE", "NOSUPERUSER", "NOT", "OF", "ON", "ONE", "ORDER", "PASSWORD", "PERMISSION", "PERMISSIONS", "PRIMARY", "QUORUM", "RENAME", "REVOKE", "SCHEMA", "SELECT", "SET", "STATIC", "STORAGE", "SUPERUSER", "TABLE", "TEXT", "TIMESTAMP", "TIMEUUID", "THREE", "TO", "TOKEN", "TRUNCATE", "TTL", "TWO", "TYPE", "UNLOGGED", "UPDATE", "USE", "USER", "USERS", "USING", "UUID", "VALUES", "VARCHAR", "VARINT", "WHERE", "WITH", "WRITETIME"};

    private tCassandraOutputDIProperties props;
    private String action;
    private String keyspace;
    private String tableName;
    private Boolean useSpark = false;
    private List<Column> valueColumns;

    public CQLManager(ComponentProperties props, List<SchemaElement> columnList) {
        this.props = (tCassandraOutputDIProperties) props;
        this.action = this.props.dataAction.getStringValue();
        this.keyspace = this.props.keyspace.getStringValue();
        if (!this.keyspace.startsWith("\"")) {
            this.keyspace = "\"" + this.keyspace + "\"";
        }
        this.tableName = this.props.columnFamily.getStringValue();
        if (!this.tableName.startsWith("\"")) {
            this.tableName = "\"" + this.tableName + "\"";
        }
        this.tableName = this.keyspace + "." + this.tableName;
        createColumnList(columnList);
        this.valueColumns = collectValueColumns();
    }

    public CQLManager(ComponentProperties props, List<SchemaElement> columnList, boolean useSpark) {
        this(props, columnList);
        this.useSpark = useSpark;
    }

    private List<Column> all;
    private List<Column> keys;
    private List<Column> normals;
    private List<Column> conditions;
    private Column ttl;
    private Column timestamp;

    private void createColumnList(List<SchemaElement> columnList) {
        all = new ArrayList<Column>();
        for (SchemaElement column : columnList) {
            all.add(new Column(column));
        }
        keys = new ArrayList<Column>();
        normals = new ArrayList<Column>();
        conditions = new ArrayList<Column>();
        boolean usingTimestamp = props.usingTimestamp.getBooleanValue();
        String timestampColName = props.timestamp.getStringValue();
        for (Column column : all) {
            if ("INSERT".equals(action) || "UPDATE".equals(action)) {
                boolean usingTTL = props.usingTTL.getBooleanValue();
                String ttlColName = props.ttl.getStringValue();
                if (usingTTL && ttlColName.equals(column.getName())) {
                    ttl = column;
                    ttl.setMark("TTL ?");
                    continue;
                }
            }
            if (usingTimestamp && timestampColName.equals(column.getName())) {
                timestamp = column;
                timestamp.setMark("TIMESTAMP ?");
                continue;
            }
            if (column.isKey()) {
                keys.add(column);
                continue;
            }
            //TODO make if condition work first
//            if ("UPDATE".equals(action) || ("DELETE".equals(action) && !props.deleteIfExists.getBooleanValue())) {
//                List<Map<String, String>> ifCoditions = (List<Map<String, String>>) ElementParameterParser.getObjectValue(node, "__IF_CONDITION__");
//                boolean matched = false;
//                for (Map<String, String> ifCodition : ifCoditions) {
//                    if (ifCodition.get("COLUMN_NAME").equals(column.getName())) {
//                        conditions.add(column);
//                        matched = true;
//                        continue;
//                    }
//                }
//                if (matched) {
//                    continue;
//                }
//            }
            normals.add(column);
        }
        if ("UPDATE".equals(action)) {
            //TODO make assign operations works first
//            List<Map<String, String>> assignOperations = (List<Map<String, String>>) ElementParameterParser.getObjectValue(node, "__ASSIGNMENT_OPERATION__");
//            List<Column> keyColumns = new ArrayList<Column>();
//            for (Column column : normals) {
//                for (Map<String, String> operation : assignOperations) {
//                    String updateColumnKeyName = operation.get("KEY_COLUMN");
//                    String updateColumnOperation = operation.get("OPERATION");
//                    if ("p/k".equals(updateColumnOperation) && column.getName().equals(updateColumnKeyName)) {
//                        keyColumns.add(column);
//                    }
//                }
//            }
//            normals.removeAll(keyColumns);
//            for (Column column : normals) {
//                for (Map<String, String> operation : assignOperations) {
//                    String updateColumnName = operation.get("COLUMN_NAME");
//                    String updateColumnKeyName = operation.get("KEY_COLUMN");
//                    String updateColumnOperation = operation.get("OPERATION");
//                    if (updateColumnName.equals(column.getName())) {
//                        column.setAssignmentOperation(updateColumnOperation);
//                        if ("p/k".equals(updateColumnOperation)) {
//                            for (Column keyColumn : keyColumns) {
//                                if (keyColumn.getName().equals(updateColumnKeyName)) {
//                                    column.setAssignmentKey(keyColumn);
//                                }
//                            }
//                        }
//                        continue;
//                    }
//                }
//            }
        }
        if ("DELETE".equals(action)) {
            //TODO make delete columns works first
//            List<Map<String, String>> columnsKey = (List<Map<String, String>>) ElementParameterParser.getObjectValue(node, "__DELETE_COLUMN_BY_POSITION_KEY__");
//            for (Column column : normals) {
//                for (Map<String, String> columnKey : columnsKey) {
//                    if (column.getName().equals(columnKey.get("COLUMN_NAME"))) {
//                        column.setAsColumnKey(true);
//                    }
//                }
//            }
        }
    }

    private List<Column> collectValueColumns() {
        List<Column> columns = new ArrayList<>();
        if ("INSERT".equals(action)) {
            columns.addAll(keys);
            columns.addAll(normals);
            if (ttl != null)
                columns.add(ttl);
            if (timestamp != null)
                columns.add(timestamp);
        } else if ("UPDATE".equals(action)) {
            if (ttl != null)
                columns.add(ttl);
            if (timestamp != null)
                columns.add(timestamp);
            for (Column normal : normals) {
                if (normal.getAssignmentKey() != null) {
                    columns.add(normal.getAssignmentKey());
                }
                columns.add(normal);
            }
            columns.addAll(keys);
            columns.addAll(conditions);
        } else if ("DELETE".equals(action)) {
            for (Column column : normals) {
                if (column.getAsColumnKey()) {
                    columns.add(column);
                }
            }
            if (timestamp != null)
                columns.add(timestamp);
            columns.addAll(keys);
            boolean ifExist = props.deleteIfExists.getBooleanValue();
            if (!ifExist) {
                columns.addAll(conditions);
            }
        }
        return columns;
    }

    private String getLProtectedChar(String keyword) {
        return "\\\"";
    }

    private String getRProtectedChar(String keyword) {
        return "\\\"";
    }

    private String wrapProtectedChar(String keyword) {
        if (keyword.matches("^[a-z]+$")) {
            return keyword;
        } else {
            return getLProtectedChar(keyword) + keyword + getRProtectedChar(keyword);
        }
    }

    public List<String> getValueColumns() {
        java.util.List<String> valueColumnsName = new java.util.ArrayList<String>();
        for (Column col : valueColumns) {
            valueColumnsName.add(col.getName());
        }
        return valueColumnsName;
    }

    public String getDropKSSQL(boolean ifExists) {
        StringBuilder dropKSSQL = new StringBuilder();
        dropKSSQL.append("\"DROP KEYSPACE ");
        if (ifExists) {
            dropKSSQL.append("IF EXISTS ");
        }
        dropKSSQL.append("\" + ");
        dropKSSQL.append(this.keyspace);
        return dropKSSQL.toString();
    }

//    public String getCreateKSSQL(boolean ifNotExists) {
    //TODO make keyspace properties works first
//        StringBuilder createKSSQL = new StringBuilder();
//        createKSSQL.append("\"CREATE KEYSPACE ");
//        if (ifNotExists) {
//            createKSSQL.append("IF NOT EXISTS ");
//        }
//        createKSSQL.append("\" + ");
//        createKSSQL.append(this.keyspace);
//        createKSSQL.append(" + \"");
//        createKSSQL.append("WITH REPLICATION = {\'class\' : \'" + ElementParameterParser.getValue(this.node, "__REPLICA_STRATEGY__") + "\',");
//        if ("SimpleStrategy".equals(ElementParameterParser.getValue(this.node, "__REPLICA_STRATEGY__"))) {
//            createKSSQL.append("'replication_factor' : \" + " + ElementParameterParser.getValue(this.node, "__SIMEPLE_REPLICA_NUMBER__") + " + \"}\"");
//        } else {
//            List<Map<String, String>> replicas = ElementParameterParser.getTableValue(this.node, "__NETWORK_REPLICA_TABLE__");
//            int count = 1;
//            for (Map<String, String> replica : replicas) {
//                createKSSQL.append("\'\" + " + replica.get("DATACENTER_NAME") + " + \"\' : \" + " + replica.get("REPLICA_NUMBER") + " + \"");
//                if (count < replicas.size()) {
//                    createKSSQL.append(",");
//                }
//                count++;
//            }
//            createKSSQL.append("}\"");
//        }
//
//        return createKSSQL.toString();
//    }

    public String getDropTableSQL(boolean ifExists) {
        StringBuilder dropTableSQL = new StringBuilder();
        dropTableSQL.append("\"DROP TABLE ");
        if (ifExists) {
            dropTableSQL.append("IF EXISTS ");
        }
        dropTableSQL.append("\" + " + tableName);
        return dropTableSQL.toString();
    }

    public String getCreateTableSQL(boolean ifNotExists) {
        StringBuilder createSQL = new StringBuilder();
        createSQL.append("\"CREATE TABLE ");
        if (ifNotExists) {
            createSQL.append("IF NOT EXISTS ");
        }
        createSQL.append("\" + " + tableName + " + \"(");
        List<Column> columns = new ArrayList<Column>();
        if (!"DELETE".equals(action)) {
            columns.addAll(keys);
            columns.addAll(normals);
            if ("UPDATE".equals(action)) {
                columns.addAll(conditions);
            }
        }
        int count = 1;
        for (Column column : columns) {
            createSQL.append(wrapProtectedChar(column.getDBName()));
            createSQL.append(" ");
            createSQL.append(validateDBType(column));
            if (count < columns.size()) {
                createSQL.append(",");
            }
            count++;
        }
        if (keys.size() > 0) {
            createSQL.append(",PRIMARY KEY(");
            int i = 1;
            for (Column column : keys) {
                createSQL.append(wrapProtectedChar(column.getDBName()));
                if (i < keys.size()) {
                    createSQL.append(",");
                }
                i++;
            }
            createSQL.append(")");
        }
        createSQL.append(")\"");
        return createSQL.toString();
    }

    public boolean containsUnsupportTypes() {
        boolean unsupport = false;
        List<String> unsupportTypes = java.util.Arrays.asList(new String[]{"set", "list", "map"});
        List<Column> columns = new ArrayList<Column>();
        if ("INSERT".equals(action)) {
            columns.addAll(keys);
            columns.addAll(normals);
        }
        for (Column column : columns) {
            if (unsupportTypes.contains(validateDBType(column))) {
                return true;
            }
        }
        return false;
    }

    public String getDeleteTableSQL() {
        StringBuilder deleteTableSQL = new StringBuilder();
        deleteTableSQL.append("\"DELETE FROM \" + " + tableName);
        return deleteTableSQL.toString();
    }

    public String getTruncateTableSQL() {
        StringBuilder truncateTableSQL = new StringBuilder();
        truncateTableSQL.append("\"TRUNCATE \" + " + tableName);
        return truncateTableSQL.toString();
    }

    public String generatePreActionSQL() {
        if ("INSERT".equals(action)) {
            return generatePreInsertSQL();
        } else if ("UPDATE".equals(action)) {
//            return generatePreUpdateSQL();
        } else if ("DELETE".equals(action)) {
//            return generatePreDeleteSQL();
        }
        return "";
    }

//    public String generateStmt(String assignStmt, String inConnName) {
//        if ("INSERT".equals(action) || "UPDATE".equals(action) || "DELETE".equals(action)) {
//            StringBuilder stmt = new StringBuilder();
//            int index = 0;
//            for (Column column : valueColumns) {
//                stmt.append(generateSetStmt(assignStmt, column, inConnName, index));
//                index++;
//            }
//            return stmt.toString();
//        } else {
//            return "";
//        }
//    }

    /*INSERT INTO table_name
     *( identifier, column_name...)
     *VALUES ( value, value ... )
     *USING option AND option
     */
    private String generatePreInsertSQL() {
        List<Column> columns = new ArrayList<Column>();
        columns.addAll(keys);
        columns.addAll(normals);

        int count = 1;
        StringBuilder preInsertSQL = new StringBuilder();
        preInsertSQL.append("INSERT INTO " + tableName + " (");
        for (Column column : columns) {
            preInsertSQL.append(wrapProtectedChar(column.getDBName()));
            if (count < columns.size()) {
                preInsertSQL.append(",");
            }
            count++;
        }
        preInsertSQL.append(") VALUES (");
        count = 1;
        for (Column column : columns) {
            preInsertSQL.append(column.getMark());
            if (count < columns.size()) {
                preInsertSQL.append(",");
            }
            count++;
        }
        preInsertSQL.append(")");
        boolean ifNotExist = props.insertIfNotExists.getBooleanValue();
        if (ifNotExist) {
            preInsertSQL.append(" IF NOT EXISTS");
        }
        if (ttl != null || timestamp != null) {
            preInsertSQL.append(" USING ");
            if (ttl != null) {
                preInsertSQL.append(ttl.getMark());
                if (timestamp != null) {
                    preInsertSQL.append(" AND ");
                }
            }
            if (timestamp != null) {
                preInsertSQL.append(timestamp.getMark());
            }
        }
        return preInsertSQL.toString();
    }

//    private String generatePreUpdateSQL() {
//        StringBuilder preUpdateSQL = new StringBuilder();
//        preUpdateSQL.append("\"UPDATE \" + " + tableName + "+ \"");
//        if (ttl != null || timestamp != null) {
//            preUpdateSQL.append(" USING ");
//            if (ttl != null) {
//                preUpdateSQL.append(ttl.getMark());
//                if (timestamp != null) {
//                    preUpdateSQL.append(" AND ");
//                }
//            }
//            if (timestamp != null) {
//                preUpdateSQL.append(timestamp.getMark());
//            }
//        }
//        preUpdateSQL.append(" SET ");
//        int count = 1;
//        for (Column column : normals) {
//
//            String assignment = wrapProtectedChar(column.getDBName()) + "=" + column.getMark();
//
//            if ("+v".equals(column.getAssignmentOperation())) {
//                assignment = wrapProtectedChar(column.getDBName()) + "=" + wrapProtectedChar(column.getDBName()) + "+" + column.getMark();
//            } else if ("v+".equals(column.getAssignmentOperation())) {
//                assignment = wrapProtectedChar(column.getDBName()) + "=" + column.getMark() + "+" + wrapProtectedChar(column.getDBName());
//            } else if ("-".equals(column.getAssignmentOperation())) {
//                assignment = wrapProtectedChar(column.getDBName()) + "=" + wrapProtectedChar(column.getDBName()) + "-" + column.getMark();
//            } else if ("p/k".equals(column.getAssignmentOperation())) {
//                assignment = wrapProtectedChar(column.getDBName()) + "[?]=" + column.getMark();
//            }
//
//            preUpdateSQL.append(assignment);
//
//            if (count < normals.size()) {
//                preUpdateSQL.append(",");
//            }
//            count++;
//        }
//        preUpdateSQL.append(" WHERE ");
//        count = 1;
//        for (Column column : keys) {
//            preUpdateSQL.append(wrapProtectedChar(column.getDBName()));
//            preUpdateSQL.append(rowKeyInList(column) ? " IN " : "=");
//            preUpdateSQL.append(column.getMark());
//            if (count < keys.size()) {
//                preUpdateSQL.append(" AND ");
//            }
//            count++;
//        }
//        if (conditions.size() > 0) {
//            preUpdateSQL.append(" IF ");
//            count = 1;
//            for (Column column : conditions) {
//                preUpdateSQL.append(wrapProtectedChar(column.getDBName()));
//                preUpdateSQL.append("=");
//                preUpdateSQL.append(column.getMark());
//                if (count < conditions.size()) {
//                    preUpdateSQL.append(" AND ");
//                }
//                count++;
//            }
//        }
//        // can't work actually, even it supported on office document
//        // boolean ifExist = "true".equals(ElementParameterParser.getValue(node, "__UPDATE_IF_EXISTS__"));
//        // if(ifExist){
//        // 	preUpdateSQL.append(" IF EXISTS");
//        // }
//
//        preUpdateSQL.append("\"");
//        return preUpdateSQL.toString();
//
//    }

//    private boolean rowKeyInList(Column column) {
    //TODO make row key in list properties works first
//        List<Map<String, String>> rowKeyInList = (List<Map<String, String>>) ElementParameterParser.getObjectValue(node, "__ROW_KEY_IN_LIST__");
//        for (Map<String, String> rowKey : rowKeyInList) {
//            if (column.getName().equals(rowKey.get("COLUMN_NAME"))) {
//                return true;
//            }
//        }
//        return false;
//    }

//    private String generatePreDeleteSQL() {
//        StringBuilder preDeleteSQL = new StringBuilder();
//        preDeleteSQL.append("\"DELETE ");
//        int count = 1;
//        for (Column column : normals) {
//            preDeleteSQL.append(wrapProtectedChar(column.getDBName()));
//            if (column.getAsColumnKey()) {
//                preDeleteSQL.append("[?]");
//            }
//            if (count < normals.size()) {
//                preDeleteSQL.append(",");
//            }
//            count++;
//        }
//        preDeleteSQL.append(" FROM \" + " + tableName + " + \"");
//        if (timestamp != null) {
//            preDeleteSQL.append(" USING ");
//            preDeleteSQL.append(timestamp.getMark());
//        }
//        if (keys.size() > 0) {
//            preDeleteSQL.append(" WHERE ");
//            count = 1;
//            for (Column column : keys) {
//                preDeleteSQL.append(wrapProtectedChar(column.getDBName()));
//                preDeleteSQL.append(rowKeyInList(column) ? " IN " : "=");
//                preDeleteSQL.append(column.getMark());
//                if (count < keys.size()) {
//                    preDeleteSQL.append(" AND ");
//                }
//                count++;
//            }
//        }
//        boolean ifExist = props.deleteIfExists.getBooleanValue();
//        if (ifExist) {
//            preDeleteSQL.append(" IF EXISTS");
//        } else {
//            if (conditions.size() > 0) {
//                preDeleteSQL.append(" IF ");
//                count = 1;
//                for (Column column : conditions) {
//                    preDeleteSQL.append(wrapProtectedChar(column.getDBName()));
//                    preDeleteSQL.append("=");
//                    preDeleteSQL.append(column.getMark());
//                    if (count < conditions.size()) {
//                        preDeleteSQL.append(" AND ");
//                    }
//                    count++;
//                }
//            }
//        }
//        preDeleteSQL.append("\"");
//        return preDeleteSQL.toString();
//    }

    private Class<? extends ExternalBaseType> validateDBType(Column column) {
        Class<? extends ExternalBaseType> dbType = column.getDBType();
        //TODO it's ok? compare old implement
        return dbType;
    }

//    private String generateSetStmt(String assignStmt, Column column, String inConnName, int index) {
//        Class<? extends ExternalBaseType> dbType = validateDBType(column);
//        String columnValue = inConnName + "." + column.getName();
//        StringBuilder setStmt = new StringBuilder();
//        if (column.isObject()) {
//            setStmt.append("if(" + columnValue + " == null){\r\n");
//            setStmt.append(assignStmt + ".setToNull(" + index + ");\r\n");
//            setStmt.append("} else {");
//        }
//
//        if ("ascii".equals(dbType) || "text".equals(dbType) || "varchar".equals(dbType)) {
//            if (JavaTypesManager.STRING == column.getJavaType()) {
//                setStmt.append(assignStmt + ".setString(" + index + ", " + columnValue + ");\r\n");
//            } else if (JavaTypesManager.CHARACTER == column.getJavaType()) {
//                setStmt.append(assignStmt + ".setString(" + index + ", String.valueOf(" + columnValue + "));\r\n");
//            }
//        } else if ("timeuuid".equals(dbType) || "uuid".equals(dbType)) {
//            setStmt.append(assignStmt + ".setUUID(" + index + ", java.util.UUID.fromString(" + columnValue + "));\r\n");
//        } else if ("varint".equals(dbType)) {
//            setStmt.append(assignStmt + ".setVarint(" + index + ", (java.math.BigInteger)" + columnValue + ");\r\n");
//        } else if ("inet".equals(dbType)) {
//            setStmt.append(assignStmt + ".setInet(" + index + ", (java.net.InetAddress)" + columnValue + ");\r\n");
//        } else if ("map".equals(dbType)) {
//            setStmt.append(assignStmt + ".setMap(" + index + ", (java.util.Map)" + columnValue + ");\r\n");
//        } else if ("set".equals(dbType)) {
//            setStmt.append(assignStmt + ".setSet(" + index + ", (java.util.Set)" + columnValue + ");\r\n");
//        } else if ("list".equals(dbType)) {
//            setStmt.append(assignStmt + ".setList(" + index + ", " + columnValue + ");\r\n");
//        } else if ("boolean".equals(dbType)) {
//            setStmt.append(assignStmt + ".setBool(" + index + ", " + columnValue + ");\r\n");
//        } else if ("blob".equals(dbType)) {
//            if (useSpark) {
//                setStmt.append(assignStmt + ".setBytes(" + index + ", " + columnValue + ");\r\n");
//            } else {
//                setStmt.append(assignStmt + ".setBytes(" + index + ", java.nio.ByteBuffer.wrap(" + columnValue + "));\r\n");
//            }
//        } else if ("timestamp".equals(dbType)) {
//            setStmt.append(assignStmt + ".setDate(" + index + ", " + columnValue + ");\r\n");
//        } else if ("decimal".equals(dbType)) {
//            setStmt.append(assignStmt + ".setDecimal(" + index + ", " + columnValue + ");\r\n");
//        } else if ("double".equals(dbType)) {
//            setStmt.append(assignStmt + ".setDouble(" + index + ", " + columnValue + ");\r\n");
//        } else if ("float".equals(dbType)) {
//            setStmt.append(assignStmt + ".setFloat(" + index + ", " + columnValue + ");\r\n");
//        } else if ("int".equals(dbType)) {
//            setStmt.append(assignStmt + ".setInt(" + index + ", " + columnValue + ");\r\n");
//        } else if ("bigint".equals(dbType) || "count".equals(dbType)) {
//            setStmt.append(assignStmt + ".setLong(" + index + ", " + columnValue + ");\r\n");
//        }
//
//        if (column.isObject()) {
//            setStmt.append("}\r\n");
//        }
//        return setStmt.toString();
//    }
}