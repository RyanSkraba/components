package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.schema.type.TFloat;

/**
 * Created by bchen on 16-1-10.
 */
public class FLOAT extends BaseType<Float, TFloat> {
    @Override
    public Class<TFloat> getDefaultTalendType() {
        return TFloat.class;
    }

    @Override
    protected Float getAppValue(Row app, String key) {
        return app.getFloat(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Float value) {
        app.setFloat(key, value);
    }

    @Override
    protected Float convert2AType(TFloat value) {
        return value.getValue();
    }

    @Override
    protected TFloat convert2TType(Float value) {
        TFloat v = new TFloat();
        v.setValue(value);
        return v;
    }
}
