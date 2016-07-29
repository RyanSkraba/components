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
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.datastewardship.runtime.writer.TdsCampaignWriter;

/**
 * TDS Campaign {@link WriteOperation}
 */
public class TdsCampaignWriteOperation extends TdsWriteOperation {

    private static final long serialVersionUID = 7572409371695331857L;

    /**
     * Constructor sets {@link TdsCampaignSink} instance
     *
     * @param sink {@link TdsCampaignSink} instance
     */
    public TdsCampaignWriteOperation(TdsCampaignSink sink) {
        super(sink);
    }

    /**
     * Does nothing
     *
     * @param runtimeContainer {@link RuntimeContainer} instance
     */
    @Override
    public void initialize(RuntimeContainer runtimeContainer) {
        // Nothing to do here
    }

    /**
     * Computes total of output results and sets output data
     *
     * @param iterable {@link Iterable} of output results
     * @param runtimeContainer {@link RuntimeContainer} instance
     */
    @Override
    public Map<String, Object> finalize(Iterable<Result> iterable, RuntimeContainer runtimeContainer) {
        return Result.accumulateAndReturnMap(iterable);
    }

    /**
     * Creates appropriate {@link TdsCampaignWriter} depending on action specified by user. <br>
     * Possible actions are: Delete, Insert, Update. <br>
     *
     * @param runtimeContainer {@link RuntimeContainer} instance
     * @return appropriate {@link TdsCampaignWriter}
     */
    @Override
    public Writer<Result> createWriter(RuntimeContainer runtimeContainer) {
        return new TdsCampaignWriter(this);
    }

    /**
     * Returns the Sink that this write operation writes to.
     *
     * @return the Sink
     */
    @Override
    public TdsCampaignSink getSink() {
        return (TdsCampaignSink)super.sink;
    }
}
