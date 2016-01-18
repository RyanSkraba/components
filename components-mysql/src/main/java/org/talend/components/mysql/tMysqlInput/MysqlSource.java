package org.talend.components.mysql.tMysqlInput;

import org.talend.components.api.component.runtime.input.Reader;
import org.talend.components.api.component.runtime.input.SingleSplit;
import org.talend.components.api.component.runtime.input.Source;
import org.talend.components.api.component.runtime.input.Split;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.internal.DataSchemaElement;
import org.talend.components.mysql.DBTableSplit;
import org.talend.components.mysql.JDBCHelper;
import org.talend.components.mysql.type.MysqlBaseType;

import java.io.IOException;
import java.sql.*;
import java.util.List;

/**
 * Created by bchen on 16-1-18.
 */
public class MysqlSource implements Source {

    tMysqlInputProperties props;
    Connection conn;

    @Override
    public void init(ComponentProperties properties) {

        props = (tMysqlInputProperties) properties;
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //TODO dbproperties should not empty, need check first
        String url = "jdbc:mysql://" + props.HOST.getStringValue() + ":" + props.PORT.getStringValue() + "/" + props.DBNAME.getStringValue() + "?" + props.PROPERTIES.getStringValue();
        try {
            conn = DriverManager.getConnection(url, props.USER.getStringValue(), props.PASS.getStringValue());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Reader getRecordReader(Split split) {
        if (split != null && !(split instanceof SingleSplit)) {
            List<SchemaElement> columns = getSchema();
            StringBuilder columnsStr = new StringBuilder();
            boolean removeEnd = false;
            for (SchemaElement column : columns) {
                columnsStr.append(((DataSchemaElement) column).getAppColName());
                columnsStr.append(",");
                removeEnd = true;
            }
            String s = removeEnd ? columnsStr.substring(0, columnsStr.length() - 1) : columnsStr.toString();
            try {
                return new MysqlReader(conn, (new JDBCHelper(conn)).getDBSql(split, props.QUERY.getStringValue(), s));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new MysqlReader(conn, props.QUERY.getStringValue());
    }

    @Override
    public boolean supportSplit() {
        return true;
    }

    @Override
    public Split[] getSplit(int num) {
        String dbCountQuery = "select count(*) from (" + props.QUERY.getStringValue() + ") TalendTable";
        long count = 0;
        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(dbCountQuery);
            resultSet.next();
            count = resultSet.getLong(1);
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        long chunkSize = count / num;

        Split[] splits = new Split[num];

        for (int i = 0; i < num; i++) {
            if (i == num - 1) {
                splits[i] = new DBTableSplit(i * chunkSize, count);
            } else {
                splits[i] = new DBTableSplit(i * chunkSize, (i + 1) * chunkSize);
            }
        }
        return splits;
    }

    @Override
    public List<SchemaElement> getSchema() {
        return ((Schema) props.schema.schema.getValue()).getRoot().getChildren();
    }

    @Override
    public String getFamilyName() {
        return MysqlBaseType.FAMILY_NAME;
    }

    public class MysqlReader implements Reader {
        private ResultSet rs;
        private Statement statement;

        MysqlReader(Connection conn, String splitQuery) {
            try {
                statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                rs = statement.executeQuery(splitQuery);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean start() {
            return advance();
        }

        @Override
        public boolean advance() {
            try {
                return rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public Object getCurrent() {
            return rs;
        }

        @Override
        public void close() {
            try {
                rs.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
