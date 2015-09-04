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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.talend.components.api.ComponentDefinition;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Deactivate;

/**
 * This will provide a component registry that will mask the fact that we are using a spring context or an OSGI context
 * to get registered components.
 *
 */
@org.springframework.stereotype.Component
@aQute.bnd.annotation.component.Component
public class ComponentRegistry {

    private static final Logger                               LOGGER = LoggerFactory.getLogger(ComponentRegistry.class);

    @Autowired
    private ApplicationContext                                context;

    Map<String, ComponentDefinition>                          components;

    private Collection<ServiceReference<ComponentDefinition>> serviceReferences;

    private BundleContext                                     bundleContext;

    @Activate
    void activate(BundleContext bundleContext) throws InvalidSyntaxException {
        this.bundleContext = bundleContext;
        // this code get called from an OSGI container.
        serviceReferences = bundleContext.getServiceReferences(ComponentDefinition.class, null);
        // we do not find the actual Component now because be want to do some lazy loading and do the actual resolution
        // when on component is required.
    }

    /**
     * @return a map of components using their name as a key, never null.
     */
    public Map<String, ComponentDefinition> getComponents() {
        if (components == null) {
            fillComponentsMap();
        }
        return components;
    }

    /**
     * This will fill the components map with registered component either from OSGI services or spring beans
     */
    private void fillComponentsMap() {
        // try OSGI first
        if (serviceReferences != null) {// we are in the OSGI world
            LOGGER.info("Look for OSGI registered Talend components ");
            components = new HashMap<String, ComponentDefinition>();
            for (ServiceReference<ComponentDefinition> sr : serviceReferences) {
                ComponentDefinition ComponentDef = bundleContext.getService(sr);
                Object nameProp = sr.getProperty("name"); //$NON-NLS-1$
                if (nameProp instanceof String) {
                    components.put((String) nameProp, ComponentDef);
                } else {// no name set so issue a warning
                    LOGGER.warn("Failed to registrer the following component because it is unamed: "
                            + ComponentDef.getClass().getCanonicalName());
                }
            }
        } else {// try the spring world
            LOGGER.info("Look for Spring registered Talend components ");
            components = context.getBeansOfType(ComponentDefinition.class);
            if (components == null) {// create an empty map
                components = new HashMap<String, ComponentDefinition>();
            } // else we found some componnents so we are api
        }
    }

    @Deactivate
    void deactivate() {
        if (serviceReferences != null) {
            serviceReferences.clear();
        }
        if (components != null) {
            components.clear();
        }
    }

}
