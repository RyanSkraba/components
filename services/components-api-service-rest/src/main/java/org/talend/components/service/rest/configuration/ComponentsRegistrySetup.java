//==============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//==============================================================================
package org.talend.components.service.rest.configuration;

import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.daikon.definition.service.DefinitionRegistryService;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Configuration that deals with ComponentRegistry setup.
 */
@Configuration
public class ComponentsRegistrySetup {

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(ComponentsRegistrySetup.class);

    @Autowired
    private ApplicationContext context;

    private DefinitionRegistry registry;

    @Bean
    public ComponentService getComponentService() {
        return new ComponentServiceImpl(getComponentRegistry());
    }

    @Bean
    public DefinitionRegistryService getDefintionRegistryService() {
        return getComponentRegistry();
    }

    private DefinitionRegistry getComponentRegistry() {
        if (registry == null) {
            registry = new DefinitionRegistry();
            Map<String, ComponentInstaller> installers = context.getBeansOfType(ComponentInstaller.class);
            for (ComponentInstaller installer : installers.values()) {
                installer.install(registry);
                LOGGER.debug("{} installed in the registry", installer);
            }

            registry.lock();
        } // else registry already initialised
        return registry;
    }
}
