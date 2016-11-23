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
package org.talend.components.api.wizard;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.definition.Definition;

/**
 * created by sgandon on 8 févr. 2016
 */
public interface ComponentWizardDefinition extends Definition {

    String getMenuItemName();

    /**
     * Creates an empty {@link ComponentWizard} based on the this definition.
     *
     * Optionally asks the wizard to remembers a repository location string to allow the wizard to notify the
     * {@link ComponentDesigner} when {@link ComponentProperties} objects are created.
     *
     * @param location the repository location where the {@link ComponentProperties} associated with this wizard are to
     *            be stored. See {@link Repository}.
     * @return
     */
    ComponentWizard createWizard(String location);

    /**
     * Creates a {@link ComponentWizard} based on the this definition populated with the specified
     * {@link ComponentProperties}
     *
     * Optionally asks the wizard to remembers a repository location string to allow the wizard to notify the
     * {@link ComponentDesigner} when {@link ComponentProperties} objects are created.
     *
     * @param location the repository location where the {@link ComponentProperties} associated with this wizard are to
     *            be stored. See {@link Repository}.
     * @return
     */
    ComponentWizard createWizard(ComponentProperties properties, String location);

    /**
     * return the image path used to decorate the wizard and and possible tree node related to it. This shall be a path
     * relative to the current Wizard definition, ideally is should just be the name of the png image if placed in the
     * same resource folder that the current class. This icon will be computed with the following code
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
    String getPngImagePath(WizardImageType imageType);

    /**
     * tell whether this Wizard is allowed to edit the given properties.
     * 
     * @param propertiesClass, the class to be checked upon.
     * @return true is this wizard can handle the specific properties.
     */
    boolean supportsProperties(Class<? extends ComponentProperties> propertiesClass);

    /**
     * This tell the client whether this wizard is of type connector instead of specific input/output data specification
     * that may be just lower level specification.
     * 
     * @return true is the wizard edits top level connections.
     */
    boolean isTopLevel();

}
