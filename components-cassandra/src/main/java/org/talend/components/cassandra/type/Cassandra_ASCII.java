package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_ASCII extends CassandraBaseType<String, String> {

    @Override
    protected String convert2AType(String value) {
        return value;
    }

    @Override
    protected String convert2TType(String value) {
        return value;
    }

    @Override
    protected String getAppValue(Row app, String key) {
        return app.getString(key);
    }


    @Override
    protected void setAppValue(BoundStatement app, String key, String value) {
        app.setString(key, value);
    }


}
