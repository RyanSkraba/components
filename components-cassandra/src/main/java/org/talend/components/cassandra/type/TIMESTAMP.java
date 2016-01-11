package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.schema.type.TDate;

import java.util.Date;

/**
 * Created by bchen on 16-1-10.
 */
public class TIMESTAMP extends BaseType<Date, TDate> {
    @Override
    public Class<TDate> getDefaultTalendType() {
        return TDate.class;
    }

    @Override
    protected Date getAppValue(Row app, String key) {
        return app.getDate(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Date value) {
        app.setDate(key, value);
    }

    @Override
    protected Date convert2AType(TDate value) {
        return value.getValue();
    }

    @Override
    protected TDate convert2TType(Date value) {
        TDate v = new TDate();
        v.setValue(value);
        return v;
    }
}
