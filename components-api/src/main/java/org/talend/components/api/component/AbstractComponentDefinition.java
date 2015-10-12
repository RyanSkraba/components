package org.talend.components.api.component;

import org.talend.components.api.AbstractTopLevelDefinition;
import org.talend.components.api.properties.ComponentProperties;

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

    @Override
    protected String getI18nPrefix() {
        return "component."; //$NON-NLS-1$
    }

    /**
     * create the ComponentProperties instance and initialise it before returning it.
     */
    @Override
    public ComponentProperties createProperties() {
        ComponentProperties compProp = doCreateProperties();
        compProp.init();
        return compProp;
    }

    /**
     * Shall be implemented to create the component properties
     */
    abstract protected ComponentProperties doCreateProperties();


    //
    // DI Flags - default definitions
    //

    public boolean isSchemaAutoPropagate() {
        return false;
    }

    public boolean isDataAutoPropagate() {
        return false;
    }

    public boolean isConditionalInputs() {
        return false;
    }

    public boolean isStartable() {
        return false;
    }

    public static final String AUTO = "Auto";
    public static final String NONE = "None";

    public String getPartitioning() {
        return null;
    }

}
