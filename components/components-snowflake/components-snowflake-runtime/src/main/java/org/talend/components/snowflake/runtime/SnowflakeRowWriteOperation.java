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

import java.util.Map;

import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;

/**
 * This class implements {@link WriteOperation} for tSnowflakeRow component with
 * {@link ConnectorTopology#INCOMING} or {@link ConnectorTopology#INCOMING_AND_OUTGOING} topologies.
 *
 */
public class SnowflakeRowWriteOperation implements WriteOperation<Result> {

    private static final long serialVersionUID = -4196260617136733302L;

    private final SnowflakeRowSink sink;

    public SnowflakeRowWriteOperation(SnowflakeRowSink sink) {
        this.sink = sink;
    }

    @Override
    public void initialize(RuntimeContainer adaptor) {

    }

    @Override
    public Map<String, Object> finalize(Iterable<Result> writerResults, RuntimeContainer adaptor) {
        return Result.accumulateAndReturnMap(writerResults);
    }

    @Override
    public Writer<Result> createWriter(RuntimeContainer adaptor) {
        return new SnowflakeRowWriter(adaptor, this);
    }

    @Override
    public SnowflakeRowSink getSink() {
        return sink;
    }

}
