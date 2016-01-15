package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.components.api.schema.column.type.TList;

import java.util.List;

/**
 * Created by bchen on 16-1-10.
 */
public class LIST extends CassandraBaseType<List, TList> {

    @Override
    protected List getAppValue(Row app, String key) {
        return app.getList(key, Object.class);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, List value) {
        app.setList(key, value);
    }

    @Override
    protected List convert2AType(TList value) {
        return value.getValue();
    }

    @Override
    protected TList convert2TType(List value) {
        TList v = new TList();
        v.setValue(value);
        return v;
    }
}
