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
package org.talend.components.api.service.common;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;
import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.service.common.testcomponent.TestComponentDefinition;
import org.talend.components.api.service.common.testcomponent.TestComponentFamilyDefinition;
import org.talend.components.api.service.common.testcomponent.TestComponentWizardDefinition;
import org.talend.components.api.service.common.testcomponent.inject.TestInjectComponentDefinition;
import org.talend.components.api.service.common.testcomponent.inject.TestInjectComponentProperties;
import org.talend.components.api.service.common.testcomponent.inject.TestNestedInjectComponentDefinition;
import org.talend.components.api.service.common.testcomponent.inject.TestNestedInjectComponentProperties;
import org.talend.components.api.service.common.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.service.common.testcomponent.nestedprop.inherited.InheritedComponentProperties;
import org.talend.components.api.test.SimpleComponentDefinition;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public class DefintitionRegistryTest {

    @Test
    public void testEmpty() {
        DefinitionRegistry registry = new DefinitionRegistry();
        assertThat(registry.getIterableDefinitions(), emptyIterable());
        assertThat(registry.getDefinitionsMapByType(ComponentWizardDefinition.class).keySet(), empty());
        assertThat(registry.getComponentFamilies().keySet(), empty());
    }

    @Test
    public void testAddComponentDefinition() {
        DefinitionRegistry registry = new DefinitionRegistry();
        ComponentDefinition def = new TestComponentDefinition();
        registry.registerDefinition(Arrays.asList(def));
        assertThat(registry.getIterableDefinitions(), contains((Definition) def));
        assertThat(registry.getDefinitionsByType(ComponentDefinition.class), contains(def));
    }

    @Test
    public void testAddComponentWizardDefinition() {
        DefinitionRegistry registry = new DefinitionRegistry();
        ComponentWizardDefinition def = new TestComponentWizardDefinition();
        registry.registerComponentWizardDefinition(Arrays.asList(def));
        assertThat(registry.getDefinitionsMapByType(ComponentWizardDefinition.class).keySet(), hasSize(1));
        assertThat(registry.getDefinitionsMapByType(ComponentWizardDefinition.class), hasEntry(def.getName(), def));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testAddComponentFamilyDefinition() {
        DefinitionRegistry registry = new DefinitionRegistry();
        ComponentFamilyDefinition def = new TestComponentFamilyDefinition();
        registry.registerComponentFamilyDefinition(def);
        assertThat(registry.getComponentFamilies().keySet(), hasSize(1));
        assertThat(registry.getComponentFamilies(), hasEntry(def.getName(), def));

        // All of the nested definitions were added.
        Iterator<? extends Definition> iterator = def.getDefinitions().iterator();
        assertThat(registry.getDefinitionsMapByType(ComponentDefinition.class).values(), contains((Definition) iterator.next()));
        assertThat(registry.getDefinitionsMapByType(ComponentWizardDefinition.class).values(),
                contains((ComponentWizardDefinition) iterator.next()));
    }

    @Test
    public void testLock() {
        DefinitionRegistry registry = new DefinitionRegistry();
        ComponentFamilyDefinition def = new TestComponentFamilyDefinition();
        registry.registerComponentFamilyDefinition(def);
        registry.lock();
        try {
            registry.registerComponentFamilyDefinition(def);
            fail("registry should be locked hence not def should be added");
        } catch (UnsupportedOperationException e) {
            // all is good if we are here
        }
    }

    @Test
    public void testGetDefinitionForPropertiesType() {
        // we'll check that 2 derived Properties have thier definition returned.
        SimpleComponentDefinition compDef = new SimpleComponentDefinition("def", ExecutionEngine.DI);
        compDef.setPropertyClass(NestedComponentProperties.class);
        SimpleComponentDefinition inheritedDef = new SimpleComponentDefinition("DefOfinherited", ExecutionEngine.DI);
        inheritedDef.setPropertyClass(InheritedComponentProperties.class);

        DefinitionRegistry definitionRegistry = new DefinitionRegistry();
        definitionRegistry.registerDefinition(Arrays.asList(compDef, inheritedDef, new TestComponentDefinition()));

        Iterable<Definition> definitionForPropertiesType = definitionRegistry
                .getDefinitionForPropertiesType(NestedComponentProperties.class);
        assertThat(definitionForPropertiesType, contains((Definition) compDef, inheritedDef));

    }

    @Test
    public void testInjectDefinitionRegistry() {
        DefinitionRegistry registry = new DefinitionRegistry();
        TestInjectComponentProperties properties = new TestInjectComponentProperties("props");
        registry.injectDefinitionRegistry(properties);

        assertThat((DefinitionRegistry) properties.getDefinitionRegistry(), equalTo(registry));
    }

    @Test
    public void testCreateNewPropertiesWithInjected() {
        DefinitionRegistryService registry = new DefinitionRegistry();
        TestInjectComponentDefinition def = new TestInjectComponentDefinition();
        TestInjectComponentProperties testProps = (TestInjectComponentProperties) registry.createProperties(def, "testProps");
        assertThat(testProps.getDefinitionRegistry(), equalTo(registry));
    }

    @Test
    public void testCreateNewPropertiesWithNestedInjected() {
        DefinitionRegistryService registry = new DefinitionRegistry();
        TestNestedInjectComponentDefinition def = new TestNestedInjectComponentDefinition();
        TestNestedInjectComponentProperties testProps = (TestNestedInjectComponentProperties) registry.createProperties(def,
                "testProps");
        assertThat(testProps.getNestedProperties().getDefinitionRegistry(), equalTo(registry));
    }

}
