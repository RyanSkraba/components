// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api;

import java.util.Set;

/**
 * The Main service provided by this project to get access to all registered components and their properties.
 * 
 */
public interface ComponentService {

    /**
     * Used to get a new {@link ComponentProperties} object for the specified component.
     * <p>
     * The {@code ComponentProperties} has everything required to render a UI and as well capture and validate the
     * values of the properties associated with the component, based on interactions with this service.
     *
     * @param name the name of the component
     * @return a {@code ComponentProperties} object.
     */
    ComponentProperties getComponentProperties(String name);

    /**
     * Get the list of all the components name that are registered
     * 
     * @return return the list of components name, never null
     */
    Set<String> getAllComponentsName();

    /**
     * Get the list of all the components {@link ComponentDefinition} that are registered
     * 
     * @return return the list of components definition, never null.
     */
    Set<ComponentDefinition> getAllComponents();

    ComponentProperties validateProperty(String propName, ComponentProperties properties) throws Throwable;

    ComponentProperties beforeProperty(String propName, ComponentProperties properties) throws Throwable;

    ComponentProperties afterProperty(String propName, ComponentProperties properties) throws Throwable;

}