package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.schema.type.TString;

/**
 * Created by bchen on 16-1-10.
 */
public class VARCHAR extends BaseType<String, TString> {
    @Override
    public Class<TString> getDefaultTalendType() {
        return TString.class;
    }

    @Override
    protected String getAppValue(Row app, String key) {
        return app.getString(key);
    }

    @Override
    protected void setAppValue(BoundStatement app, String key, String value) {
        app.setString(key, value);
    }

    @Override
    protected String convert2AType(TString value) {
        return value.getValue();
    }

    @Override
    protected TString convert2TType(String value) {
        TString v = new TString();
        v.setValue(value);
        return v;
    }
}
