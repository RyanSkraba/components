package org.talend.components.mysql.type;

import org.talend.components.api.schema.column.type.common.ExternalBaseType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by bchen on 16-1-18.
 */
public abstract class MysqlBaseType<AppType extends Object, TalendType extends Object> extends ExternalBaseType {
    public static final String FAMILY_NAME = "Mysql";

    @Override
    protected Object getValue(Object obj, String key) {
        return getAppValue((ResultSet) obj, key);
    }

    @Override
    protected void setValue(Object app, String key, Object value) {
        setAppValue((PreparedStatement) app, key, (AppType) value);
    }

    @Override
    protected Object c2AType(Object value) {
        return convert2AType((TalendType) value);
    }

    @Override
    protected Object c2TType(Object value) {
        return convert2TType((AppType) value);
    }

    protected abstract AppType convert2AType(TalendType value);

    protected abstract TalendType convert2TType(AppType value);

    protected abstract AppType getAppValue(ResultSet app, String key);

    protected abstract void setAppValue(PreparedStatement app, String key, AppType value);
}
