package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.components.api.schema.column.type.TString;

/**
 * Created by bchen on 16-1-10.
 */
public class UUID extends CassandraBaseType<java.util.UUID, TString> {

    @Override
    protected java.util.UUID getAppValue(Row app, String key) {
        return app.getUUID(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, java.util.UUID value) {
        app.setUUID(key, value);
    }

    @Override
    protected java.util.UUID convert2AType(TString value) {
        return java.util.UUID.fromString(value.getValue());
    }

    @Override
    protected TString convert2TType(java.util.UUID value) {
        TString v = new TString();
        v.setValue(value.toString());
        return v;
    }
}
