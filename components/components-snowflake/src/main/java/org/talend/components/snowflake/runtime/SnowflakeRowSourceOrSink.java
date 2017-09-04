package org.talend.components.snowflake.runtime;

import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;

/**
 *
 *
 */
public abstract class SnowflakeRowSourceOrSink  extends SnowflakeSourceOrSink{

    private static final long serialVersionUID = -4331259463070838256L;

    protected TSnowflakeRowProperties getRowProperties() {
        return (TSnowflakeRowProperties) properties;
    }

    public Boolean usePreparedStatement() {
        return getRowProperties().usePreparedStatement();
    }

    public String getQuery() {
        return getRowProperties().getQuery();
    }
}
