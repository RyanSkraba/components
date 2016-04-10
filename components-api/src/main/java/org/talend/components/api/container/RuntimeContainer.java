package org.talend.components.api.container;

/**
 * Expected to be implemented by the container in which components execute.
 */
public interface RuntimeContainer {

    public static final String DATA_KEY_EXECUTION_RESULT = "executionResult";

    /**
     * Return a data value applicable for a component with the given key and component Id. This is used
     * for communication between components with a container.
     *
     * @param componentId the identifier of the component within the container (as related to the calling component).
     * @param key         an identifier fo the type of data required (for example the connection information).
     * @return an Object, the context for the component.
     */
    Object getComponentData(String componentId, String key);

    /**
     * Sets a data value for a component with the specified Id. Used in conjunction with {@link #getComponentData(String, String)}.
     *
     * @param componentId the identifier of the component within the container (as related to the calling component).
     * @param key         an identifier fo the type of data required (for example the connection information).
     * @param data        an Object, the data for the component.
     */
    void setComponentData(String componentId, String key, Object data);

    /**
     * Return the id of the currently execution component.
     */
    String getCurrentComponentId();

}
