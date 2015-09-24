package org.talend.components.api;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.schema.SchemaElement;

/**
 * The ComponentDesigner is optionally implemented by the design-time client of the component service.
 * <p/>
 * FIXME - not clear what we are going to do with this
 * It it used to help configure a component.
 */
public interface ComponentDesigner {

    /**
     * Asks for any instances of the given component name in the current design scope (like a job).
     *
     * @param componentName the name of the component
     * @return an array of identifiers of the specified component which can be used in a
     * {@link #getComponentProperties(String)} call.
     */
    public String[] getAvailableComponents(String componentName);

    /**
     * Returns a ComponentProperties object for the specified component.
     *
     * @param componentId the identification of the component, for example as returned by
     * {@link #getAvailableComponents(String)}.
     * @return a {@link ComponentProperties} object for that component.
     */
    public ComponentProperties getComponentProperties(String componentId);
}
