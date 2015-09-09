package org.talend.components.api.wizard;

import org.talend.components.api.ComponentDesigner;
import org.talend.components.api.TopLevelDefinition;
import org.talend.components.api.properties.ComponentProperties;

/**
 */
public interface ComponentWizardDefinition extends TopLevelDefinition {

    public String getMenuItemName();

    /**
     * Creates a {@link ComponentWizard} based on the this definition.
     *
     * Optionally asks the wizard to remembers an arbitrary object to allow the wizard to notify the
     * {@link ComponentDesigner} when {@link ComponentProperties} objects are created.
     *
     * @param location
     * @return
     */
    ComponentWizard createWizard(Object location);

}
