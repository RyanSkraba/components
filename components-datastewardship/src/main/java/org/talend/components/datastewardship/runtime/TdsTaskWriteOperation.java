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
import org.talend.components.datastewardship.runtime.writer.TdsTaskWriter;

/**
 * TDS Task {@link WriteOperation}
 */
public class TdsTaskWriteOperation extends TdsWriteOperation {

    private static final long serialVersionUID = 7034535576323944643L;

    /**
     * Constructor sets {@link TdsTaskSink} instance
     *
     * @param sink {@link TdsTaskSink} instance
     */
    public TdsTaskWriteOperation(TdsTaskSink sink) {
        super(sink);
    }

    /**
     * Does nothing
     *
     * @param container {@link RuntimeContainer} instance
     */
    @Override
    public void initialize(RuntimeContainer container) {
        // nothing to be done here
    }

    /**
     * Creates appropriate {@link TdsTaskWriter} depending on action specified by user. <br>
     * Possible actions are: Delete, Insert, Update. <br>
     *
     * @param container {@link RuntimeContainer} instance
     * @return appropriate {@link TdsTaskWriter}
     */
    @Override
    public TdsTaskWriter createWriter(RuntimeContainer container) {
        return new TdsTaskWriter(this);
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
    public TdsTaskSink getSink() {
        return (TdsTaskSink) super.sink;
    }

}
