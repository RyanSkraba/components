// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.container;

/**
 * Expected to be implemented by the container in which components execute.
 *
 * To support the partitioned operation of components, it's important not to use this to store state that is shared
 * between component instances that might be executing in different partitions. The shared component data mechanism in
 * this interface is intended only for local state about the execution of a component, or sharing from another component
 * known to also be in the local partition (the properties associated with a connection object for example).
 */
public interface RuntimeContainer {

    String DATA_KEY_EXECUTION_RESULT = "executionResult";

    /**
     * Return a data value applicable for a component with the given key and component Id. This is used for
     * communication between components with a container.
     *
     * @param componentId the identifier of the component within the container (as related to the calling component).
     * @param key an identifier for the type of data required (for example the connection information).
     * @return an Object, the context for the component.
     */
    Object getComponentData(String componentId, String key);

    /**
     * Sets a data value for a component with the specified Id. Used in conjunction with
     * {@link #getComponentData(String, String)}.
     *
     * @param componentId the identifier of the component within the container (as related to the calling component).
     * @param key an identifier fo the type of data required (for example the connection information).
     * @param data an Object, the data for the component.
     */
    void setComponentData(String componentId, String key, Object data);

    /**
     * Return the id of the currently execution component.
     */
    String getCurrentComponentId();

    /**
     * Return a data value applicable for a component with the given key
     */
    Object getGlobalData(String key);

}
