// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import java.util.Set;

import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Defines a component.
 * <p/>
 * A class implementing this interface is the definition of a component. Instances are registered with the
 * {@link org.talend.components.api.service.ComponentService} to allow components to be discovered.
 */
public interface ComponentDefinition extends Definition<ComponentProperties> {

    /**
     * Returns an array of paths that represent the categories of the component.
     */
    String[] getFamilies();

    /**
     * Create and initialize a suitable {@link ComponentProperties} which configures an instance of this component.
     */
    @Override
    Class<ComponentProperties> getPropertiesClass();

    /**
     * create an instance of the associated ComponentProperties
     * 
     * @deprecated please use {@link PropertiesImpl#createNewInstance(Class, String)} along with the {@link #getPropertiesClass()}
     */
    @Deprecated
    ComponentProperties createProperties();

    /**
     * FIXME - is this really necessary? create the ComponentProperties and initialize it's properties only and not the
     * UI Layout not usefull for runtime
     */
    ComponentProperties createRuntimeProperties();

    /**
     * Common return properties names
     */
    static final String RETURN_ERROR_MESSAGE = "errorMessage";

    static final String RETURN_TOTAL_RECORD_COUNT = "totalRecordCount";

    static final String RETURN_SUCCESS_RECORD_COUNT = "successRecordCount";

    static final String RETURN_REJECT_RECORD_COUNT = "rejectRecordCount";

    static final Property<String> RETURN_ERROR_MESSAGE_PROP = PropertyFactory.newString(RETURN_ERROR_MESSAGE);

    static final Property<Integer> RETURN_TOTAL_RECORD_COUNT_PROP = PropertyFactory.newInteger(RETURN_TOTAL_RECORD_COUNT);

    static final Property<Integer> RETURN_SUCCESS_RECORD_COUNT_PROP = PropertyFactory.newInteger(RETURN_SUCCESS_RECORD_COUNT);

    static final Property<Integer> RETURN_REJECT_RECORD_COUNT_PROP = PropertyFactory.newInteger(RETURN_REJECT_RECORD_COUNT);

    /**
     * Returns a list of the properties that the component returns at runtime.
     *
     * The returns properties are properties that are populated at runtime and provided after execution completes. For connector
     * components, the values are obtained using {@link WriteOperation#finalize()} and {@link Reader#getReturnValues()}.
     *
     * @return a list of {@link Property} objects, one for each return property.
     */
    Property[] getReturnProperties();

    /**
     * Returns true if this {@code ComponentDefinition} will work with the specified list of {@link ComponentProperties}
     */
    boolean supportsProperties(ComponentProperties... properties);

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
     * @deprecated use {@link #getImagePath(DefinitionImageType)}.
     */
    @Deprecated
    String getPngImagePath(ComponentImageType imageType);

    //
    // FIXME - DI flags - do we need all of these?
    //

    boolean isSchemaAutoPropagate();

    boolean isDataAutoPropagate();

    boolean isConditionalInputs();

    /**
     * True if the component needs to report on rejected writes after the close() method in the writer.
     */
    boolean isRejectAfterClose();

    boolean isStartable();

    // FIXME - An ENUM perhaps?
    String getPartitioning();

    /**
     * @return the set of execution engines that business objects can provide runtimes for.
     */
    Set<ExecutionEngine> getSupportedExecutionEngines();

    /**
     * @return true if the execution engine is compatible with the component.
     */
    boolean isSupportingExecutionEngines(ExecutionEngine executionEngine);

    /**
     * this will create the runtime the information required for running the component. They can depend an the given
     * <code>properties</code> parameter and the connector topology. The <code>connectorTopology</code> shall be one of
     * supported topologies returned by {@link #getSupportedConnectorTopologies()}
     *
     * @param engine the engine to get a runtime for.
     * @param properties may be used to compute the runtime dependencies or class, may be null.
     * @param connectorTopology the topology of connectors you want to get the Runtime from.
     * 
     * @return the runtime information related to this component and <code>connectorTopology</code>. Should return null if the
     *         <code>componentType</code> is not par of the supported types returned by {@link #getSupportedConnectorTopologies()}
     */
    RuntimeInfo getRuntimeInfo(ExecutionEngine engine, ComponentProperties properties, ConnectorTopology connectorTopology);

    /**
     * This will returns a set connectors topologies that this component supports.
     * 
     * @return all the type this component provides.
     */
    Set<ConnectorTopology> getSupportedConnectorTopologies();

}
