// ==============================================================================
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
// ==============================================================================
package org.talend.components.service.rest.configuration;

import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.service.spi.ServiceSpiFactory;
import org.talend.daikon.definition.service.DefinitionRegistryService;
import org.talend.daikon.runtime.RuntimeUtil;

/**
 * Configuration that deals with ComponentRegistry setup.
 */
@Configuration
@PropertySource(value = "classpath:components.list", name = ComponentsRegistrySetup.TCOMP_COMPONENTS_LIST_RESOURCE_NAME, ignoreResourceNotFound = true)
public class ComponentsRegistrySetup {

    public static final String TCOMP_COMPONENTS_LIST_PROP = "tcomp.components.list";

    public static final String TCOMP_COMPONENTS_LIST_RESOURCE_NAME = "tcomp.components.list.properties";

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(ComponentsRegistrySetup.class);

    private static final String List = null;

    @Autowired
    private ApplicationContext context;

    private DefinitionRegistry registry;

    @Autowired
    ConfigurableEnvironment env;

    private DefinitionRegistry definitionRegistry;

    @Bean
    public ComponentService getComponentService() {
        return new ComponentServiceImpl(getDefinitionRegistry());
    }

    @Bean
    public DefinitionRegistryService getDefintionRegistryService() {
        return getDefinitionRegistry();
    }

    private DefinitionRegistry getDefinitionRegistry() {
        if (definitionRegistry == null) {
            definitionRegistry = createDefinitionRegistry();
        } // else return existing one
        return definitionRegistry;
    }

    DefinitionRegistry createDefinitionRegistry() {
        RuntimeUtil.registerMavenUrlHandler();
        // read the list of components and register them
        org.springframework.core.env.PropertySource<?> propertySource = env.getPropertySources()
                .get(TCOMP_COMPONENTS_LIST_RESOURCE_NAME);
        if (propertySource != null) {
            LOGGER.info("Component list properties found");
            String compListStr = (String) propertySource.getProperty(TCOMP_COMPONENTS_LIST_PROP);
            if (compListStr != null) {
                definitionRegistry = ServiceSpiFactory.createDefinitionRegistry(extractComponentsUrls(compListStr));
            } // else return the default registry
        } else {// else return the default registry
            LOGGER.warn("Component list properties not found");
        }
        // using the spi registry
        return ServiceSpiFactory.getDefinitionRegistry();
    }

    /**
     * returns the list of working URL found in the compListStr or an empty array.
     */
    URL[] extractComponentsUrls(String compListStr) {
        if (compListStr == null) {
            return new URL[0];
        } else {
            return Pattern.compile(",").splitAsStream(compListStr)//
                    .map(string -> {
                        try {
                            return new URL(string);
                        } catch (MalformedURLException e) {
                            LOGGER.error("Components URL [" + string + "] is not a valid URL, so it will be ignored.");
                            return null;
                        }
                    })//
                    .filter(url -> url != null)//
                    .toArray(URL[]::new);
        }
    }
}
