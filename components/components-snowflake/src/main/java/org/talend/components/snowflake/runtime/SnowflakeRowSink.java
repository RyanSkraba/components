// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;

/**
 * This class implements {@link Sink} for tSnowflakeRow component with
 * {@link ConnectorTopology#INCOMING} or {@link ConnectorTopology#INCOMING_AND_OUTGOING} topologies.
 *
 */
public class SnowflakeRowSink extends SnowflakeRowSourceOrSink implements Sink {

    private static final long serialVersionUID = -6866612725653393551L;

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new SnowflakeRowWriteOperation(this);
    }

}
