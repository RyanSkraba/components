package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_DOUBLE extends CassandraBaseType<Double, Double> {

    @Override
    protected Double convert2AType(Double value) {
        return value;
    }

    @Override
    protected Double convert2TType(Double value) {
        return value;
    }

    @Override
    protected Double getAppValue(Row app, String key) {
        return app.getDouble(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Double value) {
        app.setDouble(key, value);
    }

}
