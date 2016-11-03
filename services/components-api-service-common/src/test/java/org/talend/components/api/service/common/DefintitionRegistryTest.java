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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.service.common.testcomponent.TestComponentDefinition;
import org.talend.components.api.service.common.testcomponent.TestComponentFamilyDefinition;
import org.talend.components.api.service.common.testcomponent.TestComponentWizardDefinition;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.daikon.definition.Definition;

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
        assertThat(registry.getDefinitionsMapByType(ComponentDefinition.class).values(),
                contains((Definition) def.getDefinitions().iterator().next()));
        assertThat(registry.getDefinitionsMapByType(ComponentWizardDefinition.class).values(),
                contains((ComponentWizardDefinition) def.getComponentWizards().iterator().next()));
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

}
