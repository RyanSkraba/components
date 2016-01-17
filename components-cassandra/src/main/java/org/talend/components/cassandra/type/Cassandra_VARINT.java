package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

import java.math.BigInteger;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_VARINT extends CassandraBaseType<BigInteger, Object> {

    @Override
    protected BigInteger convert2AType(Object value) {
        return (BigInteger) value;
    }

    @Override
    protected Object convert2TType(BigInteger value) {
        return value;
    }

    @Override
    protected BigInteger getAppValue(Row app, String key) {
        return app.getVarint(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, BigInteger value) {
        app.setVarint(key, value);
    }

}
