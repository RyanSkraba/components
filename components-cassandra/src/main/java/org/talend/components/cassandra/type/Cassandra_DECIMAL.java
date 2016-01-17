package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

import java.math.BigDecimal;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_DECIMAL extends CassandraBaseType<BigDecimal, BigDecimal> {

    @Override
    protected BigDecimal convert2AType(BigDecimal value) {
        return value;
    }

    @Override
    protected BigDecimal convert2TType(BigDecimal value) {
        return value;
    }

    @Override
    protected BigDecimal getAppValue(Row app, String key) {
        return app.getDecimal(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, BigDecimal value) {
        app.setDecimal(key, value);
    }


}
