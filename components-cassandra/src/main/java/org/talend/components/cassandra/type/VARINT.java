package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.schema.type.TObject;

import java.math.BigInteger;

/**
 * Created by bchen on 16-1-10.
 */
public class VARINT extends BaseType<BigInteger, TObject> {
    @Override
    public Class<TObject> getDefaultTalendType() {
        return TObject.class;
    }

    @Override
    protected BigInteger getAppValue(Row app, String key) {
        return app.getVarint(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, BigInteger value) {
        app.setVarint(key, value);
    }

    @Override
    protected BigInteger convert2AType(TObject value) {
        return (BigInteger) value.getValue();
    }

    @Override
    protected TObject convert2TType(BigInteger value) {
        TObject v = new TObject();
        v.setValue(value);
        return v;
    }
}
