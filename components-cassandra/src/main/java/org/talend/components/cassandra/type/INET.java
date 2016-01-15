package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.components.api.schema.column.type.TObject;

import java.net.InetAddress;

/**
 * Created by bchen on 16-1-10.
 */
public class INET extends CassandraBaseType<InetAddress, TObject> {

    @Override
    protected InetAddress getAppValue(Row app, String key) {
        return app.getInet(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, InetAddress value) {
        app.setInet(key, value);
    }

    @Override
    protected InetAddress convert2AType(TObject value) {
        return (InetAddress) value.getValue();
    }

    @Override
    protected TObject convert2TType(InetAddress value) {
        TObject v = new TObject();
        v.setValue(value);
        return v;
    }
}
