package org.talend.components.api.properties;

import org.talend.components.api.ComponentConnector;

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

}
