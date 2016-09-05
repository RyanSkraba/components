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
package org.talend.components.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.wizard.ComponentWizardDefinition;

/**
 * The AbstractComponentFamilyDefinition provides an implementation without any nested definitions.
 */
public abstract class AbstractComponentFamilyDefinition extends AbstractTopLevelDefinition implements ComponentFamilyDefinition {

    /** Family name, must be unique in the framework. */
    private String familyName;

    private final Iterable<ComponentDefinition> components;

    private final Iterable<ComponentWizardDefinition> componentWizards;

    /**
     * Construct the subclass with a given, predefined, unmodifiable set of definitions.
     *
     * @param familyName Unique identifier for the family in the component framework.
     * @param definitions A list of definitions that are related to this component family. If the type of the definition
     * is not correct or unknown, it will ignored. Otherwise, it will appear in one of the getXxxxDefinitions() methods.
     */
    public AbstractComponentFamilyDefinition(String familyName, Object... definitions) {
        this.familyName = familyName;

        if (definitions == null || definitions.length == 0) {
            // Shortcut if there are no definitions. The subclass can overwrite the implementations instead.
            this.components = Collections.<ComponentDefinition> emptyList();
            this.componentWizards = Collections.<ComponentWizardDefinition> emptyList();
        } else {
            // Otherwise sort the definitions into their respective categories.
            List<ComponentDefinition> comp = new ArrayList<>();
            List<ComponentWizardDefinition> compw = new ArrayList<>();
            for (Object def : definitions) {
                if (def instanceof ComponentDefinition) {
                    comp.add((ComponentDefinition) def);
                }
                if (def instanceof ComponentWizardDefinition) {
                    compw.add((ComponentWizardDefinition) def);
                }
            }
            this.components = comp.size() != 0 ? Collections.unmodifiableList(comp)
                    : Collections.<ComponentDefinition> emptyList();
            this.componentWizards = compw.size() != 0 ? Collections.unmodifiableList(compw)
                    : Collections.<ComponentWizardDefinition> emptyList();
        }
    }

    /**
     * @return A unique name for this definition in the framework.
     */
    @Override
    public String getName() {
        return familyName;
    }

    @Override
    protected String getI18nPrefix() {
        return "family."; //$NON-NLS-1$
    }

    @Override
    public Iterable<ComponentDefinition> getComponents() {
        return components;
    }

    @Override
    public Iterable<ComponentWizardDefinition> getComponentWizards() {
        return componentWizards;
    }
}
