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
package org.talend.components.api.service;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.daikon.properties.service.PropertiesService;
import org.talend.daikon.properties.service.Repository;

/**
 * The Main service provided by this project to get access to all registered components and their properties.
 */
public interface ComponentService extends PropertiesService<ComponentProperties> {

    /**
     * Get the list of all the component names that are registered
     *
     * @return return the set of component names, never null
     */
    Set<String> getAllComponentNames();

    /**
     * Get the list of all the components {@link ComponentDefinition} that are registered
     *
     * @return return the set of component definitions, never null.
     */
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
     */
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
     * given to an implementation of the {@link Repository} object when the {@link ComponentProperties} are stored.
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
     * @return the list of compatbible {@link ComponentDefinition} objects.
     */
    List<ComponentDefinition> getPossibleComponents(ComponentProperties... properties) throws Throwable;

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
     * @return a set of maven uri following the pax-maven uri scheme @see
     * <a href="https://ops4j1.jira.com/wiki/display/paxurl/Mvn+Protocol">https://ops4j1.jira.com/wiki/display/paxurl/
     * Mvn+Protocol</a>
     */
    Set<String> getMavenUriDependencies(String componentName);

}