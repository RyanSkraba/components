package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_BOOLEAN extends CassandraBaseType<Boolean, Boolean> {

    @Override
    protected Boolean convert2AType(Boolean value) {
        return value;
    }

    @Override
    protected Boolean convert2TType(Boolean value) {
        return value;
    }

    @Override
    protected Boolean getAppValue(Row app, String key) {
        return app.getBool(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Boolean value) {
        app.setBool(key, value);
    }

}
