package org.talend.components.api;

/**
 */
public interface ComponentWizardDefinition {

    public String getName();

    public String getMenuItemName();

    // FIXME
    public Object getIcon();

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
