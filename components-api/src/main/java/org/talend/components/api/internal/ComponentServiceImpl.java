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
package org.talend.components.api.internal;

import org.talend.components.api.ComponentDefinition;
import org.talend.components.api.ComponentProperties;
import org.talend.components.api.ComponentService;
import org.talend.components.api.Constants;

public class ComponentServiceImpl implements ComponentService {

    private ComponentRegistry componentRegistry;

    public ComponentServiceImpl(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.components.api.internal.IComponentService#getComponentProperties(java.lang.String)
     */
    public ComponentProperties getComponentProperties(String name) {
        final String beanName = Constants.COMPONENT_BEAN_PREFIX + name;
        ComponentDefinition compDef = componentRegistry.getComponents().get(beanName);
        ComponentProperties properties = compDef.createProperties();
        return properties;
    }

    public ComponentProperties validateProperty(String propName, ComponentProperties properties) throws Throwable {
        properties.validateProperty(propName);
        return properties;
    }

    public ComponentProperties beforeProperty(String propName, ComponentProperties properties) throws Throwable {
        properties.beforeProperty(propName);
        return properties;
    }

    public ComponentProperties afterProperty(String propName, ComponentProperties properties) throws Throwable {
        properties.afterProperty(propName);
        return properties;
    }

}
