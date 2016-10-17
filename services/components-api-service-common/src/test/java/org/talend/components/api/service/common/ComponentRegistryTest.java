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
import org.talend.components.api.RuntimableDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.service.common.ComponentRegistry;
import org.talend.components.api.service.common.testcomponent.TestComponentDefinition;
import org.talend.components.api.service.common.testcomponent.TestComponentFamilyDefinition;
import org.talend.components.api.service.common.testcomponent.TestComponentWizardDefinition;
import org.talend.components.api.wizard.ComponentWizardDefinition;

public class ComponentRegistryTest {

    @Test
    public void testEmpty() {
        ComponentRegistry registry = new ComponentRegistry();
        assertThat(registry.getDefinitions(), emptyIterable());
        assertThat(registry.getComponentWizards().keySet(), empty());
        assertThat(registry.getComponentFamilies().keySet(), empty());
    }

    @Test
    public void testAddComponentDefinition() {
        ComponentRegistry registry = new ComponentRegistry();
        ComponentDefinition def = new TestComponentDefinition();
        registry.registerDefinition(Arrays.asList(def));
        assertThat(registry.getDefinitions(), contains((RuntimableDefinition) def));
        assertThat(registry.getDefinitionsByType(ComponentDefinition.class), contains(def));
    }

    @Test
    public void testAddComponentWizardDefinition() {
        ComponentRegistry registry = new ComponentRegistry();
        ComponentWizardDefinition def = new TestComponentWizardDefinition();
        registry.registerComponentWizardDefinition(Arrays.asList(def));
        assertThat(registry.getComponentWizards().keySet(), hasSize(1));
        assertThat(registry.getComponentWizards(), hasEntry(def.getName(), def));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testAddComponentFamilyDefinition() {
        ComponentRegistry registry = new ComponentRegistry();
        ComponentFamilyDefinition def = new TestComponentFamilyDefinition();
        registry.registerComponentFamilyDefinition(def);
        assertThat(registry.getComponentFamilies().keySet(), hasSize(1));
        assertThat(registry.getComponentFamilies(), hasEntry(def.getName(), def));

        // All of the nested definitions were added.
        assertThat(registry.getDefinitions(), contains((RuntimableDefinition) def.getDefinitions().iterator().next()));
        assertThat(registry.getComponentWizards().values(),
                contains((ComponentWizardDefinition) def.getComponentWizards().iterator().next()));
    }

    @Test
    public void testLock() {
        // TODO: verify that the returned maps are unmodifiable after the registry is locked.
    }

}
