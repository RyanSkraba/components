package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.components.api.schema.column.type.TBigDecimal;

import java.math.BigDecimal;

/**
 * Created by bchen on 16-1-10.
 */
public class DECIMAL extends CassandraBaseType<BigDecimal, TBigDecimal> {

    @Override
    protected BigDecimal getAppValue(Row app, String key) {
        return app.getDecimal(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, BigDecimal value) {
        app.setDecimal(key, value);
    }

    @Override
    protected BigDecimal convert2AType(TBigDecimal value) {
        return value.getValue();
    }

    @Override
    protected TBigDecimal convert2TType(BigDecimal value) {
        TBigDecimal v = new TBigDecimal();
        v.setValue(value);
        return v;
    }

}
