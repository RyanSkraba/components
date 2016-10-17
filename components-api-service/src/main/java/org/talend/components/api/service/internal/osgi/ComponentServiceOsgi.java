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
package org.talend.components.api.service.internal.osgi;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.RuntimableDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentRegistry;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.runtime.RuntimeInfo;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 * This is the OSGI specific service implementation that completely delegates the implementation to the Framework
 * agnostic {@link ComponentServiceImpl}
 */
@Component
public class ComponentServiceOsgi implements ComponentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentServiceOsgi.class);

    GlobalI18N gctx;

    @Reference
    public void osgiInjectGlobalContext(GlobalI18N aGctx) {
        this.gctx = aGctx;
    }

    private ComponentService componentServiceDelegate;

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
        ComponentRegistry registry = new ComponentRegistry();
        Map<String, ComponentInstaller> installers = populateMap(bundleContext, ComponentInstaller.class);
        for (ComponentInstaller installer : installers.values())
            installer.install(registry);
        registry.lock();
        this.componentServiceDelegate = new ComponentServiceImpl(registry);
    }

    @Override
    public ComponentProperties getComponentProperties(String name) {
        return componentServiceDelegate.getComponentProperties(name);
    }

    @Override
    public ComponentDefinition getComponentDefinition(String name) {
        return componentServiceDelegate.getComponentDefinition(name);
    }

    @Override
    public ComponentWizard getComponentWizard(String name, String userData) {
        return componentServiceDelegate.getComponentWizard(name, userData);
    }

    @Override
    public List<ComponentWizard> getComponentWizardsForProperties(ComponentProperties properties, String location) {
        return componentServiceDelegate.getComponentWizardsForProperties(properties, location);
    }

    @Override
    public List<ComponentDefinition> getPossibleComponents(ComponentProperties... properties) throws Throwable {
        return componentServiceDelegate.getPossibleComponents(properties);
    }

    @Override
    public Properties makeFormCancelable(Properties properties, String formName) {
        return componentServiceDelegate.makeFormCancelable(properties, formName);
    }

    @Override
    public Properties cancelFormValues(Properties properties, String formName) {
        return componentServiceDelegate.cancelFormValues(properties, formName);
    }

    public ComponentProperties commitFormValues(ComponentProperties properties, String formName) {
        // FIXME - remove this
        return properties;
    }

    @Override
    public Properties validateProperty(String propName, Properties properties) throws Throwable {
        return componentServiceDelegate.validateProperty(propName, properties);
    }

    @Override
    public Properties beforePropertyActivate(String propName, Properties properties) throws Throwable {
        return componentServiceDelegate.beforePropertyActivate(propName, properties);
    }

    @Override
    public Properties beforePropertyPresent(String propName, Properties properties) throws Throwable {
        return componentServiceDelegate.beforePropertyPresent(propName, properties);
    }

    @Override
    public Properties afterProperty(String propName, Properties properties) throws Throwable {
        return componentServiceDelegate.afterProperty(propName, properties);
    }

    @Override
    public Properties beforeFormPresent(String formName, Properties properties) throws Throwable {
        return componentServiceDelegate.beforeFormPresent(formName, properties);
    }

    @Override
    public Properties afterFormNext(String formName, Properties properties) throws Throwable {
        return componentServiceDelegate.afterFormNext(formName, properties);
    }

    @Override
    public Properties afterFormBack(String formName, Properties properties) throws Throwable {
        return componentServiceDelegate.afterFormBack(formName, properties);
    }

    @Override
    public Properties afterFormFinish(String formName, Properties properties) throws Throwable {
        return componentServiceDelegate.afterFormFinish(formName, properties);
    }

    @Override
    public Set<String> getAllComponentNames() {
        return componentServiceDelegate.getAllComponentNames();
    }

    @Override
    public Set<ComponentDefinition> getAllComponents() {
        return componentServiceDelegate.getAllComponents();
    }

    @Override
    public <T extends RuntimableDefinition<?, ?>> Iterable<T> getDefinitionsByType(Class<T> cls) {
        return componentServiceDelegate.getDefinitionsByType(cls);
    }

    @Override
    public Set<ComponentWizardDefinition> getTopLevelComponentWizards() {
        return componentServiceDelegate.getTopLevelComponentWizards();
    }

    @Override
    public InputStream getWizardPngImage(String wizardName, WizardImageType imageType) {
        return componentServiceDelegate.getWizardPngImage(wizardName, imageType);
    }

    @Override
    public InputStream getComponentPngImage(String componentName, ComponentImageType imageType) {
        return componentServiceDelegate.getComponentPngImage(componentName, imageType);
    }

    @Override
    public String storeProperties(Properties properties, String name, String repositoryLocation, String schemaPropertyName) {
        return componentServiceDelegate.storeProperties(properties, name, repositoryLocation, schemaPropertyName);
    }

    @Override
    public void setRepository(Repository repository) {
        componentServiceDelegate.setRepository(repository);
    }

    @Override
    public Schema getSchema(ComponentProperties componentProperties, Connector connector, boolean isOuput) {
        return componentServiceDelegate.getSchema(componentProperties, connector, isOuput);
    }

    @Override
    public Set<? extends Connector> getAvailableConnectors(ComponentProperties componentProperties,
            Set<? extends Connector> connectedConnetor, boolean isOuput) {
        return componentServiceDelegate.getAvailableConnectors(componentProperties, connectedConnetor, isOuput);
    }

    @Override
    public void setSchema(ComponentProperties componentProperties, Connector connector, Schema schema, boolean isOuput) {
        componentServiceDelegate.setSchema(componentProperties, connector, schema, isOuput);
    }

    @Override
    public boolean setNestedPropertiesValues(ComponentProperties targetProperties, Properties nestedValues) {
        return componentServiceDelegate.setNestedPropertiesValues(targetProperties, nestedValues);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(String componentName, Properties properties, ConnectorTopology componentType) {
        return componentServiceDelegate.getRuntimeInfo(componentName, properties, componentType);
    }

}
