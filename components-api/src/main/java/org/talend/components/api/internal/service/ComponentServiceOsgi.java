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
package org.talend.components.api.internal.service;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.ComponentDefinition;
import org.talend.components.api.ComponentProperties;
import org.talend.components.api.ComponentService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is the OSGI specific service implementation that completly delegates the implementation to the Framework
 * agnostic ComponentServiceImpl
 */
@Component public class ComponentServiceOsgi implements ComponentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentServiceOsgi.class);

    /**
     * created by sgandon on 7 sept. 2015 Detailled comment
     */
    private final class ComponentRegistryOsgi implements ComponentRegistry {

        private BundleContext bc;

        public ComponentRegistryOsgi(BundleContext bc) {
            this.bc = bc;

        }

        private HashMap<String, ComponentDefinition> components;

        @Override public Map<String, ComponentDefinition> getComponents() {
            if (components == null) {
                try {
                    Collection<ServiceReference<ComponentDefinition>> serviceReferences = bc
                            .getServiceReferences(ComponentDefinition.class, null);
                    components = new HashMap<String, ComponentDefinition>();
                    for (ServiceReference<ComponentDefinition> sr : serviceReferences) {
                        ComponentDefinition componentDef = bc.getService(sr);
                        Object nameProp = sr.getProperty("component.name"); //$NON-NLS-1$
                        if (nameProp instanceof String) {
                            components.put((String) nameProp, componentDef);
                            LOGGER.info("Registred the component: " + nameProp + "(" + componentDef.getClass().getCanonicalName()
                                    + ")");
                        } else {// no name set so issue a warning
                            LOGGER.warn(
                                    "Failed to registrer the following component because it is unamed: " + componentDef.getClass()
                                            .getCanonicalName());
                        }
                    }
                    if (components.isEmpty()) {// warn if not comonents where registered
                        LOGGER.warn("Could not find any registred components.");
                    } // else everything is fine
                } catch (InvalidSyntaxException e) {
                    LOGGER.error("Failed to get ComponentDefinition services", e); //$NON-NLS-1$
                }
            } // else already instanciated so return it.
            return components;
        }

    }

    private ComponentService componentServiceDelegate;

    @Activate void activate(BundleContext bundleContext) throws InvalidSyntaxException {
        this.componentServiceDelegate = new ComponentServiceImpl(new ComponentRegistryOsgi(bundleContext));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.components.api.ComponentService#getComponentProperties(java.lang.String)
     */
    @Override public ComponentProperties getComponentProperties(String name) {
        return componentServiceDelegate.getComponentProperties(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.components.api.ComponentService#validateProperty(java.lang.String,
     * org.talend.components.api.ComponentProperties)
     */
    @Override public ComponentProperties validateProperty(String propName, ComponentProperties properties) throws Throwable {
        return componentServiceDelegate.validateProperty(propName, properties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.components.api.ComponentService#beforeProperty(java.lang.String,
     * org.talend.components.api.ComponentProperties)
     */
    @Override public ComponentProperties beforeProperty(String propName, ComponentProperties properties) throws Throwable {
        return componentServiceDelegate.beforeProperty(propName, properties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.components.api.ComponentService#afterProperty(java.lang.String,
     * org.talend.components.api.ComponentProperties)
     */
    @Override public ComponentProperties afterProperty(String propName, ComponentProperties properties) throws Throwable {
        return componentServiceDelegate.afterProperty(propName, properties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.components.api.ComponentService#getAllComponentsName()
     */
    @Override public Set<String> getAllComponentsName() {
        return componentServiceDelegate.getAllComponentsName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.components.api.ComponentService#getAllComponents()
     */
    @Override public Set<ComponentDefinition> getAllComponents() {
        return componentServiceDelegate.getAllComponents();
    }

}
