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
package org.talend.components.api.service;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.service.PropertiesService;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * The Main service provided by this project to get access to all registered components and their properties.
 */
public interface ComponentService extends PropertiesService<Properties> {

    /**
     * Get the list of all the component names that are registered
     *
     * @return the set of component names, never null
     * @deprecated use {@link DefinitionRegistryService#getDefinitionsMapByType(ComponentDefinition.class).keySet()}
     */
    @Deprecated
    Set<String> getAllComponentNames();

    /**
     * Get the list of all the components {@link ComponentDefinition} that are registered
     *
     * @return the set of component definitions, never null.
     * @deprecated use {@link DefinitionRegistryService#getDefinitionsByType(ComponentDefinition.class)}
     */
    @Deprecated
    Set<ComponentDefinition> getAllComponents();

    /**
     * Return all top-level wizards that can be used to create component property sets.
     *
     * @return the set of component wizards, never null.
     */
    Set<ComponentWizardDefinition> getTopLevelComponentWizards();

    /**
     * Used to get a new {@link ComponentProperties} object for the specified component.
     * 
     * The {@code ComponentProperties} has everything required to render a UI and as well capture and validate the
     * values of the properties associated with the component, based on interactions with this service.
     *
     * @param name the name of the component
     * @return a {@code ComponentProperties} object.
     * @exception ComponentException thrown if the component is not registered in the service
     */
    ComponentProperties getComponentProperties(String name);

    /**
     * Used to get a the {@link ComponentDefinition} object for the specified component.
     *
     *
     * @param name the name of the component
     * @return the {@code ComponentDefinition} object.
     * @exception ComponentException thrown if the component is not registered in the service
     * @deprecated use {@link DefinitionRegistryService#getDefinitionsMapByType(ComponentDefinition.class).get(name)}
     */
    @Deprecated
    ComponentDefinition getComponentDefinition(String name);

    /**
     * Creates a new instance of a {@link ComponentWizard} for the specified wizard name.
     *
     * Wizard names are globally unique. Non-top-level wizards should be named using the name of the top-level wizard.
     * For example, the Salesforce wizard would be called "salesforce", and a wizard dealing with only the Salesforce
     * modules would be called "salesforce.modules" in order to make sure the name is unique.
     *
     * @param name the name of the wizard
     * @param location an arbitrary repositoryLocation string to optionally be used in the wizard processing. This is
     *            given to an implementation of the {@link Repository} object when the {@link ComponentProperties} are stored.
     * @return a {@code ComponentWizard} object.
     * @exception ComponentException thrown if the wizard is not registered in the service
     */
    ComponentWizard getComponentWizard(String name, String location);

    /**
     * Creates {@link ComponentWizard}(s) that are populated by the given properties.
     *
     * This is used when you already have the {@link T} object from a previous execution of the wizard and you wish to
     * show wizards applicable to the those properties.
     * 
     * @param properties a {@link T} object previously created
     * @param location the repository location of where the {@link T} were stored.
     * @return a {@link List} of {@code ComponentWizard} object(s)
     */
    List<ComponentWizard> getComponentWizardsForProperties(ComponentProperties properties, String location);

    /**
     * Return the {@link ComponentDefinition} objects for any component(s) that can be constructed from the given
     * {@link ComponentProperties} object.
     * 
     * @param properties the {@link ComponentProperties} object to look for.
     * @return the list of compatible {@link ComponentDefinition} objects.
     */
    List<ComponentDefinition> getPossibleComponents(ComponentProperties... properties) throws Throwable;

    /**
     * Copy the nestedValues properties into the targetProperties nested properties if the targetProperties accepts it.
     * It is guarantied to be accepted if the targetProperties is associated with the Component definition that was
     * return by {@link #getPossibleComponents(ComponentProperties...)} using the nestedValue as a parameter.
     * 
     * @param targetProperties the ComponentProperties to be updated with the nestedValues properties.
     * @param nestedValues the ComponentProperties which properties will be copied inot the targetProperties.
     * @return true if the copy was done and false if the targetProperties does not accept the nestedValues type.
     */
    boolean setNestedPropertiesValues(ComponentProperties targetProperties, Properties nestedValues);

    /**
     * Return the png image related to the given wizard
     * 
     * @param wizardName, name of the wizard to get the image for
     * @param imageType, the type of image requested
     * @return the png image stream or null if none was provided or could not be found
     * @exception ComponentException thrown if the componentName is not registered in the service
     */
    InputStream getWizardPngImage(String wizardName, WizardImageType imageType);

    /**
     * Return the png image related to the given component
     * 
     * @param componentName, name of the comonent to get the image for
     * @param imageType, the type of image requested
     * @return the png image stream or null if none was provided or an error occurred
     * @exception ComponentException thrown if the componentName is not registered in the service
     */
    InputStream getComponentPngImage(String componentName, ComponentImageType imageType);

    /**
     * Allows for a local implementation to setup a repository store used to store {@link ComponentProperties}.
     * 
     * @param repository
     */
    @Override
    void setRepository(Repository repository);

    /**
     * list all the depencencies required for this component to be executed at runtime
     * 
     * @param componentName name of the component to get the dependencies of.
     * @param properties the properties to compute the dependencies from.
     * @param componentType will determine the runtime class to be used.
     * @return the runtime info for running the given component with the given properties
     */
    RuntimeInfo getRuntimeInfo(String componentName, Properties properties, ConnectorTopology componentType);

    /**
     * get the schema associated with a given named connection for a componentProperties
     * 
     * @param componentProperties the Properties to get the schema for a given connector name
     * @param connector token used to identify the connection.
     * @param isOuput true is the connection is an output connection, false if it is an input connection
     * @return the schema associated with a given connector token of input or ouput connectors, may be null.
     * @exception ComponentException thrown if the connector is not recognized for the given component.
     */
    Schema getSchema(ComponentProperties componentProperties, Connector connector, boolean isOuput);

    /**
     * set the schema associated with a given named connection for a componentProperties
     * 
     * @param componentProperties the Properties to get the schema for a given connector name
     * @param connector token used to identify the connection.
     * @param schema schema to be set for the given connector
     * @param isOuput true is the connection is an output connection, false if it is an input connection
     * @return the schema associated with a given connector token of input or ouput connectors, may be null if schema is
     *         associated with the connector. This should never be the case for output connections but may be null for input
     *         connections because the component does not need to have any input schema and can handle any data type.
     */
    void setSchema(ComponentProperties componentProperties, Connector connector, Schema schema, boolean isOuput);

    /**
     * get the schema associated with a given named connector for a componentProperties
     * 
     * @param componentProperties the Properties to get the connectors from
     * @param connectedConnetor list of connectors already setup. This shall be managed by the client.
     * @param isOuput true is the requested connections are output connections, false if the request is on input
     *            connections
     * @return the set of availalble connectors, may be empty.
     */
    Set<? extends Connector> getAvailableConnectors(ComponentProperties componentProperties,
            Set<? extends Connector> connectedConnetor, boolean isOuput);

}
