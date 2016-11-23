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
package org.talend.components.api;

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.daikon.definition.Definition;

/**
 * Install all of the functionality of a component family into the framework.
 *
 * This is the single point of registration that a component family must implement, in order to make all of its
 * definitions available in the system.
 *
 * The concrete implementation must be annotated with:
 *
 * <code>
 * Component(name = "UniqueComponentFamilyID", provide = ComponentInstaller.class)
 * </code>
 */
public interface ComponentInstaller {

    /**
     * Called once for each component family at framework startup, giving it the opportunity to register itself into the
     * system.
     * 
     * @param ctx A context object with the available registration methods.
     */
    void install(ComponentFrameworkContext ctx);

    /**
     * The actions that a {@link ComponentInstaller} can take when registering itself to the component framework.
     */
    public interface ComponentFrameworkContext {

        /**
         * Installs an entire component family.
         *
         * This will cause any other definitions discovered through {@link ComponentFamilyDefinition#getExtensions()},
         * {@link ComponentFamilyDefinition#getComponents()} or
         * {@link ComponentFamilyDefinition#getComponentWizards()} to be installed.
         *
         * @param def An entire component family to install.
         */
        void registerComponentFamilyDefinition(ComponentFamilyDefinition def);

        /**
         * Install a {@link RuntimableDefinition}, including {@link ComponentDefinition} into the framework.
         *
         * @param defs The list of definitions to install.
         */
        void registerDefinition(Iterable<? extends Definition> defs);

        /**
         * @param defs The list of component wizard definitions to install.
         * @deprecated please use the {@link #registerDefinition(Iterable)}
         */
        @Deprecated
        void registerComponentWizardDefinition(Iterable<? extends ComponentWizardDefinition> defs);
    }
}
