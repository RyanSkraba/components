package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.schema.type.TLong;

/**
 * Created by bchen on 16-1-10.
 */
public class BIGINT extends BaseType<Long, TLong> {
    @Override
    public Class<TLong> getDefaultTalendType() {
        return TLong.class;
    }

    @Override
    protected Long getAppValue(Row app, String key) {
        return app.getLong(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Long value) {
        app.setLong(key, value);
    }

    @Override
    protected Long convert2AType(TLong value) {
        return value.getValue();
    }

    @Override
    protected TLong convert2TType(Long value) {
        TLong v = new TLong();
        v.setValue(value);
        return v;
    }

}
