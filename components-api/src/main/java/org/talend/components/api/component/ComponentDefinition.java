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
package org.talend.components.api.component;

import org.talend.components.api.TopLevelDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntime;

/**
 * Component definition service.
 * <p/>
 * An instance of this class handles the setup of the properties associated with a components.
 *
 * @author Francis Upton
 */

public interface ComponentDefinition extends TopLevelDefinition {

    /**
     * Component categorization - this is an issue that wants further study. - which designer (big data, di, etc) and
     * then which family.
     */
    // FIXME - this needs to be corrected
    public static final String FAMILY_BUSINESS = "Business/Salesforce";

    public static final String FAMILY_CLOUD = "Cloud/Salesforce";

    /*
     * Where do we specify a wizard is required? Maybe list of groups that comprise wizard.
     */

    /*
     * Intercomponent property references - need examples for this. - shared clumps of properties, referring to
     * properties in the same job, refers to properties upstream in the connection.
     * 
     * all properties should support context variables (non-text properties need this).
     */

    public ComponentProperties createProperties();

    /**
     * Create a {@link ComponentRuntime} object.
     * 
     * @return a {@link ComponentRuntime} object.
     */
    public ComponentRuntime createRuntime();

    // public void setDesignerFamily(Family family);

    public String[] getSupportedFamilies();

    public ComponentConnector[] getConnectors();

    /**
     * This shall be a path relative to the current Component definition, ideally is should just be the name of the png
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
     * @return the path to the png resource or null if the type is not handled.
     */

    public abstract String getPngImagePath(ComponentImageType imageType);

}
