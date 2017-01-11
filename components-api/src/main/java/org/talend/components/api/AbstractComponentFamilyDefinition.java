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
package org.talend.components.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.talend.daikon.definition.Definition;

/**
 * The AbstractComponentFamilyDefinition provides a simple implementation of a component family.
 */
public abstract class AbstractComponentFamilyDefinition extends AbstractTopLevelDefinition implements ComponentFamilyDefinition {

    /** Family name, must be unique in the framework. */
    private final String familyName;

    private final List<Definition<?>> definitions;

    /**
     * Construct the subclass with a given, predefined, unmodifiable set of definitions.
     *
     * @param familyName Unique identifier for the family in the component framework.
     * @param definitions A list of definitions that are related to this component family. If the type of the definition
     *            is not correct or unknown, it will ignored. Otherwise, it will appear in one of the getXxxxDefinitions()
     *            methods.
     */
    public AbstractComponentFamilyDefinition(String familyName, Definition<?>... definitions) {
        this.familyName = familyName;

        if (definitions == null || definitions.length == 0) {
            // Shortcut if there are no definitions. The subclass can overwrite the implementations instead.
            this.definitions = Collections.emptyList();
        } else {
            // Otherwise sort the definitions into their respective categories.
            this.definitions = Collections.unmodifiableList(Arrays.asList(definitions));
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
    public Iterable<? extends Definition> getDefinitions() {
        return definitions;
    }
}
