// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
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
