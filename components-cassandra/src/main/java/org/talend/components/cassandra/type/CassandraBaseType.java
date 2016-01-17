package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.components.api.schema.column.type.common.ExternalBaseType;

/**
 * Created by bchen on 16-1-10.
 */
public abstract class CassandraBaseType<AppType extends Object, TalendType extends Object> extends ExternalBaseType {
    //TODO pull this up to Definition or Properties?
    public static final String FAMILY_NAME = "Cassandra";

    @Override
    protected Object getValue(Object obj, String key) {
        return getAppValue((Row) obj, key);
    }

    @Override
    protected void setValue(Object app, String key, Object value) {
        setAppValue((BoundStatement) app, key, (AppType) value);
    }

    @Override
    protected Object c2AType(Object value) {
        return convert2AType((TalendType) value);
    }

    @Override
    protected TalendType c2TType(Object value) {
        return convert2TType((AppType) value);
    }

    protected abstract AppType convert2AType(TalendType value);

    protected abstract TalendType convert2TType(AppType value);

    protected abstract AppType getAppValue(Row app, String key);

    protected abstract void setAppValue(BoundStatement app, String key, AppType value);
}

