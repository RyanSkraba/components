// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.service.common.testcomponent;

import java.util.Arrays;
import java.util.List;

import org.talend.components.api.ComponentFamilyDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.i18n.I18nMessages;

public class TestComponentFamilyDefinition implements ComponentFamilyDefinition {

    private final List<Definition> defs = Arrays.asList((ComponentDefinition) new TestComponentDefinition(),
            new TestComponentWizardDefinition());

    @Override
    public Iterable<? extends Definition> getDefinitions() {
        return defs;
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
