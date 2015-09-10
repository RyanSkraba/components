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
     * Optionally asks the wizard to remembers an arbitrary string to allow the wizard to notify the
     * {@link ComponentDesigner} when {@link ComponentProperties} objects are created.
     *
     * @param userData optional user data to be used by the wizard
     * @return
     */
    ComponentWizard createWizard(String userData);

}
