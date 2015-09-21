package org.talend.components.api.properties;

import org.talend.components.api.AbstractTopLevelDefinition;

/**
 */
public abstract class AbstractComponentDefinition extends AbstractTopLevelDefinition implements ComponentDefinition {

    private ComponentConnector[] connectors;

    public void setConnectors(ComponentConnector... conns) {
        this.connectors = conns;
    }

    @Override
    public ComponentConnector[] getConnectors() {
        return connectors;
    }

    // FIXME - this should get it from the message file - temporary implementation
    @Override
    public String getDisplayName() {
        return getName();
    }

    // FIXME this should get it from the message file - temporary implementation
    @Override
    public String getTitle() {
        return "Title: " + getName();
    }
}
