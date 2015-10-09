package org.talend.components.api.component;

import org.talend.components.api.AbstractTopLevelDefinition;
import org.talend.components.api.properties.ComponentProperties;

/**
 */
public abstract class AbstractComponentDefinition extends AbstractTopLevelDefinition implements ComponentDefinition {

    protected Class propertiesClass;

    private ComponentConnector[] connectors;

    public void setConnectors(ComponentConnector... conns) {
        this.connectors = conns;
    }

    @Override
    public ComponentConnector[] getConnectors() {
        return connectors;
    }

    @Override
    protected String getI18nPrefix() {
        return "component."; //$NON-NLS-1$
    }

    public ComponentProperties createProperties() {
        try {
            return ((ComponentProperties)propertiesClass.newInstance()).init();
        } catch (InstantiationException e) {
            // FIXME - right exceptions
            throw new RuntimeException(e.getCause());
        } catch (IllegalAccessException e) {
            // FIXME - right exceptions
            throw new RuntimeException(e);
        }
    }

}
