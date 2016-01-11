package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.schema.type.TByteArray;

import java.nio.ByteBuffer;

/**
 * Created by bchen on 16-1-10.
 */
public class BLOB extends BaseType<ByteBuffer, TByteArray> {
    @Override
    public Class<TByteArray> getDefaultTalendType() {
        return TByteArray.class;
    }

    @Override
    protected ByteBuffer getAppValue(Row app, String key) {
        return app.getBytes(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, ByteBuffer value) {
        app.setBytes(key, value);
    }

    @Override
    protected ByteBuffer convert2AType(TByteArray value) {
        return ByteBuffer.wrap(value.getValue());
    }

    @Override
    protected TByteArray convert2TType(ByteBuffer value) {
        TByteArray v = new TByteArray();
        byte[] bytes = new byte[value.remaining()];
        value.get(bytes, 0, bytes.length);
        v.setValue(bytes);
        return v;
    }

}
