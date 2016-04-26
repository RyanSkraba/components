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
package org.talend.components.jira.tjirainput;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.Trigger;
import org.talend.components.api.component.Connector.ConnectorType;
import org.talend.components.api.component.Trigger.TriggerType;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.JiraDefinition;
import org.talend.components.jira.runtime.JiraSource;

import aQute.bnd.annotation.component.Component;

/**
 * Jira input component definition
 * 
 * created by ivan.honchar on Apr 22, 2016
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TJiraInputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TJiraInputDefinition extends JiraDefinition implements InputComponentDefinition {

    /**
     * Jira input component name
     */
    public static final String COMPONENT_NAME = "tJIRAInput";
    
    /**
     * Constructor sets component name
     * 
     * @param componentName component name
     */
    public TJiraInputDefinition() {
        super(COMPONENT_NAME);
        setConnectors(new Connector(ConnectorType.FLOW, 0, 1));
        setTriggers(new Trigger(TriggerType.ITERATE, 1, 1), new Trigger(TriggerType.SUBJOB_OK, 1, 0),
                new Trigger(TriggerType.SUBJOB_ERROR, 1, 0));
    }

    @Override
    public Source getRuntime() {
        return new JiraSource();
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TJiraInputProperties.class;
    }
    
}
