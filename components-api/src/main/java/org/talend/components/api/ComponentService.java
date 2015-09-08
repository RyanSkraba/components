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
 * created by sgandon on 3 sept. 2015 Detailled comment
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
     * @return return the list of components name
     */
    Set<String> getAllComponentsName();

    /**
     * @return return the list of components
     */
    Set<ComponentDefinition> getAllComponents();

    ComponentProperties validateProperty(String propName, ComponentProperties properties) throws Throwable;

    ComponentProperties beforeProperty(String propName, ComponentProperties properties) throws Throwable;

    ComponentProperties afterProperty(String propName, ComponentProperties properties) throws Throwable;

}