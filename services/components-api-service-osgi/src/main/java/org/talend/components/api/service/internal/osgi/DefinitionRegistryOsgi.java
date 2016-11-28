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
package org.talend.components.api.service.internal.osgi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.daikon.definition.service.DefinitionRegistryService;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;

@Component(provide = { DefinitionRegistry.class, DefinitionRegistryService.class })
public class DefinitionRegistryOsgi extends DefinitionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefinitionRegistryOsgi.class);

    protected static <T> Map<String, T> populateMap(BundleContext bc, Class<T> cls) {
        Map<String, T> map = new HashMap<>();
        try {
            String typeCanonicalName = cls.getCanonicalName();
            Collection<ServiceReference<T>> serviceReferences = bc.getServiceReferences(cls, null);
            for (ServiceReference<T> sr : serviceReferences) {
                T service = bc.getService(sr);
                Object nameProp = sr.getProperty("component.name"); //$NON-NLS-1$
                if (nameProp instanceof String) {
                    map.put((String) nameProp, service);
                    LOGGER.info("Registered the component: " + nameProp + "(" + service.getClass().getCanonicalName() + ")"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                } else {// no name set so issue a warning
                    LOGGER.warn("Failed to register the following component because it is unnamed: " //$NON-NLS-1$
                            + service.getClass().getCanonicalName());
                }
            }
            if (map.isEmpty()) {// warn if no components were registered
                LOGGER.warn("Could not find any registered components for type :" + typeCanonicalName); //$NON-NLS-1$
            } // else everything is fine
        } catch (InvalidSyntaxException e) {
            LOGGER.error("Failed to get " + cls.getSimpleName() + " services", e); //$NON-NLS-1$
        }
        return map;
    }

    @Activate
    void activate(BundleContext bundleContext) throws InvalidSyntaxException {
        Map<String, ComponentInstaller> installers = populateMap(bundleContext, ComponentInstaller.class);
        for (ComponentInstaller installer : installers.values()) {
            installer.install(this);
        }
        this.lock();
    }

}
