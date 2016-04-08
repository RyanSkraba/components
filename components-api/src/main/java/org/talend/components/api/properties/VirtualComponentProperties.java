package org.talend.components.api.properties;

/**
 *  This is used for virtual component
 *  Split the properties for virtual component.
 */

public interface VirtualComponentProperties {

    /**
     *  Get input part component properties
     */
    public ComponentProperties getInputComponentProperties();

    /**
     *  Get output part component properties
     */
    public ComponentProperties getOutputComponentProperties();

}