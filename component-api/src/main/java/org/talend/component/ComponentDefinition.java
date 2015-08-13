package org.talend.component;

/**
 * Component design service.
 * <p>
 * An instance of this class handles the setup of the properties associated with a component.
 *
 * @author Francis Upton
 */

public abstract class ComponentDefinition {

    /**
     * Component categorization - this is an issue that wants further study. - which designer (big data, di, etc) and
     * then which family.
     */
    public enum Family {
                        BUSINESS,
                        CLOUD
    }

    /*
     * Where do we specify a wizard is required? Maybe list of groups that comprise wizard.
     */

    /*
     * Intercomponent property references - need examples for this. - shared clumps of properties, referring to
     * properties in the same job, refers to properties upstream in the connection.
     * 
     * all properties should support context variables (non-text properties need this).
     */

    public abstract ComponentProperties createProperties();

    public void setDesignerFamily(Family family) {

    }

    public abstract Family[] getSupportedFamilies();

    public abstract String getName();

}
