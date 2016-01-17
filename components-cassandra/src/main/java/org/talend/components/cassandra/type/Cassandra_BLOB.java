package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

import java.nio.ByteBuffer;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_BLOB extends CassandraBaseType<ByteBuffer, byte[]> {

    @Override
    protected ByteBuffer convert2AType(byte[] value) {
        return ByteBuffer.wrap(value);
    }

    @Override
    protected byte[] convert2TType(ByteBuffer value) {
        byte[] bytes = new byte[value.remaining()];
        value.get(bytes, 0, bytes.length);
        return bytes;
    }

    @Override
    protected ByteBuffer getAppValue(Row app, String key) {
        return app.getBytes(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, ByteBuffer value) {
        app.setBytes(key, value);
    }

}
