package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_INT extends CassandraBaseType<Integer, Integer> {

    @Override
    protected Integer convert2AType(Integer value) {
        return value;
    }

    @Override
    protected Integer convert2TType(Integer value) {
        return value;
    }

    @Override
    protected Integer getAppValue(Row app, String key) {
        return app.getInt(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Integer value) {
        app.setInt(key, value);
    }

}
