package org.talend.components.service.rest.configuration;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentRegistry;
import org.talend.components.api.service.common.ComponentServiceImpl;

/**
 * Configuration that deals with ComponentRegistry setup.
 */
@Configuration
public class ComponentsRegistrySetup {

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(ComponentsRegistrySetup.class);

    @Autowired
    private ApplicationContext context;

    @Bean
    public ComponentService getComponentService() {
        return new ComponentServiceImpl(getComponentRegistry());
    }

    private ComponentRegistry getComponentRegistry() {
        ComponentRegistry registry = new ComponentRegistry();
        Map<String, ComponentInstaller> installers = context.getBeansOfType(ComponentInstaller.class);
        for (ComponentInstaller installer : installers.values()) {
            installer.install(registry);
            LOGGER.debug("{} installed in the registry", installer);
        }

        registry.lock();
        return registry;
    }
}
