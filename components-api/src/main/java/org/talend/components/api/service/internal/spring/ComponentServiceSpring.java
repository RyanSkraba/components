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
package org.talend.components.api.service.internal.spring;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentRegistry;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.schema.Schema;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * This is a spring only class that is instantiated by the spring framework. It delegates all its calls to the
 * ComponentServiceImpl delegate create in it's constructor. This delegate uses a Component registry implementation
 * specific to spring.
 */

@Service
@Api(value = "components")
@Path("")
public class ComponentServiceSpring implements ComponentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentServiceSpring.class);

    public static final String BASE_PATH = "/components"; //$NON-NLS-1$

    private ComponentService componentServiceDelegate;

    @Autowired
    public ComponentServiceSpring(final ApplicationContext context) {
        this.componentServiceDelegate = new ComponentServiceImpl(new ComponentRegistry() {

            @Override
            public Map<String, ComponentDefinition> getComponents() {
                Map<String, ComponentDefinition> compDefs = context.getBeansOfType(ComponentDefinition.class);
                return compDefs;
            }

            @Override
            public Map<String, ComponentWizardDefinition> getComponentWizards() {
                Map<String, ComponentWizardDefinition> wizardDefs = context.getBeansOfType(ComponentWizardDefinition.class);
                return wizardDefs;
            }

        });
    }

    @Override
    @GET
    @Path("/properties/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentProperties getComponentProperties(
            @PathParam("name") @ApiParam(name = "name", value = "Name of the component") String name) {
        return componentServiceDelegate.getComponentProperties(name);
    }

    @Override
    @GET
    @Path("/definition/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentDefinition getComponentDefinition(
            @PathParam("name") @ApiParam(name = "name", value = "Name of the component") String name) {
        return componentServiceDelegate.getComponentDefinition(name);
    }

    @Override
    @GET
    @Path("/dependencies/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getMavenUriDependencies(
            @PathParam("name") @ApiParam(name = "name", value = "Name of the component") String name) {
        return componentServiceDelegate.getMavenUriDependencies(name);
    }

    @Override
    @GET
    @Path("/wizard/{name}/{repositoryLocation}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentWizard getComponentWizard(
            @PathParam("name") @ApiParam(name = "name", value = "Name of the component") String name,
            @PathParam("repositoryLocation") @ApiParam(name = "repositoryLocation", value = "Repository location") String repositoryLocation) {
        return componentServiceDelegate.getComponentWizard(name, repositoryLocation);
    }

    @Override
    @POST
    @Path("/wizardForProperties/{repositoryLocation}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ComponentWizard> getComponentWizardsForProperties(
            @ApiParam(name = "properties", value = "Component properties") ComponentProperties properties,
            @PathParam("repositoryLocation") @ApiParam(name = "repositoryLocation", value = "Repository location") String repositoryLocation) {
        return componentServiceDelegate.getComponentWizardsForProperties(properties, repositoryLocation);
    }

    @Override
    @POST
    @Path("/possibleComponents")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ComponentDefinition> getPossibleComponents(
            @ApiParam(name = "properties", value = "Component properties") ComponentProperties... properties) throws Throwable {
        return componentServiceDelegate.getPossibleComponents(properties);
    }

    @Override
    @POST
    @Path("/makeFormCancelable")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentProperties makeFormCancelable(
            @ApiParam(name = "properties", value = "Component properties") ComponentProperties properties,
            @ApiParam(name = "formName", value = "Name of the form") String formName) {
        return componentServiceDelegate.makeFormCancelable(properties, formName);
    }

    @Override
    @POST
    @Path("/cancelFormValues")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentProperties cancelFormValues(
            @ApiParam(name = "properties", value = "Component properties") ComponentProperties properties,
            @ApiParam(name = "formName", value = "Name of the form") String formName) {
        return componentServiceDelegate.cancelFormValues(properties, formName);
    }

    @Override
    @POST
    @Path("/properties/{propName}/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentProperties validateProperty(
            @PathParam("propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") ComponentProperties properties) throws Throwable {
        componentServiceDelegate.validateProperty(propName, properties);
        return properties;
    }

    @Override
    @POST
    @Path("/properties/{propName}/beforeActivate")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentProperties beforePropertyActivate(
            @PathParam("propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") ComponentProperties properties) throws Throwable {
        componentServiceDelegate.beforePropertyActivate(propName, properties);
        return properties;
    }

    @Override
    @POST
    @Path("/properties/{propName}/beforeRender")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentProperties beforePropertyPresent(
            @PathParam("propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") ComponentProperties properties) throws Throwable {
        componentServiceDelegate.beforePropertyPresent(propName, properties);
        return properties;
    }

    @Override
    @POST
    @Path("/properties/{propName}/after")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentProperties afterProperty(
            @PathParam("propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") ComponentProperties properties) throws Throwable {
        componentServiceDelegate.afterProperty(propName, properties);
        return properties;
    }

    @Override
    @POST
    @Path("/properties/beforeFormPresent/{formName}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentProperties beforeFormPresent(
            @PathParam("formName") @ApiParam(name = "formName", value = "Name of form") String formName,
            @ApiParam(name = "properties", value = "Component properties") ComponentProperties properties) throws Throwable {
        componentServiceDelegate.beforeFormPresent(formName, properties);
        return properties;
    }

    @Override
    @POST
    @Path("/properties/afterFormNext/{formName}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentProperties afterFormNext(
            @PathParam("formName") @ApiParam(name = "formName", value = "Name of form") String formName,
            @ApiParam(name = "properties", value = "Component properties") ComponentProperties properties) throws Throwable {
        componentServiceDelegate.afterFormNext(formName, properties);
        return properties;
    }

    @Override
    @POST
    @Path("/properties/afterFormBack/{formName}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentProperties afterFormBack(
            @PathParam("formName") @ApiParam(name = "formName", value = "Name of form") String formName,
            @ApiParam(name = "properties", value = "Component properties") ComponentProperties properties) throws Throwable {
        componentServiceDelegate.afterFormBack(formName, properties);
        return properties;
    }

    @Override
    @POST
    @Path("/properties/afterFormFinish/{formName}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComponentProperties afterFormFinish(
            @PathParam("formName") @ApiParam(name = "formName", value = "Name of form") String formName,
            @ApiParam(name = "properties", value = "Component properties") ComponentProperties properties) throws Throwable {
        componentServiceDelegate.afterFormFinish(formName, properties);
        return properties;
    }

    @Override
    @GET
    @Path("/names")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getAllComponentNames() {
        return componentServiceDelegate.getAllComponentNames();
    }

    @Override
    @GET
    @Path("/definitions")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<ComponentDefinition> getAllComponents() {
        return componentServiceDelegate.getAllComponents();
    }

    @Override
    @GET
    @Path("/wizards/definitions")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<ComponentWizardDefinition> getTopLevelComponentWizards() {
        return componentServiceDelegate.getTopLevelComponentWizards();
    }

    @Override
    // this cannot be used as is as a rest api so see getWizardImageRest.
    public InputStream getWizardPngImage(String wizardName, WizardImageType imageType) {
        return componentServiceDelegate.getWizardPngImage(wizardName, imageType);
    }

    @GET
    @Path("/wizards/{name}/icon/{type}")
    @Produces(MediaType.MEDIA_TYPE_WILDCARD)
    @ApiOperation(value = "Return the icon related to the wizard", notes = "return the png image related to the wizard parameter.")
    public void getWizardImageRest(@PathParam("name") @ApiParam(name = "name", value = "Name of wizard") String name,
            @PathParam("type") @ApiParam(name = "type", value = "Type of the icon requested") WizardImageType type,
            final HttpServletResponse response) {
        InputStream wizardPngImageStream = getWizardPngImage(name, type);
        sendStreamBack(response, wizardPngImageStream);
    }

    private void sendStreamBack(final HttpServletResponse response, InputStream inputStream) {
        try {
            if (inputStream != null) {
                try {
                    IOUtils.copy(inputStream, response.getOutputStream());
                } catch (IOException e) {
                    throw new ComponentException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                } finally {
                    inputStream.close();
                }
            } else {// could not get icon so respond a resource_not_found : 404
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IOException e) {// is sendError fails or inputstream fails when closing
            LOGGER.error("sendError failed or inputstream failed when closing.", e); //$NON-NLS-1$
            throw new ComponentException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    // this cannot be used as is as a rest api so see getWizardPngIconRest.
    public InputStream getComponentPngImage(String componentName, ComponentImageType imageType) {
        return componentServiceDelegate.getComponentPngImage(componentName, imageType);
    }

    @Override
    public void setRepository(Repository repository) {
        componentServiceDelegate.setRepository(repository);
    }

    @GET
    @Path("/icon/{name}")
    @Produces(MediaType.MEDIA_TYPE_WILDCARD)
    @ApiOperation(value = "Return the icon related to the Component", notes = "return the png image related to the Component name parameter.")
    public void getComponentsImageRest(@PathParam("name") @ApiParam(name = "name", value = "Name of Component") String name,
            @PathParam("type") @ApiParam(name = "type", value = "Type of the icon requested") ComponentImageType type,
            final HttpServletResponse response) {
        InputStream componentPngImageStream = getComponentPngImage(name, type);
        sendStreamBack(response, componentPngImageStream);
    }

    // FIXME - make this work for web
    @Override
    public String storeProperties(ComponentProperties properties, String name, String repositoryLocation, Schema schema) {
        return componentServiceDelegate.storeProperties(properties, name, repositoryLocation, schema);
    }

}
