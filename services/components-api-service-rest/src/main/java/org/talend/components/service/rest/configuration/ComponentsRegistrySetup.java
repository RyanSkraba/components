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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.service.spi.ServiceSpiFactory;
import org.talend.daikon.definition.service.DefinitionRegistryService;

/**
 * Configuration that deals with ComponentRegistry setup.
 */
@Configuration
@AutoConfigureAfter({ DefaultComponentConfiguration.class })
public class ComponentsRegistrySetup {

    public static final String TCOMP_COMPONENTS_LIST_PROP = "tcomp.components.list";

    public static final String TCOMP_COMPONENTS_LIST_RESOURCE_PATH = "components.list";

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(ComponentsRegistrySetup.class);

    @Autowired
    ConfigurableEnvironment env;

    private DefinitionRegistry definitionRegistry;

    @PostConstruct
    public void init() {
        //
    }

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
        new JarRuntimeInfo((URL) null, null, null); // ensure mvn url - pax url - is registered

        // read the list of components and register them
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(TCOMP_COMPONENTS_LIST_RESOURCE_PATH)) {
            if (is != null) {
                LOGGER.info("Component list file found");
                Properties properties = new Properties();
                properties.load(is);
                String compListStr = (String) properties.get(TCOMP_COMPONENTS_LIST_PROP);
                if (compListStr != null) {
                    return ServiceSpiFactory.createDefinitionRegistry(extractComponentsUrls(compListStr));
                } else {// else no definition file found.
                    LOGGER.warn("Component list properties not found");
                }
            } else {
                LOGGER.warn("Component list file not found in the application classpath");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load the component liste file", e);
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
