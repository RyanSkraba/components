package org.talend.components.mysql.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by bchen on 16-1-18.
 */
public class Mysql_VARCHAR extends MysqlBaseType<String, String> {
    @Override
    protected String convert2AType(String value) {
        return value;
    }

    @Override
    protected String convert2TType(String value) {
        return value;
    }

    @Override
    protected String getAppValue(ResultSet app, String key) {
        //TODO throw Talend exception
        String value = null;
        try {
            value = app.getString(key);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    protected void setAppValue(PreparedStatement app, String key, String value) {
        //TODO throw Talend exception
        try {
            app.setString(Integer.valueOf(key), value);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
