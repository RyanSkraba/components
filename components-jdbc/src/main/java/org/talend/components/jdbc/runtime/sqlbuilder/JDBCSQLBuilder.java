package org.talend.components.jdbc.runtime.sqlbuilder;

import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.jdbc.runtime.JDBCTemplate;
import org.talend.daikon.avro.SchemaConstants;

public class JDBCSQLBuilder {

    private JDBCSQLBuilder() {
    }

    public static JDBCSQLBuilder getInstance() {
        return new JDBCSQLBuilder();
    }

    protected String getProtectedChar() {
        return "";
    }

    public String generateSQL4SelectTable(String tablename, Schema schema) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ");
        List<Schema.Field> fields = schema.getFields();
        boolean firstOne = true;
        for (Schema.Field field : fields) {
            if (firstOne) {
                firstOne = false;
            } else {
                sql.append(", ");
            }

            String dbColumnName = field.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            sql.append(tablename).append(".").append(dbColumnName);
        }
        sql.append(" FROM ").append(getProtectedChar()).append(tablename).append(getProtectedChar());

        return sql.toString();
    }

    public String generateSQL4DeleteTable(String tablename) {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(getProtectedChar()).append(tablename).append(getProtectedChar());
        return sql.toString();
    }

    public String generateSQL4Insert(String tablename, Schema schema) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(getProtectedChar()).append(tablename).append(getProtectedChar()).append(" ");

        sb.append("(");
        List<Schema.Field> fields = schema.getFields();
        boolean firstOne = true;
        for (Schema.Field field : fields) {
            if (firstOne) {
                firstOne = false;
            } else {
                sb.append(",");
            }

            String dbColumnName = field.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            sb.append(dbColumnName);
        }
        sb.append(")");

        sb.append(" VALUES ");

        sb.append("(");

        firstOne = true;
        for (@SuppressWarnings("unused")
        Schema.Field field : fields) {
            if (firstOne) {
                firstOne = false;
            } else {
                sb.append(",");
            }

            sb.append("?");
        }
        sb.append(")");

        return sb.toString();
    }

    public String generateSQL4Delete(String tablename, Schema schema) {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(getProtectedChar()).append(tablename).append(getProtectedChar()).append(" WHERE ");

        List<Schema.Field> keys = JDBCTemplate.getKeyColumns(schema.getFields());
        boolean firstOne = true;
        for (Schema.Field key : keys) {
            if (firstOne) {
                firstOne = false;
            } else {
                sb.append(" AND ");
            }

            String dbColumnName = key.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            sb.append(getProtectedChar()).append(dbColumnName).append(getProtectedChar()).append(" = ").append("?");
        }

        return sb.toString();
    }

    public String generateSQL4Update(String tablename, Schema schema) {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(getProtectedChar()).append(tablename).append(getProtectedChar()).append(" SET ");

        List<Schema.Field> values = JDBCTemplate.getValueColumns(schema.getFields());
        boolean firstOne = true;
        for (Schema.Field value : values) {
            if (firstOne) {
                firstOne = false;
            } else {
                sb.append(",");
            }

            String dbColumnName = value.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            sb.append(getProtectedChar()).append(dbColumnName).append(getProtectedChar()).append(" = ").append("?");
        }

        sb.append(" WHERE ");

        List<Schema.Field> keys = JDBCTemplate.getKeyColumns(schema.getFields());
        firstOne = true;
        for (Schema.Field key : keys) {
            if (firstOne) {
                firstOne = false;
            } else {
                sb.append(" AND ");
            }

            String dbColumnName = key.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            sb.append(getProtectedChar()).append(dbColumnName).append(getProtectedChar()).append(" = ").append("?");
        }

        return sb.toString();
    }

    public String generateQuerySQL4InsertOrUpdate(String tablename, Schema schema) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(1) FROM ").append(getProtectedChar()).append(tablename).append(getProtectedChar())
                .append(" WHERE ");

        List<Schema.Field> keys = JDBCTemplate.getKeyColumns(schema.getFields());
        boolean firstOne = true;
        for (Schema.Field key : keys) {
            if (firstOne) {
                firstOne = false;
            } else {
                sb.append(" AND ");
            }

            String dbColumnName = key.getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME);
            sb.append(getProtectedChar()).append(dbColumnName).append(getProtectedChar()).append(" = ").append("?");
        }

        return sb.toString();
    }

}
