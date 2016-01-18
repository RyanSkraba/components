package org.talend.components.mysql.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by bchen on 16-1-18.
 */
public class Mysql_INT extends MysqlBaseType<Integer, Integer> {
    @Override
    protected Integer convert2AType(Integer value) {
        return value;
    }

    @Override
    protected Integer convert2TType(Integer value) {
        return value;
    }

    @Override
    protected Integer getAppValue(ResultSet app, String key) {
        //TODO throw Talend exception
        Integer value = null;
        try {
            value = app.getInt(key);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    protected void setAppValue(PreparedStatement app, String key, Integer value) {
        //TODO throw Talend exception
        try {
            app.setInt(Integer.valueOf(key), value);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
