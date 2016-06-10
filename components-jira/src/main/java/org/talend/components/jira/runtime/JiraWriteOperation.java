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
package org.talend.components.jira.runtime;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jira.Action;
import org.talend.components.jira.runtime.writer.JiraDeleteWriter;
import org.talend.components.jira.runtime.writer.JiraInsertWriter;
import org.talend.components.jira.runtime.writer.JiraUpdateWriter;
import org.talend.components.jira.runtime.writer.JiraWriter;

/**
 * Jira {@link WriteOperation}
 */
public class JiraWriteOperation implements WriteOperation<Result> {

    private static final long serialVersionUID = -2928519020311179621L;

    private static final Logger LOG = LoggerFactory.getLogger(JiraWriteOperation.class);

    /**
     * {@link JiraSink} instance
     */
    private final JiraSink sink;

    /**
     * Constructor sets {@link JiraSink} instance
     *
     * @param sink {@link JiraSink} instance
     */
    public JiraWriteOperation(JiraSink sink) {
        this.sink = sink;
    }

    /**
     * Resets output results values
     *
     * @param container {@link RuntimeContainer} instance
     */
    @Override
    public void initialize(RuntimeContainer container) {
    }

    /**
     * Creates appropriate {@link JiraWriter} depending on action specified by user. <br>
     * Possible actions are: Delete, Insert, Update. <br>
     *
     * @param container {@link RuntimeContainer} instance
     * @return appropriate {@link JiraWriter}
     */
    @Override
    public JiraWriter createWriter(RuntimeContainer container) {

        Action action = sink.getAction();
        switch (action) {
        case DELETE: {
            return new JiraDeleteWriter(this);
        }
        case INSERT: {
            return new JiraInsertWriter(this);
        }
        case UPDATE: {
            return new JiraUpdateWriter(this);
        }
        default: {
            LOG.debug("Impossible action retrieved from Jira sink");
            return null;
        }
        }
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
    public JiraSink getSink() {
        return this.sink;
    }

}
