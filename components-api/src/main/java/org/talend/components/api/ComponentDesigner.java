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

import org.talend.components.api.properties.ComponentProperties;

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
