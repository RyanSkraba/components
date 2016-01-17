package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

import java.util.Set;


/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_SET extends CassandraBaseType<Set, Object> {

    @Override
    protected Set convert2AType(Object value) {
        return (Set) value;
    }

    @Override
    protected Object convert2TType(Set value) {
        return value;
    }

    @Override
    protected Set getAppValue(Row app, String key) {
        return app.getSet(key, Object.class);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Set value) {
        app.setSet(key, value);
    }

}
