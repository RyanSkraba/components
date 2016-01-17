package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_FLOAT extends CassandraBaseType<Float, Float> {

    @Override
    protected Float convert2AType(Float value) {
        return value;
    }

    @Override
    protected Float convert2TType(Float value) {
        return value;
    }

    @Override
    protected Float getAppValue(Row app, String key) {
        return app.getFloat(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Float value) {
        app.setFloat(key, value);
    }

}
