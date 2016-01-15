package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.components.api.schema.column.type.TObject;

import java.util.Map;

/**
 * Created by bchen on 16-1-10.
 */
public class MAP extends CassandraBaseType<Map, TObject> {

    @Override
    protected Map getAppValue(Row app, String key) {
        return app.getMap(key, Object.class, Object.class);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Map value) {
        app.setMap(key, value);
    }

    @Override
    protected Map convert2AType(TObject value) {
        return (Map) value.getValue();
    }

    @Override
    protected TObject convert2TType(Map value) {
        TObject v = new TObject();
        v.setValue(value);
        return v;
    }
}
