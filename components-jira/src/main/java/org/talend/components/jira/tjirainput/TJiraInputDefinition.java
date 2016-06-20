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

import aQute.bnd.annotation.component.Component;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.JiraDefinition;
import org.talend.components.jira.runtime.JiraSource;
import org.talend.daikon.properties.property.Property;

/**
 * Jira input component definition
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TJiraInputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TJiraInputDefinition extends JiraDefinition implements InputComponentDefinition {

    /**
     * Jira input component name
     */
    public static final String COMPONENT_NAME = "tJIRAInput";

    /**
     * Constructor sets component name
     */
    public TJiraInputDefinition() {
        super(COMPONENT_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Source getRuntime() {
        return new JiraSource();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TJiraInputProperties.class;
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] { newInteger(RETURN_TOTAL_RECORD_COUNT), newProperty(RETURN_ERROR_MESSAGE) };
    }
}
