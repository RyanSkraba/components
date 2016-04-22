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
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.JiraDefinition;

import aQute.bnd.annotation.component.Component;

/**
 * Jira input component definition
 * 
 * created by ivan.honchar on Apr 22, 2016
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TJiraInputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TJiraInputDefinition extends JiraDefinition implements InputComponentDefinition {

    /**
     * Constructor sets component name
     * 
     * @param componentName component name
     */
    public TJiraInputDefinition(String componentName) {
        super(componentName);
    }

    /**
     * Jira input component name
     */
    public static final String COMPONENT_NAME = "tJIRAInput";

    @Override
    public Source getRuntime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
