package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

import java.net.InetAddress;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_INET extends CassandraBaseType<InetAddress, Object> {

    @Override
    protected InetAddress convert2AType(Object value) {
        return (InetAddress) value;
    }

    @Override
    protected Object convert2TType(InetAddress value) {
        return value;
    }

    @Override
    protected InetAddress getAppValue(Row app, String key) {
        return app.getInet(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, InetAddress value) {
        app.setInet(key, value);
    }

}
