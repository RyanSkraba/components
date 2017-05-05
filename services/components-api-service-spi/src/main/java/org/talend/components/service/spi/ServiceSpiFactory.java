// ============================================================================
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
// ============================================================================
package org.talend.components.service.spi;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.service.common.DefinitionRegistry;

/**
 * creates a {@link DefinitionRegistry} and {@link ComponentService} based on the jars currently available in the
 * classpath using the JDK ServiceLoader mechanism for registering the definitions.
 */
/**
 *
 */
public class ServiceSpiFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceSpiFactory.class);

    /** Singleton for definition registry. This will be reloaded if set to null. */
    private static DefinitionRegistry defReg;

    /** Singleton for the component service. This will be reloaded if set to null. */
    private static ComponentService componentService;

    private static ExtensibleUrlClassLoader extensibleClassLoader;

    /**
     * return a singleton registry of all the definition red from the java ServiceLoader
     */
    public static synchronized DefinitionRegistry getDefinitionRegistry() {
        if (defReg == null) {
            // Create a new instance of the definition registry.
            DefinitionRegistry reg = new DefinitionRegistry();
            for (ComponentInstaller installer : ServiceLoader.load(ComponentInstaller.class)) {
                installer.install(reg);
            }
            reg.lock();
            // Only assign it to the singleton after being locked.
            defReg = reg;
        }
        return defReg;
    }

    /**
     * create the singleton registry reading all the dependencies embedded in each component from the componentUrls and
     * adding them to the current service classpath. if the componentUrls is null (or empty) then the existing registry
     * is returned, otherwise a new registry is constructed with all the dependencies computed from each of the
     * components and they are added to the current service classpath .
     */
    public static synchronized DefinitionRegistry createDefinitionRegistry(URL[] componentUrls) {
        if (defReg == null || (componentUrls != null && componentUrls.length > 0)) {
            extensibleClassLoader = new ExtensibleUrlClassLoader(ServiceSpiFactory.class.getClassLoader());
            URL[] allDependenciesURLs = readAllComponentDependencies(componentUrls);
            createInternalDefintionRegistry(allDependenciesURLs);
        }
        return defReg;
    }

    /**
     * this will gather all components dependencies jar urls.
     */
    private static URL[] readAllComponentDependencies(URL[] componentUrls) {
        Set<URL> dependenciesUrl = new HashSet<>(componentUrls.length * 3);// assuming at least 3 deps
        for (URL compURL : componentUrls) {
            try {
                List<URL> componentDepsURLs = DependenciesReader.extractDependencies(compURL);
                // resolve all url, this means they can be downloaded from a remote maven repo too.
                LOG.info("resolving dependencies for [" + compURL + "]");
                try {
                    for (URL depsURL : componentDepsURLs) {
                        depsURL.openConnection();
                    }
                } catch (IOException e) {
                    LOG.error("Error when resolving dependencies for [" + compURL + "]:" + e);
                }
                dependenciesUrl.addAll(componentDepsURLs);
            } catch (ComponentException ce) {
                LOG.error("Failed to load component from URL[" + compURL + "] because :" + ce.getMessage());
            }
        }
        return dependenciesUrl.toArray(new URL[dependenciesUrl.size()]);
    }

    /**
     * return a singleton registry adding all the dependencied of the componentURLs to the existing registry classpath
     * if any. if the componentsUrls is null then the existing registry is returned, otherwise a new registry is
     * constructed with each component dependencies added current registry classpath.
     */
    public static synchronized DefinitionRegistry createUpdatedDefinitionRegistry(URL[] componentsUrls) {
        if (defReg == null || componentsUrls != null) {
            if (extensibleClassLoader == null) {
                extensibleClassLoader = new ExtensibleUrlClassLoader(ServiceSpiFactory.class.getClassLoader());
            } // else use the existing classloader
            URL[] allDependenciesURLs = readAllComponentDependencies(componentsUrls);
            createInternalDefintionRegistry(allDependenciesURLs);
        }
        return defReg;
    }

    private static void createInternalDefintionRegistry(URL[] classpath) {
        // add the classpath to the classloader.
        if (classpath != null && classpath.length > 0) {
            for (URL url : classpath) {
                extensibleClassLoader.addURL(url);
            }
        }
        // Create a new instance of the definition registry.
        DefinitionRegistry reg = new DefinitionRegistry();
        for (ComponentInstaller installer : ServiceLoader.load(ComponentInstaller.class, extensibleClassLoader)) {
            installer.install(reg);
        }
        reg.lock();
        // Only assign it to the singleton after being locked.
        defReg = reg;
    }

    public static ComponentService getComponentService() {
        if (componentService == null) {
            componentService = new ComponentServiceImpl(getDefinitionRegistry());
        }
        return componentService;
    }

    static void resetDefinitionRegistryOnlyForTest() {
        defReg = null;
    }

}
