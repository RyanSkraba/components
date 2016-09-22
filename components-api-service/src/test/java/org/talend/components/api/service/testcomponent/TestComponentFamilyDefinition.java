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
package org.talend.components.api.service.testcomponent;

import java.util.Arrays;
import java.util.List;

import org.talend.components.api.RuntimableDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.daikon.i18n.I18nMessages;

public class TestComponentFamilyDefinition implements ComponentFamilyDefinition {

    private final List<ComponentDefinition> components = Arrays.asList((ComponentDefinition) new TestComponentDefinition());

    private final List<ComponentWizardDefinition> componentWizards = Arrays
            .asList((ComponentWizardDefinition) new TestComponentWizardDefinition());

    @Override
    public Iterable<? extends RuntimableDefinition<?, ?>> getDefinitions() {
        return components;
    }

    @Override
    public Iterable<ComponentWizardDefinition> getComponentWizards() {
        return componentWizards;
    }

    @Override
    public void setI18nMessageFormatter(I18nMessages i18nMessages) {

    }

    @Override
    public String getI18nMessage(String key, Object... arguments) {
        return getName();
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getTitle() {
        return getName();
    }
}
