package org.talend.components.api.component;

/**
 * This the definition of virtual component
 */
public interface IVirtualComponentDefinition {

    /**
     *  Return the input component definition of current virtual component.
     *
     */
    public Class<? extends InputComponentDefinition> getInputComponentDefinitionClass();
    /**
     *  Return the output component definition of current virtual component.
     *
     */
    public Class<? extends OutputComponentDefinition> getOutputComponentDefinitionClass();

    /**
     * Returns the {@link AbstractComponentConnection} type inside of current virtual component.
     *
     */
    public AbstractComponentConnection getLinkedType();



}