package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.components.api.schema.column.type.TDouble;

/**
 * Created by bchen on 16-1-10.
 */
public class DOUBLE extends CassandraBaseType<Double, TDouble> {

    @Override
    protected Double getAppValue(Row app, String key) {
        return app.getDouble(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Double value) {
        app.setDouble(key, value);
    }

    @Override
    protected Double convert2AType(TDouble value) {
        return value.getValue();
    }

    @Override
    protected TDouble convert2TType(Double value) {
        TDouble v = new TDouble();
        v.setValue(value);
        return v;
    }
}
