package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;

import java.util.Date;

/**
 * Created by bchen on 16-1-10.
 */
public class Cassandra_TIMESTAMP extends CassandraBaseType<Date, Date> {

    @Override
    protected Date convert2AType(Date value) {
        return value;
    }

    @Override
    protected Date convert2TType(Date value) {
        return value;
    }

    @Override
    protected Date getAppValue(Row app, String key) {
        return app.getDate(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, Date value) {
        app.setDate(key, value);
    }

}
