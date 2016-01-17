package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_UUID extends CassandraBaseType<java.util.UUID, String> {

    @Override
    protected java.util.UUID convert2AType(String value) {
        return java.util.UUID.fromString(value);
    }

    @Override
    protected String convert2TType(java.util.UUID value) {
        return value.toString();
    }

    @Override
    protected java.util.UUID getAppValue(Row app, String key) {
        return app.getUUID(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, java.util.UUID value) {
        app.setUUID(key, value);
    }
}
