package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_BIGINT extends CassandraBaseType<Long, Long> {

    @Override
    protected Long getAppValue(Row app, String key) {
        return app.getLong(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Long value) {
        app.setLong(key, value);
    }

    @Override
    protected Long convert2AType(Long value) {
        return value;
    }

    @Override
    protected Long convert2TType(Long value) {
        return value;
    }


}
