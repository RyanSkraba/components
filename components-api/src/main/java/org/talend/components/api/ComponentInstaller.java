package org.talend.components.api;

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.wizard.ComponentWizardDefinition;

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

        void registerComponentDefinition(Iterable<ComponentDefinition> defs);

        void registerComponentWizardDefinition(Iterable<ComponentWizardDefinition> defs);

        /**
         * Installs an entire component family.
         *
         * This will cause any other definitions discovered through {@link ComponentFamilyDefinition#getComponents()} or
         * {@link ComponentFamilyDefinition#getComponentWizards()} to be installed.
         *
         * @param def An entire component family to install.
         */
        void registerComponentFamilyDefinition(ComponentFamilyDefinition def);
    }
}
