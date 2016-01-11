package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.schema.type.TString;

import java.util.UUID;

/**
 * Created by bchen on 16-1-10.
 */
public class TIMEUUID extends BaseType<UUID, TString> {
    @Override
    public Class<TString> getDefaultTalendType() {
        return TString.class;
    }

    @Override
    protected UUID getAppValue(Row app, String key) {
        return app.getUUID(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, UUID value) {
        app.setUUID(key, value);
    }

    @Override
    protected UUID convert2AType(TString value) {
        return UUID.fromString(value.getValue());
    }

    @Override
    protected TString convert2TType(UUID value) {
        TString v = new TString();
        v.setValue(value.toString());
        return v;
    }
}
