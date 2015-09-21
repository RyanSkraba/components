package org.talend.components.api.properties;

/**
 */
public abstract class AbstractComponentDefinition implements ComponentDefinition {

    private ComponentConnector[] connectors;

    public void setConnectors(ComponentConnector... conns) {
        this.connectors = conns;
    }

    @Override
    public ComponentConnector[] getConnectors() {
        return connectors;
    }

    // FIXME - this should get it from the message file - temporary implementation
    public String getDisplayName() {
        return getName();
    }

    // FIXME this should get it from the message file - temporary implementation
    public String getTitle() {
        return "Title: " + getName();
    }
}
