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
package org.talend.components.jira.tjiraoutput;

import aQute.bnd.annotation.component.Component;
import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.OutputComponentDefinition;
import org.talend.components.api.component.Trigger;
import org.talend.components.api.component.Trigger.TriggerType;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.JiraDefinition;
import org.talend.components.jira.runtime.JiraSink;
import org.talend.daikon.properties.property.Property;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

/**
 * Jira output component definition
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TJiraOutputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TJiraOutputDefinition extends JiraDefinition implements OutputComponentDefinition {

    /**
     * Jira output component name
     */
    public static final String COMPONENT_NAME = "tJIRAOutput";

    /**
     * Constructor sets component name
     */
    public TJiraOutputDefinition() {
        super(COMPONENT_NAME);
        setTriggers(new Trigger(TriggerType.ITERATE, 1, 1), new Trigger(TriggerType.SUBJOB_OK, 1, 0),
                new Trigger(TriggerType.SUBJOB_ERROR, 1, 0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sink getRuntime() {
        return new JiraSink();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TJiraOutputProperties.class;
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[]{newProperty(RETURN_TOTAL_RECORD_COUNT), newProperty(RETURN_SUCCESS_RECORD_COUNT),
                newProperty(RETURN_REJECT_RECORD_COUNT)};
    }

}
