package org.talend.components.cassandra.type;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import org.talend.schema.type.TBaseType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bchen on 16-1-10.
 */
public abstract class BaseType<AppType extends Object, TalendType extends TBaseType> {

    public static final String familyName = "Cassandra";

    // TODO: change it to dynamic registry
    public static List<Class<? extends BaseType>> types;
    static {
        types = new ArrayList<Class<? extends BaseType>>();
    }

    // TODO: move to static
    public abstract Class<TalendType> getDefaultTalendType();

    protected abstract AppType getAppValue(Row app, String key);

    protected abstract void setAppValue(BoundStatement app, String key, AppType value);

    protected abstract AppType convert2AType(TalendType value);

    protected abstract TalendType convert2TType(AppType value);

    public final TalendType retrieveTValue(Row app, String key) {
        return convert2TType(getAppValue(app, key));
    }

    public final void assign2AValue(TalendType value, BoundStatement app, String key) {
        setAppValue(app, key, convert2AType(value));
    }

}

