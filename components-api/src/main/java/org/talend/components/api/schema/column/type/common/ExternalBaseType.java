package org.talend.components.api.schema.column.type.common;

/**
 * Created by bchen on 16-1-14.
 */
public abstract class ExternalBaseType<Object, TalendType extends TBaseType> {

    public abstract String getFamilyName();

    protected abstract Object getValue(Object obj, String key);

    protected abstract void setValue(Object app, String key, Object value);

    protected abstract Object c2AType(TalendType value);

    protected abstract TalendType c2TType(Object value);

    //TODO to support (app,key); (app,position); (value), now only (app,key)
    public final TalendType retrieveTValue(Object app, String key) {
        return c2TType(getValue(app, key));
    }

    public final void assign2AValue(TalendType value, Object app, String key) {
        setValue(app, key, c2AType(value));
    }
}
