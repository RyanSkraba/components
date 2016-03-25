package org.talend.components.api.properties;

import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.Property;

import java.lang.reflect.Field;

/**
 *  This is used for virtual component
 *  Split the properties for virtual component.
 */

public interface  IVirtualComponentProperties {

    /**
     *  Get input part component properties
     */
    public ComponentProperties getInputComponentProperties();

    /**
     *  Get output part component properties
     */
    public ComponentProperties getOutputComponentProperties();

}