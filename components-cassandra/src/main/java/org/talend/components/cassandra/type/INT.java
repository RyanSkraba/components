package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.components.api.schema.column.type.TInt;

/**
 * Created by bchen on 16-1-10.
 */
public class INT extends CassandraBaseType<Integer, TInt> {

    @Override
    protected Integer getAppValue(Row app, String key) {
        return app.getInt(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Integer value) {
        app.setInt(key, value);
    }

    @Override
    protected Integer convert2AType(TInt value) {
        return value.getValue();
    }

    @Override
    protected TInt convert2TType(Integer value) {
        TInt v = new TInt();
        v.setValue(value);
        return v;
    }
}
