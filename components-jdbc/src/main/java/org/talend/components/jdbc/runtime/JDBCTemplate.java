package org.talend.components.jdbc.runtime;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.daikon.avro.SchemaConstants;

public class JDBCTemplate {

    public static Connection createConnection(JDBCConnectionModule properties) throws ClassNotFoundException, SQLException {
        java.lang.Class.forName(properties.driverClass.getValue());
        Connection conn = java.sql.DriverManager.getConnection(properties.jdbcUrl.getValue(),
                properties.userPassword.userId.getValue(), properties.userPassword.password.getValue());
        return conn;
    }

    public static List<Schema.Field> getKeyColumns(List<Schema.Field> allFields) {
        List<Schema.Field> result = new ArrayList<Schema.Field>();

        for (Schema.Field field : allFields) {
            boolean isKey = "true".equalsIgnoreCase(field.getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
            if (isKey) {
                result.add(field);
            }
        }

        return result;
    }

    public static List<Schema.Field> getValueColumns(List<Schema.Field> allFields) {
        List<Schema.Field> result = new ArrayList<Schema.Field>();

        for (Schema.Field field : allFields) {
            boolean isKey = "true".equalsIgnoreCase(field.getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));

            if (!isKey) {
                result.add(field);
            }
        }

        return result;
    }

}
