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

import java.io.InputStream;

import org.talend.components.api.TopLevelDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntime;

/**
 * Component definition service.
 * <p/>
 * An instance of this class handles the setup of the properties associated with a components.
 */

public interface ComponentDefinition extends TopLevelDefinition {

    /**
     * Returns an array of paths that represent the categories of the component.
     */
    public String[] getFamilies();

    /**
     * create the ComponentProperties and initialize it's properties and ui layout
     */
    public ComponentProperties createProperties();

    /**
     * create the ComponentProperties and initialize it's properties only and not the UI Layout not usefull for runtime
     */
    public ComponentProperties createRuntimeProperties();

    /**
     * Create a {@link ComponentRuntime} object.
     */
    public ComponentRuntime createRuntime();

    /**
     * Returns the types of {@link ComponentConnector} objects supported by this component.
     */
    public ComponentConnector[] getConnectors();

    /**
     * Returns true if this {@code ComponentDefinition} will work with the specified {@link ComponentProperties}.
     */
    public boolean supportsProperties(ComponentProperties properties);

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
    public String getPngImagePath(ComponentImageType imageType);

    //
    // FIXME - DI flags - do we need all of these?
    //

    public boolean isSchemaAutoPropagate();

    public boolean isDataAutoPropagate();

    public boolean isConditionalInputs();

    public boolean isStartable();

    // FIXME - An ENUM perhaps?
    public String getPartitioning();

    public InputStream getMavenPom();

}
