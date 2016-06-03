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
package org.talend.components.api.test;

import java.util.HashMap;
import java.util.Map;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.service.internal.ComponentRegistry;
import org.talend.components.api.wizard.ComponentWizardDefinition;

/**
 * created by sgandon on 10 déc. 2015
 */
public class SimpleComponentRegistry implements ComponentRegistry {

    private Map<String, ComponentDefinition> components = new HashMap<>();

    Map<String, ComponentWizardDefinition> wizards = new HashMap<>();

    public void addComponent(String name, ComponentDefinition component) {
        components.put(Constants.COMPONENT_BEAN_PREFIX + name, component);
    }

    public void removeComponent(String name) {
        components.remove(Constants.COMPONENT_BEAN_PREFIX + name);
    }

    @Override
    public Map<String, ComponentDefinition> getComponents() {
        return components;
    }

    public void addWizard(String name, ComponentWizardDefinition wizard) {
        wizards.put(Constants.COMPONENT_WIZARD_BEAN_PREFIX + name, wizard);
    }

    public void removeWizard(String name) {
        wizards.remove(Constants.COMPONENT_WIZARD_BEAN_PREFIX + name);
    }

    @Override
    public Map<String, ComponentWizardDefinition> getComponentWizards() {
        return wizards;
    }
}