package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.components.api.schema.column.type.TObject;

import java.util.Set;


/**
 * Created by bchen on 16-1-10.
 */
public class SET extends CassandraBaseType<Set, TObject> {

    @Override
    protected Set getAppValue(Row app, String key) {
        return app.getSet(key, Object.class);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Set value) {
        app.setSet(key, value);
    }

    @Override
    protected Set convert2AType(TObject value) {
        return (Set) value.getValue();
    }

    @Override
    protected TObject convert2TType(Set value) {
        TObject v = new TObject();
        v.setValue(value);
        return v;
    }


}
