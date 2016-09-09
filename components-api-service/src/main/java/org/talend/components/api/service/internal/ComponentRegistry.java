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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.wizard.ComponentWizardDefinition;

/**
 * Utility for getting and setting the definitions registered in the component framework.
 *
 * This should be populated by all of the {@link ComponentInstaller} instances found, using the methods from
 * {@link ComponentInstaller.ComponentFrameworkContext}. Once it has been been initialized, it can be used by the
 * {@link org.talend.components.api.service.ComponentService} implementations.
 */
public class ComponentRegistry implements ComponentInstaller.ComponentFrameworkContext {

    private Map<String, ComponentDefinition> components;

    private Map<String, ComponentWizardDefinition> componentWizards;

    private Map<String, ComponentFamilyDefinition> componentFamilies;

    public ComponentRegistry() {
        reset();
    }

    /**
     * @return a map of components using their name as a key, never null.
     */
    public Map<String, ComponentDefinition> getComponents() {
        return components;
    }

    /**
     * @return a map of component wizards using their name as a key, never null.
     */
    public Map<String, ComponentWizardDefinition> getComponentWizards() {
        return componentWizards;
    }

    /**
     * @return a map of component families using their name as a key, never null.
     */
    public Map<String, ComponentFamilyDefinition> getComponentFamilies() {
        return componentFamilies;
    }

    /**
     * Remove all known definitions that are stored in the registry and returns it to a modifiable, empty state.
     */
    public void reset() {
        components = new HashMap<>();
        componentWizards = new HashMap<>();
        componentFamilies = new HashMap<>();
    }

    /**
     * After all of the definitions have been stored in the registry, ensure that no further modifications can be made.
     */
    public void lock() {
        components = Collections.unmodifiableMap(components);
        componentWizards = Collections.unmodifiableMap(componentWizards);
        componentFamilies = Collections.unmodifiableMap(componentFamilies);
    }

    @Override
    public void registerComponentDefinition(Iterable<ComponentDefinition> defs) {
        for (ComponentDefinition component : defs)
            getComponents().put(component.getName(), component);
    }

    @Override
    public void registerComponentWizardDefinition(Iterable<ComponentWizardDefinition> defs) {
        for (ComponentWizardDefinition componentWizard : defs)
            getComponentWizards().put(componentWizard.getName(), componentWizard);
    }

    @Override
    public void registerComponentFamilyDefinition(ComponentFamilyDefinition componentFamily) {
        getComponentFamilies().put(componentFamily.getName(), componentFamily);
        // Always automatically register the nested definitions in the component family.
        registerComponentDefinition(componentFamily.getComponents());
        registerComponentWizardDefinition(componentFamily.getComponentWizards());
    }
}