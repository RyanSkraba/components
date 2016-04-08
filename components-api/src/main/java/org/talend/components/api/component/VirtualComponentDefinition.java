package org.talend.components.api.component;

/**
 * This the definition of virtual component
 */
public interface VirtualComponentDefinition {

    /**
     *  Return the input component definition of current virtual component.
     *
     */
    public ComponentDefinition getInputComponentDefinition();
    /**
     *  Return the output component definition of current virtual component.
     *
     */
    public ComponentDefinition getOutputComponentDefinition();


}