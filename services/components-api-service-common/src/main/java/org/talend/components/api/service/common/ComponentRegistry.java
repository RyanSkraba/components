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
package org.talend.components.api.service.common;

import java.util.*;

import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.RuntimableDefinition;
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

    /**
     * All of the {@link RuntimableDefinition}s that have been added to the framework, including
     * {@link ComponentDefinition}s.
     */
    private List<RuntimableDefinition<?, ?>> definitions;

    private Map<String, ComponentWizardDefinition> componentWizards;

    private Map<String, ComponentFamilyDefinition> componentFamilies;

    public ComponentRegistry() {
        reset();
    }

    /**
     * @return a list of all the extended {@link RuntimableDefinition} that have been added to the framework.
     */
    public Iterable<RuntimableDefinition<?, ?>> getDefinitions() {
        return definitions;
    }

    /**
     * @return a subset of the known definitions
     */
    public <T extends RuntimableDefinition<?, ?>> Iterable<T> getDefinitionsByType(final Class<T> cls) {
        // If we ever add a guava dependency: return Iterables.filter(definitions, cls);
        List<T> byType = new ArrayList<>();
        for (RuntimableDefinition<?, ?> def: definitions) {
            if (cls.isAssignableFrom(def.getClass())) {
                byType.add((T) def);
            }
        }
        return byType;
    }

    /**
     * @return a subset of the known definitions, keyed by name.
     */
    private <T extends RuntimableDefinition<?, ?>> Map<String, T> getDefinitionMapByType(final Class<T> cls) {
        Map<String, T> definitionsAsMap = new HashMap<>();
        for (T def : getDefinitionsByType(cls)) {
            definitionsAsMap.put(def.getName(), def);
        }
        return definitionsAsMap;
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
        definitions = new ArrayList<>();
        componentWizards = new HashMap<>();
        componentFamilies = new HashMap<>();
    }

    /**
     * After all of the definitions have been stored in the registry, ensure that no further modifications can be made.
     */
    public void lock() {
        definitions = Collections.unmodifiableList(definitions);
        componentWizards = Collections.unmodifiableMap(componentWizards);
        componentFamilies = Collections.unmodifiableMap(componentFamilies);
    }

    @Override
    public void registerDefinition(Iterable<? extends RuntimableDefinition<?, ?>> defs) {
        for (RuntimableDefinition<?, ?> runtimable : defs) {
            definitions.add(runtimable);
        }
    }

    @Override
    public void registerComponentWizardDefinition(Iterable<? extends ComponentWizardDefinition> defs) {
        for (ComponentWizardDefinition componentWizard : defs)
            getComponentWizards().put(componentWizard.getName(), componentWizard);
    }

    @Override
    public void registerComponentFamilyDefinition(ComponentFamilyDefinition componentFamily) {
        getComponentFamilies().put(componentFamily.getName(), componentFamily);
        // Always automatically register the nested definitions in the component family.
        registerComponentWizardDefinition(componentFamily.getComponentWizards());
        registerDefinition(componentFamily.getDefinitions());
    }
}