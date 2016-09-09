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

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.Action;
import org.talend.components.jira.tjiraoutput.TJiraOutputProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

/**
 * Jira {@link Sink}
 */
public class JiraSink extends JiraSourceOrSink implements Sink {

    private static final long serialVersionUID = 7829658146079315880L;

    /**
     * Output action. Possible values are: Delete, Insert, Update
     */
    private Action action;

    /**
     * Delete action property, which defines whether issueSubtasks should be deleted too
     */
    private boolean deleteSubtasks;

    /**
     * Initializes this {@link Sink} with user specified properties
     * 
     * @param container {@link RuntimeContainer} instance
     * @param properties user specified properties
     */
    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        ValidationResult validate = super.initialize(container, properties);
        if (validate.getStatus() == Result.ERROR) {
            return validate;
        }
        TJiraOutputProperties outputProperties = (TJiraOutputProperties) properties;
        action = outputProperties.action.getValue();
        deleteSubtasks = outputProperties.deleteSubtasks.getValue();
        return ValidationResult.OK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WriteOperation<?> createWriteOperation() {
        return new JiraWriteOperation(this);
    }

    /**
     * Returns output action
     * 
     * @return output action
     */
    public Action getAction() {
        return action;
    }

    /**
     * Returns whether issue subtasks should be deleted
     * 
     * @return <code>true</code> if issue subtasks should be deleted, <code>false</code> otherwise
     */
    public boolean doDeleteSubtasks() {
        return deleteSubtasks;
    }
}
