package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

import java.util.UUID;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_TIMEUUID extends CassandraBaseType<UUID, String> {

    @Override
    protected UUID convert2AType(String value) {
        return UUID.fromString(value);
    }

    @Override
    protected String convert2TType(UUID value) {
        return value.toString();
    }

    @Override
    protected UUID getAppValue(Row app, String key) {
        return app.getUUID(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, UUID value) {
        app.setUUID(key, value);
    }

}
