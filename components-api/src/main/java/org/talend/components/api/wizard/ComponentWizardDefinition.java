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

    /**
     * This shall be a path relative to the current Wizard definition, ideally is should just be the name of the png
     * image if placed in the same resource folder that the current class. This icon will be computed with the following
     * code
     * 
     * <pre>
     * {@code
     *    this.getClass().getResourceAsStream(getIconPngPath())
     * }
     * </pre>
     * 
     * @see {@link java.lang.Class#getResourceAsStream(String)}
     * @param imageType the type of image requested
     * @return the path to the png resource
     */

    public abstract String getPngImagePath(WizardImageType imageType);

}
