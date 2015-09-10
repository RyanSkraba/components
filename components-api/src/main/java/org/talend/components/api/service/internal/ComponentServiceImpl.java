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
package org.talend.components.api.service.internal;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.Constants;
import org.talend.components.api.TopLevelDefinition;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.api.properties.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.daikon.exception.TalendExceptionContext;

/**
 * Main Component Service implementation that is not related to any framework (neither OSGI, nor Spring) it uses a
 * ComponentRegistry implementation that will be provided by framework specific Service classes
 */
public class ComponentServiceImpl implements ComponentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentServiceImpl.class);

    private ComponentRegistry   componentRegistry;

    public ComponentServiceImpl(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    @Override
    public Set<String> getAllComponentNames() {
        // remove the components# internal prefix to return the simple name
        Collection<String> componentsInternalNames = componentRegistry.getComponents().keySet();
        Set<String> compNames = new HashSet<>(componentsInternalNames.size());
        for (String name : componentsInternalNames) {
            compNames.add(name.substring(Constants.COMPONENT_BEAN_PREFIX.length()));
        }
        return compNames;
    }

    @Override
    public Set<ComponentDefinition> getAllComponents() {
        return new HashSet<>(componentRegistry.getComponents().values());
    }

    @Override
    public Set<ComponentWizardDefinition> getTopLevelComponentWizards() {
        return new HashSet<>(componentRegistry.getComponentWizards().values());
    }

    @Override
    public ComponentProperties getComponentProperties(String name) {
        final String beanName = Constants.COMPONENT_BEAN_PREFIX + name;
        ComponentDefinition compDef = componentRegistry.getComponents().get(beanName);
        if (compDef == null) {
            throw new ComponentException(ComponentsErrorCode.WRONG_COMPONENT_NAME,
                    TalendExceptionContext.build().put("name", name)); //$NON-NLS-1$
        } // else got the def so use it
        ComponentProperties properties = compDef.createProperties();
        return properties;
    }

    @Override
    public ComponentWizard getComponentWizard(String name, String location) {
        final String beanName = Constants.COMPONENT_WIZARD_BEAN_PREFIX + name;
        ComponentWizardDefinition wizardDefinition = componentRegistry.getComponentWizards().get(beanName);
        if (wizardDefinition == null) {
            throw new ComponentException("Failed to find the wizard [" + beanName + "], it may not be registered."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        ComponentWizard wizard = wizardDefinition.createWizard(location);
        return wizard;
    }

    @Override
    public ComponentProperties validateProperty(String propName, ComponentProperties properties) throws Throwable {
        properties.validateProperty(propName);
        return properties;
    }

    @Override
    public ComponentProperties beforeProperty(String propName, ComponentProperties properties) throws Throwable {
        properties.beforeProperty(propName);
        return properties;
    }

    @Override
    public ComponentProperties afterProperty(String propName, ComponentProperties properties) throws Throwable {
        properties.afterProperty(propName);
        return properties;
    }

    @Override
    public InputStream getWizardPngImage(String wizardName) {
        TopLevelDefinition wizardDefinition = componentRegistry.getComponentWizards().get(
                Constants.COMPONENT_WIZARD_BEAN_PREFIX + wizardName);
        if (wizardDefinition != null) {
            return getImageStream(wizardDefinition);
        } else {
            throw new ComponentException(ComponentsErrorCode.WRONG_WIZARD_NAME,
                    TalendExceptionContext.build().put("name", wizardName)); //$NON-NLS-1$
        }

    }

    @Override
    public InputStream getComponentPngImage(String componentName) {
        TopLevelDefinition componentDefinition = componentRegistry.getComponents().get(
                Constants.COMPONENT_BEAN_PREFIX + componentName);
        if (componentDefinition != null) {
            return getImageStream(componentDefinition);
        } else {
            throw new ComponentException(ComponentsErrorCode.WRONG_COMPONENT_NAME,
                    TalendExceptionContext.build().put("name", componentName)); //$NON-NLS-1$
        }
    }

    /**
     * get the image stream or null
     * 
     * @param definition, must not be null
     * @return the stream or null if no image was defined for th component or the path is wrong
     */
    private InputStream getImageStream(TopLevelDefinition definition) {
        InputStream result = null;
        String pngIconPath = definition.getPngImagePath();
        if (pngIconPath != null && !"".equals(pngIconPath)) { //$NON-NLS-1$
            InputStream resourceAsStream = definition.getClass().getResourceAsStream(pngIconPath);
            if (resourceAsStream == null) {// no resource found so this is an component error, so log it and return
                                           // null
                LOGGER.error("Failed to load the Wizard icon [" + definition.getName() + "," + pngIconPath + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                result = resourceAsStream;
            }
        } else {// no path provided so will return null but log it.
            LOGGER.warn("The defintion of [" + definition.getName() + "] did not specify any icon"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return result;
    }

}
