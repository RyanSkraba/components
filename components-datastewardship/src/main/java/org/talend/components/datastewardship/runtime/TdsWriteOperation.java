// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.datastewardship.runtime;

import java.util.Map;

import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;

/**
 * TDS {@link WriteOperation}
 */
public abstract class TdsWriteOperation implements WriteOperation<Result> {

    private static final long serialVersionUID = 7034535576323944643L;

    /**
     * {@link TdsSink} instance
     */
    protected final TdsSink sink;

    public TdsWriteOperation(TdsSink sink) {
        this.sink = sink;
    }

    /**
     * Computes total of output results and sets output data
     *
     * @param results {@link Iterable} of output results
     * @param container {@link RuntimeContainer} instance
     */
    @Override
    public Map<String, Object> finalize(Iterable<Result> results, RuntimeContainer container) {
        return Result.accumulateAndReturnMap(results);
    }

    /**
     * Returns the Sink that this write operation writes to.
     *
     * @return the Sink
     */
    @Override
    public TdsSink getSink() {
        return sink;
    }

}
