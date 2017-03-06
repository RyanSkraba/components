//==============================================================================
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
//==============================================================================
package org.talend.components.service.rest;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.daikon.annotation.Service;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.service.Repository;

/**
 *
 */
@Service(name = "ComponentController")
@RequestMapping("/components")
public interface ComponentController {

    /**
     * Get the list of all the component names that are registered
     *
     * @param name the component name.
     * @param formName the wanted form name (default value is Main).
     * @return the set of component names, never null.
     * @returnWrapped org.talend.components.api.properties.ComponentProperties
     */
    @RequestMapping(value = "/properties/{name}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    StreamingResponseBody getComponentProperties(@PathVariable(value = "name") String name, @RequestParam(required = false) String formName);

    /**
     * Get the list of all the components {@link ComponentDefinition} that are registered
     *
     * @return the set of component definitions, never null.
     * @returnWrapped org.talend.components.api.component.ComponentDefinition
     */
    @RequestMapping(value = "/definition/{name}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    ComponentDefinition getComponentDefinition(@PathVariable(value = "name") String name);

    /**
     * Creates a new instance of a {@link ComponentWizard} for the specified wizard name.
     * <p>
     * Wizard names are globally unique. Non-top-level wizards should be named using the name of the top-level wizard.
     * For example, the Salesforce wizard would be called "salesforce", and a wizard dealing with only the Salesforce
     * modules would be called "salesforce.modules" in order to make sure the name is unique.
     *
     * @param name               the name of the wizard
     * @param repositoryLocation an arbitrary repositoryLocation string to optionally be used in the wizard processing. This is
     *                           given to an implementation of the {@link Repository} object when the {@link ComponentProperties} are stored.
     * @return a {@code ComponentWizard} object.
     * @returnWrapped org.talend.components.api.wizard.ComponentWizard
     */
    @RequestMapping(value = "/wizard/{name}/{repositoryLocation}", method = GET, produces = APPLICATION_JSON_VALUE)
    ComponentWizard getComponentWizard(@PathVariable(value = "name") String name,
                                       @PathVariable(value = "repositoryLocation") String repositoryLocation);

    /**
     * Creates {@link ComponentWizard}(s) that are populated by the given properties.
     * <p>
     * This is used when you already have the object from a previous execution of the wizard and you wish to
     * show wizards applicable to the those properties.
     *
     * @param properties         a object previously created
     * @param repositoryLocation the repository location of where the were stored.
     * @return a {@link List} of {@code ComponentWizard} object(s)
     * @returnWrapped java.util.List<org.talend.components.api.wizard.ComponentWizard>
     */
    @RequestMapping(value = "/wizardForProperties/{repositoryLocation}", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    List<ComponentWizard> getComponentWizardsForProperties( //
                                                            @RequestBody ComponentProperties properties, //
                                                            @PathVariable(value = "repositoryLocation") String repositoryLocation);

    /**
     * Return the {@link ComponentDefinition} objects for any component(s) that can be constructed from the given
     * {@link ComponentProperties} object.
     *
     * @param properties the {@link ComponentProperties} object to look for.
     * @return the list of compatible {@link ComponentDefinition} objects.
     * @returnWrapped java.util.List<org.talend.components.api.component.ComponentDefinition>
     */
    @RequestMapping(value = "/possibleComponents", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    List<ComponentDefinition> getPossibleComponents(@RequestBody ComponentProperties... properties) throws Throwable;

    /**
     * Makes the specified {@link Form} object cancelable, which means that modifications to the values can be canceled.
     * <p>
     * This is intended for local use only. When using this with the REST service, the values can simply be reset in the JSON
     * version of the {@link Form} object, so the cancel operation can be implemented entirely by the client.
     *
     * @param properties the {@link Properties} object associated with the {@code Form}.
     * @param formName   the name of the form
     * @return the {@link Properties} object specified as modified by this service.
     * @returnWrapped org.talend.daikon.properties.Properties
     */
    @RequestMapping(value = "/makeFormCancelable", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    Properties makeFormCancelable(@RequestBody Properties properties, @RequestParam(required = false, defaultValue = Form.MAIN) String formName);

    /**
     * Cancels the changes to the values in the specified {@link Form} object after the it was made cancelable.
     * <p>
     * This is intended for local use only. When using this with the REST service, the values can simply be reset in the JSON
     * version of the {@link Form} object, so the cancel operation can be implemented entirely by the client.
     *
     * @param properties the {@link Properties} object associated with the {@code Form}.
     * @param formName   the name of the form
     * @return the {@link Properties} object specified as modified by this service.
     * @returnWrapped org.talend.daikon.properties.Properties
     */
    @RequestMapping(value = "/commitFormValues", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    Properties cancelFormValues(@RequestBody Properties properties, @RequestParam(required = false, defaultValue = Form.MAIN) String formName);

    /**
     * @return the {@link Properties} object specified as modified by this service.
     * @returnWrapped org.talend.daikon.properties.Properties
     * @see {@link Properties} for a description of the meaning of this method.
     */
    @RequestMapping(value = "/properties/{propName}/validate", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Properties validateProperty(@PathVariable(value = "propName") String propName, @RequestBody Properties properties)
            throws Throwable;

    /**
     * @return the {@link Properties} object specified as modified by this service.
     * @returnWrapped org.talend.daikon.properties.Properties
     * @see {@link Properties} for a description of the meaning of this method.
     */
    @RequestMapping(value = "/properties/{propName}/beforeActivate", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Properties beforePropertyActivate(@PathVariable(value = "propName") String propName, @RequestBody Properties properties)
            throws Throwable;

    /**
     * @return the {@link Properties} object specified as modified by this service.
     * @returnWrapped org.talend.daikon.properties.Properties
     * @see {@link Properties} for a description of the meaning of this method.
     */
    @RequestMapping(value = "/properties/{propName}/beforeRender", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Properties beforePropertyPresent(@PathVariable(value = "propName") String propName, @RequestBody Properties properties)
            throws Throwable;

    /**
     * @return the {@link Properties} object specified as modified by this service.
     * @returnWrapped org.talend.daikon.properties.Properties
     * @see {@link Properties} for a description of the meaning of this method.
     */
    @RequestMapping(value = "/properties/{propName}/after", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Properties afterProperty(@PathVariable(value = "propName") String propName, @RequestBody Properties properties)
            throws Throwable;

    /**
     * @return the {@link Properties} object specified as modified by this service.
     * @returnWrapped org.talend.daikon.properties.Properties
     * @see {@link Properties} for a description of the meaning of this method.
     */
    @RequestMapping(value = "/properties/beforeFormPresent/{formName}", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Properties beforeFormPresent(@PathVariable(value = "formName") String formName, @RequestBody Properties properties)
            throws Throwable;

    /**
     * @return the {@link Properties} object specified as modified by this service.
     * @returnWrapped org.talend.daikon.properties.Properties
     * @see {@link Properties} for a description of the meaning of this method.
     */
    @RequestMapping(value = "/properties/afterFormNext/{formName}", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Properties afterFormNext(@PathVariable(value = "formName") String formName, @RequestBody Properties properties)
            throws Throwable;

    /**
     * @return the {@link Properties} object specified as modified by this service.
     * @returnWrapped org.talend.daikon.properties.Properties
     * @see {@link Properties} for a description of the meaning of this method.
     */
    @RequestMapping(value = "/properties/afterFormBack/{formName}", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Properties afterFormBack(@PathVariable(value = "formName") String formName, @RequestBody Properties properties)
            throws Throwable;

    /**
     * @return the {@link Properties} object specified as modified by this service.
     * @returnWrapped org.talend.daikon.properties.Properties
     * @see {@link Properties} for a description of the meaning of this method.
     */
    @RequestMapping(value = "/properties/afterFormFinish/{formName}", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Properties afterFormFinish(@PathVariable(value = "formName") String formName, @RequestBody Properties properties)
            throws Throwable;

    /**
     * Get the list of all the component names that are registered
     *
     * @return the set of component names, never null
     * @returnWrapped java.util.Set<java.lang.String>
     */
    @RequestMapping(value = "/names", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Set<String> getAllComponentNames();

    /**
     * Get the list of all the components {@link ComponentDefinition} that are registered
     *
     * @return the set of component definitions, never null.
     * @returnWrapped java.util.Set<org.talend.components.api.component.ComponentDefinition>
     */
    @RequestMapping(value = "/definitions", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    Set<ComponentDefinition> getAllComponents();

    /**
     * Return the png image related to the given wizard
     *
     * @param name, name of the wizard to get the image for
     * @param type, the type of image requested
     * @return the png image stream or null if none was provided or could not be found
     * @throws ComponentException thrown if the componentName is not registered in the service
     *                            <p>
     *                            TODO change the method signature not to deal with HttpServletResponse
     */
    @RequestMapping(value = "/wizards/{name}/icon/{type}", method = GET, produces = IMAGE_PNG_VALUE)
    void getWizardImageRest(@PathVariable(value = "name") String name, @PathVariable(value = "type") WizardImageType type,
                            HttpServletResponse response);

    /**
     * Return the png image related to the given component
     *
     * @param name, name of the comonent to get the image for
     * @param type, the type of image requested
     * @return the png image stream or null if none was provided or an error occurred
     * @throws ComponentException thrown if the componentName is not registered in the service
     */
    @RequestMapping(value = "/icon/{name}", method = GET, produces = IMAGE_PNG_VALUE)
    void getComponentsImageRest(@PathVariable(value = "name") String name, @PathVariable(value = "type") ComponentImageType type,
                                HttpServletResponse response);

}
