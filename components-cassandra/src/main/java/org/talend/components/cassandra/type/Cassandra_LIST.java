package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

import java.util.List;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_LIST extends CassandraBaseType<List, List> {

    @Override
    protected List convert2AType(List value) {
        return value;
    }

    @Override
    protected List convert2TType(List value) {
        return value;
    }

    @Override
    protected List getAppValue(Row app, String key) {
        return app.getList(key, Object.class);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, List value) {
        app.setList(key, value);
    }

}
