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
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.property.Property;

/**
 * Defines a component.
 * <p/>
 * A class implementing this interface is the definition of a component. Instances are registered with the
 * {@link org.talend.components.api.service.ComponentService} to allow components to be discovered.
 */

public interface ComponentDefinition extends TopLevelDefinition {

    /**
     * Returns an array of paths that represent the categories of the component.
     */
    public String[] getFamilies();

    /**
     * Create and initialize a suitable {@link ComponentProperties} which configures an instance of this component.
     */
    public ComponentProperties createProperties();

    /**
     * FIXME - is this really necessary? create the ComponentProperties and initialize it's properties only and not the
     * UI Layout not usefull for runtime
     */
    public ComponentProperties createRuntimeProperties();

    /**
     * Returns the types of {@link Trigger} objects supported by this component.
     *
     * A trigger is a link between two components that schedule the different subjobs.
     */
    public Trigger[] getTriggers();

    /**
     * Common return properties names
     */
    public static final String RETURN_ERROR_MESSAGE = "errorMessage";

    public static final String RETURN_TOTAL_RECORD_COUNT = "totalRecordCount";

    public static final String RETURN_SUCCESS_RECORD_COUNT = "successRecordCount";

    public static final String RETURN_REJECT_RECORD_COUNT = "rejectRecordCount";

    /**
     * Returns a list of the properties that the component returns at runtime.
     *
     * The returns properties are properties that are populated at runtime and provided after execution completes. For connector
     * components, the values are obtained using {@link WriteOperation#finalize()} and {@link Reader#getReturnValues()}.
     *
     * @return a list of {@link Property} objects, one for each return property.
     */
    public Property[] getReturnProperties();

    /**
     * Returns true if this {@code ComponentDefinition} will work with the specified list of {@link ComponentProperties}
     */
    public boolean supportsProperties(ComponentProperties... properties);

    /**
     * A path relative to the current Component definition, ideally is should just be the name of the png image if
     * placed in the same resource folder as the implementing class. The
     * {@code org.talend.components.api.service.ComponentService} will compute the icon with the following code:
     * 
     * <pre>
     * {@code
     *    this.getClass().getResourceAsStream(getIconPngPath())
     * }
     * </pre>
     * 
     * @see {@link java.lang.Class#getResourceAsStream(String)}
     * @param imageType the type of image requested
     * @return the path to the png resource or null if an image is not required.
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

    /**
     * Used for computing the dependencies by finding the pom.xml and dependencies.properties in the META-INF/ folder
     * 
     * @return the maven Group Id of the component family
     */
    public String getMavenGroupId();

    /**
     * Used for computing the dependencies by finding the pom.xml and dependencies.properties in the META-INF/ folder
     * 
     * @return the maven Artifact Id of the component family
     */
    public String getMavenArtifactId();

}
