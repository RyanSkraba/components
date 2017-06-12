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

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.daikon.definition.service.DefinitionRegistryService;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

@Component(provide = { DefinitionRegistry.class, DefinitionRegistryService.class })
public class DefinitionRegistryOsgi extends DefinitionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefinitionRegistryOsgi.class);

    private ServiceTracker<ComponentInstaller, ComponentInstaller> serviceTracker;

    @Activate
    void activate(final BundleContext bundleContext) throws InvalidSyntaxException {
        serviceTracker = new ServiceTracker<>(bundleContext, ComponentInstaller.class.getName(),
                new ServiceTrackerCustomizer<ComponentInstaller, ComponentInstaller>() {

                    @Override
                    public ComponentInstaller addingService(ServiceReference<ComponentInstaller> serviceRef) {
                        ComponentInstaller componentInstaller = bundleContext.getService(serviceRef);
                        Object nameProp = serviceRef.getProperty("component.name"); //$NON-NLS-1$
                        if (nameProp instanceof String) {
                            LOGGER.info("Registered the component: " + nameProp + "(" //$NON-NLS-1$//$NON-NLS-2$
                                    + componentInstaller.getClass().getCanonicalName() + ")"); //$NON-NLS-1$
                            componentInstaller.install(DefinitionRegistryOsgi.this);
                        } else {// no name set so issue a warning
                            LOGGER.warn("Failed to register the following component because it is unnamed: " //$NON-NLS-1$
                                    + serviceRef.getClass().getCanonicalName());
                        }
                        return componentInstaller;
                    }

                    @Override
                    public void modifiedService(ServiceReference<ComponentInstaller> arg0,
                            ComponentInstaller componentInstaller) {
                        // not handled for now
                    }

                    @Override
                    public void removedService(ServiceReference<ComponentInstaller> arg0, ComponentInstaller componentInstaller) {
                        // No any un-install yet from the service
                    }
                });
        serviceTracker.open();
    }

    @Deactivate
    void deactivate(BundleContext bundleContext) throws Exception {
        serviceTracker.close();
    }

}
