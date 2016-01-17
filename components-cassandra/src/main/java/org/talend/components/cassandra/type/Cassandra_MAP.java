package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

import java.util.Map;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_MAP extends CassandraBaseType<Map, Object> {

    @Override
    protected Map convert2AType(Object value) {
        return (Map) value;
    }

    @Override
    protected Object convert2TType(Map value) {
        return value;
    }

    @Override
    protected Map getAppValue(Row app, String key) {
        return app.getMap(key, Object.class, Object.class);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Map value) {
        app.setMap(key, value);
    }

}
