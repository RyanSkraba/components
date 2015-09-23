package org.talend.components.api;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.schema.SchemaElement;

/**
 * The ComponentDesigner is optionally implemented by the design-time client of the component service.
 * <p/>
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

    /**
     * Adds the specified {@link ComponentProperties} into the design environment.
     *
     * @param properties the {@code ComponentProperties} object to add.
     * @param name the name of the collection of properties
     * @param userData user data that was provided to the wizard when it was created, or was provided by a previous call
     * to this service. This would typically give the location of the property in the repository.
     * @param schema an optional schema to be added at this location.
     * @return userData, a String containing the location of this object in the repository to be used for a subsequent
     * call.
     */
    public String addComponentProperties(ComponentProperties properties, String name, String userData, SchemaElement schema);

}
