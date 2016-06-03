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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jira.Action;
import org.talend.components.jira.runtime.result.DataCountResult;
import org.talend.components.jira.runtime.writer.JiraDeleteWriter;
import org.talend.components.jira.runtime.writer.JiraInsertWriter;
import org.talend.components.jira.runtime.writer.JiraUpdateWriter;
import org.talend.components.jira.runtime.writer.JiraWriter;
import org.talend.components.jira.tjiraoutput.TJiraOutputProperties;

/**
 * Jira {@link WriteOperation}
 */
public class JiraWriteOperation implements WriteOperation<DataCountResult> {

    private static final long serialVersionUID = -2928519020311179621L;

    private static final Logger LOG = LoggerFactory.getLogger(JiraWriteOperation.class);

    /**
     * {@link JiraSink} instance
     */
    private final JiraSink sink;
    
    /**
     * Output results
     */
    private int totalDataCount;
    
    private int totalSuccessCount;
    
    private int totalRejectCount;

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
        totalDataCount = 0;
        totalSuccessCount = 0;
        totalRejectCount = 0;
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
    public void finalize(Iterable<DataCountResult> results, RuntimeContainer container) {
        
        for(DataCountResult result : results) {
            totalDataCount = totalDataCount + result.getDataCount();
            totalSuccessCount = totalSuccessCount + result.getSuccessCount();
            totalRejectCount = totalRejectCount + result.getRejectCount();
        }
        
        if (container != null) {
            container.setComponentData(container.getCurrentComponentId(), TJiraOutputProperties.NB_LINE, totalDataCount);
            container.setComponentData(container.getCurrentComponentId(), TJiraOutputProperties.NB_SUCCESS, totalSuccessCount);
            container.setComponentData(container.getCurrentComponentId(), TJiraOutputProperties.NB_REJECT, totalRejectCount);
        }
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
