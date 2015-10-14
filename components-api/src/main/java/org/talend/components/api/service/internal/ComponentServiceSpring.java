// ============================================================================
//
// copyright (c) 2006-2015 talend inc. - www.talend.com
//
// this source code is available under agreement available at
// %installdir%\features\org.talend.rcp.branding.%productname%\%productname%license.txt
//
// you should have received a copy of the agreement
// along with this program; if not, write to talend sa
// 9 rue pages 92150 suresnes, france
//
// ============================================================================
package org.talend.components.api.service.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.context.GlobalContext;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Repository;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.daikon.exception.error.CommonErrorCodes;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * This is a spring only class that is instanciated by the spring framework. It delegates all its calls to the
 * ComponentServiceImpl delegate create in it's constructor. This delegate uses a Component regitry implementation
 * specific to spring.
 */
@RestController
@Api(value = "components", basePath = ComponentServiceSpring.BASE_PATH, description = "Component services")
@Service
public class ComponentServiceSpring implements ComponentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentServiceSpring.class);

    static final String BASE_PATH = "/components"; //$NON-NLS-1$

    private ComponentService componentServiceDelegate;

    @Autowired
    public ComponentServiceSpring(final ApplicationContext context, final GlobalContext gc) {
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
    @RequestMapping(value = BASE_PATH + "/properties/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties getComponentProperties(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the component") String name) {
        return componentServiceDelegate.getComponentProperties(name);
    }

    @Override
    @RequestMapping(value = BASE_PATH
            + "/dependencies/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Set<String> getMavenUriDependencies(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the component") String name) {
        return componentServiceDelegate.getMavenUriDependencies(name);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/wizard/{name}/{repositoryLocation}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ComponentWizard getComponentWizard(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of the component") String name,
            @PathVariable(value = "repositoryLocation") @ApiParam(name = "repositoryLocation", value = "Repository location") String repositoryLocation) {
        return componentServiceDelegate.getComponentWizard(name, repositoryLocation);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/wizardForProperties/{repositoryLocation}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ComponentWizard> getComponentWizardsForProperties(
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties,
            @PathVariable(value = "repositoryLocation") @ApiParam(name = "repositoryLocation", value = "Repository location") String repositoryLocation) {
        return componentServiceDelegate.getComponentWizardsForProperties(properties, repositoryLocation);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/possibleComponents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ComponentDefinition> getPossibleComponents(
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
            throws Throwable {
        return componentServiceDelegate.getPossibleComponents(properties);
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/properties/{propName}/validate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties validateProperty(
            @PathVariable(value = "propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
            throws Throwable {
        componentServiceDelegate.validateProperty(propName, properties);
        return properties;
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/properties/{propName}/before", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties beforeProperty(
            @PathVariable(value = "propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
            throws Throwable {
        componentServiceDelegate.beforeProperty(propName, properties);
        return properties;
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/properties/{propName}/after", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties afterProperty(
            @PathVariable(value = "propName") @ApiParam(name = "propName", value = "Name of property") String propName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
            throws Throwable {
        componentServiceDelegate.afterProperty(propName, properties);
        return properties;
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/properties/beforeFormPresent/{formName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties beforeFormPresent(
            @PathVariable(value = "formName") @ApiParam(name = "formName", value = "Name of form") String formName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
            throws Throwable {
        componentServiceDelegate.beforeFormPresent(formName, properties);
        return properties;
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/properties/afterFormNext/{formName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties afterFormNext(
            @PathVariable(value = "formName") @ApiParam(name = "formName", value = "Name of form") String formName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
            throws Throwable {
        componentServiceDelegate.afterFormNext(formName, properties);
        return properties;
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/properties/afterFormBack/{formName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties afterFormBack(
            @PathVariable(value = "formName") @ApiParam(name = "formName", value = "Name of form") String formName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
            throws Throwable {
        componentServiceDelegate.afterFormBack(formName, properties);
        return properties;
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/properties/afterFormFinish/{formName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ComponentProperties afterFormFinish(
            @PathVariable(value = "formName") @ApiParam(name = "formName", value = "Name of form") String formName,
            @ApiParam(name = "properties", value = "Component properties") @RequestBody ComponentProperties properties)
            throws Throwable {
        componentServiceDelegate.afterFormFinish(formName, properties);
        return properties;
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/names", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Set<String> getAllComponentNames() {
        return componentServiceDelegate.getAllComponentNames();
    }

    @Override
    @RequestMapping(value = BASE_PATH + "/definitions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Set<ComponentDefinition> getAllComponents() {
        return componentServiceDelegate.getAllComponents();
    }

    @RequestMapping(value = BASE_PATH + "/wizards/definitions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Set<ComponentWizardDefinition> getTopLevelComponentWizards() {
        return componentServiceDelegate.getTopLevelComponentWizards();
    }

    @Override
    // this cannot be used as is as a rest api so see getWizardImageRest.
    public InputStream getWizardPngImage(String wizardName, WizardImageType imageType) {
        return componentServiceDelegate.getWizardPngImage(wizardName, imageType);
    }

    @RequestMapping(value = BASE_PATH + "/wizards/{name}/icon/{type}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    @ApiOperation(value = "Return the icon related to the wizard", notes = "return the png image related to the wizard parameter.")
    public void getWizardImageRest(@PathVariable(value = "name") @ApiParam(name = "name", value = "Name of wizard") String name,
            @PathVariable(value = "type") @ApiParam(name = "type", value = "Type of the icon requested") WizardImageType type,
            final HttpServletResponse response) {
        InputStream wizardPngImageStream = getWizardPngImage(name, type);
        sendStreamBack(response, wizardPngImageStream);
    }

    /**
     * DOC sgandon Comment method "sendStreamBack".
     * 
     * @param response
     * @param inputStream
     * @throws IOException
     */
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

    @RequestMapping(value = BASE_PATH + "/icon/{name}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    @ApiOperation(value = "Return the icon related to the Component", notes = "return the png image related to the Component name parameter.")
    public void getComponentsImageRest(
            @PathVariable(value = "name") @ApiParam(name = "name", value = "Name of Component") String name,
            @PathVariable(value = "type") @ApiParam(name = "type", value = "Type of the icon requested") ComponentImageType type,
            final HttpServletResponse response) {
        InputStream componentPngImageStream = getComponentPngImage(name, type);
        sendStreamBack(response, componentPngImageStream);
    }

    // FIXME - make this work for web
    @Override
    public String storeComponentProperties(ComponentProperties properties, String name, String repositoryLocation, Schema schema) {
        return componentServiceDelegate.storeComponentProperties(properties, name, repositoryLocation, schema);
    }

    // FIXME - make this work for web
    @Override
    public ComponentProperties getPropertiesForComponent(String componentId) {
        return componentServiceDelegate.getPropertiesForComponent(componentId);
    }



}
